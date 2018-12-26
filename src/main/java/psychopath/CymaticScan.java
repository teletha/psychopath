/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.BiPredicate;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;

class CymaticScan implements FileVisitor<Path>, Runnable, Disposable {

    // =======================================================
    // For Pattern Matching Facility
    // =======================================================
    private Path original;

    /** The user speecified event listener. */
    private Observer observer;

    /** The user speecified event listener. */
    private Disposable disposer;

    /** The source. */
    private Path from;

    /** The destination. */
    private Path to;

    /** The operation type. */
    private int type;

    /** The include file patterns. */
    private BiPredicate<Path, BasicFileAttributes> include;

    /** The exclude file patterns. */
    private BiPredicate<Path, BasicFileAttributes> exclude;

    /** The exclude directory pattern. */
    private BiPredicate<Path, BasicFileAttributes> directory;

    /** Can we accept root directory? */
    private boolean root;

    /** Flags whether the current directory can be deleted or not. */
    private LinkedList deletable;

    /**
     * <p>
     * Utility for file tree traversal.
     * </p>
     * <p>
     * Type parameter represents the following:
     * </p>
     * <ol>
     * <li>0 - copy</li>
     * <li>1 - move</li>
     * <li>2 - delete</li>
     * <li>3 - file scan</li>
     * <li>4 - directory scan</li>
     * <li>5 - observe</li>
     * </ol>
     */
    CymaticScan(Path from, Path to, int type, Observer observer, Disposable disposer, String... patterns) {
        this(from, to, type, observer, disposer, (BiPredicate) null);

        if (patterns == null) {
            patterns = new String[0];
        }

        // Parse and create path matchers.
        for (String pattern : patterns) {
            // convert pattern to reduce unnecessary file system scanning
            if (pattern.equals("*") && patterns.length == 1) {
                this.from = from;
                this.root = false;
            } else if (pattern.equals("**")) {
                this.from = from;
                this.root = false;
                continue;
            }

            if (pattern.charAt(0) != '!') {
                // include
                include = glob(include, pattern);
            } else if (pattern.endsWith("/**")) {
                // exclude directory
                directory = glob(directory, pattern.substring(1, pattern.length() - 3));
            } else if (type < 4) {
                // exclude files
                exclude = glob(exclude, pattern.substring(1));
            } else {
                // exclude directory
                directory = glob(directory, pattern.substring(1));
            }
        }
    }

    /**
     * <p>
     * Create {@link BiPredicate} filter by using the specified glob pattern.
     * </p>
     * 
     * @param base
     * @param pattern
     * @return
     */
    private BiPredicate<Path, BasicFileAttributes> glob(BiPredicate<Path, BasicFileAttributes> base, String pattern) {
        // Default file system doesn't support close method, so we can ignore to release resource.
        PathMatcher matcher = from.getFileSystem().getPathMatcher("glob:".concat(pattern));
        BiPredicate<Path, BasicFileAttributes> filter = (path, attrs) -> matcher.matches(path);

        return base == null ? filter : base.or(filter);
    }

    /**
     * <p>
     * Utility for file tree traversal.
     * </p>
     * <p>
     * Type parameter represents the following:
     * </p>
     * <ol>
     * <li>0 - copy</li>
     * <li>1 - move</li>
     * <li>2 - delete</li>
     * <li>3 - file scan</li>
     * <li>4 - directory scan</li>
     * <li>5 - observe</li>
     * </ol>
     */
    CymaticScan(Path from, Path to, int type, Observer observer, Disposable disposer, BiPredicate<Path, BasicFileAttributes> filter) {
        this.original = from;
        this.type = type;
        this.observer = observer;
        this.disposer = disposer;
        this.include = filter;
        this.root = filter == null && !isZip(from);

        try {
            boolean directory = Files.isDirectory(from);

            // The copy and move operations need the root path.
            this.from = directory && type < 2 ? from.getParent() : from;

            // The copy and move operations need destination. If the source is file,
            // so destination must be file and its name is equal to source file.
            this.to = !directory && type < 2 && Files.isDirectory(to) ? to.resolve(from.getFileName()) : to;

            if (type < 2 && 1 < to.getNameCount()) {
                Files.createDirectories(to.getParent());
            }

            if (type < 3) {
                deletable = new LinkedList();
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Walk file tree actually.
     * </p>
     * 
     * @return
     */
    CymaticScan walk() {
        try {
            Files.walkFileTree(original, Collections.EMPTY_SET, Integer.MAX_VALUE, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        if (disposer.isDisposed()) {
            return TERMINATE;
        }

        // Retrieve relative path from base.
        Path relative = from.relativize(path);
        // Skip root directory.
        // Directory exclusion make fast traversing file tree.

        // Normally, we can't use identical equal against path object. But only root path object
        // is passed as parameter value, so we can use identical equal here.
        if (from != path && directory != null && directory.test(relative, attrs)) {
            return SKIP_SUBTREE;
        }

        switch (type) {
        case 0: // copy
        case 1: // move
            Files.createDirectories(to.resolve(relative));
            // fall-through to reduce footprint

        case 2: // delete
            deletable.add(0, null);

        case 3: // walk file
            return CONTINUE;

        case 4: // walk directory
            if ((root || from != path) && accept(relative, attrs)) {
                observer.accept(Locator.directory(path));
            }
            // fall-through to reduce footprint

        default: // observe dirctory
            return CONTINUE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        if (disposer.isDisposed()) {
            return TERMINATE;
        }

        switch (type) {
        case 0: // copy
        case 1: // move
            Files.setLastModifiedTime(to.resolve(from.relativize(path)), Files.getLastModifiedTime(path));
            // fall-through to reduce footprint

        case 2: // delete
            if (type != 0 && (root || from != path) && deletable.peek() == null) {
                Files.delete(path);
            }
            deletable.poll();
            // fall-through to reduce footprint

        default: // walk directory and walk file
            return CONTINUE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (disposer.isDisposed()) {
            return TERMINATE;
        }

        if (type < 4) {
            // Retrieve relative path from base.
            Path relative = from.relativize(path);

            if (accept(relative, attrs)) {
                switch (type) {
                case 0: // copy
                    Files.copy(path, to.resolve(relative), COPY_ATTRIBUTES, REPLACE_EXISTING);
                    break;

                case 1: // move
                    Files.move(path, to.resolve(relative), ATOMIC_MOVE, REPLACE_EXISTING);
                    break;

                case 2: // delete
                    Files.delete(path);
                    break;

                case 3: // walk file
                    observer.accept(Locator.file(path));
                    break;
                }
            } else if (type < 3) {
                deletable.set(0, this);
            }
        }
        return CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return CONTINUE;
    }

    /**
     * <p>
     * Helper method to check zip path.
     * </p>
     * 
     * @param path A path to check.
     * @return
     */
    private static boolean isZip(Path path) {
        return path.getClass().getSimpleName().endsWith("ZipPath");
    }

    /**
     * <p>
     * Helper method to test whether the path is acceptable or not.
     * </p>
     *
     * @param path A target path.
     * @return A result.
     */
    private boolean accept(Path path, BasicFileAttributes attr) {
        // File exclusion
        if (exclude != null && exclude.test(path, attr)) {
            return false;
        }

        // File inclusion
        return include == null || include.test(path, attr);
    }

    // =======================================================
    // For File Watching Facility
    // =======================================================

    /** The actual file event notification facility. */
    private WatchService service;

    /**
     * <p>
     * Sinobu's file event notification facility.
     * </p>
     *
     * @param path A target directory.
     * @param observer A event listener.
     * @param patterns Name matching patterns.
     */
    CymaticScan(Path path, Observer observer, Disposable disposer, String... patterns) {
        this(path, null, 5, observer, disposer, patterns);

        try {
            this.service = path.getFileSystem().newWatchService();

            // register
            if (patterns.length == 1 && patterns[0].equals("*")) {
                path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } else {
                if (Files.isDirectory(path)) {
                    Directory directory = Locator.directory(path);

                    for (Directory dir : directory.walkDirectories().startWith(directory).toList()) {
                        dir.path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    }
                }
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        while (true) {
            try {
                WatchKey key = service.take();

                for (WatchEvent event : key.pollEvents()) {
                    // make current modified path
                    Path path = ((Path) key.watchable()).resolve((Path) event.context());

                    // pattern matching
                    if (accept(from.relativize(path), null)) {
                        observer.accept(new Watch(Locator.locate(path), event));

                        if (event.kind() == ENTRY_CREATE) {
                            if (Files.isDirectory(path) && preVisitDirectory(path, null) == CONTINUE) {
                                Directory directory = Locator.directory(path);

                                for (Directory dir : directory.walkDirectories().startWith(directory).toList()) {
                                    dir.path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                }
                            }
                        }
                    }
                }

                // reset key
                key.reset();
            } catch (ClosedWatchServiceException e) {
                break; // Dispose this file watching service.
            } catch (Exception e) {
                // TODO Can we ignore error?
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        I.quiet(service);
    }

    /**
     * 
     */
    private static class Watch implements WatchEvent<Location> {

        /** Generic object. */
        private final Location location;

        /** The event holder. */
        private final WatchEvent event;

        /**
         * @param location
         * @param event
         */
        private Watch(Location location, WatchEvent event) {
            this.location = location;
            this.event = event;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Kind kind() {
            return event.kind();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int count() {
            return event.count();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Location context() {
            return location;
        }
    }
}

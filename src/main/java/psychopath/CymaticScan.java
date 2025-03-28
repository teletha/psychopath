/*
 * Copyright (C) 2025 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;

class CymaticScan implements FileVisitor<Path>, Runnable, Disposable {

    // =======================================================
    // For Pattern Matching Facility
    // =======================================================
    /** The user speecified event listener. */
    private final Observer observer;

    /** The user speecified event listener. */
    private final Disposable disposer;

    /** The source. */
    private final Path from;

    /** The destination. */
    private final Path to;

    /** The operation type. */
    private final int type;

    /** The replace mode. */
    private final Option o;

    /** The include file patterns. */
    private BiPredicate<Path, BasicFileAttributes> include;

    /** The exclude file patterns. */
    private BiPredicate<Path, BasicFileAttributes> exclude;

    /** The exclude directory pattern. */
    private BiPredicate<Path, BasicFileAttributes> directory;

    /** The copy options */
    private final CopyOption[] copies;

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
    CymaticScan(Directory from, Directory to, int type, Observer observer, Disposable disposer, Option o) {
        this.type = type;
        this.observer = observer;
        this.disposer = disposer;
        this.include = o.filter;
        this.o = o;

        Set<CopyOption> copies = new HashSet();
        if (type == 0) copies.add(StandardCopyOption.COPY_ATTRIBUTES);
        if (o.existingMode <= 2) copies.add(StandardCopyOption.REPLACE_EXISTING);
        this.copies = copies.toArray(new CopyOption[copies.size()]);

        // The copy and move operations need the root path.
        if (o.strip == 0 && type < 2) from = from.parent();
        this.from = from.path;
        this.to = to.directory(o.allocator.toString()).path;

        // Parse and create path matchers.
        for (String pattern : o.patterns) {
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
            Files.createDirectories(resolve(to, relative));
            // fall-through to reduce footprint

        case 2: // delete
        case 3: // walk file
            return CONTINUE;

        case 4: // walk directory
            if ((o.strip == 0 || from != path) && accept(relative, attrs)) {
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
            Directory dir = Locator.directory(resolve(to, from.relativize(path)));

            if (dir.isEmpty()) {
                Files.delete(dir.path);
            } else {
                Files.setLastModifiedTime(dir.path, Files.getLastModifiedTime(path));
            }
            // fall-through to reduce footprint

        case 2: // delete
            if (type != 0 && (o.strip == 0 || from != path) && Locator.directory(path).isEmpty()) {
                Files.delete(path);
            }
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
                observer.accept(Locator.file(path));

                switch (type) {
                case 0: // copy
                    Path copyDestination = resolve(to, relative);
                    if (o.canReplace(path, copyDestination)) {
                        Files.copy(path, copyDestination, copies);
                    }
                    break;

                case 1: // move
                    Path moveDestination = resolve(to, relative);
                    if (o.canReplace(path, moveDestination)) {
                        Files.move(path, moveDestination, copies);
                    }
                    break;

                case 2: // delete
                    Files.delete(path);
                    break;

                case 3: // walk file
                    break;
                }
            }
        }
        return CONTINUE;
    }

    private Path resolve(Path to, Path relative) {
        if (1 < o.strip) {
            int count = relative.getNameCount();
            if (count == 1) {
                relative = relative.resolveSibling("");
            } else {
                relative = relative.subpath(o.strip - 1, count);
            }
        }
        return to.resolve(relative.toString());
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
     * @param directory A target directory.
     * @param observer A event listener.
     * @param patterns Name matching patterns.
     */
    CymaticScan(Directory directory, Observer observer, Disposable disposer, String... patterns) {
        this(directory, directory, 5, observer, disposer, new Option().glob(patterns));

        try {
            this.service = directory.path.getFileSystem().newWatchService();

            // register
            if (patterns.length == 1 && patterns[0].equals("*")) {
                directory.path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } else {
                if (directory.isPresent()) {
                    for (Directory dir : directory.walkDirectory().startWith(directory).toList()) {
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

                                for (Directory dir : directory.walkDirectory().startWith(directory).toList()) {
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
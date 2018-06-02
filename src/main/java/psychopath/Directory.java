/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.function.BiPredicate;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/04/08 12:22:35
 */
public class Directory extends Location {

    /**
     * @param path
     */
    Directory(Path path) {
        super(path);
    }

    public boolean isEmpty() {
        try {
            return Files.list(path).count() == 0;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Shortcut for {@link Locator#file(String)}
     * 
     * @param path A file path.
     * @return A located {@link File}.
     */
    public File file(String path) {
        return Locator.file(this, path);
    }

    /**
     * Shortcut for {@link Locator#file(Path)}
     * 
     * @param path A file path.
     * @return A located {@link File}.
     */
    public File file(Path path) {
        return Locator.file(this, path);
    }

    /**
     * Shortcut for {@link Locator#directory(String)}
     * 
     * @param path A directory path.
     * @return A located {@link Directory}.
     */
    public Directory directory(String path) {
        return Locator.directory(this, path);
    }

    /**
     * Shortcut for {@link Locator#directory(Path)}
     * 
     * @param path A directory path.
     * @return A located {@link Directory}.
     */
    public Directory directory(Path path) {
        return Locator.directory(this, path);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<File> files(String... patterns) {
        return files(Integer.MAX_VALUE, patterns);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param depth A tree depth to search.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<File> files(int depth, String... patterns) {
        return new Signal<>((observer, disposer) -> {
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, new CymaticScan(path, null, 3, observer, patterns));
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<File> files(BiPredicate<Path, BasicFileAttributes> filter) {
        return files(Integer.MAX_VALUE, filter);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     * 
     * @param depth A tree depth to search.
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<File> files(int depth, BiPredicate<Path, BasicFileAttributes> filter) {
        return new Signal<>((observer, disposer) -> {
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, new CymaticScan(path, null, 3, observer, filter));
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<Directory> directories(String... patterns) {
        return directories(Integer.MAX_VALUE, patterns);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param depth A tree depth to search.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<Directory> directories(int depth, String... patterns) {
        return new Signal<Directory>((observer, disposer) -> {
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, new CymaticScan(path, null, 4, observer, patterns));
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<Directory> directories(BiPredicate<Path, BasicFileAttributes> filter) {
        return directories(Integer.MAX_VALUE, filter);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     * 
     * @param depth A tree depth to search.
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<Directory> directories(int depth, BiPredicate<Path, BasicFileAttributes> filter) {
        return new Signal<>((observer, disposer) -> {
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, new CymaticScan(path, null, 4, observer, filter));
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveTo(Directory destination) {
        moveTo(destination, (BiPredicate) null);
    }

    /**
     * <p>
     * Move a input {@link Path} to an output {@link Path} with its attributes. Simplified strategy is
     * the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   if (output.isFile) {
     *     // Move input file to output file.
     *   } else {
     *     // Move input file under output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Move input directory under output directory deeply.
     *     // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     *   }
     * }
     * </pre>
     * <p>
     * If the output file already exists, it will be replaced by input file unconditionaly. The exact
     * file attributes that are copied is platform and file system dependent and therefore unspecified.
     * Minimally, the last-modified-time is copied to the output file if supported by both the input and
     * output file store. Copying of file timestamps may result in precision loss.
     * </p>
     * <p>
     * Moving a file is an atomic operation.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An output {@link Path} object which can be file or directory.
     * @param filter A file filter to move.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file is directory and the output file is <em>not</em>
     *             directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to check
     *             read access to the source file, the {@link SecurityManager#checkWrite(String)} is
     *             invoked to check write access to the target file. If a symbolic link is copied the
     *             security manager is invoked to check {@link LinkPermission}("symbolic").
     */
    public void moveTo(Directory destination, String... patterns) {
        new Visitor(path, destination.path, 1, patterns).walk();
    }

    /**
     * <p>
     * Move a input {@link Path} to an output {@link Path} with its attributes. Simplified strategy is
     * the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   if (output.isFile) {
     *     // Move input file to output file.
     *   } else {
     *     // Move input file under output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Move input directory under output directory deeply.
     *     // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     *   }
     * }
     * </pre>
     * <p>
     * If the output file already exists, it will be replaced by input file unconditionaly. The exact
     * file attributes that are copied is platform and file system dependent and therefore unspecified.
     * Minimally, the last-modified-time is copied to the output file if supported by both the input and
     * output file store. Copying of file timestamps may result in precision loss.
     * </p>
     * <p>
     * Moving a file is an atomic operation.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An output {@link Path} object which can be file or directory.
     * @param filter A file filter to move.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file is directory and the output file is <em>not</em>
     *             directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to check
     *             read access to the source file, the {@link SecurityManager#checkWrite(String)} is
     *             invoked to check write access to the target file. If a symbolic link is copied the
     *             security manager is invoked to check {@link LinkPermission}("symbolic").
     */
    public void moveTo(Directory destination, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(path, destination.path, 1, filter).walk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyTo(Directory destination) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<WatchEvent<Path>> observe() {
        return null;
    }
}

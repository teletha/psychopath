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
import kiss.Ⅱ;

/**
 * @version 2018/04/08 12:22:35
 */
public class Directory extends Location<Directory> {

    /**
     * @param path
     */
    Directory(Path path) {
        super(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory absolutize() {
        if (path.isAbsolute()) {
            return this;
        }
        return Locator.directory(path.toAbsolutePath());
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
     * Shortcut for {@link Locator#file(Path)}
     * 
     * @param path A file path.
     * @return A located {@link File}.
     */
    public File file(File path) {
        return file(path.path);
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
     * Shortcut for {@link Locator#directory(Path)}
     * 
     * @param path A directory path.
     * @return A located {@link Directory}.
     */
    public Directory directory(Directory path) {
        return directory(path.path);
    }

    /**
     * Walk file tree and collect absolute {@link File}s which are filtered by various conditions.
     * 
     * @param filters Glob patterns.
     * @return All matched absolute {@link File}s.
     */
    public Signal<File> walkFiles(String... filters) {
        return walkFiles(3, filters, null, Integer.MAX_VALUE, false);
    }

    /**
     * Walk file tree and collect absolute {@link File}s which are filtered by various conditions.
     * 
     * @param filters Your condition.
     * @return All matched absolute {@link File}s.
     */
    public Signal<File> walkFiles(BiPredicate<Path, BasicFileAttributes> filters) {
        return walkFiles(3, null, filters, Integer.MAX_VALUE, false);
    }

    /**
     * Walk file tree and collect absolute {@link File}s which are filtered by various conditions.
     * 
     * @param filters Your condition.
     * @param depth A max file tree depth to search.
     * @return All matched absolute {@link File}s.
     */
    public Signal<File> walkFiles(BiPredicate<Path, BasicFileAttributes> filters, int depth) {
        return walkFiles(3, null, filters, depth, false);
    }

    /**
     * Walk file tree and collect absolute {@link File}s which are filtered by various conditions.
     * 
     * @param type Scan type.
     * @param patterns Glob patterns.
     * @param filters Your condition.
     * @param depth A max file tree depth to search.
     * @return All matched absolute {@link File}s.
     */
    private Signal<File> walkFiles(int type, String[] patterns, BiPredicate<Path, BasicFileAttributes> filters, int depth, boolean relatively) {
        return new Signal<File>((observer, disposer) -> {
            // build new scanner
            CymaticScan scanner;

            if (filters == null) {
                scanner = new CymaticScan(path, null, type, observer, disposer, patterns);
            } else {
                scanner = new CymaticScan(path, null, type, observer, disposer, filters);
            }
            scanner.relatively = relatively;

            // try to scan
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, scanner);
                observer.complete();
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * Walk file tree and collect relative {@link File}s which are filtered by various conditions.
     * 
     * @param filters Glob patterns.
     * @return All matched relative {@link File}s.
     */
    public Signal<Ⅱ<Directory, File>> walkFilesRelatively(String... filters) {
        return walkFilesRelatively(3, filters, null, Integer.MAX_VALUE);
    }

    /**
     * Walk file tree and collect relative {@link File}s which are filtered by various conditions.
     * 
     * @param filters Your condition.
     * @return All matched relative {@link File}s.
     */
    public Signal<Ⅱ<Directory, File>> walkFilesRelatively(BiPredicate<Path, BasicFileAttributes> filters) {
        return walkFilesRelatively(3, null, filters, Integer.MAX_VALUE);
    }

    /**
     * Walk file tree and collect relative {@link File}s which are filtered by various conditions.
     * 
     * @param filters Your condition.
     * @param depth A max file tree depth to search.
     * @return All matched relative {@link File}s.
     */
    public Signal<Ⅱ<Directory, File>> walkFilesRelatively(BiPredicate<Path, BasicFileAttributes> filters, int depth) {
        return walkFilesRelatively(3, null, filters, depth);
    }

    /**
     * Walk file tree and collect absolute {@link File}s which are filtered by various conditions.
     * 
     * @param type Scan type.
     * @param patterns Glob patterns.
     * @param filters Your condition.
     * @param depth A max file tree depth to search.
     * @return All matched absolute {@link File}s.
     */
    private Signal<Ⅱ<Directory, File>> walkFilesRelatively(int type, String[] patterns, BiPredicate<Path, BasicFileAttributes> filters, int depth) {
        return walkFiles(type, patterns, filters, depth, true).map(file -> I.pair(this, file));
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
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, new CymaticScan(path, null, 4, observer, disposer, patterns));
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
                Files.walkFileTree(path, Collections.EMPTY_SET, depth, new CymaticScan(path, null, 4, observer, disposer, filter));
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
     * Move a input {@link Path} to an output {@link Path} with its attributes. Simplified strategy
     * is the following:
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
     * If the output file already exists, it will be replaced by input file unconditionaly. The
     * exact file attributes that are copied is platform and file system dependent and therefore
     * unspecified. Minimally, the last-modified-time is copied to the output file if supported by
     * both the input and output file store. Copying of file timestamps may result in precision
     * loss.
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
     * @throws NoSuchFileException If the input file is directory and the output file is
     *             <em>not</em> directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public void moveTo(Directory destination, String... patterns) {
        new Visitor(path, destination.path, 1, patterns).walk();
    }

    /**
     * <p>
     * Move a input {@link Path} to an output {@link Path} with its attributes. Simplified strategy
     * is the following:
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
     * If the output file already exists, it will be replaced by input file unconditionaly. The
     * exact file attributes that are copied is platform and file system dependent and therefore
     * unspecified. Minimally, the last-modified-time is copied to the output file if supported by
     * both the input and output file store. Copying of file timestamps may result in precision
     * loss.
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
     * @throws NoSuchFileException If the input file is directory and the output file is
     *             <em>not</em> directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public void moveTo(Directory destination, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(path, destination.path, 1, filter).walk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyTo(Directory destination) {
        copyTo(destination, (BiPredicate) null);
    }

    /**
     * <p>
     * Copy a input {@link Path} to the output {@link Path} with its attributes. Simplified strategy
     * is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   if (output.isFile) {
     *     // Copy input file to output file.
     *   } else {
     *     // Copy input file to output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Copy input directory under output directory deeply.
     *     // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     *   }
     * }
     * </pre>
     * <p>
     * If the output file already exists, it will be replaced by input file unconditionaly. The
     * exact file attributes that are copied is platform and file system dependent and therefore
     * unspecified. Minimally, the last-modified-time is copied to the output file if supported by
     * both the input and output file store. Copying of file timestamps may result in precision
     * loss.
     * </p>
     * <p>
     * Copying a file is not an atomic operation. If an {@link IOException} is thrown then it
     * possible that the output file is incomplete or some of its file attributes have not been
     * copied from the input file.
     * </p>
     *
     * @param destination An output {@link Path} object which can be file or directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file is directory and the output file is
     *             <em>not</em> directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public void copyTo(Directory destination, String... patterns) {
        new Visitor(path, destination.path, 0, patterns).walk();
    }

    /**
     * <p>
     * Copy a input {@link Path} to the output {@link Path} with its attributes. Simplified strategy
     * is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   if (output.isFile) {
     *     // Copy input file to output file.
     *   } else {
     *     // Copy input file to output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Copy input directory under output directory deeply.
     *   }
     * }
     * </pre>
     * <p>
     * If the output file already exists, it will be replaced by input file unconditionaly. The
     * exact file attributes that are copied is platform and file system dependent and therefore
     * unspecified. Minimally, the last-modified-time is copied to the output file if supported by
     * both the input and output file store. Copying of file timestamps may result in precision
     * loss.
     * </p>
     * <p>
     * Copying a file is not an atomic operation. If an {@link IOException} is thrown then it
     * possible that the output file is incomplete or some of its file attributes have not been
     * copied from the input file.
     * </p>
     *
     * @param destination An output {@link Path} object which can be file or directory.
     * @param filter A file filter to copy.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file is directory and the output file is
     *             <em>not</em> directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public void copyTo(Directory destination, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(path, destination.path, 0, filter).walk();
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

    /**
     * Constructs a relative path between this path and a given path.
     * <p>
     * Relativization is the inverse of {@link #resolve(Path) resolution}. This method attempts to
     * construct a {@link #isAbsolute relative} path that when {@link #resolve(Path) resolved}
     * against this path, yields a path that locates the same file as the given path. For example,
     * on UNIX, if this path is {@code "/a/b"} and the given path is {@code "/a/b/c/d"} then the
     * resulting relative path would be {@code "c/d"}. Where this path and the given path do not
     * have a {@link #getRoot root} component, then a relative path can be constructed. A relative
     * path cannot be constructed if only one of the paths have a root component. Where both paths
     * have a root component then it is implementation dependent if a relative path can be
     * constructed. If this path and the given path are {@link #equals equal} then an <i>empty
     * path</i> is returned.
     * <p>
     * For any two {@link #normalize normalized} paths <i>p</i> and <i>q</i>, where <i>q</i> does
     * not have a root component, <blockquote> <i>p</i>{@code .relativize(}<i>p</i>
     * {@code .resolve(}<i>q</i>{@code )).equals(}<i>q</i>{@code )} </blockquote>
     * <p>
     * When symbolic links are supported, then whether the resulting path, when resolved against
     * this path, yields a path that can be used to locate the {@link Files#isSameFile same} file as
     * {@code other} is implementation dependent. For example, if this path is {@code "/a/b"} and
     * the given path is {@code "/a/x"} then the resulting relative path may be {@code
     * "../x"}. If {@code "b"} is a symbolic link then is implementation dependent if
     * {@code "a/b/../x"} would locate the same file as {@code "/a/x"}.
     *
     * @param other the path to relativize against this path
     * @return the resulting relative path, or an empty path if both paths are equal
     * @throws IllegalArgumentException if {@code other} is not a {@code Path} that can be
     *             relativized against this path
     */
    public File relativize(File file) {
        return Locator.file(path.relativize(file.path));
    }

    /**
     * Constructs a relative path between this path and a given path.
     * <p>
     * Relativization is the inverse of {@link #resolve(Path) resolution}. This method attempts to
     * construct a {@link #isAbsolute relative} path that when {@link #resolve(Path) resolved}
     * against this path, yields a path that locates the same file as the given path. For example,
     * on UNIX, if this path is {@code "/a/b"} and the given path is {@code "/a/b/c/d"} then the
     * resulting relative path would be {@code "c/d"}. Where this path and the given path do not
     * have a {@link #getRoot root} component, then a relative path can be constructed. A relative
     * path cannot be constructed if only one of the paths have a root component. Where both paths
     * have a root component then it is implementation dependent if a relative path can be
     * constructed. If this path and the given path are {@link #equals equal} then an <i>empty
     * path</i> is returned.
     * <p>
     * For any two {@link #normalize normalized} paths <i>p</i> and <i>q</i>, where <i>q</i> does
     * not have a root component, <blockquote> <i>p</i>{@code .relativize(}<i>p</i>
     * {@code .resolve(}<i>q</i>{@code )).equals(}<i>q</i>{@code )} </blockquote>
     * <p>
     * When symbolic links are supported, then whether the resulting path, when resolved against
     * this path, yields a path that can be used to locate the {@link Files#isSameFile same} file as
     * {@code other} is implementation dependent. For example, if this path is {@code "/a/b"} and
     * the given path is {@code "/a/x"} then the resulting relative path may be {@code
     * "../x"}. If {@code "b"} is a symbolic link then is implementation dependent if
     * {@code "a/b/../x"} would locate the same file as {@code "/a/x"}.
     *
     * @param other the path to relativize against this path
     * @return the resulting relative path, or an empty path if both paths are equal
     * @throws IllegalArgumentException if {@code other} is not a {@code Path} that can be
     *             relativized against this path
     */
    public Directory relativize(Directory directory) {
        return Locator.directory(path.relativize(directory.path));
    }
}

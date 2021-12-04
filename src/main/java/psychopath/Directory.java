/*
 * Copyright (C) 2021 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkPermission;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.Ⅱ;

public class Directory extends Location<Directory> {

    private final Function<Option, Option> option;

    /**
     * @param path
     */
    Directory(Path path) {
        this(path, Function.identity());
    }

    /**
     * 
     */
    Directory(Path path, Function<Option, Option> option) {
        super(path);
        this.option = option;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> children() {
        return new Signal<>((observer, disposer) -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path path : stream) {
                    observer.accept(Locator.locate(path));
                }
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> descendant() {
        return I.signal((Location) this).recurseMap(s -> s.flatMap(Location::children)).skip(this);
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
     * Whether this {@link Directory} has some items or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅱ<Directory, File>> walkFileWithBase(Function<Option, Option> option) {
        return walk(File.class, this, 3, option).map(file -> I.pair(this, file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅱ<Directory, Directory>> walkDirectoryWithBase(Function<Option, Option> option) {
        return walk(Directory.class, this, 4, option).skip(this).map(dir -> I.pair(this, dir));
    }

    /**
     * Walk file tree and collect {@link File}s which are filtered by various conditions.
     * 
     * @param type Scan type.
     * @param patterns Glob patterns.
     * @param filters Your condition.
     * @param depth A max file tree depth to search.
     * @return All matched {@link File}s.
     */
    private <L extends Location> Signal<L> walk(Class<L> clazz, Directory out, int type, Function<Option, Option> option) {
        return new Signal<L>((observer, disposer) -> {
            try {
                Option o = this.option.andThen(option).apply(new Option());
                Files.walkFileTree(path, Set.of(), o.depth, new CymaticScan(this, out, type, observer, disposer, o));
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory create(FileAttribute<?>... attrs) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path, attrs);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
        return this;
    }

    /**
     * <p>
     * Delete this {@link Directory} by using various {@link Option}.
     * </p>
     * <p>
     * On some operating systems it may not be possible to remove a file when it is open and in use
     * by this Java virtual machine or other programs.
     * </p>
     *
     * @param option A {@link Option} builder.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    @Override
    public Signal<Location> observeDeleting(Function<Option, Option> option) {
        return walk(Location.class, this, 2, option);
    }

    /**
     * <p>
     * Copy this {@link Directory} to the output {@link Directory} by using various {@link Option}.
     * </p>
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
     * @param destination An output {@link Directory}.
     * @param option A {@link Option} builder.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    @Override
    public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
        return walk(Location.class, destination, 0, option);
    }

    /**
     * <p>
     * Move this {@link Directory} to an output {@link Directory} by using various {@link Option}s.
     * </p>
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
     * @param destination An output {@link Path} object which can be file or directory.
     * @param option A {@link Option} builder.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    @Override
    public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
        return walk(Location.class, destination, 1, option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> observePackingTo(File destination, Function<Option, Option> option) {
        return Locator.folder().add(this, option).observePackingTo(destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<WatchEvent<Location>> observe() {
        return observe(new String[0]);
    }

    /**
     * <p>
     * Observe the file system change and raises events when a file, directory, or file in a
     * directory, changes.
     * </p>
     * <p>
     * You can watch for changes in files and subdirectories of the specified directory.
     * </p>
     * <p>
     * The operating system interpret a cut-and-paste action or a move action as a rename action for
     * a directory and its contents. If you cut and paste a folder with files into a directory being
     * watched, the {@link Observer} object reports only the directory as new, but not its contents
     * because they are essentially only renamed.
     * </p>
     * <p>
     * Common file system operations might raise more than one event. For example, when a file is
     * moved from one directory to another, several Modify and some Create and Delete events might
     * be raised. Moving a file is a complex operation that consists of multiple simple operations,
     * therefore raising multiple events. Likewise, some applications might cause additional file
     * system events that are detected by the {@link Observer}.
     * </p>
     *
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out. Ignore
     *            patterns if you want to observe a file.
     * @return A observable event stream.
     * @throws NullPointerException If the specified path or listener is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public Signal<WatchEvent<Location>> observe(String... patterns) {
        return new Signal<>((observer, disposer) -> {
            // Create logical file system watch service.
            CymaticScan watcher = new CymaticScan(this, observer, disposer, patterns);

            // Run in anothor thread.
            I.schedule(watcher);

            // API definition
            return watcher;
        });
    }

    /**
     * <p>
     * Observe the file system change and raises events when a file, directory, or file in a
     * directory, changes.
     * </p>
     * <p>
     * You can watch for changes in files and subdirectories of the specified directory.
     * </p>
     * <p>
     * The operating system interpret a cut-and-paste action or a move action as a rename action for
     * a directory and its contents. If you cut and paste a folder with files into a directory being
     * watched, the {@link Observer} object reports only the directory as new, but not its contents
     * because they are essentially only renamed.
     * </p>
     * <p>
     * Common file system operations might raise more than one event. For example, when a file is
     * moved from one directory to another, several Modify and some Create and Delete events might
     * be raised. Moving a file is a complex operation that consists of multiple simple operations,
     * therefore raising multiple events. Likewise, some applications might cause additional file
     * system events that are detected by the {@link Observer}.
     * </p>
     *
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out. Ignore
     *            patterns if you want to observe a file.
     * @return A observable event stream.
     * @throws NullPointerException If the specified path or listener is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public Signal<WatchEvent<Location>> observe(Collection<String> patterns) {
        return observe(patterns.toArray(new String[patterns.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<FileLock> lock() {
        return file(".lock").lock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Directory convert(Path path) {
        return Locator.directory(path);
    }
}
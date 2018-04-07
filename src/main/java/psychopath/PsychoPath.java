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

import static psychopath.PsychoPathFileSystemProvider.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import kiss.I;

/**
 * @version 2018/04/07 22:41:16
 */
public class PsychoPath implements Path {

    /** The root temporary directory for Sinobu. */
    private static final PsychoPath temporaries;

    /** The temporary directory for the current processing JVM. */
    private static final PsychoPath temporary;

    static {
        try {
            // Create the root temporary directory for Sinobu.
            temporaries = new PsychoPath(Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), "PsychoPath")));

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (PsychoPath path : temporaries.walkDirectory("temporary*")) {
                // create a file to represent the lock
                RandomAccessFile file = new RandomAccessFile(path.resolve("lock").toFile(), "rw");

                // test whether we can acquire lock or not
                FileLock lock = file.getChannel().tryLock();

                // release lock immediately
                file.close();

                // delete the all contents in the temporary directory since we could acquire a
                // exclusive lock
                if (lock != null) {
                    try {
                        path.delete();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            // Create the temporary directory for the current processing JVM.
            temporary = new PsychoPath(Files.createTempDirectory(temporaries, "temporary"));

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            new RandomAccessFile(temporary.resolve("lock").toFile(), "rw").getChannel().tryLock();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The actual {@link Path} . */
    final Path base;

    /**
     * @param base
     */
    PsychoPath(Path base) {
        this.base = base;
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
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An output {@link Path} object which can be file or directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an  I/O error occurs.
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
    public void copy(Path output, String... patterns) {
        new Visitor(this, output, 0, patterns).walk();
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
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An output {@link Path} object which can be file or directory.
     * @param filter A file filter to copy.
     * @throws IOException If an  I/O error occurs.
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
    public void copy(Path output, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(this, output, 0, filter).walk();
    }

    /**
     * <p>
     * Delete a input {@link Path}. Simplified strategy is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   // Delete input file unconditionaly.
     * } else {
     *   // Delete input directory deeply.
     *   // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     * }
     * </pre>
     * <p>
     * On some operating systems it may not be possible to remove a file when it is open and in use
     * by this Java virtual machine or other programs.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an  I/O error occurs.
     * @throws NullPointerException If the specified input file is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public void delete(String... patterns) {
        new Visitor(this, null, 2, patterns).walk();
    }

    /**
     * <p>
     * Delete a input {@link Path}. Simplified strategy is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   // Delete input file unconditionaly.
     * } else {
     *   // Delete input directory deeply.
     *   // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     * }
     * </pre>
     * <p>
     * On some operating systems it may not be possible to remove a file when it is open and in use
     * by this Java virtual machine or other programs.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param filter A file filter.
     * @throws IOException If an  I/O error occurs.
     * @throws NullPointerException If the specified input file is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public void delete(BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(this, null, 2, filter).walk();
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
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an  I/O error occurs.
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
    public void move(Path output, String... patterns) {
        new Visitor(this, output, 1, patterns).walk();
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
     * @throws IOException If an  I/O error occurs.
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
    public void move(Path output, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(this, output, 1, filter).walk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(Consumer<? super Path> action) {
        base.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spliterator<Path> spliterator() {
        return base.spliterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem() {
        return new PsychoPathFileSystem(base.getFileSystem());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbsolute() {
        return base.isAbsolute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath getRoot() {
        return new PsychoPath(base.getRoot());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath getFileName() {
        return new PsychoPath(base.getFileName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath getParent() {
        return new PsychoPath(base.getParent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        return base.getNameCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath getName(int index) {
        return new PsychoPath(base.getName(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath subpath(int beginIndex, int endIndex) {
        return new PsychoPath(base.subpath(beginIndex, endIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(Path other) {
        return base.startsWith(unwrap(other));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(String other) {
        return base.startsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(Path other) {
        return base.endsWith(unwrap(other));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(String other) {
        return base.endsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath normalize() {
        return new PsychoPath(base.normalize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath resolve(Path other) {
        return new PsychoPath(base.resolve(unwrap(other)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath resolve(String other) {
        return new PsychoPath(base.resolve(other));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath resolveSibling(Path other) {
        return new PsychoPath(base.resolveSibling(unwrap(other)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath resolveSibling(String other) {
        return new PsychoPath(base.resolveSibling(other));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath relativize(Path other) {
        return new PsychoPath(base.relativize(unwrap(other)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI toUri() {
        return base.toUri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath toAbsolutePath() {
        return new PsychoPath(base.toAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychoPath toRealPath(LinkOption... options) throws IOException {
        return new PsychoPath(base.toRealPath(options));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File toFile() {
        return base.toFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        return base.register(watcher, events, modifiers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return base.register(watcher, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Path> iterator() {
        return base.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Path other) {
        return base.compareTo(unwrap(other));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        return base.equals(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return base.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return base.toString();
    }

    /**
     * <p>
     * Walk a file tree and collect files you want to filter by pattern matching.
     * </p>
     *
     * @param start A depature point. The result list doesn't include this starting path.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched Files. (<em>not</em> including directory)
     */
    public List<PsychoPath> walk(String... patterns) {
        return new Visitor(this, null, 3, patterns).walk();
    }

    /**
     * <p>
     * Walk a file tree and collect files you want to filter by pattern matching.
     * </p>
     *
     * @param start A depature point. The result list doesn't include this starting path.
     * @param filter A file filter to visit.
     * @return All matched Files. (<em>not</em> including directory)
     */
    public List<PsychoPath> walk(BiPredicate<Path, BasicFileAttributes> filter) {
        return new Visitor(this, null, 3, filter).walk();
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param start A depature point. The result list include this starting path.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public List<PsychoPath> walkDirectory(String... patterns) {
        return new Visitor(this, null, 4, patterns).walk();
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param start A departure point. The result list include this starting path.
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public List<PsychoPath> walkDirectory(BiPredicate<Path, BasicFileAttributes> filter) {
        return new Visitor(this, null, 4, filter).walk();
    }

    /**
     * <p>
     * Locate the specified file URL and return the plain {@link Path} object.
     * </p>
     *
     * @param path A location path.
     * @return A located {@link Path}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a   security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static PsychoPath locate(URL path) {
        try {
            // Use File constructor with URI to resolve escaped character.
            return new PsychoPath(new File(path.toURI()).toPath());
        } catch (URISyntaxException e) {
            return new PsychoPath(new File(path.getPath()).toPath());
        }
    }

    /**
     * <p>
     * Locate the specified file path and return the plain {@link Path} object.
     * </p>
     *
     * @param filePath A location path.
     * @return A located {@link Path}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a   security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static PsychoPath locate(String filePath) {
        return new PsychoPath(Paths.get(filePath));
    }

    /**
     * <p>
     * Locate the class archive (e.g. jar file, classes directory) by the specified sample class. If
     * the sample class belongs to system classloader (e.g. {@link String}), <code>null</code> will
     * be returned.
     * </p>
     *
     * @param clazz A sample class.
     * @return A class archive (e.g. jar file, classes directory) or <code>null</code>.
     */
    public static PsychoPath locate(Class clazz) {
        // retrieve code source of this sample class
        CodeSource source = clazz.getProtectionDomain().getCodeSource();

        // API definition
        return (source == null) ? null : locate(source.getLocation());
    }

    /**
     * <p>
     * Locate the class resource (e.g. in jar file, in classes directory) by the specified sample
     * class. If the sample class belongs to system classloader (e.g. {@link String}),
     * <code>null</code> will be returned.
     * </p>
     *
     * @param clazz A sample class.
     * @param filePath A location path.
     * @return A class resource (e.g. in jar file, in classes directory) or <code>null</code>.
     */
    public static PsychoPath locate(Class clazz, String filePath) {
        try {
            Path root = locate(clazz);

            if (Files.isRegularFile(root)) {
                root = FileSystems.newFileSystem(root, PsychoPath.class.getClassLoader()).getPath("/");
            }
            return new PsychoPath(root.resolve(clazz.getName().replaceAll("\\.", "/")).resolveSibling(filePath));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Creates a new abstract file somewhere beneath the system's temporary directory (as defined by
     * the <code>java.io.tmpdir</code> system property).
     * </p>
     *
     * @return A newly created temporary file which is not exist yet.
     * @throws SecurityException If a   security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static PsychoPath locateTemporary() {
        try {
            Path path = Files.createTempDirectory(temporary, "temporary");

            // Delete entity file.
            Files.delete(path);

            // API definition
            return new PsychoPath(path);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Locate the specified file path and return the plain {@link Path} object.
     * </p>
     *
     * @param filePath A location path.
     * @return A located {@link Path}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a   security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static PsychoPath locate(Path path) {
        if (path instanceof PsychoPath) {
            return (PsychoPath) path;
        } else {
            return new PsychoPath(path);
        }
    }
}

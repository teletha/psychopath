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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.List;
import java.util.function.BiPredicate;

import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2018/04/08 9:19:09
 */
public class PsychoPath {

    /** The root temporary directory for Sinobu. */
    private static final Path temporaries;

    /** The temporary directory for the current processing JVM. */
    private static final Path temporary;

    static {
        try {
            // Create the root temporary directory for Sinobu.
            temporaries = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), PsychoPath.class.getName()));

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (Path path : walkDirectory(temporaries, "temporary*")) {
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
                        delete(path);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            // Create the temporary directory for the current processing JVM.
            temporary = Files.createTempDirectory(temporaries, "temporary");

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            new RandomAccessFile(temporary.resolve("lock").toFile(), "rw").getChannel().tryLock();
        } catch (Exception e) {
            throw I.quiet(e);
        }
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
    public static List<Path> walkDirectory(Path start, String... patterns) {
        return new Visitor(start, null, 4, patterns).walk();
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
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input file is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static void delete(Path input, String... patterns) {
        if (input != null) {
            new Visitor(input, null, 2, patterns).walk();
        }
    }

    /**
     * <p>
     * Locate the specified file URL and return the plain {@link Path} object.
     * </p>
     *
     * @param filePath A location path.
     * @return A located {@link Path}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path locate(URL filePath) {
        try {
            // Use File constructor with URI to resolve escaped character.
            return new File(filePath.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(filePath.getPath()).toPath();
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
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path locate(String filePath) {
        return Paths.get(filePath);
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
    public static Path locate(Class clazz) {
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
    public static Path locate(Class clazz, String filePath) {
        try {
            Path root = locate(clazz);

            if (Files.isRegularFile(root)) {
                root = FileSystems.newFileSystem(root, null).getPath("/");
            }
            return root.resolve(clazz.getName().replaceAll("\\.", "/")).resolveSibling(filePath);
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
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path locateTemporary() {
        try {
            Path path = Files.createTempDirectory(temporary, "temporary");

            // Delete entity file.
            Files.delete(path);

            // API definition
            return path;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}

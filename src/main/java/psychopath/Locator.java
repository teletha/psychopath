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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.function.Consumer;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

public class Locator {

    /** The root temporary directory for psychopath. */
    private static final Directory temporaries;

    /** The temporary directory for the current processing JVM. */
    private static final Path temporary;

    static {
        I.load(DirectoryCodec.class, false);

        try {
            // Create the root temporary directory for psychopath.
            temporaries = directory(Path.of(System.getProperty("java.io.tmpdir"), "psychopath"));

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (Directory sub : temporaries.walkDirectories("temporary*").toList()) {
                // create a file to represent the lock
                RandomAccessFile file = new RandomAccessFile(sub.file("lock").asJavaFile(), "rw");

                // test whether we can acquire lock or not
                FileLock lock = file.getChannel().tryLock();

                // release lock immediately
                file.close();

                // delete the all contents in the temporary directory since we could acquire a
                // exclusive lock
                if (lock != null) {
                    try {
                        sub.delete();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            // Create the temporary directory for the current processing JVM.
            Files.createDirectories(temporaries.path);
            temporary = Files.createTempDirectory(temporaries.path, "temporary");

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            new RandomAccessFile(temporary.resolve("lock").toFile(), "rw").getChannel().tryLock();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Locate {@link File}.
     * 
     * @param path A path to the file.
     * @return The specified {@link File}.
     */
    public static File file(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Empty file name is invalid.");
        }
        return file(Paths.get(path));
    }

    /**
     * Locate the specified file URL and return the plain {@link File} object.
     *
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static File file(URL filePath) {
        return file(locate(filePath));
    }

    /**
     * Locate {@link File}.
     * 
     * @param path A path to the file.
     * @return The specified {@link File}.
     */
    public static File file(Path path) {
        return new File(path);
    }

    /**
     * Locate {@link File}.
     * 
     * @param base A base directory.
     * @param path A path to the file.
     * @return The specified {@link File}.
     */
    public static File file(Directory base, String path) {
        return file(base.path.resolve(path));
    }

    /**
     * Locate {@link File}.
     * 
     * @param base A base directory.
     * @param path A path to the file.
     * @return The specified {@link File}.
     */
    public static File file(Directory base, Path path) {
        return file(base.path.resolve(path));
    }

    /**
     * Locate {@link Directory}.
     * 
     * @param path A path to the directory.
     * @return The specified {@link Directory}.
     */
    public static Directory directory(String path) {
        return directory(Paths.get(path));
    }

    /**
     * Locate the specified file URL and return the plain {@link Directory} object.
     *
     * @param directoryPath A location path.
     * @return A located {@link Directory}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Directory directory(URL directoryPath) {
        return directory(locate(directoryPath));
    }

    /**
     * Locate {@link Directory}.
     * 
     * @param path A path to the directory.
     * @return The specified {@link Directory}.
     */
    public static Directory directory(Path path) {
        return new Directory(path);
    }

    /**
     * Locate {@link Directory}.
     * 
     * @param base A base directory.
     * @param path A path to the directory.
     * @return The specified {@link Directory}.
     */
    public static Directory directory(Directory base, String path) {
        return directory(base.path.resolve(path));
    }

    /**
     * Locate {@link Directory}.
     * 
     * @param base A base directory.
     * @param path A path to the directory.
     * @return The specified {@link Directory}.
     */
    public static Directory directory(Directory base, Path path) {
        return directory(base.path.resolve(path));
    }

    /**
     * Locate the class archive (e.g. jar file, classes directory) by the specified sample class. If
     * the sample class belongs to system classloader (e.g. {@link String}), <code>null</code> will
     * be returned.
     *
     * @param clazz A sample class.
     * @return A class archive (e.g. jar file, classes directory) or <code>null</code>.
     */
    public static Location locate(Class clazz) {
        // retrieve code source of this sample class
        CodeSource source = clazz.getProtectionDomain().getCodeSource();

        // API definition
        return (source == null) ? null : locate(locate(source.getLocation()));
    }

    /**
     * Locate the class resource (e.g. in jar file, in classes directory) by the specified sample
     * class. If the sample class belongs to system classloader (e.g. {@link String}),
     * <code>null</code> will be returned.
     *
     * @param clazz A sample class.
     * @param filePath A location path.
     * @return A class resource (e.g. in jar file, in classes directory) or <code>null</code>.
     */
    public static File locate(Class clazz, String filePath) {
        try {
            Location root = locate(clazz);

            if (!root.isDirectory()) {
                root = directory(FileSystems.newFileSystem(root.path, null).getPath("/"));
            }

            Directory dir = (Directory) root;
            return dir.file(clazz.getName().replaceAll("\\.", "/")).parent().file(filePath);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Convert {@link URL} to {@link Path}.
     * 
     * @param url
     * @return
     */
    private static Path locate(URL url) {
        try {
            // Use File constructor with URI to resolve escaped character.
            return new java.io.File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new java.io.File(url.getPath()).toPath();
        }
    }

    /**
     * Convert {@link Path} to {@link Location}.
     * 
     * @param path
     * @return
     */
    public static Location locate(Path path) {
        if (Files.isDirectory(path)) {
            return directory(path);
        } else {
            return file(path);
        }
    }

    /**
     * Locate {@link Temporary}.
     * 
     * @param file A path to the archive file.
     * @return The specified archive.
     */
    public static Temporary temporary() {
        return new Temporary();
    }

    /**
     * Locate the system temporary {@link Directory}.
     * 
     * @return
     */
    public static Directory temporaryDirectory() {
        try {
            return directory(Files.createTempDirectory(temporary, "temporary"));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Creates a new abstract file somewhere beneath the system's temporary directory (as defined by
     * the <code>java.io.tmpdir</code> system property).
     * 
     * @return
     */
    public static File temporaryFile() {
        try {
            return file(Files.createTempDirectory(temporary, "temporary"));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    public static Consumer<Directory> copy(Path destination, String... patterns) {
        return copy(directory(destination), patterns);
    }

    public static Consumer<Directory> copy(Directory destination, String... patterns) {
        return departure -> {
            departure.copyTo(departure, patterns);
        };
    }

    public static Consumer<Directory> move(Path destination, String... patterns) {
        return move(directory(destination), patterns);
    }

    public static Consumer<Directory> move(Directory destination, String... patterns) {
        return departure -> {
            departure.moveTo(departure, patterns);
        };
    }

    /**
     * @version 2018/09/27 9:47:39
     */
    private static class DirectoryCodec implements Decoder<Directory>, Encoder<Directory> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Directory value) {
            return value.path();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Directory decode(String value) {
            return Locator.directory(value);
        }
    }

    /**
     * @version 2018/09/27 9:47:39
     */
    @SuppressWarnings("unused")
    private static class FileCodec implements Decoder<File>, Encoder<File> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(File value) {
            return value.path();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public File decode(String value) {
            return Locator.file(value);
        }
    }
}

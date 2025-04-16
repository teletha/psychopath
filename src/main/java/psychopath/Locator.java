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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.security.SecureRandom;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

public class Locator {

    /** The root temporary directory for psychopath. */
    private static final Directory temporaries;

    /** The temporary directory for the current processing JVM. */
    private static final Directory temporary;

    /** The temporary name generator. */
    private static final SecureRandom random = new SecureRandom();

    static {
        I.load(DirectoryCodec.class);

        try {
            // Create the root temporary directory for psychopath.
            temporaries = directory(Path.of(System.getProperty("java.io.tmpdir"), "psychopath"));

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (Directory sub : temporaries.walkDirectory("temporary*").toList()) {
                // Prevent race condition with other JVMs by checking the last modified time
                if (System.currentTimeMillis() - sub.lastModifiedMilli() >= 1000 * 60 * 10) {
                    // create a file to represent the lock
                    try (RandomAccessFile file = new RandomAccessFile(sub.file("lock").asJavaFile(), "rw")) {
                        // test whether we can acquire lock or not
                        FileLock lock = file.getChannel().tryLock();

                        // delete the all contents in the temporary directory
                        // since we could acquire a exclusive lock
                        if (lock != null) {
                            // unlock immediately
                            lock.release();

                            // Clean up old temporary directories asynchronously.
                            I.schedule(sub::delete);
                        }
                    }
                }
            }

            // Create the temporary directory for the current processing JVM.
            temporary = create(Directory.class, temporaries);

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            Path lockPath = temporary.file("lock").asJavaPath();
            FileChannel channel = null;
            FileLock jvmLock = null;

            try {
                channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                jvmLock = channel.tryLock();

                if (jvmLock == null) {
                    throw new IllegalStateException("Could not acquire lock for JVM temporary directory: " + temporary + ". Another process might be holding the lock or there could be a file system issue.");
                }

                // Note: Ideally, 'channel' and 'jvmLock' should be kept referenced somewhere
                // to explicitly manage their lifecycle, but within a static initializer, relying on
                // JVM exit for release is a common (though less explicit) pattern. If you have a
                // central shutdown hook mechanism, releasing the lock there would be cleaner.
            } catch (Throwable e) {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ignored) {
                        // Ignore close exception
                    }
                }
                throw new IllegalStateException("Failed to create or lock the JVM temporary directory lock file: " + lockPath, e);
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * Locate {@link Folder}.
     * 
     * @return The specified archive.
     */
    public static Folder folder() {
        return new Folder();
    }

    /**
     * Locate {@link File}.
     * 
     * @param path A path to the file.
     * @return The specified {@link File}.
     */
    public static File file(String path) {
        return file(Paths.get(path));
    }

    /**
     * Locate the specified file URL and return the plain {@link File} object.
     *
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
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
                root = directory(FileSystems.newFileSystem(root.path, (ClassLoader) null).getPath("/"));
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
     * Convert path to {@link Location}.
     * 
     * @param path
     * @return
     */
    public static Location locate(String path) {
        return locate(Path.of(path));
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
     * Convert {@link java.io.File} to {@link Location}.
     * 
     * @param path
     * @return
     */
    public static Location locate(java.io.File path) {
        return locate(path.toPath());
    }

    /**
     * Generates temporary files that are guaranteed to be created and deleted safely. The file name
     * will be completely random.
     * 
     * @return The definitely generated temporary file.
     */
    public static File temporaryFile() {
        return create(File.class, temporary);
    }

    /**
     * Generates temporary files that are guaranteed to be created and deleted safely. The file name
     * can be specified.
     * 
     * @param name A file name.
     * @return The definitely generated temporary file.
     */
    public static File temporaryFile(String name) {
        return temporaryDirectory().file(name);
    }

    /**
     * Generates temporary directory that are guaranteed to be created and deleted safely. The
     * directory name will be completely random.
     * 
     * @return The definitely generated temporary directory.
     */
    public static Directory temporaryDirectory() {
        return create(Directory.class, temporary);
    }

    /**
     * Generates temporary directory that are guaranteed to be created and deleted safely. The
     * directory name can be specified.
     * 
     * @param name A directory name.
     * @return The definitely generated temporary directory.
     */
    public static Directory temporaryDirectory(String name) {
        return temporaryDirectory().directory(name);
    }

    /**
     * <p>
     * Create the safe temporary file or directory.
     * </p>
     * <p>
     * When creating a file, you need to pay attention to the following.
     * </p>
     * <ul>
     * <li>Do not use a fixed file name unless there is a particular need for it.</li>
     * <li>Specify permissions at the time of creation (if you specify them later, there is a risk
     * that a third party will gain access between creation and permission setting).</li>
     * <li>Create a new file with the setting of "fail if file already exists" (to prevent attacks
     * that prepare a file in advance and wait for it).</li>
     * </ul>
     * <p>
     * If you check that a file meets a certain condition and then access it based on that
     * condition, there is a risk that a third party will intervene between the check and the
     * execution (especially in a shared temporary directory, where a third party can also create
     * files). Therefore, it is necessary to defend yourself by specifying that execution should be
     * performed while checking, or by making sure that the checked file and the file to be
     * manipulated are identical.
     * </p>
     * 
     * @param <T> A location type.
     * @param type A location type.
     * @param dir A target directory.
     * @return A generated temporary location.
     */
    private static <T extends Location> T create(Class<T> type, Directory dir) {
        Location temp;
        do {
            String name = "temporary" + Integer.toUnsignedString(random.nextInt());
            temp = type == File.class ? dir.file(name) : dir.directory(name);
        } while (temp.isPresent());

        return (T) temp.create();
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
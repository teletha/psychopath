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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

public class Locator {

    static {
        I.load(DirectoryCodec.class, false);
    }

    /**
     * Locate {@link Archive}.
     * 
     * @param path A path to the archive file.
     * @return The specified archive.
     */
    public static Archive archive(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Empty file name is invalid.");
        }
        return archive(file(path));
    }

    /**
     * Locate {@link Archive}.
     * 
     * @param path A path to the archive file.
     * @return The specified archive.
     */
    public static Archive archive(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Empty file name is invalid.");
        }
        return archive(file(path));
    }

    /**
     * Locate {@link Archive}.
     * 
     * @param file A path to the archive file.
     * @return The specified archive.
     */
    public static Archive archive(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Empty file name is invalid.");
        }
        return new Archive(file);
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
     * Locate the system temporary {@link Directory}.
     * 
     * @return
     */
    public static Directory temporary() {
        return directory(System.getProperty("java.io.tmpdir"));
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

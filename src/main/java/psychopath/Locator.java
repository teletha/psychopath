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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * @version 2018/06/02 19:35:53
 */
public class Locator {

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
}

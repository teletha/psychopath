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

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @version 2018/05/31 8:34:34
 */
public abstract class Location {

    /** The actual location. */
    protected final Path path;

    /**
     * @param path
     */
    protected Location(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    /**
     * Returns the name of the file or directory denoted by this path as a {@code Locate} object. The
     * file name is the <em>farthest</em> element from the root in the directory hierarchy.
     *
     * @return a path representing the name of the file or directory, or {@code null} if this path has
     *         zero elements
     */
    public String name() {
        return String.valueOf(path.getFileName());
    }

    /**
     * Locate parent {@link Directory}.
     * 
     * @return
     */
    public Directory parent() {
        return directory(path.getParent());
    }

    /**
     * Tests whether this location does not exist or not. This method is intended for cases where it is
     * required to take action when it can be confirmed that a file does not exist.
     * 
     * @param options options indicating how symbolic links are handled
     * @return {@code true} if the file does not exist; {@code false} if the file exists or its
     *         existence cannot be determined
     */
    public final boolean isAbsent(LinkOption... options) {
        return Files.notExists(path, options);
    }

    /**
     * Tests whether this location does exist or not. This method is intended for cases where it is
     * required to take action when it can be confirmed that a file does exist.
     * 
     * @param options options indicating how symbolic links are handled
     * @return {@code false} if the file does not exist; {@code true} if the file exists or its
     *         existence cannot be determined
     */
    public final boolean isPresent(LinkOption... options) {
        return Files.exists(path, options);
    }

    /**
     * Returns a {@link File} object representing this path. Where this {@code
     * Path} is associated with the default provider, then this method is equivalent to returning a
     * {@code File} object constructed with the {@code String} representation of this path.
     * <p>
     * If this path was created by invoking the {@code File} {@link File#toPath toPath} method then
     * there is no guarantee that the {@code
     * File} object returned by this method is {@link #equals equal} to the original {@code File}.
     *
     * @implSpec The default implementation is equivalent for this path to: <pre>{@code
     *     new File(toString());
     * }</pre> if the {@code FileSystem} which created this {@code Path} is the default file system;
     *           otherwise an {@code UnsupportedOperationException} is thrown.
     * @return a {@code File} object representing this path
     * @throws UnsupportedOperationException if this {@code Path} is not associated with the default
     *             provider
     */
    public final java.io.File asFile() {
        return path.toFile();
    }

    /**
     * Returns a {@link Path java.nio.file.Path} object constructed from the this abstract path. The
     * resulting {@code Path} is associated with the {@link java.nio.file.FileSystems#getDefault
     * default-filesystem}.
     * <p>
     * The first invocation of this method works as if invoking it were equivalent to evaluating the
     * expression: <blockquote><pre>
     * {@link java.nio.file.FileSystems#getDefault FileSystems.getDefault}().{@link
     * java.nio.file.FileSystem#getPath getPath}(this.{@link #getPath getPath}());
     * </pre></blockquote> Subsequent invocations of this method return the same {@code Path}.
     * <p>
     * If this abstract pathname is the empty abstract pathname then this method returns a {@code Path}
     * that may be used to access the current user directory.
     *
     * @return a {@code Path} constructed from this abstract path
     * @throws java.nio.file.InvalidPathException if a {@code Path} object cannot be constructed from
     *             the abstract path (see {@link java.nio.file.FileSystem#getPath FileSystem.getPath})
     */
    public final Path asPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return path.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return path.toString();
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
}

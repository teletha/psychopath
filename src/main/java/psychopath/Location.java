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
import java.nio.file.LinkOption;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2018/05/31 8:34:34
 */
public abstract class Location<Self extends Location> {

    /** The separator flag. */
    private static final boolean useNativeSeparator = java.io.File.separatorChar == '/';

    /** The actual location. */
    protected final Path path;

    /** The relative path. */
    String relative;

    /**
     * @param path
     */
    protected Location(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    /**
     * Returns the name of the file or directory denoted by this path as a {@code Location} object. The
     * file name is the <em>farthest</em> element from the root in the directory hierarchy.
     *
     * @return A path representing the name of the file or directory, or {@code null} if this path has
     *         zero elements
     */
    public final String name() {
        return String.valueOf(path.getFileName());
    }

    /**
     * Returns the path expression of this {@link Location}.
     * 
     * @return A path to this {@link Location}.
     */
    public final String path() {
        if (useNativeSeparator) {
            return path.toString();
        } else {
            return path.toString().replace(java.io.File.separatorChar, '/');
        }
    }

    public abstract Self absolutize();

    /**
     * Compute relative path which is from base directory.
     * 
     * @return
     */
    public final String relativePath() {
        if (relative == null) {
            return toString();
        } else {
            return relative;
        }
    }

    /**
     * Locate parent {@link Directory}.
     * 
     * @return
     */
    public final Directory parent() {
        return Locator.directory(path.getParent());
    }

    public abstract Signal<Location<? extends Location>> children();

    /**
     * Returns the size of a file (in bytes). The size may differ from the actual size on the file
     * system due to compression, support for sparse files, or other reasons. The size of files that are
     * not {@link #isRegularFile regular} files is implementation specific and therefore unspecified.
     *
     * @return the file size, in bytes
     * @throws IOException if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, its {@link SecurityManager#checkRead(String) checkRead} method denies read
     *             access to the file.
     * @see BasicFileAttributes#size
     */
    public final long size() {
        try {
            return Files.size(path);
        } catch (NoSuchFileException e) {
            return 0;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Returns a file's last modified time.
     * <p>
     * The {@code options} array may be used to indicate how symbolic links are handled for the case
     * that the file is a symbolic link. By default, symbolic links are followed and the file attribute
     * of the final target of the link is read. If the option {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS} is present then symbolic links are not followed.
     *
     * @param options options indicating how symbolic links are handled
     * @return a {@code FileTime} representing the time the file was last modified, or an implementation
     *         specific default when a time stamp to indicate the time of last modification is not
     *         supported by the file system
     * @throws IOException if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, its {@link SecurityManager#checkRead(String) checkRead} method denies read
     *             access to the file.
     * @see BasicFileAttributes#lastModifiedTime
     */
    public final long lastModified(LinkOption... options) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Retrieve the {@link BasicFileAttributes} of this {@link Location}.
     * 
     * @return A {@link BasicFileAttributes}.
     */
    public final BasicFileAttributes attribute(LinkOption... options) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, options);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Tells whether or not this path is absolute.
     * <p>
     * An absolute path is complete in that it doesn't need to be combined with other path information
     * in order to locate a file.
     *
     * @return {@code true} if, and only if, this path is absolute
     */
    public final boolean isAbsolute() {
        return path.isAbsolute();
    }

    /**
     * Tells whether or not this path is relative.
     * <p>
     * An absolute path is complete in that it doesn't need to be combined with other path information
     * in order to locate a file.
     *
     * @return {@code true} if, and only if, this path is relative
     */
    public final boolean isRelative() {
        return !path.isAbsolute();
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
     * Test whether this location can have items or not.
     * 
     * @return
     */
    public abstract boolean isContainer();

    public abstract Signal<Directory> asDirectory();

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
     * @param destination An output {@link Path} object which can be file or directory.
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
    public abstract void moveTo(Directory destination);

    /**
     * <p>
     * Copy a input {@link Path} to the output {@link Path} with its attributes. Simplified strategy is
     * the following:
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
     * If the output file already exists, it will be replaced by input file unconditionaly. The exact
     * file attributes that are copied is platform and file system dependent and therefore unspecified.
     * Minimally, the last-modified-time is copied to the output file if supported by both the input and
     * output file store. Copying of file timestamps may result in precision loss.
     * </p>
     * <p>
     * Copying a file is not an atomic operation. If an {@link IOException} is thrown then it possible
     * that the output file is incomplete or some of its file attributes have not been copied from the
     * input file.
     * </p>
     *
     * @param destination An output {@link Path} object which can be file or directory.
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
    public abstract void copyTo(Directory destination);

    public abstract void create();

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
     * On some operating systems it may not be possible to remove a file when it is open and in use by
     * this Java virtual machine or other programs.
     * </p>
     *
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input file is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to check
     *             read access to the source file, the {@link SecurityManager#checkWrite(String)} is
     *             invoked to check write access to the target file. If a symbolic link is copied the
     *             security manager is invoked to check {@link LinkPermission}("symbolic").
     */
    public abstract void delete();

    /**
     * <p>
     * Observe the file system change and raises events when a file, directory, or file in a directory,
     * changes.
     * </p>
     * <p>
     * You can watch for changes in files and subdirectories of the specified directory.
     * </p>
     * <p>
     * The operating system interpret a cut-and-paste action or a move action as a rename action for a
     * directory and its contents. If you cut and paste a folder with files into a directory being
     * watched, the {@link Observer} object reports only the directory as new, but not its contents
     * because they are essentially only renamed.
     * </p>
     * <p>
     * Common file system operations might raise more than one event. For example, when a file is moved
     * from one directory to another, several Modify and some Create and Delete events might be raised.
     * Moving a file is a complex operation that consists of multiple simple operations, therefore
     * raising multiple events. Likewise, some applications might cause additional file system events
     * that are detected by the {@link Observer}.
     * </p>
     *
     * @param path A target path you want to observe. (file and directory are acceptable)
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out. Ignore
     *            patterns if you want to observe a file.
     * @return A observable event stream.
     * @throws NullPointerException If the specified path or listener is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to check
     *             read access to the source file, the {@link SecurityManager#checkWrite(String)} is
     *             invoked to check write access to the target file. If a symbolic link is copied the
     *             security manager is invoked to check {@link LinkPermission}("symbolic").
     */
    public abstract Signal<WatchEvent<Path>> observe();

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
}

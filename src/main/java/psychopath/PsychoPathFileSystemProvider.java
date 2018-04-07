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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @version 2018/04/08 1:56:59
 */
public class PsychoPathFileSystemProvider extends FileSystemProvider {

    /** The actual {@link FileSystemProvider}. */
    private final FileSystemProvider base;

    /**
     * 
     */
    public PsychoPathFileSystemProvider() {
        this(FileSystems.getDefault().provider());
    }

    /**
     * @param base
     */
    PsychoPathFileSystemProvider(FileSystemProvider base) {
        this.base = base;
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
    public boolean equals(Object obj) {
        return base.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return base.getScheme();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        return base.newFileSystem(uri, env);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem(URI uri) {
        return base.getFileSystem(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return base.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getPath(URI uri) {
        return base.getPath(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
        return FileSystems.newFileSystem(unwrap(path), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        return base.newInputStream(unwrap(path), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        return base.newOutputStream(unwrap(path), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return base.newFileChannel(unwrap(path), options, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>... attrs)
            throws IOException {
        return base.newAsynchronousFileChannel(unwrap(path), options, executor, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return base.newByteChannel(unwrap(path), options, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        return base.newDirectoryStream(unwrap(dir), filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        base.createDirectory(unwrap(dir), attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        base.createSymbolicLink(unwrap(link), unwrap(target), attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createLink(Path link, Path existing) throws IOException {
        base.createLink(unwrap(link), unwrap(existing));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Path path) throws IOException {
        base.delete(unwrap(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteIfExists(Path path) throws IOException {
        return base.deleteIfExists(unwrap(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        return base.readSymbolicLink(unwrap(link));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        base.copy(unwrap(source), unwrap(target), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        base.move(unwrap(source), unwrap(target), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return base.isSameFile(unwrap(path), unwrap(path2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden(Path path) throws IOException {
        return base.isHidden(unwrap(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return base.getFileStore(unwrap(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        base.checkAccess(unwrap(path), modes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return base.getFileAttributeView(unwrap(path), type, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return base.readAttributes(unwrap(path), type, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return base.readAttributes(unwrap(path), attributes, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        base.setAttribute(unwrap(path), attribute, value, options);
    }

    /**
     * Unwrap {@link PsychoPath} to {@link Path}.
     * 
     * @param path
     * @return
     */
    static Path unwrap(Path path) {
        if (path instanceof PsychoPath) {
            return ((PsychoPath) path).base;
        } else {
            return path;
        }
    }
}

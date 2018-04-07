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
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

/**
 * @version 2018/04/08 1:54:40
 */
class PsychoPathFileSystem extends FileSystem {

    /** The actual {@link FileSystem}. */
    private final FileSystem base;

    /**
     * @param base
     */
    PsychoPathFileSystem(FileSystem base) {
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
    public FileSystemProvider provider() {
        return new PsychoPathFileSystemProvider(base.provider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        base.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        return base.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return base.isReadOnly();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSeparator() {
        return base.getSeparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Path> getRootDirectories() {
        return base.getRootDirectories();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        return base.getFileStores();
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
    public Set<String> supportedFileAttributeViews() {
        return base.supportedFileAttributeViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getPath(String first, String... more) {
        return base.getPath(first, more);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return base.getPathMatcher(syntaxAndPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return base.getUserPrincipalLookupService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchService newWatchService() throws IOException {
        return base.newWatchService();
    }

}

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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class LocatableOption {

    /** The glob patterns. */
    List<String> patterns = new ArrayList();

    /** The generic filter. */
    BiPredicate<Path, BasicFileAttributes> filter;

    /** The departure's root handling. */
    boolean acceptRoot = true;

    /** The destination's root handling. */
    Directory relativePath;

    /** The depth of directory digging. */
    int depth = Integer.MAX_VALUE;

    /**
     * Hide.
     */
    LocatableOption() {
    }

    /**
     * Sepcify the depth of directory traversing.
     * 
     * @param depthToSearch
     * @return
     */
    public LocatableOption depth(int depthToSearch) {
        this.depth = depthToSearch;
        return this;
    }

    /**
     * Specify glob pattern to specify location.
     * 
     * @param patterns
     * @return
     */
    public LocatableOption glob(String... patterns) {
        if (patterns != null) {
            this.patterns.addAll(Set.of(patterns));
        }
        return this;
    }

    /**
     * Specify generic filter to specify location.
     * 
     * @param filter
     * @return
     */
    public LocatableOption take(BiPredicate<Path, BasicFileAttributes> filter) {
        if (filter != null) {
            this.filter = filter;
        }
        return this;
    }

    /**
     * <p>
     * Strip the departure's root directory path.
     * </p>
     * <p>
     * Normally, files in the directory 'departure-root' will be allocated in
     * 'destination-root/departure-root/*'. But this option will allocate them in
     * 'destination-root/*'.
     * </p>
     * 
     * @return
     */
    public LocatableOption ignoreRoot() {
        this.acceptRoot = false;
        return this;
    }

    /**
     * <p>
     * All files will be allocated in the specified destination directory.
     * </p>
     * 
     * @param relativePath A relative path from the destination's root {@link Directory}.
     * @return
     */
    public LocatableOption allocateIn(String relativePath) {
        return allocateIn(Locator.directory(relativePath));
    }

    /**
     * <p>
     * All files will be allocated in the specified destination directory.
     * </p>
     * 
     * @param relativePath A relative path from the destination's root {@link Directory}.
     * @return
     */
    public LocatableOption allocateIn(Path relativePath) {
        return allocateIn(Locator.directory(relativePath));
    }

    /**
     * <p>
     * All files will be allocated in the specified destination directory.
     * </p>
     * 
     * @param relativePath A relative path from the destination's root {@link Directory}.
     * @return
     */
    public LocatableOption allocateIn(Directory relativePath) {
        if (relativePath != null) {
            if (relativePath.isAbsolute()) {
                throw new IllegalArgumentException("Only relative path is acceptable. [" + relativePath + "]");
            }
            this.relativePath = relativePath;
        }
        return this;
    }

}
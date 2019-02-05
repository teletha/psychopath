/*
 * Copyright (C) 2019 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class Option {

    /** The glob patterns. */
    List<String> patterns = new ArrayList();

    /** The generic filter. */
    BiPredicate<Path, BasicFileAttributes> filter;

    /** The departure's root handling. */
    boolean acceptRoot = true;

    /** The depth of directory digging. */
    int depth = Integer.MAX_VALUE;

    /** The relative path in the destination. */
    Directory allocator = Locator.directory("");

    /**
     * Hide.
     */
    Option() {
    }

    /**
     * Sepcify the depth of directory traversing.
     * 
     * @param depthToSearch
     * @return
     */
    public Option depth(int depthToSearch) {
        this.depth = depthToSearch;
        return this;
    }

    /**
     * Specify glob pattern to specify location.
     * 
     * @param patterns
     * @return
     */
    public Option glob(String... patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (pattern != null) {
                    this.patterns.add(pattern);
                }
            }
        }
        return this;
    }

    /**
     * Specify generic filter to specify location.
     * 
     * @param filter
     * @return
     */
    public Option take(BiPredicate<Path, BasicFileAttributes> filter) {
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
    public Option strip() {
        this.acceptRoot = false;
        return this;
    }

    /**
     * Specify the relative path in the destination.
     * 
     * @param relative
     * @return
     */
    public Option allocateIn(String relative) {
        return allocateIn(Locator.directory(relative));
    }

    /**
     * Specify the relative path in the destination.
     * 
     * @param relative
     * @return
     */
    public Option allocateIn(Directory relative) {
        if (relative != null && relative.isRelative()) {
            this.allocator = relative;
        }
        return this;
    }

    /**
     * Help to build option builder from glob patterns.
     * 
     * @param patterns A list of glob patterns.
     * @return A {@link Option} builder.
     */
    static Function<Option, Option> of(String... patterns) {
        return o -> o.glob(patterns);
    }
}

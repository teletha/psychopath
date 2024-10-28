/*
 * Copyright (C) 2024 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import kiss.I;

public class Option {

    /** The atomic writing. */
    public static final OpenOption ATOMIC_WRITE = new OpenOption() {
    };

    /** The glob patterns. */
    List<String> patterns = new ArrayList();

    /** The generic filter. */
    BiPredicate<Path, BasicFileAttributes> filter;

    /** The departure's root handling. */
    int strip;

    /** The depth of directory digging. */
    int depth = Integer.MAX_VALUE;

    /** The relative path in the destination. */
    Directory allocator = Locator.directory("");

    /**
     * <ul>
     * <li>0 - replace</li>
     * <li>1 - replace old</li>
     * <li>2 - skip</li>
     * <li>3 - stop with error</li>
     * </ul>
     */
    int existingMode;

    /** The synchronize mode. */
    boolean sync;

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
    public Option glob(List<String> patterns) {
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
     * Specify glob pattern to specify location.
     * 
     * @param patterns
     * @return
     */
    public Option glob(String... patterns) {
        return glob(I.list(patterns));
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
        strip++;
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
     * Specify synchronize mode.
     * 
     * @return
     */
    public Option sync() {
        this.sync = true;
        return this;
    }

    /**
     * Specify override mode.
     * 
     * @return
     */
    public Option replaceExisting() {
        existingMode = 0;
        return this;
    }

    /**
     * Specify override mode.
     * 
     * @return
     */
    public Option replaceOld() {
        existingMode = 1;
        return this;
    }

    /**
     * Specify override mode.
     * 
     * @return
     */
    public Option replaceDifferent() {
        existingMode = 2;
        return this;
    }

    /**
     * Specify override mode.
     * 
     * @return
     */
    public Option skipExisting() {
        existingMode = 3;
        return this;
    }

    /**
     * Specify override mode.
     * 
     * @return
     */
    public Option stopExisting() {
        existingMode = 4;
        return this;
    }

    boolean canReplace(Path from, Path to) throws FileAlreadyExistsException {
        if (Files.notExists(to)) {
            return true;
        }

        switch (existingMode) {
        case 0:
            return true;

        case 1:
            try {
                long fromMilli = Files.getLastModifiedTime(from).toMillis();
                long toMilli = Files.getLastModifiedTime(to).toMillis();
                return toMilli < fromMilli;
            } catch (IOException e) {
                return false;
            }

        case 2:
            try {
                long fromMilli = Files.getLastModifiedTime(from).toMillis();
                long toMilli = Files.getLastModifiedTime(to).toMillis();
                long fromSize = Files.size(from);
                long toSize = Files.size(to);
                return toMilli != fromMilli || fromSize != toSize;
            } catch (IOException e) {
                return false;
            }

        case 3:
            return false;

        default:
            throw new FileAlreadyExistsException("The path [" + to + "] alread exist.");
        }
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
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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public interface Option {

    /**
     * Build {@link PathManagement} option with {@link PathManagement#glob(String...)}.
     * 
     * @param patterns A list of glob patterns.
     * @return New created {@link PathManagement}.
     */
    static PathManagement glob(String... patterns) {
        return new PathManagement().glob(patterns);
    }

    /**
     * Build {@link PathManagement} option with {@link PathManagement#take(BiPredicate)}.
     * 
     * @param filter A location filter.
     * @return New created {@link PathManagement}.
     */
    static PathManagement take(BiPredicate<Path, BasicFileAttributes> filter) {
        return new PathManagement().take(filter);
    }

    /**
     * Build {@link PathManagement} option with {@link PathManagement#acceptRoot}.
     * 
     * @return New created {@link PathManagement}.
     */
    static PathManagement ignoreRoot() {
        return new PathManagement().ignoreRoot();
    }

    /**
     * Build {@link PathManagement} option with {@link PathManagement#allocateIn(String)}.
     * 
     * @param relativePath A relative path from the destination's root {@link Directory}.
     * @return New created {@link PathManagement}.
     */
    static PathManagement allocateIn(String relativePath) {
        return new PathManagement().allocateIn(relativePath);
    }

    /**
     * 
     */
    class PathManagement {

        /** The glob patterns. */
        private List<String> patterns = new ArrayList();

        /** The include file patterns. */
        BiPredicate<Path, BasicFileAttributes> include;

        /** The exclude file patterns. */
        BiPredicate<Path, BasicFileAttributes> exclude;

        /** The exclude directory pattern. */
        BiPredicate<Path, BasicFileAttributes> directory;

        /** The generic filter. */
        BiPredicate<Path, BasicFileAttributes> filter;

        /** The departure's root handling. */
        boolean acceptRoot = true;

        /** The destination's root handling. */
        Directory relativePath;

        /**
         * Hide.
         */
        private PathManagement() {
        }

        /**
         * Specify glob pattern to specify location.
         * 
         * @param patterns
         * @return
         */
        public PathManagement glob(String... patterns) {
            if (patterns != null) {
                // Parse and create path matchers.
                for (String pattern : patterns) {
                    if (pattern.charAt(0) != '!') {
                        // include
                        include = glob(include, pattern);
                    } else if (pattern.endsWith("/**")) {
                        // exclude directory
                        directory = glob(directory, pattern.substring(1, pattern.length() - 3));
                    } else {
                        // exclude files
                        exclude = glob(exclude, pattern.substring(1));
                        // directory = glob(directory, pattern.substring(1));
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
        public PathManagement take(BiPredicate<Path, BasicFileAttributes> filter) {
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
        public PathManagement ignoreRoot() {
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
        public PathManagement allocateIn(String relativePath) {
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
        public PathManagement allocateIn(Path relativePath) {
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
        public PathManagement allocateIn(Directory relativePath) {
            if (relativePath != null) {
                if (relativePath.isAbsolute()) {
                    throw new IllegalArgumentException("Only relative path is acceptable. [" + relativePath + "]");
                }
                this.relativePath = relativePath;
            }
            return this;
        }

        /**
         * <p>
         * Create {@link BiPredicate} filter by using the specified glob pattern.
         * </p>
         * 
         * @param base
         * @param pattern
         * @return
         */
        private static BiPredicate<Path, BasicFileAttributes> glob(BiPredicate<Path, BasicFileAttributes> base, String pattern) {
            // Default file system doesn't support close method, so we can ignore to release
            // resource.
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:".concat(pattern));
            BiPredicate<Path, BasicFileAttributes> filter = (path, attrs) -> matcher.matches(path);

            return base == null ? filter : base.or(filter);
        }

    }
}

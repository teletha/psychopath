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
import java.util.ArrayList;
import java.util.List;

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
     * Build {@link PathManagement} option with {@link PathManagement#ignoreRoot}.
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
        List<String> patterns = new ArrayList();

        /** The departure's root handling. */
        boolean ignoreRoot;

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
                this.patterns.addAll(List.of(patterns));
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
            this.ignoreRoot = true;
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
    }
}

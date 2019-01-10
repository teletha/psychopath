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

import java.util.ArrayList;
import java.util.List;

public interface Option {

    /**
     * Build {@link PathManagement} option with {@link PathManagement#glob(String...)}.
     * 
     * @param patterns A list of glob patterns.
     * @return New created {@link PathManagement}.
     */
    public static PathManagement glob(String... patterns) {
        return new PathManagement().glob(patterns);
    }

    /**
     * Build {@link PathManagement} option with {@link PathManagement#ignoreRoot}.
     * 
     * @return New created {@link PathManagement}.
     */
    public static PathManagement ignoreRoot() {
        return new PathManagement().ignoreRoot();
    }

    /**
     * Build {@link PathManagement} option with {@link PathManagement#allocateIn(String)}.
     * 
     * @param relativePath A relative path from the destination's root {@link Directory}.
     * @return New created {@link PathManagement}.
     */
    public static PathManagement allocateIn(String relativePath) {
        return new PathManagement().allocateIn(relativePath);
    }

    /**
     * 
     */
    public class PathManagement {

        /** The glob patterns. */
        List<String> patterns = new ArrayList();

        /** The departure's root handling. */
        boolean ignoreRoot;

        /** The destination's root handling. */
        String relativePath;

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
         * Hide.
         */
        private PathManagement() {
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
            if (relativePath != null) {
                this.relativePath = relativePath;
            }
            return this;
        }
    }
}

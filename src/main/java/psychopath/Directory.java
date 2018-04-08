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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.function.BiPredicate;

import kiss.Signal;

/**
 * @version 2018/04/08 12:22:35
 */
public class Directory extends Location {

    /**
     * @param path
     */
    Directory(Path path) {
        super(path);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<Directory> directories(String... patterns) {
        return new Signal<Directory>((observer, disposer) -> {
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, Integer.MAX_VALUE, new CymaticScan(path, null, 4, observer, patterns));
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public Signal<Path> directories(BiPredicate<Path, BasicFileAttributes> filter) {
        return new Signal<>((observer, disposer) -> {
            try {
                Files.walkFileTree(path, Collections.EMPTY_SET, Integer.MAX_VALUE, new CymaticScan(path, null, 4, observer, filter));
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }
}

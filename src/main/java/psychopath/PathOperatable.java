/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath;

import java.util.function.Function;

import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

public interface PathOperatable {

    /**
     * Delete resources.
     * 
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A destination {@link Directory}.
     */
    default void delete(String... patterns) {
        delete(Option.of(patterns));
    }

    /**
     * Delete resources.
     * 
     * @param option A operation {@link Option}.
     * @return A destination {@link Directory}.
     */
    default void delete(Function<Option, Option> option) {
        observeDeleting(option).to(I.NoOP);
    }

    /**
     * Build stream that delete resources.
     * 
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A event stream which emits operated {@link File}s.
     */
    default Signal<Location> observeDeleting(String... patterns) {
        return observeDeleting(Option.of(patterns));
    }

    /**
     * Build stream that delete resources.
     * 
     * @param option A operation {@link Option}.
     * @return A event stream which emits operated {@link File}s.
     */
    Signal<Location> observeDeleting(Function<Option, Option> option);

    /**
     * Copy resources to the destination {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A destination {@link Directory}.
     */
    default Directory copyTo(Directory destination, String... patterns) {
        return copyTo(destination, Option.of(patterns));
    }

    /**
     * Copy resources to the destination {@link Directory} with some {@link Option}.
     * 
     * @param destination A destination {@link Directory}.
     * @param option A operation {@link Option}.
     * @return A destination {@link Directory}.
     */
    default Directory copyTo(Directory destination, Function<Option, Option> option) {
        observeCopyingTo(destination, option).to(I.NoOP);

        return destination;
    }

    /**
     * Build stream that copy resources to the destination {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A event stream which emits operated {@link File}s.
     */
    default Signal<Location> observeCopyingTo(Directory destination, String... patterns) {
        return observeCopyingTo(destination, Option.of(patterns));
    }

    /**
     * Build stream that copy resources to the destination {@link Directory} with some
     * {@link Option}.
     * 
     * @param destination A destination {@link Directory}.
     * @param option A operation {@link Option}.
     * @return A event stream which emits operated {@link File}s.
     */
    Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option);

    /**
     * Move resources to the destination {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A destination {@link Directory}.
     */
    default Directory moveTo(Directory destination, String... patterns) {
        return moveTo(destination, Option.of(patterns));
    }

    /**
     * Move resources to the destination {@link Directory} with some {@link Option}.
     * 
     * @param destination A destination {@link Directory}.
     * @param option A operation {@link Option}.
     * @return A destination {@link Directory}.
     */
    default Directory moveTo(Directory destination, Function<Option, Option> option) {
        observeMovingTo(destination, option).to(I.NoOP);

        return destination;
    }

    /**
     * Build stream that move resources to the destination {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A event stream which emits operated {@link File}s.
     */
    default Signal<Location> observeMovingTo(Directory destination, String... patterns) {
        return observeMovingTo(destination, Option.of(patterns));
    }

    /**
     * Build stream that move resources to the destination {@link Directory} with some
     * {@link Option}.
     * 
     * @param destination A destination {@link Directory}.
     * @param option A operation {@link Option}.
     * @return A event stream which emits operated {@link File}s.
     */
    Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option);

    /**
     * Pack resources to the destination {@link File}.
     * 
     * @param destination A destination {@link File}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A destination {@link File}.
     */
    default File packTo(File destination, String... patterns) {
        return packTo(destination, Option.of(patterns));
    }

    /**
     * Pack resources to the destination {@link File}.
     * 
     * @param destination A destination {@link File}.
     * @param option A operation {@link Option}.
     * @return A destination {@link File}.
     */
    default File packTo(File destination, Function<Option, Option> option) {
        observePackingTo(destination, option).to(I.NoOP);

        return destination;
    }

    /**
     * Build stream that pack resources to the destination {@link File}.
     * 
     * @param destination A destination {@link File}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A event stream which emits operated {@link File}s.
     */
    default Signal<Location> observePackingTo(File destination, String... patterns) {
        return observePackingTo(destination, Option.of(patterns));
    }

    /**
     * Build stream that pack resources to the destination {@link File}.
     * 
     * @param destination A destination {@link File}.
     * @param option A operation {@link Option}.
     * @return A event stream which emits operated {@link File}s.
     */
    Signal<Location> observePackingTo(File destination, Function<Option, Option> option);

    default Signal<File> walkFile(String... patterns) {
        return walkFile(Option.of(patterns));
    }

    default Signal<File> walkFile(Function<Option, Option> option) {
        return walkFileWithBase(option).map(Ⅱ<Directory, File>::ⅱ);
    }

    default Signal<Ⅱ<Directory, File>> walkFileWithBase(String... patterns) {
        return walkFileWithBase(Option.of(patterns));
    }

    Signal<Ⅱ<Directory, File>> walkFileWithBase(Function<Option, Option> option);

    default Signal<Directory> walkDirectory(String... patterns) {
        return walkDirectory(Option.of(patterns));
    }

    default Signal<Directory> walkDirectory(Function<Option, Option> option) {
        return walkDirectoryWithBase(option).map(Ⅱ<Directory, Directory>::ⅱ);
    }

    default Signal<Ⅱ<Directory, Directory>> walkDirectoryWithBase(String... patterns) {
        return walkDirectoryWithBase(Option.of(patterns));
    }

    Signal<Ⅱ<Directory, Directory>> walkDirectoryWithBase(Function<Option, Option> option);
}

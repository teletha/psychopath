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

public interface PathOperatable {

    /**
     * Delete resources.
     * 
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A destination {@link Directory}.
     */
    default void delete(String... patterns) {
        delete(o -> o.glob(patterns));
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
        return observeDeleting(o -> o.glob(patterns));
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
        return copyTo(destination, o -> o.glob(patterns));
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
        return observeCopyingTo(destination, o -> o.glob(patterns));
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
        return moveTo(destination, o -> o.glob(patterns));
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
        return observeMovingTo(destination, o -> o.glob(patterns));
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
        observePackingTo(destination, patterns).to(I.NoOP);

        return destination;
    }

    /**
     * Build stream that pack resources to the destination {@link File}.
     * 
     * @param destination A destination {@link File}.
     * @param patterns A list of glob patterns to accept file by its name.
     * @return A event stream which emits operated {@link File}s.
     */
    Signal<Location> observePackingTo(File destination, String... patterns);
}

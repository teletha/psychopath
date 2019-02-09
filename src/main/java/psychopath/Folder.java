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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

/**
 * Virtual directory to manage resources from various entries.
 */
public final class Folder implements PathOperatable {

    /** The operations. */
    private final List<Operation> operations = new ArrayList();

    /**
     * 
     */
    Folder() {
    }

    /**
     * Add entry by path expression.
     * 
     * @param entry A path to entry.
     * @return Chainable API.
     */
    public Folder add(String entry) {
        return add(entry, Function.identity());
    }

    /**
     * Add entry by path expression.
     * 
     * @param entry A path to entry.
     * @return Chainable API.
     */
    public Folder add(String entry, Function<Option, Option> option) {
        if (entry == null) {
            return this;
        }
        return add(Locator.locate(entry), option);
    }

    /**
     * Add entry by {@link Path}.
     * 
     * @param entry A path to entry.
     * @return Chainable API.
     */
    public Folder add(Path entry) {
        return add(entry, Function.identity());
    }

    /**
     * Add entry by {@link Path}.
     * 
     * @param entry A path to entry.
     * @return Chainable API.
     */
    public Folder add(Path entry, Function<Option, Option> option) {
        if (entry == null) {
            return this;
        }
        return add(Locator.locate(entry), option);
    }

    /**
     * Merge entries from other {@link Folder}.
     * 
     * @param entries A entries to merge.
     * @return Chainable API.
     */
    public Folder add(Folder entries) {
        if (entries != null) {
            operations.addAll(entries.operations);
        }
        return this;
    }

    /**
     * Merge entries from other {@link Folder}.
     * 
     * @param entries A entries to merge.
     * @return Chainable API.
     */
    public Folder add(Folder entries, Function<Option, Option> option) {
        if (entries != null) {
            for (Operation operation : entries.operations) {
                this.operations.add(new Operation(operation.location, operation.option.andThen(option)));
            }
        }
        return this;
    }

    /**
     * Add entries by {@link Location}.
     * 
     * @param entry A location to entry.
     * @return Chainable API.
     */
    public Folder add(Location entry) {
        return add(I.signal(entry), Function.identity());
    }

    /**
     * Add entries by {@link Location}.
     * 
     * @param entry A location to entry.
     * @return Chainable API.
     */
    public Folder add(Location entry, Function<Option, Option> option) {
        return add(I.signal(entry), option);
    }

    /**
     * Add entries.
     * 
     * @param entries
     */
    public Folder add(Signal<? extends Location> entries) {
        return add(entries, Function.identity());
    }

    /**
     * Add entries.
     * 
     * @param entries
     */
    public Folder add(Signal<? extends Location> entries, Function<Option, Option> option) {
        if (entries != null) {
            operations.addAll(entries.map(e -> new Operation(e, option)).toList());
        }
        return this;
    }

    /**
     * Add pattern matching path.
     * 
     * @param base A base path.
     * @param patterns "glob" include/exclude patterns.
     */
    public Folder add(Directory base, String... patterns) {
        return add(base, Option.of(patterns));
    }

    /**
     * Add pattern matching path.
     * 
     * @param base A base path.
     * @param patterns "glob" include/exclude patterns.
     */
    public Folder add(Directory base, Function<Option, Option> option) {
        if (base != null) {
            operations.add(new Operation(base, option));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> observeDeleting(Function<Option, Option> option) {
        return I.signal(operations).flatMap(operation -> operation.observeDeleting(option));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
        Objects.requireNonNull(destination);

        return I.signal(operations).flatMap(operation -> operation.observeCopyingTo(destination, option));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
        Objects.requireNonNull(destination);

        return I.signal(operations).flatMap(operation -> operation.observeMovingTo(destination, option));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location> observePackingTo(File archive, Function<Option, Option> option) {
        return new Signal<>((observer, disposer) -> {
            try (ZipOutputStream out = new ZipOutputStream(archive.newOutputStream(), Charset.defaultCharset())) {
                I.signal(operations).flatMap(operation -> operation.observePackingTo(out, Locator.directory(""), option)).to(observer);
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅱ<Directory, File>> walkFileWithBase(Function<Option, Option> option) {
        return I.signal(operations).flatMap(op -> op.walkFilesWithBase(option));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅱ<Directory, Directory>> walkDirectoryWithBase(Function<Option, Option> option) {
        return I.signal(operations).flatMap(op -> op.walkDirectoriesWithBase(option));
    }

    /**
     * Build {@link ZipEntry} for each resources.
     * 
     * @param out
     * @param directory
     * @param file
     * @param relative
     */
    private static Signal<Location> pack(ZipOutputStream out, Directory directory, File file, Directory relative) {
        return new Signal<>((observer, disposer) -> {
            try {
                BasicFileAttributes attr = file.attr();
                ZipEntry entry = new ZipEntry(relative.file(directory.relativize(file).toString()).path());
                entry.setSize(attr.size());
                entry.setMethod(ZipEntry.DEFLATED);
                entry.setLastModifiedTime(attr.lastModifiedTime());

                observer.accept(file);
                out.putNextEntry(entry);
                try (InputStream in = file.newInputStream()) {
                    in.transferTo(out);
                }
                out.closeEntry();
                observer.complete();
            } catch (Throwable e) {
                // ignore
            }
            return disposer;
        });
    }

    /**
     * Operation for {@link Directory}.
     */
    private static class Operation {

        private final Location location;

        private final Function<Option, Option> option;

        /**
         * @param directory
         * @param option
         */
        private Operation(Location directory, Function<Option, Option> option) {
            this.location = directory;
            this.option = option;
        }

        /**
         * Delete resources.
         * 
         * @param patterns
         */
        public Signal<Location> observeDeleting(Function<Option, Option> option) {
            return location.observeDeleting(this.option.andThen(option));
        }

        /**
         * Move reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
            return location.observeMovingTo(destination, this.option.andThen(option));
        }

        /**
         * Copy reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
            return location.observeCopyingTo(destination, this.option.andThen(option));
        }

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param relative
         * @param patterns
         */
        public Signal<Location> observePackingTo(ZipOutputStream archive, Directory relative, Function<Option, Option> option) {
            Function<Option, Option> combined = this.option.andThen(option);
            Option o = combined.apply(new Option());

            if (location.isFile()) {
                return pack(archive, location.parent(), location.asFile(), o.allocator);
            } else {
                return location.walkFile(combined)
                        .flatMap(file -> pack(archive, !location.isRoot() && o.acceptRoot ? location.parent()
                                : location.asDirectory(), file, o.allocator));
            }
        }

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        public Signal<Ⅱ<Directory, File>> walkFilesWithBase(Function<Option, Option> option) {
            return location.walkFileWithBase(this.option.andThen(option));
        }

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        public Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(Function<Option, Option> option) {
            return location.walkDirectoryWithBase(this.option.andThen(option));
        }
    }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;
import psychopath.archiver.Archiver;

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
                this.operations.add(new LayerOperation(operation, option));
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
            operations.addAll(entries.map(e -> new LocationOperation(e, option)).toList());
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
        return add(base, o -> o.glob(patterns));
    }

    /**
     * Add pattern matching path.
     * 
     * @param base A base path.
     * @param patterns "glob" include/exclude patterns.
     */
    public Folder add(Directory base, Function<Option, Option> option) {
        if (base != null) {
            operations.add(new LocationOperation(base, option));
        }
        return this;
    }

    /**
     * <p>
     * Use destination relative path for entries.
     * </p>
     * <pre>
     * folder.add("main.jar").add("lib", entry -> entry.add("one.jar").add("other.jar"));
     * folder.copyTo("output");
     * </pre>
     * <p>
     * {@link Folder} will deploy jars into "lib" directory.
     * </p>
     * <pre>
     * output
     * - main.jar
     * - lib
     * - one.jar
     * - other.jar
     * </pre>
     *
     * @param relative A destination relative path.
     * @param entries Your entries.
     * @return
     */
    public Folder addIn(String relative, Consumer<Folder> entries) {
        return addIn(Locator.directory(relative), entries);
    }

    /**
     * <p>
     * Use destination relative path for entries.
     * </p>
     * <pre>
     * folder.add("main.jar").add("lib", entry -> entry.add("one.jar").add("other.jar"));
     * folder.copyTo("output");
     * </pre>
     * <p>
     * {@link Folder} will deploy jars into "lib" directory.
     * </p>
     * <pre>
     * output
     * - main.jar
     * - lib
     * - one.jar
     * - other.jar
     * </pre>
     *
     * @param relative A destination relative path.
     * @param entries Your entries.
     * @return
     */
    public Folder addIn(Path relative, Consumer<Folder> entries) {
        return addIn(Locator.directory(relative), entries);
    }

    /**
     * <p>
     * Use destination relative path for entries.
     * </p>
     * <pre>
     * folder.add("main.jar").add("lib", entry -> entry.add("one.jar").add("other.jar"));
     * folder.copyTo("output");
     * </pre>
     * <p>
     * {@link Folder} will deploy jars into "lib" directory.
     * </p>
     * <pre>
     * output
     * - main.jar
     * - lib
     * - one.jar
     * - other.jar
     * </pre>
     *
     * @param relative A destination relative path.
     * @param entries Your entries.
     * @return
     */
    public Folder addIn(Directory relative, Consumer<Folder> entries) {
        if (entries != null) {
            Folder folder = Locator.folder();
            entries.accept(folder);
            operations.addAll(I.signal(folder.operations).map(o -> new Allocator(o, relative)).toList());
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
     * Pack all resources.
     * 
     * @param archive
     */
    @Override
    public Signal<Location> observePackingTo(File archive, String... patterns) {
        return new Signal<>((observer, disposer) -> {
            Archiver archiver = Archiver.byExtension(archive.extension());

            try (ArchiveOutputStream out = new ArchiveStreamFactory()
                    .createArchiveOutputStream(archive.extension().replaceAll("7z", "7z-override"), archive.newOutputStream())) {
                I.signal(operations)
                        .flatMap(operation -> operation.observePackingTo(out, archiver, Locator.directory(""), o -> o.glob(patterns)))
                        .to(observer);
                out.finish();
                observer.complete();
            } catch (Exception e) {
                observer.error(e);
            }

            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅱ<Directory, Location>> walkWithBase(Function<Option, Option> option) {
        return I.signal(operations).flatMap(op -> op.walkWithBase(option));
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
     * Build {@link ArchiveEntry} for each resources.
     * 
     * @param out
     * @param directory
     * @param file
     * @param relative
     */
    private static Signal<Location> pack(ArchiveOutputStream out, Archiver archiver, Directory directory, File file, Directory relative) {
        return new Signal<>((observer, disposer) -> {
            try {
                ArchiveEntry entry = archiver.create(relative.file(directory.relativize(file).toString()).path(), file);
                out.putArchiveEntry(entry);

                try (InputStream in = file.newInputStream()) {
                    in.transferTo(out);
                    observer.accept(file);
                }
                out.closeArchiveEntry();
                observer.complete();
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * Definition of {@link Folder} operation.
     */
    private interface Operation {

        /**
         * Delete resources.
         * 
         * @param patterns
         */
        Signal<Location> observeDeleting(Function<Option, Option> option);

        /**
         * Move reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option);

        /**
         * Copy reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option);

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param relative
         * @param patterns
         */
        Signal<Location> observePackingTo(ArchiveOutputStream archive, Archiver archiver, Directory relative, Function<Option, Option> option);

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        Signal<Ⅱ<Directory, Location>> walkWithBase(Function<Option, Option> option);

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        Signal<Ⅱ<Directory, File>> walkFilesWithBase(Function<Option, Option> option);

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(Function<Option, Option> option);
    }

    /**
     * Allocator for destination path.
     */
    private static class Allocator implements Operation {

        /** The delegation. */
        private final Operation delegator;

        /** The destination relative path. */
        private final Directory relative;

        /**
         * @param delegator
         * @param relative
         */
        private Allocator(Operation delegator, Directory relative) {
            this.delegator = delegator;
            this.relative = relative;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeDeleting(Function<Option, Option> option) {
            return delegator.observeDeleting(option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
            return delegator.observeMovingTo(destination.directory(relative), option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
            return delegator.observeCopyingTo(destination.directory(relative), option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observePackingTo(ArchiveOutputStream archive, Archiver builder, Directory relative, Function<Option, Option> option) {
            return delegator.observePackingTo(archive, builder, this.relative, option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Location>> walkWithBase(Function<Option, Option> option) {
            return delegator.walkWithBase(option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFilesWithBase(Function<Option, Option> option) {
            return delegator.walkFilesWithBase(option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(Function<Option, Option> option) {
            return delegator.walkDirectoriesWithBase(option);
        }
    }

    /**
     * Operation for {@link Directory}.
     */
    private static class LocationOperation implements Operation {

        private final Location location;

        private final Function<Option, Option> option;

        /**
         * @param directory
         * @param option
         */
        private LocationOperation(Location directory, Function<Option, Option> option) {
            this.location = directory;
            this.option = option;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeDeleting(Function<Option, Option> option) {
            return location.observeDeleting(this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
            return location.observeMovingTo(destination, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
            return location.observeCopyingTo(destination, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observePackingTo(ArchiveOutputStream archive, Archiver builder, Directory relative, Function<Option, Option> option) {
            if (location.isFile()) {
                return pack(archive, builder, location.parent(), location.asFile(), relative);
            } else {
                Function<Option, Option> combined = this.option.andThen(option);
                Option o = combined.apply(new Option());

                return location.walkFile(combined)
                        .flatMap(file -> pack(archive, builder, !location.isRoot() && o.acceptRoot ? location.parent()
                                : location.asDirectory(), file, relative));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Location>> walkWithBase(Function<Option, Option> option) {
            return location.walkWithBase(this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFilesWithBase(Function<Option, Option> option) {
            return location.walkFileWithBase(this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(Function<Option, Option> option) {
            return location.walkDirectoryWithBase(this.option.andThen(option));
        }
    }

    /**
     * 
     */
    private static class LayerOperation implements Operation {

        private final Operation operation;

        private final Function<Option, Option> option;

        /**
         * @param operation
         * @param option
         */
        private LayerOperation(Operation operation, Function<Option, Option> option) {
            this.operation = operation;
            this.option = option;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeDeleting(Function<Option, Option> option) {
            return operation.observeDeleting(this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
            return operation.observeMovingTo(destination, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
            return operation.observeCopyingTo(destination, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> observePackingTo(ArchiveOutputStream archive, Archiver builder, Directory relative, Function<Option, Option> option) {
            return operation.observePackingTo(archive, builder, relative, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Location>> walkWithBase(Function<Option, Option> option) {
            return operation.walkWithBase(this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFilesWithBase(Function<Option, Option> option) {
            return operation.walkFilesWithBase(this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(Function<Option, Option> option) {
            return operation.walkDirectoriesWithBase(this.option.andThen(option));
        }
    }
}

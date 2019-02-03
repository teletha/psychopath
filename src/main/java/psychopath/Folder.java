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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

/**
 * Virtual directory to manage resource from various entries.
 */
public final class Folder {

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
            Signal<Operation> ops = entries.map(location -> {
                if (location.isDirectory()) {
                    return new DirectoryOperation((Directory) location, option);
                } else {
                    return new FileOperation((File) location, option);
                }
            });

            operations.add(new Operation() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete(String... patterns) {
                    ops.to(op -> op.delete(patterns));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void moveTo(Directory destination, String... patterns) {
                    ops.to(op -> op.moveTo(destination, patterns));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void copyTo(Directory destination, Function<Option, Option> option) {
                    ops.to(op -> op.copyTo(destination, option));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, Function<Option, Option> option) {
                    ops.to(op -> op.packTo(archive, builder, relative, option));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
                    return ops.flatMap(op -> op.walkFiles(patterns));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
                    return ops.flatMap(op -> op.walkDirectories(patterns));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Location> entry() {
                    return entries.as(Location.class);
                }
            });
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
            operations.add(new DirectoryOperation(base, option));
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
     * Delete all resources.
     * 
     * @return
     */
    public Folder delete(String... patterns) {
        operations.forEach(operation -> operation.delete(patterns));

        return this;
    }

    /**
     * Copy all resources to the specified {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     */
    public Directory copyTo(Directory destination, String... patterns) {
        Objects.requireNonNull(destination);

        operations.forEach(operation -> operation.copyTo(destination, patterns));

        return destination;
    }

    /**
     * Move all resources to the specified {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     */
    public Directory moveTo(Directory destination, String... patterns) {
        Objects.requireNonNull(destination);

        operations.forEach(operation -> operation.moveTo(destination, patterns));

        return destination;
    }

    /**
     * Pack all resources.
     * 
     * @param archive
     */
    public File packTo(File archive, String... patterns) {
        BiFunction<String, File, ArchiveEntry> builder = detectEntryBuilder(archive.extension());

        try (ArchiveOutputStream out = new ArchiveStreamFactory()
                .createArchiveOutputStream(archive.extension().replaceAll("7z", "7z-override"), archive.newOutputStream())) {
            operations.forEach(operation -> operation.packTo(out, builder, Locator.directory(""), patterns));
            out.finish();
        } catch (Exception e) {
            throw I.quiet(e);
        }
        return archive;
    }

    /**
     * List up all {@link File}s.
     * 
     * @return
     */
    public Signal<File> walkFiles(String... patterns) {
        return walkFilesWithBase(patterns).map(Ⅱ<Directory, File>::ⅱ);
    }

    /**
     * List up all {@link File}s.
     * 
     * @return
     */
    public Signal<Ⅱ<Directory, File>> walkFilesWithBase(String... patterns) {
        return I.signal(operations).flatMap(op -> op.walkFiles(patterns));
    }

    /**
     * List up all {@link Directory}.
     * 
     * @return
     */
    public Signal<Directory> walkDirectories(String... patterns) {
        return walkDirectoriesWithBase(patterns).map(Ⅱ<Directory, Directory>::ⅱ);
    }

    /**
     * List up all {@link Directory}.
     * 
     * @return
     */
    public Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(String... patterns) {
        return I.signal(operations).flatMap(op -> op.walkDirectories(patterns));
    }

    /**
     * List up all entries.
     * 
     * @return
     */
    public Signal<Location> entries() {
        return I.signal(operations).flatMap(Operation::entry);
    }

    /**
     * Build {@link ArchiveEntry} for each resources.
     * 
     * @param out
     * @param directory
     * @param file
     * @param relative
     */
    private static void pack(ArchiveOutputStream out, BiFunction<String, File, ArchiveEntry> builder, Directory directory, File file, Directory relative, Function<Option, Option> options) {
        try {
            Option option = options.apply(new Option());

            System.out
                    .println(option.acceptRoot + "  @  " + relative + " @ " + directory + "  @" + relative + "@   " + file + "   @  " + relative
                            .file(directory.relativize(file).toString()));

            ArchiveEntry entry = builder.apply(relative.file(directory.relativize(file).toString()).path(), file);
            out.putArchiveEntry(entry);

            try (InputStream in = file.newInputStream()) {
                in.transferTo(out);
            }
            out.closeArchiveEntry();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    private static BiFunction<String, File, ArchiveEntry> detectEntryBuilder(String extension) {
        switch (extension) {
        case "jar":
            return (name, file) -> {
                JarArchiveEntry entry = new JarArchiveEntry(name);

                return entry;
            };

        case "zip":
            return (name, file) -> {
                ZipArchiveEntry entry = new ZipArchiveEntry(name);

                return entry;
            };

        case "7z":
            return (name, file) -> {
                SevenZArchiveEntry entry = new SevenZArchiveEntry();
                entry.setName(name);

                return entry;
            };

        default:
            throw new Error();
        }
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
        void delete(String... patterns);

        /**
         * Move reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        void moveTo(Directory destination, String... patterns);

        /**
         * Copy reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        default void copyTo(Directory destination, String... patterns) {
            copyTo(destination, o -> o.glob(patterns));
        }

        /**
         * Copy reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param patterns
         */
        void copyTo(Directory destination, Function<Option, Option> option);

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param relative
         * @param patterns
         */
        default void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, String... patterns) {
            packTo(archive, builder, relative, o -> o.glob(patterns));
        }

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param relative
         * @param patterns
         */
        void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, Function<Option, Option> option);

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        Signal<Ⅱ<Directory, File>> walkFiles(String... patterns);

        /**
         * List up all resources.
         * 
         * @param patterns
         * @return
         */
        Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns);

        Signal<Location> entry();
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
        public void delete(String... patterns) {
            delegator.delete(patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void moveTo(Directory destination, String... patterns) {
            delegator.moveTo(destination.directory(relative), patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copyTo(Directory destination, Function<Option, Option> option) {
            delegator.copyTo(destination.directory(relative), option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, Function<Option, Option> option) {
            delegator.packTo(archive, builder, this.relative, option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
            return delegator.walkFiles(patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
            return delegator.walkDirectories(patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> entry() {
            return delegator.entry();
        }
    }

    /**
     * Operation for {@link File}.
     */
    private static class FileOperation implements Operation {

        private final File file;

        private final Function<Option, Option> option;

        /**
         * @param file
         */
        private FileOperation(File file, Function<Option, Option> option) {
            this.file = file;
            this.option = option;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete(String... patterns) {
            file.delete();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void moveTo(Directory destination, String... patterns) {
            file.moveTo(destination);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copyTo(Directory destination, Function<Option, Option> option) {
            file.copyTo(destination);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, Function<Option, Option> option) {
            pack(archive, builder, file.parent(), file, relative, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
            if (patterns.length == 0 || file.match(patterns)) {
                return I.signal(I.pair(file.parent(), file));
            } else {
                return Signal.empty();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
            return Signal.empty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> entry() {
            return I.signal(file);
        }
    }

    /**
     * Operation for {@link Directory}.
     */
    private static class DirectoryOperation implements Operation {

        private final Directory directory;

        private final Function<Option, Option> option;

        /**
         * @param directory
         * @param option
         */
        private DirectoryOperation(Directory directory, Function<Option, Option> option) {
            this.directory = directory;
            this.option = option;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete(String... patterns) {
            directory.delete(option.andThen(o -> o.glob(patterns)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void moveTo(Directory destination, String... patterns) {
            directory.moveTo(destination, option.andThen(o -> o.glob(patterns)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copyTo(Directory destination, Function<Option, Option> option) {
            directory.copyTo(destination, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, Function<Option, Option> option) {
            directory.walkFiles(this.option.andThen(option))
                    .to(file -> pack(archive, builder, directory.isRoot() ? directory : directory.parent(), file, relative, this.option
                            .andThen(option)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
            return directory.walkFiles(option.andThen(o -> o.glob(patterns))).map(file -> I.pair(directory, file));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
            return directory.walkDirectories(option.andThen(o -> o.glob(patterns))).map(dir -> I.pair(directory, dir));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> entry() {
            return I.signal(directory);
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
        public void delete(String... patterns) {
            operation.delete(patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void moveTo(Directory destination, String... patterns) {
            operation.moveTo(destination, patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copyTo(Directory destination, Function<Option, Option> option) {
            operation.copyTo(destination, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, BiFunction<String, File, ArchiveEntry> builder, Directory relative, Function<Option, Option> option) {
            operation.packTo(archive, builder, relative, this.option.andThen(option));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
            return operation.walkFiles(patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
            return operation.walkDirectories(patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Location> entry() {
            return operation.entry();
        }
    }
}

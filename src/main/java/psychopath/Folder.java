/*
 * Copyright (C) 2018 psychopath Development Team
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
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

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
        if (entry == null) {
            return this;
        }
        return add(Locator.locate(entry));
    }

    /**
     * Add entry by {@link Path}.
     * 
     * @param entry A path to entry.
     * @return Chainable API.
     */
    public Folder add(Path entry) {
        if (entry == null) {
            return this;
        }
        return add(Locator.locate(entry));
    }

    /**
     * Merge entries from other {@link Folder}.
     * 
     * @param folder A entries to merge.
     * @return Chainable API.
     */
    public Folder add(Folder folder) {
        if (folder != null) {
            operations.addAll(folder.operations);
        }
        return this;
    }

    /**
     * Add entries by {@link Location}.
     * 
     * @param entry A location to entry.
     * @return Chainable API.
     */
    public Folder add(Location... entries) {
        return add(I.signal(entries));
    }

    /**
     * Add resources.
     * 
     * @param resources
     */
    public Folder add(Signal<? extends Location> resources) {
        if (resources != null) {
            Signal<Operation> ops = resources.map(location -> {
                if (location.isDirectory()) {
                    return new DirectoryOperation((Directory) location, null);
                } else {
                    return new FileOperation((File) location);
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
                public void copyTo(Directory destination, String... patterns) {
                    ops.to(op -> op.copyTo(destination, patterns));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void packTo(ArchiveOutputStream archive, Directory relative, String... patterns) {
                    ops.to(op -> op.packTo(archive, relative, patterns));
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
                    return resources.as(Location.class);
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
        if (base != null) {
            operations.add(new DirectoryOperation(base, patterns));
        }
        return this;
    }

    public Folder add2(Directory base, UnaryOperator<SelectOption> op) {
        SelectOption o = op.apply(new SelectOption());

        return this;
    }

    public Folder add2(Directory base, SelectOption option) {
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
     *   - main.jar
     *   - lib
     *     - one.jar
     *     - other.jar
     * </pre>
     * 
     * @param relative A destination relative path.
     * @param entries Your entries.
     * @return
     */
    public Folder add(String relative, Function<Folder, Folder> entries) {
        return add(Locator.directory(relative), entries);
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
     *   - main.jar
     *   - lib
     *     - one.jar
     *     - other.jar
     * </pre>
     * 
     * @param relative A destination relative path.
     * @param entries Your entries.
     * @return
     */
    public Folder add(Path relative, Function<Folder, Folder> entries) {
        return add(Locator.directory(relative), entries);
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
     *   - main.jar
     *   - lib
     *     - one.jar
     *     - other.jar
     * </pre>
     * 
     * @param relative A destination relative path.
     * @param entries Your entries.
     * @return
     */
    public Folder add(Directory relative, Function<Folder, Folder> entries) {
        if (entries != null) {
            operations.addAll(I.signal(entries.apply(Locator.folder()).operations).map(op -> new Allocator(op, relative)).toList());
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
        try (ArchiveOutputStream out = new ArchiveStreamFactory()
                .createArchiveOutputStream(archive.extension().replaceAll("7z", "7z-override"), archive.newOutputStream())) {
            operations.forEach(operation -> operation.packTo(out, Locator.directory(""), patterns));
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
    private static void pack(ArchiveOutputStream out, Directory directory, File file, Directory relative) {
        try {
            ArchiveEntry entry = out.createArchiveEntry(file.asJavaFile(), relative.file(directory.relativize(file)).path());
            out.putArchiveEntry(entry);

            try (InputStream in = file.newInputStream()) {
                in.transferTo(out);
            }
            out.closeArchiveEntry();
        } catch (IOException e) {
            throw I.quiet(e);
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
        void copyTo(Directory destination, String... patterns);

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param relative
         * @param patterns
         */
        void packTo(ArchiveOutputStream archive, Directory relative, String... patterns);

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
     * Operation for {@link File}.
     */
    private static class FileOperation implements Operation {

        private final File file;

        /**
         * @param file
         */
        private FileOperation(File file) {
            this.file = file;
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
        public void copyTo(Directory destination, String... patterns) {
            file.copyTo(destination);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, Directory relative, String... patterns) {
            pack(archive, file.parent(), file, relative);
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
     * Operation for {@link File}.
     */
    private static class DirectoryOperation implements Operation {

        private final Directory directory;

        private final String[] patternsWhenAdd;

        /**
         * @param directory
         * @param patterns
         */
        private DirectoryOperation(Directory directory, String[] patterns) {
            this.directory = directory;
            this.patternsWhenAdd = patterns;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete(String... patterns) {
            directory.delete(I.array(patterns, patternsWhenAdd));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void moveTo(Directory destination, String... patterns) {
            directory.moveTo(destination, I.array(patterns, patternsWhenAdd));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copyTo(Directory destination, String... patterns) {
            directory.copyTo(destination, I.array(patterns, patternsWhenAdd));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, Directory relative, String... patterns) {
            directory.walkFiles(I.array(patterns, patternsWhenAdd)).to(file -> pack(archive, directory.parent(), file, relative));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
            return directory.walkFiles(I.array(patterns, patternsWhenAdd)).map(file -> I.pair(directory, file));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
            return directory.walkDirectories(I.array(patterns, patternsWhenAdd)).map(dir -> I.pair(directory, dir));
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
        public void copyTo(Directory destination, String... patterns) {
            delegator.copyTo(destination.directory(relative), patterns);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void packTo(ArchiveOutputStream archive, Directory relative, String... patterns) {
            delegator.packTo(archive, this.relative, patterns);
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
}

/*
 * Copyright (C) 2025 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.io.InputStream;
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
     * @param patterns A list of glob patterns to accept file by its name.
     * @return Chainable API.
     */
    public Folder add(Path entry, String... patterns) {
        return add(entry, Option.of(patterns));
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
     * @param patterns A list of glob patterns to accept file by its name.
     * @return Chainable API.
     */
    public Folder add(Folder entries, String... patterns) {
        return add(entries, Option.of(patterns));
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
     * @param patterns A list of glob patterns to accept file by its name.
     * @return Chainable API.
     */
    public Folder add(Location entry, String... patterns) {
        return add(I.signal(entry), Option.of(patterns));
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
     * @param patterns A list of glob patterns to accept file by its name.
     */
    public Folder add(Signal<? extends Location> entries, String... patterns) {
        return add(entries, Option.of(patterns));
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
     * @param option A option.
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
            // see https://commons.apache.org/proper/commons-compress/zip.html
            //
            // Traditionally the ZIP archive format uses CodePage 437 as encoding for file name,
            // which is not sufficient for many international character sets.
            //
            // Over time different archivers have chosen different ways to work around the
            // limitation - the java.util.zip packages simply uses UTF-8 as its encoding for
            // example.
            //
            // The optimal setting of flags depends on the archivers you expect as
            // consumers/producers of the ZIP archives. Below are some test results which may be
            // superseded with later versions of each tool.
            //
            // The java.util.zip package used by the jar executable or to read jars from your
            // CLASSPATH reads and writes UTF-8 names, it doesn't set or recognize any flags or
            // Unicode extra fields.
            //
            // Starting with Java7 java.util.zip writes UTF-8 by default and uses the language
            // encoding flag. It is possible to specify a different encoding when reading/writing
            // ZIPs via new constructors. The package now recognizes the language encoding flag when
            // reading and ignores the Unicode extra fields.
            //
            // 7Zip writes CodePage 437 by default but uses UTF-8 and the language encoding flag
            // when writing entries that cannot be encoded as CodePage 437 (similar to the zip task
            // with fallbacktoUTF8 set to true). It recognizes the language encoding flag when
            // reading and ignores the Unicode extra fields.
            //
            // WinZIP writes CodePage 437 and uses Unicode extra fields by default. It recognizes
            // the Unicode extra field and the language encoding flag when reading.
            // Windows' "compressed folder" feature doesn't recognize any flag or extra field and
            // creates archives using the platforms default encoding - and expects archives to be in
            // that encoding when reading them.
            //
            // InfoZIP based tools can recognize and write both, it is a compile time option and
            // depends on the platform so your mileage may vary.
            //
            // PKWARE zip tools recognize both and prefer the language encoding flag. They create
            // archives using CodePage 437 if possible and UTF-8 plus the language encoding flag for
            // file names that cannot be encoded as CodePage 437.
            //
            // If you are creating jars, then java.util.zip is your main consumer. We recommend you
            // set the encoding to UTF-8 and keep the language encoding flag enabled. The flag won't
            // help or hurt java.util.zip prior to Java7 but archivers that support it will show the
            // correct file names.
            //
            // For maximum interop it is probably best to set the encoding to UTF-8, enable the
            // language encoding flag and create Unicode extra fields when writing ZIPs. Such
            // archives should be extracted correctly by java.util.zip, 7Zip, WinZIP, PKWARE tools
            // and most likely InfoZIP tools. They will be unusable with Windows' "compressed
            // folders" feature and bigger than archives without the Unicode extra fields, though.
            try (ZipOutputStream out = new ZipOutputStream(archive.newOutputStream() /* Use UTF-8 */)) {
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
         * @param option
         */
        public Signal<Location> observeDeleting(Function<Option, Option> option) {
            return location.observeDeleting(this.option.andThen(option));
        }

        /**
         * Move reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param option
         */
        public Signal<Location> observeMovingTo(Directory destination, Function<Option, Option> option) {
            return location.observeMovingTo(destination, this.option.andThen(option));
        }

        /**
         * Copy reosources to the specified {@link Directory}.
         * 
         * @param destination
         * @param option
         */
        public Signal<Location> observeCopyingTo(Directory destination, Function<Option, Option> option) {
            return location.observeCopyingTo(destination, this.option.andThen(option));
        }

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param relative
         * @param option
         */
        public Signal<Location> observePackingTo(ZipOutputStream archive, Directory relative, Function<Option, Option> option) {
            Function<Option, Option> combined = this.option.andThen(option);
            Option o = combined.apply(new Option());

            if (location.isFile()) {
                return pack(archive, location.parent(), location.asFile(), o.allocator);
            } else {
                return location.walkFile(combined)
                        .flatMap(file -> pack(archive, !location.isRoot() && o.strip == 0 ? location.parent()
                                : location.asDirectory(), file, o.allocator));
            }
        }

        /**
         * List up all resources.
         * 
         * @param option
         * @return
         */
        public Signal<Ⅱ<Directory, File>> walkFilesWithBase(Function<Option, Option> option) {
            return location.walkFileWithBase(this.option.andThen(option));
        }

        /**
         * List up all resources.
         * 
         * @param option
         * @return
         */
        public Signal<Ⅱ<Directory, Directory>> walkDirectoriesWithBase(Function<Option, Option> option) {
            return location.walkDirectoryWithBase(this.option.andThen(option));
        }
    }
}
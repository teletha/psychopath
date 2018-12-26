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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

public final class Temporary {

    /** The operations. */
    private final List<Operation> operations = new ArrayList();

    /**
     * 
     */
    Temporary() {
    }

    /**
     * Add path.
     * 
     * @param files
     */
    public Temporary add(Path path) {
        Location location = Locator.locate(path);

        if (location.isDirectory()) {
            return add((Directory) location);
        } else {
            return add((File) location);
        }
    }

    /**
     * Merge resources.
     * 
     * @param temporary
     * @return
     */
    public Temporary add(Temporary temporary) {
        operations.addAll(temporary.operations);
        return this;
    }

    /**
     * Add files.
     * 
     * @param files
     */
    public Temporary add(File... files) {
        return add("", files);
    }

    /**
     * Add files with relative path.
     * 
     * @param destinationRelativePath
     * @param files
     * @return
     */
    public Temporary add(String destinationRelativePath, File... files) {
        return add(destinationRelativePath, I.signal(files));
    }

    /**
     * Add files.
     * 
     * @param files
     */
    public Temporary add(Signal<File> files) {
        return add("", files);
    }

    /**
     * Add files.
     * 
     * @param files
     */
    public Temporary add(String destinationRelativePath, Signal<File> files) {
        if (files != null) {
            operations.add(new Operation() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete(String... patterns) {
                    files.to(File::delete);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void moveTo(Directory destination, String... patterns) {
                    files.to(file -> file.moveTo(destination.directory(destinationRelativePath)));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void copyTo(Directory destination, String... patterns) {
                    files.to(file -> file.copyTo(destination.directory(destinationRelativePath)));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void packTo(ArchiveOutputStream archive, String... patterns) {
                    files.to(file -> pack(archive, file.parent(), file, destinationRelativePath));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
                    return files.map(file -> I.pair(file.parent(), file));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
                    return Signal.empty();
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
    public Temporary add(Directory base, String... patterns) {
        return add("", base, patterns);
    }

    /**
     * Add pattern matching path.
     * 
     * @param base A base path.
     * @param patterns "glob" include/exclude patterns.
     */
    public Temporary add(String destinationRelativePath, Directory base, String... patterns) {
        if (base != null) {
            operations.add(new Operation() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete(String... additions) {
                    base.delete(I.array(patterns, additions));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void moveTo(Directory destination, String... additions) {
                    base.moveTo(destination.directory(destinationRelativePath), I.array(patterns, additions));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void copyTo(Directory destination, String... additions) {
                    base.copyTo(destination.directory(destinationRelativePath), I.array(patterns, additions));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void packTo(ArchiveOutputStream archive, String... additions) {
                    base.walkFiles(I.array(patterns, additions)).to(file -> pack(archive, base, file, destinationRelativePath));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Ⅱ<Directory, File>> walkFiles(String... additions) {
                    return base.walkFiles(I.array(patterns, additions)).map(file -> I.pair(base, file));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... additions) {
                    return base.walkDirectories(I.array(patterns, additions)).map(dir -> I.pair(base, dir));
                }
            });
        }
        return this;
    }

    /**
     * Delete all resources.
     * 
     * @return
     */
    public Temporary delete(String... patterns) {
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
    public void packTo(File archive, String... patterns) {
        try (ArchiveOutputStream out = new ArchiveStreamFactory()
                .createArchiveOutputStream(archive.extension().replaceAll("7z", "7z-override"), archive.newOutputStream())) {
            operations.forEach(operation -> operation.packTo(out, patterns));
            out.finish();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * List up all {@link File}s.
     * 
     * @return
     */
    public Signal<Ⅱ<Directory, File>> walkFiles(String... patterns) {
        return I.signal(operations).concatMap(op -> op.walkFiles(patterns));
    }

    /**
     * List up all {@link Directory}.
     * 
     * @return
     */
    public Signal<Ⅱ<Directory, Directory>> walkDirectories(String... patterns) {
        return I.signal(operations).concatMap(op -> op.walkDirectories(patterns));
    }

    /**
     * Build {@link ArchiveEntry} for each resources.
     * 
     * @param out
     * @param directory
     * @param file
     * @param relative
     */
    private void pack(ArchiveOutputStream out, Directory directory, File file, String relative) {
        try {
            ArchiveEntry entry = out.createArchiveEntry(file.asJavaFile(), directory.relativize(file).path());
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
     * Definition of {@link Temporary} operation.
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
         * @param patterns
         * @param destination
         */
        void packTo(ArchiveOutputStream archive, String... patterns);

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
    }
}

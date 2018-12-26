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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import kiss.I;
import kiss.Signal;

public final class Temporary {

    /** The operations. */
    private final List<Operation> operations = new ArrayList();

    /**
     * 
     */
    Temporary() {
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
                public void delete() {
                    files.to(File::delete);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void moveTo(Directory destination) {
                    files.to(file -> file.moveTo(destination.directory(destinationRelativePath)));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void copyTo(Directory destination) {
                    files.to(file -> file.copyTo(destination.directory(destinationRelativePath)));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void packTo(ArchiveOutputStream archive) {
                    files.to(file -> pack(archive, file.parent(), file, destinationRelativePath));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<File> walk() {
                    return files;
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
                public void delete() {
                    base.delete(patterns);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void moveTo(Directory destination) {
                    base.moveTo(destination.directory(destinationRelativePath), patterns);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void copyTo(Directory destination) {
                    base.copyTo(destination.directory(destinationRelativePath), patterns);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void packTo(ArchiveOutputStream archive) {
                    base.walkFiles(patterns).to(file -> pack(archive, base, file, destinationRelativePath));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Signal<File> walk() {
                    return base.walkFiles();
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
    public Temporary delete() {
        operations.forEach(Operation::delete);

        return this;
    }

    /**
     * Copy all resources to the specified {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     */
    public Directory copyTo(Directory destination) {
        Objects.requireNonNull(destination);

        operations.forEach(operation -> operation.copyTo(destination));

        return destination;
    }

    /**
     * Move all resources to the specified {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     */
    public Directory moveTo(Directory destination) {
        Objects.requireNonNull(destination);

        operations.forEach(operation -> operation.moveTo(destination));

        return destination;
    }

    /**
     * Pack all resources.
     * 
     * @param archive
     */
    public void packTo(File archive) {
        try (ArchiveOutputStream out = new ArchiveStreamFactory()
                .createArchiveOutputStream(archive.extension().replaceAll("7z", "7z-override"), archive.newOutputStream())) {
            operations.forEach(operation -> operation.packTo(out));
            out.finish();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * List up all resources.
     * 
     * @return
     */
    public Signal<File> walk() {
        return I.signal(operations).concatMap(Operation::walk);
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
     * Detect archive type.
     * 
     * @param file A target archive file.
     * @return An archive.
     */
    static ArchiveOutputStream detectArchiver(File file) {
        try {
            switch (file.extension()) {
            case "7z":
                new SevenZOutputFile(file.asJavaFile());
                throw new Error();

            case "zip":
            default:
                return new ZipArchiveOutputStream(file.asJavaFile());

            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * Detect archive file system.
     * 
     * @param file A target archive file.
     * @return An archive.
     */
    static Path detectFileSystetm(File file) {
        try {
            switch (file.extension()) {
            case "zip":
            default:
                return FileSystems.newFileSystem(file.path, null).getPath("/");
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * Definition of {@link Temporary} operation.
     */
    private interface Operation {

        /**
         * Delete resources.
         */
        void delete();

        /**
         * Move reosources to the specified {@link Directory}.
         * 
         * @param destination
         */
        void moveTo(Directory destination);

        /**
         * Copy reosources to the specified {@link Directory}.
         * 
         * @param destination
         */
        void copyTo(Directory destination);

        /**
         * Pack reosources to the specified {@link File}.
         * 
         * @param destination
         */
        void packTo(ArchiveOutputStream archive);

        /**
         * List up all resources.
         * 
         * @return
         */
        Signal<File> walk();
    }
}

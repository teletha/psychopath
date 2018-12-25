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
import kiss.WiseTriConsumer;

public final class Temporary {

    /** The path entries. */
    private final List<WiseTriConsumer<Integer, Directory, ArchiveOutputStream>> entries = new ArrayList();

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
            entries.add((type, destination, archive) -> {
                switch (type) {
                case 0:
                    files.to(File::delete);
                    break;

                case 1:
                    files.to(file -> file.moveTo(destination.directory(destinationRelativePath)));
                    break;

                case 2:
                    files.to(file -> file.copyTo(destination.directory(destinationRelativePath)));
                    break;

                case 3:
                    files.to(file -> pack(archive, file.parent(), file, destinationRelativePath));
                    break;
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
            entries.add((type, destination, archive) -> {
                switch (type) {
                case 0:
                    base.delete(patterns);
                    break;

                case 1:
                    destination = destination.directory(destinationRelativePath);

                    if (patterns == null || patterns.length == 0) {
                        base.moveTo(destination);
                    } else {
                        base.moveTo(destination, patterns);
                    }
                    break;

                case 2:
                    base.copyTo(destination = destination.directory(destinationRelativePath), patterns);
                    break;

                case 3:
                    base.walkFiles(patterns).to(file -> pack(archive, base, file, destinationRelativePath));
                    break;
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
        entries.forEach(entry -> entry.accept(0, null, null));

        return this;
    }

    /**
     * Copy all resources to the specified {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     */
    public Directory copyTo(Directory destination) {
        Objects.requireNonNull(destination);

        entries.forEach(entry -> entry.accept(2, destination, null));

        return destination;
    }

    /**
     * Move all resources to the specified {@link Directory}.
     * 
     * @param destination A destination {@link Directory}.
     */
    public Directory moveTo(Directory destination) {
        Objects.requireNonNull(destination);

        entries.forEach(entry -> entry.accept(1, destination, null));

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
            entries.forEach(entry -> entry.accept(3, null, out));
            out.finish();
        } catch (Exception e) {
            throw I.quiet(e);
        }
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
}

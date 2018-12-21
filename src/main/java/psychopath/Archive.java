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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

public class Archive {

    /** The path entries. */
    private Signal<Ⅱ<Directory, File>> entries = Signal.empty();

    /**
     * 
     */
    Archive() {
    }

    /**
     * Add files.
     * 
     * @param files
     */
    public Archive add(Signal<File> files) {
        if (files != null) {
            entries = entries.merge(files.map(file -> I.pair(file.parent(), file)));
        }
        return this;
    }

    /**
     * Add pattern matching path.
     * 
     * @param base A base path.
     * @param patterns "glob" include/exclude patterns.
     */
    public Archive add(Directory base, String... patterns) {
        if (base != null) {
            entries = entries.merge(base.walkFiles(patterns).map(file -> I.pair(base, file)));
        }
        return this;
    }

    /**
     * Pack all resources.
     **
     * @param archive
     */
    public void packTo(File archive) {
        archive = archive.absolutize();

        try (ArchiveOutputStream out = new ArchiveStreamFactory()
                .createArchiveOutputStream(archive.extension().replaceAll("7z", "7z-override"), archive.newOutputStream())) {
            // Location must exist
            if (archive.isAbsent()) {
                archive.create();
            }

            entries.to(file -> {
                try {
                    ArchiveEntry entry = out.createArchiveEntry(file.ⅱ.asJavaFile(), file.ⅰ.relativize(file.ⅱ).path());
                    out.putArchiveEntry(entry);

                    if (file.ⅱ.isDirectory()) {

                    }

                    try (InputStream in = file.ⅱ.newInputStream()) {
                        in.transferTo(out);
                    }
                    out.closeArchiveEntry();

                } catch (IOException e) {
                    throw I.quiet(e);
                }
            });
            out.finish();
        } catch (Exception e) {
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

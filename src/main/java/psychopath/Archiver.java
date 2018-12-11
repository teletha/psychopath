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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import kiss.Disposable;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/06/05 14:45:33
 */
public class Archiver {

    /** The default encoding. */

    /** The default manifest. */
    protected Manifest manifest;

    /** The path entries. */
    private Signal<File> entries = Signal.empty();

    /**
     * Add files.
     * 
     * @param files
     */
    public Archiver add(Signal<File> files) {
        if (files != null) {
            entries = entries.merge(files);
        }
        return this;
    }

    /**
     * <p>
     * Add pattern matching path.
     * </p>
     * 
     * @param base A base path.
     * @param patterns "glob" include/exclude patterns.
     */
    public void add(Directory base, String... patterns) {
        if (base != null) {
            add(base.walkFiles(patterns));
        }
    }

    /**
     * <p>
     * Pack all resources.
     * </p>
     * 
     * @param location
     */
    public void packTo(Path location) {
        if (location != null) {
            location = location.toAbsolutePath();

            try {
                // Location must exist
                if (Files.notExists(location)) {
                    Files.createDirectories(location.getParent());
                    Files.createFile(location);
                }

                // Location must be file.
                if (!Files.isRegularFile(location)) {
                    throw new IllegalArgumentException("'" + location + "' must be regular file.");
                }
                
                try (OutputStream fo = Files.newOutputStream(location));
                        OutputStream gzo = new GzipCompressorOutputStream(fo);
                        ArchiveOutputStream o = new TarArchiveOutputStream(gzo)) {
                   }
                
//                Collection<File> filesToArchive = ...
//                        try (ArchiveOutputStream o = ... create the stream for your format ...) {
//                            for (File f : filesToArchive) {
//                                // maybe skip directories for formats like AR that don't store directories
//                                ArchiveEntry entry = o.createArchiveEntry(f, entryName(f));
//                                // potentially add more flags to entry
//                                o.putArchiveEntry(entry);
//                                if (f.isFile()) {
//                                    try (InputStream i = Files.newInputStream(f.toPath())) {
//                                        IOUtils.copy(i, o);
//                                    }
//                                }
//                                o.closeArchiveEntry();
//                            }
//                            out.finish();
//                        }

                Writer archiver = new Writer(location, StandardCharsets.UTF_8);

                if (manifest != null) {
                    ZipEntry entry = new JarEntry(JarFile.MANIFEST_NAME);
                    archiver.putNextEntry(entry);
                    manifest.write(new BufferedOutputStream(archiver));
                    archiver.closeEntry();
                }

                try {
                    entries.to(file -> {
                        // scan entry
                        try {
                            BasicFileAttributes attrs = file.attribute();
                            ZipEntry zip = new ZipEntry(file.relativePath());
                            zip.setSize(attrs.size());
                            zip.setLastModifiedTime(attrs.lastModifiedTime());
                            zip.setMethod(ZipEntry.DEFLATED);

                            archiver.putNextEntry(zip);
                            I.copy(file.newInputStream(), archiver, true);
                            archiver.closeEntry();
                        } catch (IOException io) {
                            // ignore
                        }
                    });
                } finally {
                    archiver.dispose();
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @version 2015/06/23 21:21:57
     */
    private static class Writer extends ZipOutputStream implements Disposable {

        /**
         * @param output
         */
        private Writer(Path destination, Charset encoding) throws IOException {
            super(Files.newOutputStream(destination), encoding);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            // super.close();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void vandalize() {
            try {
                super.close();
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }
}

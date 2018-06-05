/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import bee.Platform;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

/**
 * @version 2018/06/05 14:45:33
 */
public class Archiver {

    /** The default encoding. */
    protected Charset encoding = Platform.Encoding;

    /** The default manifest. */
    protected Manifest manifest;

    /** The path entries. */
    private Signal<Ⅱ<Directory, File>> entries = Signal.EMPTY;

    /**
     * Add files.
     * 
     * @param files
     */
    public Archiver add(Signal<Ⅱ<Directory, File>> files) {
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
            add(base.walkFilesRelatively(patterns));
        }
    }

    /**
     * <p>
     * Pack all resources.
     * </p>
     * 
     * @param location
     */
    public void pack(Path location) {
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

                Writer archiver = new Writer(location, encoding);

                if (manifest != null) {
                    ZipEntry entry = new JarEntry(JarFile.MANIFEST_NAME);
                    archiver.putNextEntry(entry);
                    manifest.write(new BufferedOutputStream(archiver));
                    archiver.closeEntry();
                }

                try {
                    entries.to(e -> {
                        // compute base directory
                        File file = e.ⅰ.file(e.ⅱ);

                        // scan entry
                        try {
                            BasicFileAttributes attrs = file.attribute();
                            ZipEntry zip = new ZipEntry(e.ⅱ.path());
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

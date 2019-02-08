/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.archiver;

import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import kiss.I;
import kiss.Signal;
import psychopath.File;

public enum Archiver {

    Jar("jar") {
        @Override
        public ArchiveEntry create(String name, File file) {
            JarArchiveEntry entry = new JarArchiveEntry(name);
            return entry;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<File> pack(Signal<File> entries, File destination) {
        }
    },

    Zip("zip") {
        @Override
        public ArchiveEntry create(String name, File file) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            return entry;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<File> pack(Signal<File> entries, File destination) {
        }
    },

    SevenZ("7z") {
        @Override
        public ArchiveEntry create(String name, File file) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName(name);
            return entry;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<File> pack(Signal<File> entries, File destination) {
            try {
                SevenZOutputFile archive = new SevenZOutputFile(destination.asJavaFile());

                return entries.effect(file -> {
                    archive.createArchiveEntry(file.asJavaFile());
                });
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    };

    /** The default extension. */
    public final String extension;

    /** The basic extension. */
    public final List<String> extensions;

    /**
     * @param extensions
     */
    private Archiver(String... extensions) {
        this.extension = extensions[0];
        this.extensions = List.of(extensions);
    }

    /**
     * Test the specified extension matches this {@link Archiver}.
     * 
     * @param extension
     * @return
     */
    public boolean match(String extension) {
        for (String e : extensions) {
            if (e.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create archive entry.
     * 
     * @param name
     * @param file
     * @return
     */
    public abstract ArchiveEntry create(String name, File file);

    public abstract Signal<File> pack(Signal<File> entries, File destination);

    /**
     * Find the suitable {@link ArchiveEntry} by extension.
     * 
     * @param extension
     * @return
     */
    public static Archiver byExtension(String extension) {
        return byExtension(extension, Zip);
    }

    /**
     * Find the suitable {@link ArchiveEntry} by extension.
     * 
     * @param extension
     * @param defaultType A default {@link Archiver}.
     * @return
     */
    public static Archiver byExtension(String extension, Archiver defaultType) {
        for (Archiver archiver : values()) {
            if (archiver.match(extension)) {
                return archiver;
            }
        }
        return defaultType;
    }
}

/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.archiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamProvider;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import kiss.I;

public class SevenZipArchiveStreamProvider implements ArchiveStreamProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveInputStream createArchiveInputStream(String name, InputStream input, String encoding) throws ArchiveException {
        try (InputStream in = input) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            input.transferTo(out);

            SevenZFile file = new SevenZFile(new SeekableInMemoryByteChannel(out.toByteArray()));

            return new ArchiveInputStream() {
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return file.read(b, off, len);
                }

                @Override
                public ArchiveEntry getNextEntry() throws IOException {
                    return file.getNextEntry();
                }

                @Override
                public void close() throws IOException {
                    file.close();
                }
            };
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveOutputStream createArchiveOutputStream(String name, OutputStream out, String encoding) throws ArchiveException {
        try {
            SeekableInMemoryByteChannel bytes = new SeekableInMemoryByteChannel();
            SevenZOutputFile file = new SevenZOutputFile(bytes);

            return new ArchiveOutputStream() {
                @Override
                public void putArchiveEntry(ArchiveEntry entry) throws IOException {
                    file.putArchiveEntry(entry);
                }

                @Override
                public void closeArchiveEntry() throws IOException {
                    file.closeArchiveEntry();
                }

                @Override
                public void finish() throws IOException {
                    file.finish();
                }

                @Override
                public ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
                    return file.createArchiveEntry(inputFile, entryName);
                }

                @Override
                public void write(int b) throws IOException {
                    file.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    file.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    file.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    file.close();

                    new ByteArrayInputStream(bytes.array()).transferTo(out);
                }
            };
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getInputStreamArchiveNames() {
        return Set.of("7z-override");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getOutputStreamArchiveNames() {
        return Set.of("7z-override");
    }
}

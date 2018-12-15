/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.archiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamProvider;
import org.apache.commons.compress.archivers.StreamingNotSupportedException;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import kiss.I;

/**
 * @version 2018/07/19 15:26:51
 */
public class RARArchiveStreamProvider implements ArchiveStreamProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveInputStream createArchiveInputStream(String name, InputStream in, String encoding) throws ArchiveException {
        try {
            Archive archive = new Archive(in);
            Iterator<FileHeader> headers = archive.iterator();

            return new ArchiveInputStream() {

                private FileHeader header;

                private InputStream in;

                @Override
                public ArchiveEntry getNextEntry() throws IOException {
                    try {
                        if (headers.hasNext()) {
                            header = headers.next();
                            in = archive.getInputStream(header);

                            return new ArchiveEntry() {

                                @Override
                                public boolean isDirectory() {
                                    return header.isDirectory();
                                }

                                @Override
                                public long getSize() {
                                    return header.getDataSize();
                                }

                                @Override
                                public String getName() {
                                    return header.getFileNameString();
                                }

                                @Override
                                public Date getLastModifiedDate() {
                                    return header.getMTime();
                                }
                            };
                        } else {
                            return null;
                        }
                    } catch (RarException e) {
                        throw I.quiet(e);
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return in.read(b, off, len);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void close() throws IOException {
                    archive.close();
                }
            };
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveOutputStream createArchiveOutputStream(String name, OutputStream out, String encoding) throws ArchiveException {
        throw new StreamingNotSupportedException("rar");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getInputStreamArchiveNames() {
        return Set.of("rar");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getOutputStreamArchiveNames() {
        return Set.of("rar");
    }
}

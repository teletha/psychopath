/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import kiss.I;

class AtomicFileOutputStream extends OutputStream {

    /** The target file to write finally. */
    private final File dest;

    /** The file to write temporary. */
    private final File temp;

    /** The actual writer. */
    private final OutputStream out;

    /**
     * @param dest
     */
    AtomicFileOutputStream(File dest, OpenOption... options) {
        this.dest = dest;
        this.temp = dest.extension(dest.extension() + ".tmp").deleteOnExit();

        try {
            if (List.of(options).contains(StandardOpenOption.APPEND)) {
                Files.copy(dest.path, temp.path);
            }

            this.out = Files.newOutputStream(temp.path, options);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        out.close();

        try {
            Files.move(temp.path, dest.path, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException moveFailed) {
            try {
                Files.move(temp.path, dest.path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException replaceFailed) {
                replaceFailed.addSuppressed(moveFailed);
                try {
                    Files.delete(temp.path);
                } catch (IOException deleteFailed) {
                    replaceFailed.addSuppressed(deleteFailed);
                }
                throw replaceFailed;
            }
        }
    }
}

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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
    AtomicFileOutputStream(File dest) {
        this.dest = dest;
        this.temp = dest.extension(dest.extension() + ".atomic").deleteOnExit();
        this.out = temp.newOutputStream();
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
        } catch (IOException moveFailed) {
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

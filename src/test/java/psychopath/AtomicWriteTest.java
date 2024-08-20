/*
 * Copyright (C) 2024 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import static psychopath.Option.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Jimfs;

class AtomicWriteTest extends LocationTestHelper {

    @Test
    void writer() throws IOException {
        File file = locateFile("test");
        BufferedWriter writer = file.newBufferedWriter();
        writer.write("ok");
        writer.close();
        assert file.text().equals("ok");

        // failed writing
        writer = file.newBufferedWriter();
        writer.write("failed");
        writer.flush(); // no close
        assert file.text().equals("failed");

        // atomic writing
        writer = file.newBufferedWriter(ATOMIC_WRITE);
        writer.write("atomic");
        writer.close();
        assert file.text().equals("atomic");

        // failed writing
        writer = file.newBufferedWriter(ATOMIC_WRITE);
        writer.write("failed");
        writer.flush(); // no close
        assert file.text().equals("atomic");
    }

    @Test
    void failOverOnUnsupportedFileSystem() throws IOException {
        FileSystem fs = Jimfs.newFileSystem();

        File file = Locator.file(fs.getPath("test"));
        BufferedWriter writer = file.newBufferedWriter();
        writer.write("ok");
        writer.close();
        assert file.text().equals("ok");

        // failed writing
        writer = file.newBufferedWriter();
        writer.write("failed");
        writer.flush(); // no close
        assert file.text().equals("failed");

        // atomic writing
        writer = file.newBufferedWriter(ATOMIC_WRITE);
        writer.write("atomic");
        writer.close();
        assert file.text().equals("atomic");

        // failed writing
        writer = file.newBufferedWriter(ATOMIC_WRITE);
        writer.write("failed");
        writer.flush(); // no close
        assert file.text().equals("atomic");
    }
}
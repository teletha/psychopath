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

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/12/10 10:21:05
 */
public class ArchiverTest {

    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    @Test
    public void file() throws Exception {
        Path file = room.locateFile("root/file");
        Path output = room.locateAbsent("out");

        Archiver jar = new Archiver();
        jar.add(Locator.directory(file.getParent()));
        jar.packTo(output);

        try (FileSystem system = FileSystems.newFileSystem(output, null)) {
            Path archive = system.getPath("/");

            assert Files.exists(archive.resolve("file"));
            assert Files.isRegularFile(archive.resolve("file"));
        }
    }

    @Test
    public void directory() throws Exception {
        Path root = room.locateDirectory("root", $ -> {
            $.file("file");
            $.dir("directory", () -> {
                $.file("file");
            });
        });
        Path output = room.locateAbsent("out");

        Archiver jar = new Archiver();
        jar.add(Locator.directory(root));
        jar.packTo(output);

        try (FileSystem system = FileSystems.newFileSystem(output, null)) {
            Path archive = system.getPath("/");

            assert Files.exists(archive.resolve("file"));
            // assert Files.exists(archive.resolve("directory"));
            // assert Files.isDirectory(archive.resolve("directory"));
            assert Files.exists(archive.resolve("directory/file"));
            // assert Files.isRegularFile(archive.resolve("directory/file"));
        }
    }
}

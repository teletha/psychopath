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

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/06/04 9:31:56
 */
public class WalkLocationTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void walkFiles() {
        Path root = room.locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("dir1", () -> {
                $.file("text1");
                $.file("text2");
            });
            $.dir("dir2", () -> {
                $.file("text1");
                $.file("text2");
            });
        });

        Directory directory = Locator.directory(root);
        assert directory.walkFiles().toList().size() == 6;
        assert directory.walkFiles("*").toList().size() == 2;
        assert directory.walkFiles("**").toList().size() == 6;
        assert directory.walkFiles("*/*").toList().size() == 4;
        directory.walkFiles().to(e -> {
            assert e.relativePath().equals(directory.relativize(e).toString());
        });
    }

    @Test
    void walkDirectories() {
        Path root = room.locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("dir1", () -> {
                $.dir("text1");
                $.dir("text2");
            });
            $.dir("dir2", () -> {
                $.dir("text1");
                $.dir("text2");
            });
        });

        Directory directory = Locator.directory(root);
        assert directory.walkDirectories().toList().size() == 8;
        assert directory.walkDirectories("*").toList().size() == 4;
        assert directory.walkDirectories("**").toList().size() == 8;
        assert directory.walkDirectories("*/*").toList().size() == 4;
        directory.walkDirectories().to(e -> {
            assert e.relativePath().equals(directory.relativize(e).toString());
        });
    }
}

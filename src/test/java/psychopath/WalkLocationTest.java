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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/06/04 9:31:56
 */
public class WalkLocationTest extends LocationTestHelper {

    @Test
    void walkFiles() {
        Directory directory = locateDirectory("root", $ -> {
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
        Directory directory = locateDirectory("root", $ -> {
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

        assert directory.walkDirectories().toList().size() == 8;
        assert directory.walkDirectories("*").toList().size() == 4;
        assert directory.walkDirectories("**").toList().size() == 8;
        assert directory.walkDirectories("*/*").toList().size() == 4;
        directory.walkDirectories().to(e -> {
            assert e.relativePath().equals(directory.relativize(e).toString());
        });
    }
}

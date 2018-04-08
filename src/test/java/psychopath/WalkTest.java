/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/03/31 3:00:22
 */
public class WalkTest {

    @RegisterExtension
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void zip() {
        Path root = room.locateArchive("zip", $ -> {
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

        assert PsychoPath.walk(root).size() == 6;
        assert PsychoPath.walk(root, "*").size() == 2;
        assert PsychoPath.walk(root, "!dir1/**").size() == 4;
        assert PsychoPath.walkDirectory(root).size() == 2;
    }
}


/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.sample;

import static psychopath.UnpackOption.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import psychopath.Directory;
import psychopath.Location;
import psychopath.Locator;

/**
 * @version 2018/12/08 21:08:38
 */
public class UnpackTest {

    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    @Test
    void unpack() {
        Path zip = room.locateFile("test.zip");
        room.locateArchive(zip, $ -> {
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });

        Directory directory = Locator.file(zip).unpack();
        List<Location<?>> children = directory.children().toList();
        assert children.size() == 1;
        assert children.get(0).asFile().getName().equals("inside");
    }

    @Test
    void stripSingleDirectory() {
        Path zip = room.locateFile("test.zip");
        room.locateArchive(zip, $ -> {
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });

        Directory directory = Locator.file(zip).unpack(StripSingleDirectory);
        List<Location<?>> children = directory.children().toList();
        assert children.size() == 3;
        assert children.get(0).asFile().getName().equals("1");
    }
}

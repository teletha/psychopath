/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.location;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;
import psychopath.Directory;
import psychopath.Location;

/**
 * @version 2018/04/08 15:21:41
 */
public class DirectoryTest {
    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    @Test
    void findDirectorySingle() {
        Directory root = define($ -> {
            $.dir("dir1");
        });

        List<Directory> dirs = root.directories().toList();
        assert dirs.size() == 1;
        assert dirs.get(0).name().equals("dir1");
    }

    @Test
    void fineDirectoryMultiple() {
        Directory root = define($ -> {
            $.dir("dir1");
            $.dir("dir2");
            $.dir("dir3");
        });

        List<Directory> dirs = root.directories().toList();
        assert dirs.size() == 3;
        assert dirs.get(0).name().equals("dir1");
        assert dirs.get(1).name().equals("dir2");
        assert dirs.get(2).name().equals("dir3");
    }

    @Test
    void fineDirectoryNest() {
        Directory root = define($ -> {
            $.dir("dir1", () -> {
                $.dir("nest11");
                $.dir("nest12");
            });
            $.dir("dir2", () -> {
                $.dir("nest21");
                $.dir("nest22");
            });
        });

        List<Directory> dirs = root.directories().toList();
        assert dirs.size() == 6;
        assert dirs.get(0).name().equals("dir1");
        assert dirs.get(1).name().equals("nest11");
        assert dirs.get(2).name().equals("nest12");
        assert dirs.get(3).name().equals("dir2");
        assert dirs.get(4).name().equals("nest21");
        assert dirs.get(5).name().equals("nest22");
    }

    /**
     * Helper.
     * 
     * @param definition
     * @return
     */
    private Directory define(Consumer<FileSystemDSL> definition) {
        room.with(definition);

        return Location.directory(room.root);
    }
}

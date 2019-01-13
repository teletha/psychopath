/*
 * Copyright (C) 2019 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.location;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.Location;
import psychopath.LocationTestHelper;

class DirectoryTest extends LocationTestHelper {

    @Test
    void name() {
        // locateAbsentDirectory
        assert locateAbsentDirectory("name").name().equals("name");
        assert locateAbsentDirectory("nest/name").name().equals("name");
        assert locateAbsentDirectory("root/nest/name").name().equals("name");

        // absolute
        assert locateAbsoluteAbsentDirectory("name").name().equals("name");
        assert locateAbsoluteAbsentDirectory("nest/name").name().equals("name");
        assert locateAbsoluteAbsentDirectory("root/nest/name").name().equals("name");
    }

    @Test
    void absolutize() {
        // locateAbsentDirectory
        Location locateAbsentDirectory = locateAbsentDirectory("name");
        Location absolute = locateAbsentDirectory.absolutize();
        assert locateAbsentDirectory != absolute;
        assert absolute.isAbsolute();

        // absolute
        locateAbsentDirectory = locateAbsoluteAbsentDirectory("name");
        absolute = locateAbsentDirectory.absolutize();
        assert locateAbsentDirectory == absolute;
        assert absolute.isAbsolute();
    }

    @Test
    void parent() {
        // locateAbsentDirectory
        assert locateAbsentDirectory("a/b").parent().equals(locateAbsentDirectory("a"));
        assert locateAbsentDirectory("a/b/c").parent().equals(locateAbsentDirectory("a/b"));

        // absolute
        assert locateAbsoluteAbsentDirectory("a/b").parent().equals(locateAbsoluteAbsentDirectory("a"));
        assert locateAbsoluteAbsentDirectory("a/b/c").parent().equals(locateAbsoluteAbsentDirectory("a/b"));
    }

    @Test
    void equal() {
        // locateAbsentDirectory
        assert locateAbsentDirectory("a").equals(locateAbsentDirectory("a"));
        assert locateAbsentDirectory("a/b").equals(locateAbsentDirectory("a/b"));
        assert locateAbsentDirectory("../a").equals(locateAbsentDirectory("../a"));

        // absolute
        assert locateAbsoluteAbsentDirectory("a").equals(locateAbsoluteAbsentDirectory("a"));
        assert locateAbsoluteAbsentDirectory("a/b").equals(locateAbsoluteAbsentDirectory("a/b"));
        assert locateAbsoluteAbsentDirectory("../a").equals(locateAbsoluteAbsentDirectory("../a"));
    }

    @Test
    void findDirectorySingle() {
        Directory directory = locateDirectory("dir", $ -> {
            $.dir("dir1");
        });

        assert directory.walkDirectories().toList().size() == 1;
    }

    @Test
    void findDirectoryMultiple() {
        Directory directory = locateDirectory("dir", $ -> {
            $.dir("dir1");
            $.dir("dir2");
            $.dir("dir3");
        });

        assert directory.walkDirectories().toList().size() == 3;
    }

    @Test
    void findDirectoryNest() {
        Directory dir = locateDirectory("dir", $ -> {
            $.dir("dir1", () -> {
                $.dir("nest11");
                $.dir("nest12");
                $.file("fileA1");
                $.file("fileA2");
            });
            $.dir("dir2", () -> {
                $.dir("nest21");
                $.dir("nest22");
                $.file("fileB1");
                $.file("fileB2");
            });
            $.file("fileC");
            $.file("fileC");
        });

        assert dir.walkDirectories().toList().size() == 6;
        assert dir.walkDirectories("*").toList().size() == 2;
        assert dir.walkDirectories("*/*").toList().size() == 4;
        assert dir.walkDirectories("**").toList().size() == 6;
        assert dir.walkDirectories("*", "*/*1").toList().size() == 4;
    }
}

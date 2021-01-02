/*
 * Copyright (C) 2021 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.location;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.FileAlreadyExistsException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
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
    void children() {
        // absent
        assert locateAbsent("a/b").children().toList().isEmpty();
        assert locateAbsoluteAbsent("a/b").children().toList().isEmpty();

        // present
        assert locateDirectory("a/b").children().toList().isEmpty();
        assert locateDirectory("a/b").absolutize().children().toList().isEmpty();

        assert locateDirectory("structure", $ -> {
            $.file("child1");
            $.file("child2");
            $.dir("dir", () -> {
                $.file("item");
                $.dir("nest", () -> {
                    $.file("nest item");
                });
            });
        }).children().toList().size() == 3;
    }

    @Test
    void descendant() {
        // absent
        assert locateAbsent("a/b").descendant().toList().isEmpty();
        assert locateAbsoluteAbsent("a/b").descendant().toList().isEmpty();

        // present
        assert locateDirectory("a/b").descendant().toList().isEmpty();
        assert locateDirectory("a/b").absolutize().descendant().toList().isEmpty();

        assert locateDirectory("structure", $ -> {
            $.file("child1");
            $.file("child2");
            $.dir("dir", () -> {
                $.file("item");
                $.dir("nest", () -> {
                    $.file("nest item");
                });
            });
        }).descendant().toList().size() == 6;
    }

    @Test
    void create() {
        // locateAbsentDirectory
        assert locateAbsentDirectory("a").create().isPresent();
        assert locateAbsentDirectory("a/b").create().isPresent();
        assert locateAbsentDirectory("a/b/c").create().isPresent();

        // absolute
        assert locateAbsoluteAbsentDirectory("b/c").create().isPresent();
        assert locateAbsoluteAbsentDirectory("b/c/d").create().isPresent();
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

        assert directory.walkDirectory().toList().size() == 1;
    }

    @Test
    void findDirectoryMultiple() {
        Directory directory = locateDirectory("dir", $ -> {
            $.dir("dir1");
            $.dir("dir2");
            $.dir("dir3");
        });

        assert directory.walkDirectory().toList().size() == 3;
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

        assert dir.walkDirectory().toList().size() == 6;
        assert dir.walkDirectory("*").toList().size() == 2;
        assert dir.walkDirectory("*/*").toList().size() == 4;
        assert dir.walkDirectory("**").toList().size() == 6;
        assert dir.walkDirectory("*", "*/*1").toList().size() == 4;
    }

    @Test
    void isEmpty() {
        Directory dir = locateDirectory("empty", $ -> {
        });
        assert dir.isEmpty();

        dir = locateDirectory("file", $ -> {
            $.file("child");
        });
        assert dir.isEmpty() == false;

        dir = locateDirectory("dir", $ -> {
            $.dir("child");
        });
        assert dir.isEmpty() == false;
    }

    @Test
    void moveUp() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("in", () -> {
                $.dir("dir");
            });
        });

        File dir = root.file("in/dir");
        File up = root.file("dir");
        assert dir.isPresent();
        assert up.isAbsent();

        File uped = dir.moveUp();
        assert dir.isAbsent();
        assert up.isPresent();
        assert uped.equals(up);
    }

    @Test
    void renameTo() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("src");
        });

        Directory source = root.directory("src");
        Directory destination = root.directory("dest");
        assert source.isPresent();
        assert destination.isAbsent();

        Directory renamed = source.renameTo("dest");
        assert source.isAbsent();
        assert destination.isPresent();
        assert destination.equals(renamed);
        assert destination != renamed;
    }

    @Test
    void renameToSameName() {
        Directory source = locateDirectory("src");
        Directory renamed = source.renameTo("src");
        assert source == renamed;
    }

    @Test
    void renameToNull() {
        Assertions.assertThrows(NullPointerException.class, () -> locateDirectory("src").renameTo(null));
    }

    @Test
    void renameToExistingType() {
        Directory dir = locateDirectory("root", $ -> {
            $.dir("src");
            $.file("dest-file");
            $.dir("dest-dir");
        });
        assertThrows(FileAlreadyExistsException.class, () -> dir.directory("src").renameTo("dest-file"));
        assertThrows(FileAlreadyExistsException.class, () -> dir.directory("src").renameTo("dest-dir"));
    }
}
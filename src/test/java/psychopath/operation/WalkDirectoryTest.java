/*
 * Copyright (C) 2024 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.operation;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.Folder;
import psychopath.LocationTestHelper;
import psychopath.Locator;

class WalkDirectoryTest extends LocationTestHelper {

    @Test
    void file() {
        File test = locateFile("test");
        assert test.walkDirectory().toList().size() == 0;
        assert test.walkDirectory("test").toList().size() == 0;
        assert test.walkDirectory("*").toList().size() == 0;
        assert test.walkDirectory("**").toList().size() == 0;
        assert test.walkDirectory("not").toList().size() == 0;
        assert test.walkDirectory((String) null).toList().size() == 0;
        assert test.walkDirectory((String[]) null).toList().size() == 0;
    }

    @Test
    void directory() {
        Directory test = locateDirectory("root", $ -> {
            $.file("file");
            $.dir("dir1");
            $.dir("dir2", () -> {
                $.file("file");
                $.dir("dir3");
            });
        });
        assert test.walkDirectory().toList().size() == 3;
        assert test.walkDirectory("dir*").toList().size() == 2;
        assert test.walkDirectory("*").toList().size() == 2;
        assert test.walkDirectory("**").toList().size() == 3;
        assert test.walkDirectory("not").toList().size() == 0;
        assert test.walkDirectory((String) null).toList().size() == 3;
        assert test.walkDirectory((String[]) null).toList().size() == 3;
    }

    @Test
    void folder() {
        Folder test = Locator.folder() //
                .add(locateDirectory("root directory is NOT mounted"))
                .add(locateDirectory("deep root directory/is NOT mounted too"))
                .add(locateDirectory("root", $ -> {
                    $.file("file");
                    $.dir("dir1");
                    $.dir("dir2", () -> {
                        $.file("file");
                        $.dir("dir3");
                    });
                }));
        assert test.walkDirectory().toList().size() == 3;
        assert test.walkDirectory("dir*").toList().size() == 2;
        assert test.walkDirectory("*").toList().size() == 2;
        assert test.walkDirectory("**").toList().size() == 3;
        assert test.walkDirectory("not").toList().size() == 0;
        assert test.walkDirectory((String) null).toList().size() == 3;
        assert test.walkDirectory((String[]) null).toList().size() == 3;
    }
}
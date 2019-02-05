/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.operatable;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.Folder;
import psychopath.LocationTestHelper;
import psychopath.Locator;

class WalkFileTest extends LocationTestHelper {

    @Test
    void file() {
        File test = locateFile("test");
        assert test.walkFile().toList().size() == 1;
        assert test.walkFile("test").toList().size() == 1;
        assert test.walkFile("*").toList().size() == 1;
        assert test.walkFile("**").toList().size() == 1;
        assert test.walkFile("not").toList().size() == 0;
        assert test.walkFile((String) null).toList().size() == 1;
        assert test.walkFile((String[]) null).toList().size() == 1;
    }

    @Test
    void directory() {
        Directory test = locateDirectory("test", $ -> {
            $.file("file1");
            $.file("file2");
            $.dir("dir", () -> {
                $.file("file3");
            });
        });
        assert test.walkFile().toList().size() == 3;
        assert test.walkFile("file*").toList().size() == 2;
        assert test.walkFile("*").toList().size() == 2;
        assert test.walkFile("**").toList().size() == 3;
        assert test.walkFile("not").toList().size() == 0;
        assert test.walkFile((String) null).toList().size() == 3;
        assert test.walkFile((String[]) null).toList().size() == 3;
    }

    @Test
    void folder() {
        Folder test = Locator.folder() //
                .add(locateFile("file1"))
                .add(locateFile("deep/file2"))
                .add(locateDirectory("first", $ -> {
                    $.file("file3");
                    $.file("file4");
                    $.dir("dir", () -> {
                        $.file("file5");
                    });
                }));
        assert test.walkFile().toList().size() == 5;
        assert test.walkFile("file*").toList().size() == 4;
        assert test.walkFile("*").toList().size() == 4;
        assert test.walkFile("**").toList().size() == 5;
        assert test.walkFile("not").toList().size() == 0;
        assert test.walkFile((String) null).toList().size() == 5;
        assert test.walkFile((String[]) null).toList().size() == 5;
    }
}

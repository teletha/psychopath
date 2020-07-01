/*
 * Copyright (C) 2020 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.archive;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.LocationTestHelper;
import psychopath.Locator;

class JarTest extends LocationTestHelper {

    private String ext = "jar";

    @Test
    void packAndUnpack() {
        Directory dir = locateDirectory("root", $ -> {
            $.file("file", "text");
            $.dir("inside", () -> {
                $.file("1", "1");
                $.file("2", "22");
                $.file("3", "333");
            });
        });

        File file = locateFile("root." + ext);
        Locator.folder().add(dir).packTo(file);

        assert match(file.unpackToTemporary(), $ -> {
            $.dir("root", () -> {
                $.file("file", "text");
                $.dir("inside", () -> {
                    $.file("1", "1");
                    $.file("2", "22");
                    $.file("3", "333");
                });
            });
        });
    }

    @Test
    void nonAscii() {
        Directory dir = locateDirectory("root", $ -> {
            $.dir("るーと", () -> {
                $.file("ファイルⅰ", "ⅰ");
                $.file("ファイル②", "②");
                $.file("ファイル参", "参");
            });
        });

        File file = locateFile("root." + ext);
        Locator.folder().add(dir).packTo(file);

        assert match(file.unpackToTemporary(), $ -> {
            $.dir("root", () -> {
                $.dir("るーと", () -> {
                    $.file("ファイルⅰ", "ⅰ");
                    $.file("ファイル②", "②");
                    $.file("ファイル参", "参");
                });
            });
        });
    }
}
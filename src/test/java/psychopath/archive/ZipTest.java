/*
 * Copyright (C) 2023 The PSYCHOPATH Development Team
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

class ZipTest extends LocationTestHelper {

    private String ext = "zip";

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

    @Test
    void unpack() {
        File archive = Locator.file("src/test/resources/root." + ext);
        assert archive.isPresent();
        assert match(archive.unpackToTemporary(), $ -> {
            $.dir("root", () -> {
                $.file("1.txt", "1");
                $.file("2.txt", "2");
                $.file("3.txt", "3");
                $.dir("sub", () -> {
                    $.file("non-ascii1.txt", "①");
                    $.file("non-ascii2.txt", "Ⅱ");
                });
            });
        });
    }

    @Test
    void unpackPattern() {
        File archive = Locator.file("src/test/resources/root." + ext);
        assert archive.isPresent();
        assert match(archive.unpackToTemporary("root/*.txt"), $ -> {
            $.dir("root", () -> {
                $.file("1.txt", "1");
                $.file("2.txt", "2");
                $.file("3.txt", "3");
            });
        });
    }
}
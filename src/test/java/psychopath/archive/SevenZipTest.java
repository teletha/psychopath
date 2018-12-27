/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.archive;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.LocationTestHelper;
import psychopath.Locator;

public class SevenZipTest extends LocationTestHelper {

    private String ext = "7z";

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
            $.file("file", "text");
            $.dir("inside", () -> {
                $.file("1", "1");
                $.file("2", "22");
                $.file("3", "333");
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
            $.dir("るーと", () -> {
                $.file("ファイルⅰ", "ⅰ");
                $.file("ファイル②", "②");
                $.file("ファイル参", "参");
            });
        });
    }
}

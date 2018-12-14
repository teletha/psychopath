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

import static psychopath.UnpackOption.StripSingleDirectory;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.LocationTestHelper;
import psychopath.Locator;

public class SevenZipTest extends LocationTestHelper {

    private String ext = "lz4.7z";

    @Test
    void pack() {
        Directory dir = locateDirectory("root", $ -> {
            $.file("file");
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });

        File file = locateFile("root." + ext);
        Locator.archive(file).add(dir).pack();

        assert match(file.unpack(), $ -> {
            $.file("file");
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });
    }

    @Test
    void unpack() {
        File zip = locateArchive("test." + ext, $ -> {
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });

        assert match(zip.unpack(), $ -> {
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });
    }

    @Test
    void unpackNonAscii() {
        File zip = locateArchive("test." + ext, $ -> {
            $.dir("るーと", () -> {
                $.file("ファイルⅰ");
                $.file("ファイル②");
                $.file("ファイル参");
            });
        });

        assert match(zip.unpack(), $ -> {
            $.dir("るーと", () -> {
                $.file("ファイルⅰ");
                $.file("ファイル②");
                $.file("ファイル参");
            });
        });
    }

    @Test
    void stripSingleDirectory() {
        File zip = locateArchive("test." + ext, $ -> {
            $.dir("inside", () -> {
                $.file("1");
                $.file("2");
                $.file("3");
            });
        });

        assert match(zip.unpack(StripSingleDirectory), $ -> {
            $.file("1");
            $.file("2");
            $.file("3");
        });
    }

    @Test
    void stripSingleDirectoryWithNest() {
        File zip = locateArchive("test." + ext, $ -> {
            $.dir("inside", () -> {
                $.dir("nest", () -> {
                    $.file("1");
                    $.file("2");
                    $.dir("dir", () -> {
                        $.file("child");
                        $.file("item");
                    });
                });
            });
        });

        assert match(zip.unpack(StripSingleDirectory), $ -> {
            $.file("1");
            $.file("2");
            $.dir("dir", () -> {
                $.file("child");
                $.file("item");
            });
        });
    }
}

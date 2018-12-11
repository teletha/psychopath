/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import static psychopath.UnpackOption.StripSingleDirectory;

import org.junit.jupiter.api.Test;

class UnpackTest extends LocationTestHelper {

    @Test
    void unpack() {
        File zip = locateArchive("test.zip", $ -> {
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
    void stripSingleDirectory() {
        File zip = locateArchive("test.zip", $ -> {
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
        File zip = locateArchive("test.zip", $ -> {
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

    @Test
    void nonAscii() {
        File zip = locateArchive("test.zip", $ -> {
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
}

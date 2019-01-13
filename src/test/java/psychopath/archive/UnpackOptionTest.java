/*
 * Copyright (C) 2019 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.archive;

import static psychopath.UnpackOptions.*;

import org.junit.jupiter.api.Test;

import psychopath.File;
import psychopath.LocationTestHelper;

class UnpackOptionTest extends LocationTestHelper {

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
}

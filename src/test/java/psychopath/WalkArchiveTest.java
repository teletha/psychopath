/*
 * Copyright (C) 2019 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import org.junit.jupiter.api.Test;

import antibug.powerassert.PowerAssertOff;

class WalkArchiveTest extends LocationTestHelper {

    @Test
    @PowerAssertOff
    public void zip() {
        File zip = locateArchive("test.zip", $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("dir1", () -> {
                $.file("text1");
                $.file("text2");
            });
            $.dir("dir2", () -> {
                $.file("text1");
                $.file("text2");
            });
        });

        assert zip.asArchive().walkFile().toList().size() == 6;
        assert zip.asArchive().walkFile("*").toList().size() == 2;
        assert zip.asArchive().walkFile("!dir1/**").toList().size() == 4;
        assert zip.asArchive().walkDirectory().toList().size() == 2;
    }
}

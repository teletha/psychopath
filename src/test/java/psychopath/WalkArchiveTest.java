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

import org.junit.jupiter.api.Test;

class WalkArchiveTest extends LocationTestHelper {

    @Test
    public void zip() {
        Directory root = locateArchive("test.zip", $ -> {
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

        assert root.walkFiles().toList().size() == 6;
        assert root.walkFiles("*").toList().size() == 2;
        assert root.walkFiles("!dir1/**").toList().size() == 4;
        assert root.walkDirectories().toList().size() == 2;
    }
}

/*
 * Copyright (C) 2023 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import org.junit.jupiter.api.Test;

class UnpackTest extends LocationTestHelper {

    @Test
    void unpackToDirectoryWithSync() {
        File in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "override");
            $.file("2.txt", "not match");
        });

        Directory out = locateDirectory("Out", $ -> {
            $.file("1.txt", "This text will be overridden.");
            $.file("3.txt", "This file will be deleted.");
        });

        in.unpackTo(out, o -> o.sync());

        assert out.file("1.txt").isPresent();
        assert out.file("2.txt").isPresent();
        assert out.file("3.txt").isAbsent();
    }

    @Test
    void unpackToDirectoryWithSyncPattern() {
        File in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "override");
            $.file("2.txt", "not match");
        });

        Directory out = locateDirectory("Out", $ -> {
            $.file("1.txt", "This text will be overridden.");
            $.file("3.txt", "This file will be deleted.");
        });

        in.unpackTo(out, o -> o.sync().glob("1.txt"));

        assert out.file("1.txt").isPresent();
        assert out.file("2.txt").isAbsent();
        assert out.file("3.txt").isPresent();
    }
}
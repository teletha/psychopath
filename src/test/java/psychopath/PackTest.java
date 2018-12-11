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

class PackTest extends LocationTestHelper {

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

        File file = locateFile("test.zip");
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
}

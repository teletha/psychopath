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

import psychopath.File;
import psychopath.LocationTestHelper;
import psychopath.Locator;

class RarTest extends LocationTestHelper {

    @Test
    void unpack() {
        File archive = Locator.file("src/test/resources/root.rar");
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
}

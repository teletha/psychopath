/*
 * Copyright (C) 2022 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import psychopath.File;
import psychopath.LocationTestHelper;
import psychopath.Locator;

@EnabledOnOs(OS.WINDOWS)
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

    @Test
    void unpackPattern() {
        File archive = Locator.file("src/test/resources/root.rar");
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
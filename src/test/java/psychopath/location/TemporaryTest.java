/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.location;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.LocationTestHelper;
import psychopath.Locator;
import psychopath.Temporary;

class TemporaryTest extends LocationTestHelper {

    @Test
    void moveTo() {
        Temporary temporary = Locator.temporary();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        Directory destination = temporary.add(dir1).add(dir2).moveTo(locateDirectory("dir3"));

        assert dir1.isAbsent();
        assert dir2.isAbsent();
        assert match(destination, $ -> {
            $.dir("dir1", () -> {
                $.file("1.txt");
                $.file("2.txt");
                $.dir("3", () -> {
                    $.file("3.txt");
                });
            });
            $.dir("dir2", () -> {
                $.file("a.txt");
                $.file("b.txt");
                $.dir("c", () -> {
                    $.file("c.txt");
                });
            });
        });
    }

    @Test
    void copyTo() {
        Temporary temporary = Locator.temporary();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        Directory destination = temporary.add(dir1).add(dir2).copyTo(locateDirectory("dir3"));

        assert dir1.isPresent();
        assert dir2.isPresent();
        assert match(destination, $ -> {
            $.dir("dir1", () -> {
                $.file("1.txt");
                $.file("2.txt");
                $.dir("3", () -> {
                    $.file("3.txt");
                });
            });
            $.dir("dir2", () -> {
                $.file("a.txt");
                $.file("b.txt");
                $.dir("c", () -> {
                    $.file("c.txt");
                });
            });
        });
    }
}

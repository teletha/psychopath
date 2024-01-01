/*
 * Copyright (C) 2024 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import org.junit.jupiter.api.Test;

class WalkFileTest extends LocationTestHelper {

    @Test
    void topLevelWildcard() {
        Directory root = locateDirectory("root", $ -> {
            $.file("file");
            $.file("text");
            $.dir("dir", () -> {
                $.file("file");
                $.file("text");
            });
        });

        assert root.walkFile("*").toList().size() == 2;
        assert root.walkFile("*/text").toList().size() == 1;
        assert root.walkFile("*", "*/text").toList().size() == 3;
    }

    @Test
    void secondLevelWildcard() {
        Directory root = locateDirectory("root", $ -> {
            $.file("file");
            $.dir("dir1", () -> {
                $.file("file1");
                $.file("file2");
                $.file("text1");
            });
            $.dir("dir2", () -> {
                $.file("file1");
                $.file("file2");
                $.file("text1");
            });
        });

        assert root.walkFile("*/*").toList().size() == 6;
        assert root.walkFile("*/file*").toList().size() == 4;
        assert root.walkFile("*/*1").toList().size() == 4;
    }

    @Test
    void deepWildcard() {
        Directory directory = locateDirectory("root", $ -> {
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

        assert directory.walkFile("**").toList().size() == 6;
        assert directory.walkFile("dir2/**").toList().size() == 2;
    }

    @Test
    void character() {
        Directory root = locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("dir", () -> {
                $.file("text1");
                $.file("text2");
            });
        });

        assert root.walkFile("text?").toList().size() == 2;
        assert root.walkFile("????1").toList().size() == 1;
        assert root.walkFile("**text?").toList().size() == 4;
    }

    @Test
    void range() {
        Directory root = locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.file("text3");
            $.file("text4");
        });

        assert root.walkFile("text[1-2]").toList().size() == 2;
        assert root.walkFile("text[2-5]").toList().size() == 3;
    }

    @Test
    void negate() {
        Directory root = locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.file("text3");
            $.file("text4");
        });

        assert root.walkFile("text[!3]").toList().size() == 3;
        assert root.walkFile("text[!34]").toList().size() == 2;
    }

    @Test
    void exclude() {
        Directory root = locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.file("text3");
            $.file("text4");
        });
        assert root.walkFile("**", "!text1").toList().size() == 3;
    }

    @Test
    void excludeDirectory() {
        Directory root = locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.file("text3");
            $.file("text4");

            $.dir("ignore", () -> {
                $.file("text1");
                $.file("text2");
                $.file("text3");
                $.file("text4");
            });
        });
        assert root.walkFile("**", "!ignore/**").toList().size() == 4;
    }

    @Test
    void multiple() {
        Directory root = locateDirectory("root", $ -> {
            $.file("text1");
            $.file("text2");
            $.file("text3");
            $.file("text4");
        });
        assert root.walkFile("**", "!**1", "!**3").toList().size() == 2;
    }
}
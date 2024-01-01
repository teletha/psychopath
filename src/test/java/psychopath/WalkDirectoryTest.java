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

class WalkDirectoryTest extends LocationTestHelper {

    @Test
    void topLevelWildcard() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("file");
            $.dir("text");
            $.dir("dir", () -> {
                $.dir("file");
                $.dir("text");
            });
        });

        assert root.walkDirectory("*").toList().size() == 3;
        assert root.walkDirectory("*/text").toList().size() == 1;
        assert root.walkDirectory("*", "*/text").toList().size() == 4;
    }

    @Test
    void secondLevelWildcard() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("file");
            $.dir("dir1", () -> {
                $.dir("file1");
                $.dir("file2");
                $.dir("text1");
            });
            $.dir("dir2", () -> {
                $.dir("file1");
                $.dir("file2");
                $.dir("text1");
            });
        });

        assert root.walkDirectory("*/*").toList().size() == 6;
        assert root.walkDirectory("*/file*").toList().size() == 4;
        assert root.walkDirectory("*/*1").toList().size() == 4;
    }

    @Test
    void deepWildcard() {
        Directory directory = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("dir1", () -> {
                $.dir("text1");
                $.dir("text2");
            });
            $.dir("dir2", () -> {
                $.dir("text1");
                $.dir("text2");
            });
        });

        assert directory.walkDirectory("**").toList().size() == 8;
        assert directory.walkDirectory("dir2/**").toList().size() == 2;
    }

    @Test
    void character() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("dir", () -> {
                $.dir("text1");
                $.dir("text2");
            });
        });

        assert root.walkDirectory("text?").toList().size() == 2;
        assert root.walkDirectory("????1").toList().size() == 1;
        assert root.walkDirectory("**text?").toList().size() == 4;
    }

    @Test
    void range() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("text3");
            $.dir("text4");
        });

        assert root.walkDirectory("text[1-2]").toList().size() == 2;
        assert root.walkDirectory("text[2-5]").toList().size() == 3;
    }

    @Test
    void negate() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("text3");
            $.dir("text4");
        });

        assert root.walkDirectory("text[!3]").toList().size() == 3;
        assert root.walkDirectory("text[!34]").toList().size() == 2;
    }

    @Test
    void exclude() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("text3");
            $.dir("text4");
        });
        assert root.walkDirectory("**", "!text1").toList().size() == 3;
    }

    @Test
    void excludeDirectory() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("text3");
            $.dir("text4");

            $.dir("ignore", () -> {
                $.dir("text1");
                $.dir("text2");
                $.dir("text3");
                $.dir("text4");
            });
        });
        assert root.walkDirectory("**", "!ignore/**").toList().size() == 4;
    }

    @Test
    void multiple() {
        Directory root = locateDirectory("root", $ -> {
            $.dir("text1");
            $.dir("text2");
            $.dir("text3");
            $.dir("text4");
        });
        assert root.walkDirectory("**", "!**1", "!**3").toList().size() == 2;
    }
}
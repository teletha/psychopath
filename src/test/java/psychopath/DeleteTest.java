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

class DeleteTest extends LocationTestHelper {

    @Test
    void file() {
        File file = locateFile("file");
        assert file.isPresent();
        file.deleteNow();
        assert file.isAbsent();
    }

    @Test
    void absentFile() {
        File file = locateAbsent("file");
        assert file.isAbsent();
        file.deleteNow();
        assert file.isAbsent();
    }

    @Test
    void directoryEmpty() {
        Directory directory = locateDirectory("dir");

        assert directory.isPresent();
        directory.deleteNow();
        assert directory.isAbsent();
    }

    @Test
    void directoryWithFile() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("text1");
            $.file("text2");
            $.file("text3");
        });

        assert directory.isPresent();
        directory.deleteNow();
        assert directory.isAbsent();
    }

    @Test
    void directoryWithFileAndDirectory() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("empty-dir");
            $.dir("dir", () -> {
                $.file("nest1");
                $.file("nest2");
            });
        });

        assert directory.isPresent();
        directory.deleteNow();
        assert directory.isAbsent();
    }

    @Test
    void absentDirectory() {
        Directory directory = locateAbsentDirectory("dir");
        assert directory.isAbsent();
        directory.deleteNow();
        assert directory.isAbsent();
    }

    @Test
    void pattern() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("dir", () -> {
                $.file("nest1");
                $.file("nest2");
            });
        });

        directory.deleteNow("text1");

        assert match(directory, $ -> {
            $.file("text2");
            $.dir("dir", () -> {
                $.file("nest1");
                $.file("nest2");
            });
        });
    }

    @Test
    void patternChildren() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });

        directory.deleteNow("*");

        assert directory.isPresent();
        assert match(directory, $ -> {
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });
    }

    @Test
    void patternChildFiles() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });

        directory.deleteNow("*.txt");

        assert directory.isPresent();
        assert match(directory, $ -> {
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });
    }

    @Test
    void patternChildDirectory() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });

        directory.deleteNow("dir/**");

        assert directory.isPresent();
        assert match(directory, $ -> {
            $.file("1.txt");
            $.file("2.txt");
        });
    }

    @Test
    void patternDecendant() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });

        directory.deleteNow(o -> o.glob("**").strip());

        assert directory.isPresent();
        assert match(directory, $ -> {
        });
    }

    @Test
    void patternDecendantFiles() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });

        directory.deleteNow("**.txt");

        assert directory.isAbsent();
    }

    @Test
    void patternDecendantDirectories() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir1", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
            $.dir("dir2", () -> {
                $.dir("dir21", () -> {
                    $.file("file");
                });
                $.dir("dir22", () -> {
                    $.file("file");
                });
            });
        });

        directory.deleteNow("**/dir**");

        assert match(directory, $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("dir1", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });
    }
}

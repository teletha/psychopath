/*
 * Copyright (C) 2025 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import antibug.powerassert.PowerAssertOff;

class MoveTest extends LocationTestHelper {

    @Test
    void absentToFile() {
        File in = locateAbsent("absent");
        File out = locateFile("out");

        in.moveTo(out);

        assert in.isAbsent();
        assert out.isPresent();
    }

    @Test
    void absentToDirectory() {
        File in = locateAbsent("absent");
        Directory out = locateDirectory("out");

        in.moveTo(out);

        assert in.isAbsent();
        assert out.file("absent").isAbsent();
    }

    @Test
    void absentToAbsent() {
        File in = locateAbsent("absent");
        File out = locateAbsent("out");

        in.moveTo(out);

        assert in.isAbsent();
        assert out.isAbsent();
    }

    @Test
    void fileToFile() {
        File in = locateFile("In", "Success");
        File out = locateFile("Out", "This text will be overwritten by input file.");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out, "Success");
    }

    @Test
    void fileToFileWithSameTimeStamp() {
        Instant now = Instant.now();
        File in = locateFile("In", now, "Success");
        File out = locateFile("Out", now, "This text will be overwritten by input file.");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out, now, "Success");
    }

    @Test
    void fileToFileWithDifferentTimeStamp() {
        Instant now = Instant.now();
        File in = locateFile("In", now, "Success");
        File out = locateFile("Out", now.plusSeconds(10), "This text will be overwritten by input file.");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out, now, "Success");
    }

    @Test
    void fileToFileOptionReplaceExisting() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        File out = locateFile("out", "new").lastModifiedTime(20);

        in.moveTo(out, o -> o.replaceExisting());
        assert out.text().equals("update");

        in = locateFile("file", "update").lastModifiedTime(10);
        out = locateFile("out", "old").lastModifiedTime(0);

        in.moveTo(out, o -> o.replaceExisting());
        assert out.text().equals("update");
    }

    @Test
    void fileToFileOptionReplaceOld() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        File out = locateFile("out", "new").lastModifiedTime(20);

        in.moveTo(out, o -> o.replaceOld());
        assert out.text().equals("new");

        in = locateFile("file", "update").lastModifiedTime(10);
        out = locateFile("out", "old").lastModifiedTime(0);

        in.moveTo(out, o -> o.replaceOld());
        assert out.text().equals("update");
    }

    @Test
    void fileToFileOptionSkip() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        File out = locateFile("out", "new").lastModifiedTime(20);

        in.moveTo(out, o -> o.skipExisting());
        assert out.text().equals("new");

        in = locateFile("file", "update").lastModifiedTime(10);
        out = locateFile("out", "old").lastModifiedTime(0);

        in.moveTo(out, o -> o.skipExisting());
        assert out.text().equals("old");
    }

    @Test
    void fileToAbsent() {
        File in = locateFile("In", "Success");
        File out = locateAbsent("Out");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out, "Success");
    }

    @Test
    void fileToDeepAbsent() {
        File in = locateFile("In", "Success");
        File out = locateAbsent("1/2/3");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out, "Success");
    }

    @Test
    void fileToDirectory() {
        File in = locateFile("In", "Success");
        Directory out = locateDirectory("Out");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out.file("In"), "Success");
    }

    @Test
    void fileToDirectoryOptionReplaceExisting() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        Directory out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "new")).lastModifiedTime(20);
        });

        in.moveTo(out, o -> o.replaceExisting());
        assert out.file("file").text().equals("update");

        in = locateFile("file", "update").lastModifiedTime(10);
        out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "old")).lastModifiedTime(0);
        });

        in.moveTo(out, o -> o.replaceExisting());
        assert out.file("file").text().equals("update");
    }

    @Test
    void fileToDirectoryOptionReplaceOld() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        Directory out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "new")).lastModifiedTime(20);
        });

        in.moveTo(out, o -> o.replaceOld());
        assert out.file("file").text().trim().equals("new");

        in = locateFile("file", "update").lastModifiedTime(10);
        out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "old")).lastModifiedTime(0);
        });

        in.moveTo(out, o -> o.replaceOld());
        assert out.file("file").text().trim().equals("update");
    }

    @Test
    void fileToDirectoryOptionSkip() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        Directory out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "new")).lastModifiedTime(20);
        });

        in.moveTo(out, o -> o.skipExisting());
        assert out.file("file").text().trim().equals("new");

        in = locateFile("file", "update").lastModifiedTime(10);
        out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "old")).lastModifiedTime(0);
        });

        in.moveTo(out, o -> o.skipExisting());
        assert out.file("file").text().trim().equals("old");
    }

    @Test
    void directoryToDirectory() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Directory out = locateDirectory("Out", $ -> {
            $.file("1", "This text will be remaining.");
        });

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out, $ -> {
            $.file("1", "This text will be remaining.");
            $.dir("In", () -> {
                $.file("1", "One");
            });
        });
    }

    @Test
    void directoryToDirectoryWithPattern() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Directory out = locateDirectory("Out", $ -> {
            $.file("1", "This text will be overwritten by input file.");
        });

        in.moveTo(out, o -> o.glob("**").strip());

        assert match(out, $ -> {
            $.file("1", "One");
        });
    }

    @Test
    void directoryToDirectoryWithFilter() {
        Directory in = locateDirectory("In", $ -> {
            $.file("file");
            $.file("text");
            $.dir("dir", () -> {
                $.file("file");
                $.file("text");
            });
        });
        Directory out = locateDirectory("Out");

        in.moveTo(out, o -> o.take((file, attr) -> file.getFileName().startsWith("file")));

        assert in.isPresent();
        assert match(out.directory("In"), $ -> {
            $.file("file");
            $.dir("dir", () -> {
                $.file("file");
            });
        });
    }

    @Test
    void directoryToDirectoryOptionReplaceExisting() {
        Directory in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        Directory out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "new")).lastModifiedTime(20);
        });

        in.moveTo(out, o -> o.replaceExisting().strip());
        assert out.file("file").text().trim().equals("update");

        in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "old")).lastModifiedTime(0);
        });

        in.moveTo(out, o -> o.replaceExisting().strip());
        assert out.file("file").text().trim().equals("update");
    }

    @Test
    void directoryToDirectoryOptionReplaceOld() {
        Directory in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        Directory out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "new")).lastModifiedTime(20);
        });

        in.moveTo(out, o -> o.replaceOld().strip());
        assert out.file("file").text().trim().equals("new");

        in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "old")).lastModifiedTime(0);
        });

        in.moveTo(out, o -> o.replaceOld().strip());
        assert out.file("file").text().trim().equals("update");
    }

    @Test
    void directoryToDirectoryOptionSkip() {
        Directory in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        Directory out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "new")).lastModifiedTime(20);
        });

        in.moveTo(out, o -> o.skipExisting().strip());
        assert out.file("file").text().trim().equals("new");

        in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "old")).lastModifiedTime(0);
        });

        in.moveTo(out, o -> o.skipExisting().strip());
        assert out.file("file").text().trim().equals("old");
    }

    @Test
    void directoryToDirectoryOptionSync() {
        Directory in = locateDirectory("In", $ -> {
            $.file("create");
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        Directory out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "new")).lastModifiedTime(20);
            $.file("delete", "This file will be deleted");
        });

        in.moveTo(out, o -> o.sync().strip());
        assert out.file("file").isPresent();
        assert out.file("create").isPresent();
        assert out.file("delete").isAbsent();
    }

    @Test
    void directoryToAbsent() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Directory out = locateAbsentDirectory("Out");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out.directory("In"), $ -> {
            $.file("1", "One");
        });
    }

    @Test
    void directoryToDeepAbsent() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Directory out = locateAbsentDirectory("1/2/3");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out.directory("In"), $ -> {
            $.file("1", "One");
        });
    }

    @Test
    void children() {
        Directory in = locateDirectory("In", $ -> {
            $.file("file");
            $.file("text");
            $.dir("dir", () -> {
                $.file("file");
                $.file("text");
            });
            $.dir("empty");
        });
        Directory out = locateDirectory("Out");

        in.moveTo(out, o -> o.glob("*").strip());

        assert in.isPresent();
        assert match(out, $ -> {
            $.file("file");
            $.file("text");
        });
    }

    @Test
    void descendant() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
            $.file("2", "Two");
            $.dir("dir", () -> {
                $.file("nest");
            });
        });
        Directory out = locateDirectory("Out");

        in.moveTo(out, o -> o.glob("**").strip());

        assert in.isPresent();
        assert match(out, $ -> {
            $.file("1", "One");
            $.file("2", "Two");
            $.dir("dir", () -> {
                $.file("nest");
            });
        });
    }

    @Test
    void archiveToDirectory() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1", "override");
        }).asArchive();

        Directory out = locateDirectory("Out", $ -> {
            $.file("1", "This text will be overridden.");
        });

        in.moveTo(out);

        assert match(out, $ -> {
            $.file("1", "override");
        });
    }

    @Test
    @PowerAssertOff
    void archiveToDirectoryWithPattern() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "override");
            $.file("2.txt", "not match");
        }).asArchive();

        Directory out = locateDirectory("Out", $ -> {
            $.file("1.txt", "This text will be overridden.");
        });

        in.moveTo(out, "1.*");

        assert match(out, $ -> {
            $.file("1.txt", "override");
        });
    }

    @Test
    void archiveToAbsent() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "ok");
        }).asArchive();

        Directory out = locateAbsentDirectory("Out");

        in.moveTo(out, "1.*");

        assert match(out, $ -> {
            $.file("1.txt", "ok");
        });
    }
}
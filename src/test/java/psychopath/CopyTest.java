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

import java.time.Instant;

import org.junit.jupiter.api.Test;

class CopyTest extends LocationTestHelper {

    @Test
    void absentToFile() {
        File in = locateAbsent("absent");
        File out = locateFile("out");

        in.copyTo(out);

        assert in.isAbsent();
        assert out.isPresent();
    }

    @Test
    void absentToDirectory() {
        File in = locateAbsent("absent");
        Directory out = locateDirectory("out");

        in.copyTo(out);

        assert in.isAbsent();
        assert out.file("absent").isAbsent();
    }

    @Test
    void absentToAbsent() {
        File in = locateAbsent("absent");
        File out = locateAbsent("out");

        in.copyTo(out);

        assert in.isAbsent();
        assert out.isAbsent();
    }

    @Test
    void fileToFile() {
        File in = locateFile("In", "Success");
        File out = locateFile("Out", "This text will be overwritten by input file.");

        in.copyTo(out);

        assert sameFile(in, out);
    }

    @Test
    void fileToFileWithSameTimeStamp() {
        Instant now = Instant.now();
        File in = locateFile("In", now, "Success");
        File out = locateFile("Out", now, "This text will be overwritten by input file.");

        in.copyTo(out);

        assert sameFile(in, out);
    }

    @Test
    void fileToFileWithDifferentTimeStamp() {
        Instant now = Instant.now();
        File in = locateFile("In", now, "Success");
        File out = locateFile("Out", now.plusSeconds(10), "This text will be overwritten by input file.");

        in.copyTo(out);

        assert sameFile(in, out);
    }

    @Test
    void fileToFileOptionReplaceExisting() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        File out = locateFile("out", "new").lastModifiedTime(20);

        in.copyTo(out, o -> o.replaceExisting());
        assert out.text().equals("update");

        out = locateFile("out", "old").lastModifiedTime(0);
        in.copyTo(out, o -> o.replaceExisting());
        assert out.text().equals("update");
    }

    @Test
    void fileToFileOptionReplaceOld() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        File out = locateFile("out", "new").lastModifiedTime(20);

        in.copyTo(out, o -> o.replaceOld());
        assert out.text().equals("new");

        out = locateFile("out", "old").lastModifiedTime(0);
        in.copyTo(out, o -> o.replaceOld());
        assert out.text().equals("update");
    }

    @Test
    void fileToFileOptionSkip() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        File out = locateFile("out", "new").lastModifiedTime(20);

        in.copyTo(out, o -> o.skipExisting());
        assert out.text().equals("new");

        out = locateFile("out", "old").lastModifiedTime(0);
        in.copyTo(out, o -> o.skipExisting());
        assert out.text().equals("old");
    }

    @Test
    void fileToAbsent() {
        File in = locateFile("In", "Success");
        File out = locateAbsent("Out");

        in.copyTo(out);

        assert sameFile(in, out);
    }

    @Test
    void fileToDeepAbsent() {
        File in = locateFile("In", "Success");
        File out = locateAbsent("1/2/3");

        in.copyTo(out);

        assert sameFile(in, out);
    }

    @Test
    void fileToDirectory() {
        File in = locateFile("In", "Success");
        Directory out = locateDirectory("Out");

        in.copyTo(out);

        assert sameFile(in, out.file("In"));
    }

    @Test
    void fileToDirectoryOptionReplaceExisting() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        Directory out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "new")).lastModifiedTime(20);
        });
        in.copyTo(out, o -> o.replaceExisting());
        assert out.file("file").text().equals("update");

        out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "old")).lastModifiedTime(0);
        });
        in.copyTo(out, o -> o.replaceExisting());
        assert out.file("file").text().equals("update");
    }

    @Test
    void fileToDirectoryOptionReplaceOld() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        Directory out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "new")).lastModifiedTime(20);
        });
        in.copyTo(out, o -> o.replaceOld());
        assert out.file("file").text().trim().equals("new");

        out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "old")).lastModifiedTime(0);
        });
        in.copyTo(out, o -> o.replaceOld());
        assert out.file("file").text().trim().equals("update");
    }

    @Test
    void fileToDirectoryOptionSkip() {
        File in = locateFile("file", "update").lastModifiedTime(10);
        Directory out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "new")).lastModifiedTime(20);
        });
        in.copyTo(out, o -> o.skipExisting());
        assert out.file("file").text().trim().equals("new");

        out = locateDirectory("out", dir -> {
            Locator.file(dir.file("file", "old")).lastModifiedTime(0);
        });
        in.copyTo(out, o -> o.skipExisting());
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

        in.copyTo(out);

        assert sameDirectory(in, out.directory("In"));
    }

    @Test
    void directoryToDirectoryWithPattern() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Directory out = locateDirectory("Out", $ -> {
            $.file("1", "This text will be overwritten by input file.");
        });

        in.copyTo(out, o -> o.glob("**").strip());

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

        in.copyTo(out, o -> o.take((file, attr) -> file.getFileName().startsWith("file")));

        assert sameFile(in.file("file"), out.file("In/file"));
        assert sameFile(in.file("dir/file"), out.file("In/dir/file"));
        assert out.file("In/text").isAbsent();
        assert out.file("In/dir/text").isAbsent();
    }

    @Test
    void directoryToDirectoryOptionReplaceExisting() {
        Directory in = locateDirectory("In", $ -> {
            Locator.file($.file("file", "update")).lastModifiedTime(10);
        });
        Directory out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "new")).lastModifiedTime(20);
        });
        in.copyTo(out, o -> o.replaceExisting().strip());
        assert out.file("file").text().trim().equals("update");

        out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "old")).lastModifiedTime(0);
        });
        in.copyTo(out, o -> o.replaceExisting().strip());
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
        in.copyTo(out, o -> o.replaceOld().strip());
        assert out.file("file").text().trim().equals("new");

        out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "old")).lastModifiedTime(0);
        });
        in.copyTo(out, o -> o.replaceOld().strip());
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
        in.copyTo(out, o -> o.skipExisting().strip());
        assert out.file("file").text().trim().equals("new");

        out = locateDirectory("Out", $ -> {
            Locator.file($.file("file", "old")).lastModifiedTime(0);
        });
        in.copyTo(out, o -> o.skipExisting().strip());
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
            $.file("delete");
        });

        in.copyTo(out, o -> o.sync().strip());
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

        in.copyTo(out);

        assert sameDirectory(in, out.directory("In"));
    }

    @Test
    void directoryToDeepAbsent() {
        Directory in = locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Directory out = locateAbsentDirectory("1/2/3");

        in.copyTo(out);

        assert sameDirectory(in, out.directory("In"));
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

        in.copyTo(out, o -> o.glob("*").strip());

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

        in.copyTo(out, o -> o.glob("**").strip());

        assert sameDirectory(in, out);
    }

    @Test
    void archiveToDirectory() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1", "override");
        }).asArchive();

        Directory out = locateDirectory("Out", $ -> {
            $.file("1", "This text will be overridden.");
        });

        in.copyTo(out);

        assert match(out, $ -> {
            $.file("1", "override");
        });
    }

    @Test
    void archiveToDirectoryWithPattern() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "override");
            $.file("2.txt", "not match");
        }).asArchive();

        Directory out = locateDirectory("Out", $ -> {
            $.file("1.txt", "This text will be overridden.");
        });

        in.copyTo(out, "1.*");

        assert match(out, $ -> {
            $.file("1.txt", "override");
        });
    }

    @Test
    void archiveToDirectoryWithSync() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "override");
            $.file("2.txt", "not match");
        }).asArchive();

        Directory out = locateDirectory("Out", $ -> {
            $.file("1.txt", "This text will be overridden.");
            $.file("3.txt", "This file will be deleted.");
        });

        in.copyTo(out, o -> o.sync());

        assert out.file("1.txt").isPresent();
        assert out.file("1.txt").isPresent();
        assert out.file("1.txt").isPresent();
    }

    @Test
    void archiveToAbsent() {
        Folder in = locateArchive("main.zip", $ -> {
            $.file("1.txt", "ok");
        }).asArchive();

        Directory out = locateAbsentDirectory("Out");

        in.copyTo(out, "1.*");

        assert match(out, $ -> {
            $.file("1.txt", "ok");
        });
    }
}
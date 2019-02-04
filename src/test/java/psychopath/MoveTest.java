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

import java.time.Instant;

import org.junit.jupiter.api.Test;

import antibug.powerassert.PowerAssertOff;

class MoveTest extends LocationTestHelper {

    @Test
    public void absentToFile() {
        File in = locateAbsent("absent");
        File out = locateFile("out");

        in.moveToNow(out);

        assert in.isAbsent();
        assert out.isPresent();
    }

    @Test
    public void absentToDirectory() {
        File in = locateAbsent("absent");
        Directory out = locateDirectory("out");

        in.moveTo(out);

        assert in.isAbsent();
        assert out.file("absent").isAbsent();
    }

    @Test
    public void absentToAbsent() {
        File in = locateAbsent("absent");
        File out = locateAbsent("out");

        in.moveToNow(out);

        assert in.isAbsent();
        assert out.isAbsent();
    }

    @Test
    public void fileToFile() {
        File in = locateFile("In", "Success");
        File out = locateFile("Out", "This text will be overwritten by input file.");

        in.moveToNow(out);

        assert in.isAbsent();
        assert match(out, "Success");
    }

    @Test
    public void fileToFileWithSameTimeStamp() {
        Instant now = Instant.now();
        File in = locateFile("In", now, "Success");
        File out = locateFile("Out", now, "This text will be overwritten by input file.");

        in.moveToNow(out);

        assert in.isAbsent();
        assert match(out, now, "Success");
    }

    @Test
    public void fileToFileWithDifferentTimeStamp() {
        Instant now = Instant.now();
        File in = locateFile("In", now, "Success");
        File out = locateFile("Out", now.plusSeconds(10), "This text will be overwritten by input file.");

        in.moveToNow(out);

        assert in.isAbsent();
        assert match(out, now, "Success");
    }

    @Test
    public void fileToAbsent() {
        File in = locateFile("In", "Success");
        File out = locateAbsent("Out");

        in.moveToNow(out);

        assert in.isAbsent();
        assert match(out, "Success");
    }

    @Test
    public void fileToDeepAbsent() {
        File in = locateFile("In", "Success");
        File out = locateAbsent("1/2/3");

        in.moveToNow(out);

        assert in.isAbsent();
        assert match(out, "Success");
    }

    @Test
    public void fileToDirectory() {
        File in = locateFile("In", "Success");
        Directory out = locateDirectory("Out");

        in.moveTo(out);

        assert in.isAbsent();
        assert match(out.file("In"), "Success");
    }

    @Test
    public void directoryToDirectory() {
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
    public void directoryToDirectoryWithPattern() {
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
    public void directoryToDirectoryWithFilter() {
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
    public void directoryToAbsent() {
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
    public void directoryToDeepAbsent() {
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
    public void children() {
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
    public void descendant() {
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
    public void archiveToDirectory() {
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
    public void archiveToDirectoryWithPattern() {
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
    public void archiveToAbsent() {
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

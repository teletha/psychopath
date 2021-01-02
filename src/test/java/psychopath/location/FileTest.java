/*
 * Copyright (C) 2021 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.location;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.Location;
import psychopath.LocationTestHelper;
import psychopath.Locator;

class FileTest extends LocationTestHelper {

    @Test
    void name() {
        // absent
        assert locateAbsent("name").name().equals("name");
        assert locateAbsent("nest/name").name().equals("name");
        assert locateAbsent("root/nest/name").name().equals("name");

        // absolute
        assert locateAbsoluteAbsent("name").name().equals("name");
        assert locateAbsoluteAbsent("nest/name").name().equals("name");
        assert locateAbsoluteAbsent("root/nest/name").name().equals("name");
    }

    @Test
    void base() {
        // absent
        assert locateAbsent("test").base().equals("test");
        assert locateAbsent("test.txt").base().equals("test");
        assert locateAbsent("test.dummy.log").base().equals("test.dummy");
        assert locateAbsent("text.").base().equals("text");
        assert locateAbsent(".gitignore").base().equals("");

        // absolute
        assert locateAbsoluteAbsent("test").base().equals("test");
        assert locateAbsoluteAbsent("test.txt").base().equals("test");
        assert locateAbsoluteAbsent("test.dummy.log").base().equals("test.dummy");
        assert locateAbsoluteAbsent("text.").base().equals("text");
        assert locateAbsoluteAbsent(".gitignore").base().equals("");
    }

    @Test
    void extension() {
        // absent
        assert locateAbsent("test").extension().equals("");
        assert locateAbsent("test.txt").extension().equals("txt");
        assert locateAbsent("test.dummy.log").extension().equals("log");
        assert locateAbsent("text.").extension().equals("");
        assert locateAbsent(".gitignore").extension().equals("gitignore");

        // absolute
        assert locateAbsoluteAbsent("test").extension().equals("");
        assert locateAbsoluteAbsent("test.txt").extension().equals("txt");
        assert locateAbsoluteAbsent("test.dummy.log").extension().equals("log");
        assert locateAbsoluteAbsent("text.").extension().equals("");
        assert locateAbsoluteAbsent(".gitignore").extension().equals("gitignore");
    }

    @Test
    void locateByNewBaseName() {
        // absent
        assert locateAbsent("test").base("new").name().equals("new");
        assert locateAbsent("test.txt").base("new").name().equals("new.txt");
        assert locateAbsent("test.dummy.log").base("new").name().equals("new.log");
        assert locateAbsent("text.").base("new").name().equals("new");
        assert locateAbsent(".gitignore").base("new").name().equals("new.gitignore");

        // absolute
        assert locateAbsoluteAbsent("test").base("new").name().equals("new");
        assert locateAbsoluteAbsent("test.txt").base("new").name().equals("new.txt");
        assert locateAbsoluteAbsent("test.dummy.log").base("new").name().equals("new.log");
        assert locateAbsoluteAbsent("text.").base("new").name().equals("new");
        assert locateAbsoluteAbsent(".gitignore").base("new").name().equals("new.gitignore");
    }

    @Test
    void locateByNewExtension() {
        // absent
        assert locateAbsent("test").extension("new").name().equals("test.new");
        assert locateAbsent("test.txt").extension("new").name().equals("test.new");
        assert locateAbsent("test.dummy.log").extension("new").name().equals("test.dummy.new");
        assert locateAbsent("text.").extension("new").name().equals("text.new");
        assert locateAbsent(".gitignore").extension("new").name().equals(".new");

        // absolute
        assert locateAbsoluteAbsent("test").extension("new").name().equals("test.new");
        assert locateAbsoluteAbsent("test.txt").extension("new").name().equals("test.new");
        assert locateAbsoluteAbsent("test.dummy.log").extension("new").name().equals("test.dummy.new");
        assert locateAbsoluteAbsent("text.").extension("new").name().equals("text.new");
        assert locateAbsoluteAbsent(".gitignore").extension("new").name().equals(".new");
    }

    @Test
    void locateByNullExtension() {
        // absent
        assert locateAbsent("test").extension(null).name().equals("test");
        assert locateAbsent("test.txt").extension(null).name().equals("test");
        assert locateAbsent("test.dummy.log").extension(null).name().equals("test.dummy");
        assert locateAbsent("text.").extension(null).name().equals("text");

        // absolute
        assert locateAbsoluteAbsent("test").extension(null).name().equals("test");
        assert locateAbsoluteAbsent("test.txt").extension(null).name().equals("test");
        assert locateAbsoluteAbsent("test.dummy.log").extension(null).name().equals("test.dummy");
        assert locateAbsoluteAbsent("text.").extension(null).name().equals("text");
    }

    @Test
    void locateByEmptyExtension() {
        // absent
        assert locateAbsent("test").extension("").name().equals("test");
        assert locateAbsent("test.txt").extension("").name().equals("test");
        assert locateAbsent("test.dummy.log").extension("").name().equals("test.dummy");
        assert locateAbsent("text.").extension("").name().equals("text");

        // absolute
        assert locateAbsoluteAbsent("test").extension("").name().equals("test");
        assert locateAbsoluteAbsent("test.txt").extension("").name().equals("test");
        assert locateAbsoluteAbsent("test.dummy.log").extension("").name().equals("test.dummy");
        assert locateAbsoluteAbsent("text.").extension("").name().equals("text");
    }

    @Test
    void absolutize() {
        // absent
        Location locateAbsent = locateAbsent("name");
        Location locateAbsoluteAbsent = locateAbsent.absolutize();
        assert locateAbsent != locateAbsoluteAbsent;
        assert locateAbsoluteAbsent.isAbsolute();

        // absolute
        locateAbsent = locateAbsoluteAbsent("name");
        locateAbsoluteAbsent = locateAbsent.absolutize();
        assert locateAbsent == locateAbsoluteAbsent;
        assert locateAbsoluteAbsent.isAbsolute();
    }

    @Test
    void parent() {
        // absent
        assert locateAbsent("a/b").parent().equals(locateAbsentDirectory("a"));
        assert locateAbsent("a/b/c").parent().equals(locateAbsentDirectory("a/b"));

        // absolute
        assert locateAbsoluteAbsent("a/b").parent().equals(locateAbsoluteAbsentDirectory("a"));
        assert locateAbsoluteAbsent("a/b/c").parent().equals(locateAbsoluteAbsentDirectory("a/b"));

        // relative
        assert Locator.file("file").parent().equals(Locator.directory(""));
    }

    @Test
    void children() {
        // absent
        assert locateAbsent("a/b").children().toList().isEmpty();
        assert locateAbsoluteAbsent("a/b").children().toList().isEmpty();

        // present
        assert locateFile("a/b").children().toList().isEmpty();
        assert locateFile("a/b").absolutize().children().toList().isEmpty();
    }

    @Test
    void descendant() {
        // absent
        assert locateAbsent("a/b").descendant().toList().isEmpty();
        assert locateAbsoluteAbsent("a/b").descendant().toList().isEmpty();

        // present
        assert locateFile("a/b").descendant().toList().isEmpty();
        assert locateFile("a/b").absolutize().descendant().toList().isEmpty();
    }

    @Test
    void equal() {
        // absent
        assert locateAbsent("a").equals(locateAbsent("a"));
        assert locateAbsent("a/b").equals(locateAbsent("a/b"));
        assert locateAbsent("../a").equals(locateAbsent("../a"));

        // absolute
        assert locateAbsoluteAbsent("a").equals(locateAbsoluteAbsent("a"));
        assert locateAbsoluteAbsent("a/b").equals(locateAbsoluteAbsent("a/b"));
        assert locateAbsoluteAbsent("../a").equals(locateAbsoluteAbsent("../a"));
    }

    @Test
    void copyTo() {
        File file = locateFile("base", "ok");
        Directory dest = locateDirectory("dest");
        assert dest.isEmpty() == true;

        file.copyTo(dest);
        assert dest.isEmpty() == false;
        File copied = dest.file("base");
        assert file.name().equals(copied.name());
    }

    @Test
    void newInputStream() throws IOException {
        File file = locateFile("test", "contents");
        assert file.isPresent();
        assert file.size() != 0;

        InputStream stream = file.newInputStream();
        assert new String(stream.readAllBytes()).trim().equals("contents");
    }

    @Test
    void newInputStreamOnAbsent() throws IOException {
        InputStream stream = locateAbsent("not exist").newInputStream();
        assert new String(stream.readAllBytes()).trim().equals("");
    }

    @Test
    void newOutputStreamOnAbsent() throws Exception {
        File file = locateAbsent("test");
        assert file.isAbsent();

        OutputStream stream = file.newOutputStream();
        stream.write("test".getBytes());
        stream.close();
        assert file.isPresent();
        assert file.size() != 0;
    }

    @Test
    void newBufferedReader() throws IOException {
        File file = locateFile("test", "contents");
        assert file.isPresent();
        assert file.size() != 0;

        BufferedReader reader = file.newBufferedReader();
        assert reader.readLine().equals("contents");
    }

    @Test
    void newBufferedReaderOnAbsent() throws IOException {
        BufferedReader reader = locateAbsent("not exist").newBufferedReader();
        assert reader.readLine() == null;
    }

    @Test
    void newBufferedWriterOnAbsent() throws Exception {
        File file = locateAbsent("test");
        assert file.isAbsent();

        BufferedWriter writer = file.newBufferedWriter();
        writer.write("test");
        writer.close();
        assert file.isPresent();
        assert file.size() != 0;
    }

    @Test
    void readFromAbsentFile() {
        File file = locateAbsent("read-from-absent");
        assert file.text().equals("");
        assert file.isAbsent();
    }

    @Test
    void readFromPresentFile() {
        File file = locateFile("read-from-present", "content");
        assert file.text().equals("content");
    }

    @Test
    void writeToAbsentFile() {
        File file = locateAbsent("write-to-absent");
        file.text("OK");
        assert file.text().equals("OK");
    }

    @Test
    void writeToDeepAbsentFile() {
        File file = locateAbsent("deep/write-to-absent");
        file.text("OK");
        assert file.text().equals("OK");
    }

    @Test
    void writeToPresentFile() {
        File file = locateFile("write-to-present", "contents");
        file.text("override");
        assert file.text().equals("override");
    }

    @Test
    void writeByInputStream() {
        File file = locateFile("write-to-present", "contents");
        file.write(new ByteArrayInputStream("override".getBytes()));
        assert file.text().equals("override");
    }

    @Test
    void writeDeeplyByInputStream() {
        File file = locateAbsent("deep/write-to-present");
        file.write(new ByteArrayInputStream("override".getBytes()));
        assert file.text().equals("override");
    }

    @Test
    void writeByReader() {
        File file = locateFile("write-to-present", "contents");
        file.write(new StringReader("override"));
        assert file.text().equals("override");
    }

    @Test
    void writeDeeplyByReader() {
        File file = locateAbsent("deep/write-to-present");
        file.write(new StringReader("override"));
        assert file.text().equals("override");
    }

    @Test
    void writeByWriter() {
        File file = locateFile("write-to-present", "contents");
        file.write(b -> b.append("override"));
        assert file.text().equals("override");
    }

    @Test
    void writeDeeplyByWriter() {
        File file = locateAbsent("deep/write-to-present");
        file.write(b -> b.append("override"));
        assert file.text().equals("override");
    }

    @Test
    void lines() {
        File file = locateAbsent("absent");
        assert file.lines().toList().isEmpty();
    }

    @Test
    void create() {
        assert locateAbsent("file").create().isPresent();
        assert locateAbsent("deep/file").create().isPresent();
    }

    @Test
    void unpack() {
        File archive = locateArchive("test.zip", $ -> {
            $.file("file");
            $.dir("dir", () -> {
                $.file("child");
            });
        });

        assert match(archive.unpack(), $ -> {
            $.file("file");
            $.dir("dir", () -> {
                $.file("child");
            });
        });
    }

    @Test
    void observeUnpackingTo() {
        File archive = locateArchive("test.zip", $ -> {
            $.file("file");
            $.dir("dir", () -> {
                $.file("child1");
                $.file("child2");
            });
        });

        List<File> files = archive.observeUnpackingTo(Locator.temporaryDirectory()).toList();
        assert files.size() == 3;
    }

    @Test
    void moveUp() {
        Directory dir = locateDirectory("root", $ -> {
            $.dir("in", () -> {
                $.file("file");
            });
        });

        File file = dir.file("in/file");
        File up = dir.file("file");
        assert file.isPresent();
        assert up.isAbsent();

        File uped = file.moveUp();
        assert file.isAbsent();
        assert up.isPresent();
        assert uped.equals(up);
    }

    @Test
    void moveUpToExistingSameType() {
        Directory dir = locateDirectory("root", $ -> {
            $.dir("in", () -> {
                $.file("file", "original");
            });
            $.file("file", "dest");
        });

        File file = dir.file("in/file");
        File up = dir.file("file");
        assert file.isPresent();

        File uped = file.moveUp();
        assert file.isAbsent();
        assert up.isPresent();
        assert uped.equals(up);
    }

    @Test
    void moveUpToExistingDifferentType() {
        Directory dir = locateDirectory("root", $ -> {
            $.dir("in", () -> {
                $.file("file", "original");
            });
            $.dir("file");
        });

        dir.file("in/file").moveUp();

        assert match(dir, $ -> {
            $.dir("in", () -> {
            });
            $.dir("file");
        });
    }

    @Test
    void renameTo() {
        Directory dir = locateDirectory("root", $ -> {
            $.file("src");
        });

        File source = dir.file("src");
        File destination = dir.file("dest");
        assert source.isPresent();
        assert destination.isAbsent();

        File renamed = source.renameTo("dest");
        assert source.isAbsent();
        assert destination.isPresent();
        assert destination.equals(renamed);
        assert destination != renamed;
    }

    @Test
    void renameToSameName() {
        File source = locateFile("src");
        File renamed = source.renameTo("src");
        assert source == renamed;
    }

    @Test
    void renameToNull() {
        assertThrows(NullPointerException.class, () -> locateFile("src").renameTo(null));
    }

    @Test
    void renameToExistingType() {
        Directory dir = locateDirectory("root", $ -> {
            $.file("src");
            $.file("dest-file");
            $.dir("dest-dir");
        });
        assertThrows(FileAlreadyExistsException.class, () -> dir.file("src").renameTo("dest-file"));
        assertThrows(FileAlreadyExistsException.class, () -> dir.file("src").renameTo("dest-dir"));
    }

    @Test
    void lock() {
        File one = locateFile("same");
        String[] result = new String[1];

        one.lock().to(lock -> {
            result[0] = "lock by one";
        }, e -> {
            result[0] = "can't lock by one";
        });
        assert result[0].equals("lock by one");
    }

    @Test
    void lockOnSameLocationWillThrowOverlapingFileLockException() {
        File one = locateFile("same");
        String[] result = new String[1];

        one.lock().to(lock -> {
            result[0] = "lock by one";
        });
        assert result[0].equals("lock by one");

        one.lock().to(lock -> {
            result[0] = "lock twice";
        }, e -> {
            result[0] = "can't lock by one again " + e.getClass().getSimpleName();
        });
        assert result[0].equals("can't lock by one again " + OverlappingFileLockException.class.getSimpleName());
    }
}
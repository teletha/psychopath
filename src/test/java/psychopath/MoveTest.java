/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/03/31 3:04:16
 */
public class MoveTest extends PathOperationTestHelper {

    @RegisterExtension
    public CleanRoom room = new CleanRoom();

    /**
     * <p>
     * Test operation.
     * </p>
     * 
     * @param one
     * @param other
     */
    private void operate(Path one, Path other, String... patterns) {
        PsychoPath.locate(one).move(other, patterns);
    }

    /**
     * <p>
     * Test operation.
     * </p>
     * 
     * @param one
     * @param other
     */
    private void operate(Path one, Path other, BiPredicate<Path, BasicFileAttributes> filter) {
        PsychoPath.locate(one).move(other, filter);
    }

    @Test
    public void nullInput() throws Exception {
        Path in = null;
        Path out = room.locateAbsent("null");

        assertThrows(NullPointerException.class, () -> operate(in, out));
    }

    @Test
    public void nullOutput() throws Exception {
        Path in = room.locateAbsent("null");
        Path out = null;

        assertThrows(NullPointerException.class, () -> operate(in, out));
    }

    @Test
    public void absentToFile() throws Exception {
        Path in = room.locateAbsent("absent");
        Path out = room.locateFile("out");

        operate(in, out);

        assert notExist(in);
        assert exist(out);
    }

    @Test
    public void absentToDirectory() throws Exception {
        Path in = room.locateAbsent("absent");
        Path out = room.locateDirectory("out");

        operate(in, out);

        assert notExist(in);
        assert exist(out);
    }

    @Test
    public void absentToAbsent() throws Exception {
        Path in = room.locateAbsent("absent");
        Path out = room.locateAbsent("out");

        operate(in, out);

        assert notExist(in);
        assert notExist(out);
    }

    @Test
    public void fileToFile() throws Exception {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateFile("Out", "This text will be overwritten by input file.");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameFile(snapshot, out);
    }

    @Test
    public void fileToFileWithSameTimeStamp() throws Exception {
        Instant now = Instant.now();
        Path in = room.locateFile("In", now, "Success");
        Path out = room.locateFile("Out", now, "This text will be overwritten by input file.");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameFile(snapshot, out);
    }

    @Test
    public void fileToFileWithDifferentTimeStamp() {
        Instant now = Instant.now();
        Path in = room.locateFile("In", now, "Success");
        Path out = room.locateFile("Out", now.plusSeconds(10), "This text will be overwritten by input file.");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameFile(snapshot, out);
    }

    @Test
    public void fileToAbsent() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateAbsent("Out");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameFile(snapshot, out);
    }

    @Test
    public void fileToDeepAbsent() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateAbsent("1/2/3");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameFile(snapshot, out);
    }

    @Test
    public void fileToDirectory() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateDirectory("Out");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameFile(snapshot, out.resolve("In"));
    }

    @Test
    public void directoryToFile() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Path out = room.locateFile("Out");

        assertThrows(NoSuchFileException.class, () -> operate(in, out));
    }

    @Test
    public void directoryToDirectory() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Path out = room.locateDirectory("Out", $ -> {
            $.file("1", "This text will be overwritten by input file.");
        });
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameDirectory(snapshot, out.resolve("In"));
    }

    @Test
    public void directoryToDirectoryWithFilter() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("file");
            $.file("text");
            $.dir("dir", () -> {
                $.file("file");
                $.file("text");
            });
        });
        Path out = room.locateDirectory("Out");
        Path snapshot = snapshot(in);

        operate(in, out, (file, attr) -> file.getFileName().startsWith("file"));

        assert sameFile(snapshot.resolve("file"), out.resolve("In/file"));
        assert sameFile(snapshot.resolve("dir/file"), out.resolve("In/dir/file"));
        assert notExist(in.resolve("file"), in.resolve("dir/file"));
        assert notExist(out.resolve("In/text"), out.resolve("In/dir/text"));
    }

    @Test
    public void directoryToAbsent() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Path out = room.locateAbsent("Out");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameDirectory(snapshot, out.resolve("In"));
    }

    @Test
    public void directoryToDeepAbsent() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Path out = room.locateAbsent("1/2/3");
        Path snapshot = snapshot(in);

        operate(in, out);

        assert notExist(in);
        assert sameDirectory(snapshot, out.resolve("In"));
    }

    @Test
    public void children() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("file");
            $.file("text");
            $.dir("dir", () -> {
                $.file("file");
                $.file("text");
            });
            $.dir("empty");
        });
        Path out = room.locateDirectory("Out");

        operate(in, out, "*");

        assert notExist(in.resolve("file"), in.resolve("text"), in.resolve("empty"));
        assert exist(in.resolve("dir"), in.resolve("dir/file"), in.resolve("dir/text"));

        assert exist(out.resolve("file"), out.resolve("text"), out.resolve("empty"), out.resolve("dir"));
        assert notExist(out.resolve("dir/file"), out.resolve("dir/text"));
    }

    @Test
    public void descendant() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("1", "One");
            $.file("2", "Two");
            $.dir("dir", () -> {
                $.file("nest");
            });
        });
        Path out = room.locateDirectory("Out");
        Path snapshot = snapshot(in);

        operate(in, out, "**");

        assert exist(in);
        assert children(in).size() == 0;
        assert sameDirectory(snapshot, out);
    }
}

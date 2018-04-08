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

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/03/31 3:01:30
 */
public class DeleteTest extends PathOperationTestHelper {

    @RegisterExtension
    public CleanRoom room = new CleanRoom();

    /**
     * <p>
     * Test operation.
     * </p>
     * 
     * @param path
     */
    private void operate(Path path, String... patterns) {
        PsychoPath.delete(path, patterns);
    }

    /**
     * <p>
     * Test operation.
     * </p>
     * 
     * @param path
     */
    private void operate(Path path, BiPredicate<Path, BasicFileAttributes> filter) {
        PsychoPath.delete(path, filter);
    }

    @Test
    public void file() {
        Path input = room.locateFile("test01/01.txt");

        operate(input);

        assert exist(input.getParent());
        assert notExist(input);
    }

    @Test
    public void directory() {
        Path input = room.locateDirectory("dir", $ -> {
            $.file("text");
            $.dir("dir", () -> {
                $.file("nest");
            });
        });

        operate(input);

        assert exist(input.getParent());
        assert notExist(input, input.resolve("text"), input.resolve("dir"), input.resolve("dir/nest"));
    }

    @Test
    public void directoryChildren() {
        Path input = room.locateDirectory("dir", $ -> {
            $.file("text");
            $.dir("dir", () -> {
                $.file("nest");
            });
        });

        operate(input, "**");

        assert exist(input);
        assert notExist(input.resolve("text"), input.resolve("dir"), input.resolve("dir/nest"));
    }

    @Test
    public void directoryFilter() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("file");
            $.file("text");
            $.dir("dir", () -> {
                $.file("file");
                $.file("text");
            });
        });

        operate(in, (file, attr) -> file.getFileName().startsWith("file"));

        assert exist(in, in.resolve("text"), in.resolve("dir/text"));
        assert notExist(in.resolve("file"), in.resolve("dir/file"));
    }

    @Test
    public void absent() {
        Path input = room.locateAbsent("absent");
        assert notExist(input);

        operate(input);

        assert notExist(input);
    }

    @Test
    public void inputNull() {
        PsychoPath.delete(null);
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

        operate(in, "*");

        assert exist(in, in.resolve("dir"), in.resolve("dir/file"), in.resolve("dir/text"));
        assert notExist(in.resolve("file"), in.resolve("text"), in.resolve("empty"));
    }
}

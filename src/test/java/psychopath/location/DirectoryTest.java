/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.location;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;
import psychopath.Directory;
import psychopath.File;
import psychopath.Location;
import psychopath.Locator;

/**
 * @version 2018/04/08 15:21:41
 */
public class DirectoryTest {

    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    private Directory current;

    @BeforeEach
    void setup() {
        current = null;
    }

    @Test
    void name() {
        // relative
        assert relative("name").name().equals("name");
        assert relative("nest/name").name().equals("name");
        assert relative("root/nest/name").name().equals("name");

        // absolute
        assert absolute("name").name().equals("name");
        assert absolute("nest/name").name().equals("name");
        assert absolute("root/nest/name").name().equals("name");
    }

    @Test
    void absolutize() {
        // relative
        Location relative = relative("name");
        Location absolute = relative.absolutize();
        assert relative != absolute;
        assert absolute.isAbsolute();

        // absolute
        relative = absolute("name");
        absolute = relative.absolutize();
        assert relative == absolute;
        assert absolute.isAbsolute();
    }

    @Test
    void parent() {
        // relative
        assert relative("a/b").parent().equals(relative("a"));
        assert relative("a/b/c").parent().equals(relative("a/b"));

        // absolute
        assert absolute("a/b").parent().equals(absolute("a"));
        assert absolute("a/b/c").parent().equals(absolute("a/b"));
    }

    @Test
    void equal() {
        // relative
        assert relative("a").equals(relative("a"));
        assert relative("a/b").equals(relative("a/b"));
        assert relative("../a").equals(relative("../a"));

        // absolute
        assert absolute("a").equals(absolute("a"));
        assert absolute("a/b").equals(absolute("a/b"));
        assert absolute("../a").equals(absolute("../a"));
    }

    @Test
    void findDirectorySingle() {
        define($ -> {
            $.dir("dir1");
        });

        assert findDirectory() == 1;
    }

    @Test
    void fineDirectoryMultiple() {
        define($ -> {
            $.dir("dir1");
            $.dir("dir2");
            $.dir("dir3");
        });

        assert findDirectory() == 3;
    }

    @Test
    void fineDirectoryNest() {
        define($ -> {
            $.dir("dir1", () -> {
                $.dir("nest11");
                $.dir("nest12");
                $.file("fileA1");
                $.file("fileA2");
            });
            $.dir("dir2", () -> {
                $.dir("nest21");
                $.dir("nest22");
                $.file("fileB1");
                $.file("fileB2");
            });
            $.file("fileC");
            $.file("fileC");
        });

        assert findDirectory() == 6;
        assert findDirectory("*") == 2;
        assert findDirectory("*/*") == 4;
        assert findDirectory("**") == 6;
        assert findDirectory("*", "*/*1") == 4;
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private Directory relative(String path) {
        return Locator.directory(room.locateAbsent(path));
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private Directory absolute(String path) {
        return Locator.directory(room.locateAbsent(path).toAbsolutePath());
    }

    /**
     * Helper.
     * 
     * @param definition
     * @return
     */
    private Directory define(Consumer<FileSystemDSL> definition) {
        room.with(definition);

        return current = Locator.directory(room.root);
    }

    /**
     * Compute directory size.
     * 
     * @param patterns
     * @return
     */
    private int findDirectory(String... patterns) {
        return current.walkDirectories(patterns).toList().size();
    }
}

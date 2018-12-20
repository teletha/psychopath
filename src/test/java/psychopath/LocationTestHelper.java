/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.CRC32;

import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;

/**
 * @version 2018/12/10 12:32:53
 */
public class LocationTestHelper {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    /**
     * Build file tree structure.
     * 
     * @param name A file name.
     * @return
     */
    public final File locateAbsent(String name) {
        return Locator.file(room.locateAbsent(name));
    }

    /**
     * Build file tree structure.
     * 
     * @param name A file name.
     * @return
     */
    public final Directory locateAbsentDirectory(String name) {
        return Locator.directory(room.locateAbsent(name));
    }

    /**
     * Build file tree structure.
     * 
     * @param name A directory name.
     * @param dsl A definition of file tree.
     * @return
     */
    public final File locateArchive(String name, Consumer<FileSystemDSL> dsl) {
        Path file = room.locateAbsent(name);
        room.locateArchive(file, dsl);

        return Locator.file(file);
    }

    /**
     * Build file tree structure.
     * 
     * @param name A file name.
     * @return
     */
    public final File locateFile(String name) {
        return Locator.file(room.locateFile(name));
    }

    /**
     * Build file tree structure.
     * 
     * @param name A file name.
     * @param content A content.
     * @return
     */
    public final File locateFile(String name, String content) {
        return Locator.file(room.locateFile(name, content));
    }

    /**
     * Build file tree structure.
     * 
     * @param name A file name.
     * @param content A content.
     * @return
     */
    public final File locateFile(String name, Instant time, String content) {
        return Locator.file(room.locateFile(name, time, content));
    }

    /**
     * Build file tree structure.
     * 
     * @param name A directory name.
     * @return
     */
    public final Directory locateDirectory(String name) {
        return Locator.directory(room.locateDirectory(name));
    }

    /**
     * Build file tree structure.
     * 
     * @param name A directory name.
     * @param dsl A definition of file tree.
     * @return
     */
    public final Directory locateDirectory(String name, Consumer<FileSystemDSL> dsl) {
        return Locator.directory(room.locateDirectory(name, dsl));
    }

    /**
     * Test matching file tree structure.
     * 
     * @param actual An actual directory.
     * @param expected An expected directory structure.
     * @return
     */
    public final boolean match(Directory actual, Consumer<FileSystemDSL> expected) {
        Directory e = locateDirectory(actual.name() + " expected", expected);
        List<Location<?>> expecteds = e.descendant().toList();
        List<Location<?>> actuals = actual.descendant().toList();

        for (int i = 0; i < expecteds.size(); i++) {
            Location<?> expectedLocation = expecteds.get(i);
            Location<?> actualLocation = actuals.get(i);

            assert e.relativize(expectedLocation).equals(actual.relativize(actualLocation));

            if (expectedLocation.isFile()) {
                // check contents
                List<String> expectedContent = expectedLocation.asFile().flatMap(f -> f.lines()).toList();
                List<String> actualContent = actualLocation.asFile().flatMap(f -> f.lines()).toList();
                assertIterableEquals(expectedContent, actualContent);
            }
        }

        return true;
    }

    /**
     * Test matching file.
     * 
     * @param actual An actual file.
     * @param expectedText An expected content.
     * @return
     */
    public final boolean match(File actual, String expectedText) {
        assert actual.text().trim().equals(expectedText);

        return true;
    }

    /**
     * Test matching file.
     * 
     * @param actual An actual file.
     * @param expectedText An expected content.
     * @return
     */
    public final boolean match(File actual, Instant expectedLastModified, String expectedText) {
        assert actual.text().trim().equals(expectedText);
        assert actual.lastModifiedTime().truncatedTo(ChronoUnit.MILLIS).equals(expectedLastModified.truncatedTo(ChronoUnit.MILLIS));

        return true;
    }

    /**
     * Helper method to check {@link File} equality as file.
     */
    protected static boolean sameFile(File one, File other) {
        assert one.isPresent() == other.isPresent();
        assert one.isFile();
        assert other.isFile();
        assert one.lastModified() == other.lastModified();
        assert one.size() == other.size();
        assert checksum(one) == checksum(other);
        return true;
    }

    /**
     * Helper method to check {@link Directory} equality as directory.
     * 
     * @return
     */
    protected static boolean sameDirectory(Directory one, Directory other) {
        assert one.isPresent() == other.isPresent();
        assert one.isDirectory();
        assert other.isDirectory();
        assert one.lastModified() == other.lastModified();

        List<Location<?>> oneChildren = one.children().toList();
        List<Location<?>> otherChildren = other.children().toList();

        assert oneChildren.size() == otherChildren.size();

        for (int i = 0; i < oneChildren.size(); i++) {
            Location<?> oneChild = oneChildren.get(i);
            Location<?> otherChild = otherChildren.get(i);

            if (oneChild.isFile()) {
                assert sameFile((File) oneChild, (File) otherChild);
            } else if (oneChild.isDirectory()) {
                assert sameDirectory((Directory) oneChild, (Directory) otherChild);
            }
        }
        return true;
    }

    /**
     * Helper method to compute {@link File} checksume.
     * 
     * @return
     */
    protected static long checksum(File path) {
        CRC32 crc = new CRC32();
        crc.update(path.bytes());

        return crc.getValue();
    }
}

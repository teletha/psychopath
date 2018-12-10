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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/12/10 12:34:07
 */
class DeleteTest extends LocationTestHelper {

    @Test
    void file() {
        File file = locateFile("file");
        assert file.isPresent();
        file.delete();
        assert file.isAbsent();
    }

    @Test
    void absentFile() {
        File file = locateAbsent("file");
        assert file.isAbsent();
        file.delete();
        assert file.isAbsent();
    }

    @Test
    void directoryEmpty() {
        Directory directory = locateDirectory("dir");

        assert directory.isPresent();
        directory.delete();
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
        directory.delete();
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
        directory.delete();
        assert directory.isAbsent();
    }

    @Test
    void absentDirectory() {
        Directory directory = locateAbsentDirectory("dir");
        assert directory.isAbsent();
        directory.delete();
        assert directory.isAbsent();
    }

    @Test
    void pattern() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("empty-dir");
            $.dir("dir", () -> {
                $.file("nest1");
                $.file("nest2");
            });
        });

        directory.delete("nest1");

        assert match(directory, $ -> {
            $.file("text1");
            $.file("text2");
            $.dir("empty-dir");
            $.dir("dir", () -> {
                $.file("nest2");
            });
        });
    }

    @Test
    void patternWildcard() {
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("empty-dir");
            $.dir("dir", () -> {
                $.file("3.txt");
                $.file("4.txt");
            });
        });

        directory.delete("**.txt");

        assert match(directory, $ -> {
            $.dir("empty-dir");
            $.dir("dir");
        });
    }
}

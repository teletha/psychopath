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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.junit.jupiter.api.Test;

import antibug.Chronus;

class LocateTest extends LocationTestHelper {

    @Test
    void locateRelative() {
        File file = Locator.file("context");
        assert !file.isAbsolute();
    }

    @Test
    void locateAbsolute() {
        File file = Locator.file(room.root.toAbsolutePath().toString());
        assert file.isAbsolute();
    }

    @Test
    void locateWhitespace() throws MalformedURLException {
        File file = Locator.file(new URL("file://white space"));
        assert !file.isAbsolute();
    }

    @Test
    void locateTemporaryFile() {
        File file = Locator.temporaryFile();
        assert file.isPresent();
    }

    @Test
    void locateTemporaryFiles() {
        File file1 = Locator.temporaryFile();
        File file2 = Locator.temporaryFile();
        File file3 = Locator.temporaryFile();
        assert file1.isPresent();
        assert file2.isPresent();
        assert file3.isPresent();
        assert file1.name() != file2.name();
        assert file3.name() != file2.name();
        assert file3.name() != file1.name();
    }

    @Test
    void locateTemporaryDirectory() {
        Directory directory = Locator.temporaryDirectory();
        assert directory.isPresent();
    }

    @Test
    void locateArchive() {
        Location archive = Locator.locate(LocateTest.class);
        assert archive != null;
        assert archive.isPresent();
        assert archive.isDirectory();
    }

    @Test
    void locateResource() {
        File resource = Locator.locate(LocateTest.class, LocateTest.class.getSimpleName() + ".class");
        assert resource != null;
        assert resource.isPresent();
        assert !resource.isDirectory();
    }

    @Test
    void locateResourceInJar() {
        File resource = Locator.locate(Chronus.class, Chronus.class.getSimpleName() + ".class");
        assert resource != null;
        assert resource.isPresent();
        assert !resource.isDirectory();
    }

    @Test
    void locateArchiveByJDKClass() {
        Location archive = Locator.locate(Map.class);
        assert archive == null;
    }

    @Test
    void temporaryFile() {
        File file = Locator.temporaryFile();
        assert file.isPresent();
        assert file.isFile();
    }

    @Test
    void temporaryDirectory() {
        Directory directory = Locator.temporaryDirectory();
        assert directory.isPresent();
        assert directory.isDirectory();
    }
}
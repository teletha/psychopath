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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.Chronus;
import antibug.CleanRoom;

/**
 * @version 2018/03/31 3:00:44
 */
public class LocateTest {

    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    @Test
    public void locateRelative() {
        Path path = PsychoPath.locate("context");
        assert !path.isAbsolute();
    }

    @Test
    public void locateAbsolute() {
        Path path = PsychoPath.locate(room.root.toAbsolutePath().toString());
        assert path.isAbsolute();
    }

    @Test
    public void locateWhitespace() throws MalformedURLException {
        Path path = PsychoPath.locate(new URL("file://white space"));
        assert !path.isAbsolute();
    }

    @Test
    public void locateTemporary() {
        Path path = PsychoPath.locateTemporary();
        assert !Files.exists(path);
        assert !Files.isDirectory(path);
        assert !Files.isRegularFile(path);
    }

    @Test
    public void locateTemporaries() {
        Path path1 = PsychoPath.locateTemporary();
        Path path2 = PsychoPath.locateTemporary();
        Path path3 = PsychoPath.locateTemporary();
        assert !Files.exists(path1);
        assert !Files.exists(path2);
        assert !Files.exists(path3);
        assert path1.getFileName() != path2.getFileName();
        assert path3.getFileName() != path2.getFileName();
        assert path3.getFileName() != path1.getFileName();
    }

    @Test
    public void locateArchive() {
        Path archive = PsychoPath.locate(LocateTest.class);
        assert archive != null;
        assert Files.exists(archive);
        assert Files.isDirectory(archive);
    }

    @Test
    public void locateResource() {
        Path resource = PsychoPath.locate(LocateTest.class, LocateTest.class.getSimpleName() + ".class");
        assert resource != null;
        assert Files.exists(resource);
        assert Files.isRegularFile(resource);
    }

    @Test
    public void locateResourceInJar() {
        Path resource = PsychoPath.locate(Chronus.class, Chronus.class.getSimpleName() + ".class");
        assert resource != null;
        assert Files.exists(resource);
        assert Files.isRegularFile(resource);
    }

    @Test
    public void locateArchiveByJDKClass() {
        Path archive = PsychoPath.locate(Map.class);
        assert archive == null;
    }
}

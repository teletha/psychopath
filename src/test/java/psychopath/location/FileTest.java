/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.location;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

/**
 * @version 2018/05/31 8:45:32
 */
class FileTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void base() {
        assert Locator.file("test").base().equals("test");
        assert Locator.file("test.txt").base().equals("test");
        assert Locator.file("test.dummy.log").base().equals("test.dummy");
        assert Locator.file("text.").base().equals("text");
        assert Locator.file(".gitignore").base().equals("");
    }

    @Test
    void extension() {
        assert Locator.file("test").extension().equals("");
        assert Locator.file("test.txt").extension().equals("txt");
        assert Locator.file("test.dummy.log").extension().equals("log");
        assert Locator.file("text.").extension().equals("");
        assert Locator.file(".gitignore").extension().equals("gitignore");
    }

    @Test
    @Disabled
    void copyTo() {
        File file = Locator.file(room.locateFile("base", "ok"));
        Directory dest = Locator.directory(room.locateDirectory("dest"));
        assert dest.isEmpty() == true;

        file.copyTo(dest);
        assert dest.isEmpty() == false;
        File copied = dest.file("base");
        assert file.name().equals(copied.name());
    }

    @Test
    void newInputStream() throws IOException {
        File file = Locator.file(room.locateFile("test", "contents"));
        assert file.isPresent();
        assert file.size() != 0;

        InputStream stream = file.newInputStream();
        assert new String(stream.readAllBytes()).trim().equals("contents");
    }

    @Test
    void newOutputStream() throws Exception {
        File file = Locator.file(room.locateAbsent("test"));
        assert file.isAbsent();

        OutputStream stream = file.newOutputStream();
        stream.write("test".getBytes());
        stream.close();
        assert file.isPresent();
        assert file.size() != 0;
    }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.Location;
import psychopath.LocationTestHelper;

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
    void newOutputStream() throws Exception {
        File file = locateAbsent("test");
        assert file.isAbsent();

        OutputStream stream = file.newOutputStream();
        stream.write("test".getBytes());
        stream.close();
        assert file.isPresent();
        assert file.size() != 0;
    }
}

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
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import psychopath.Directory;
import psychopath.File;
import psychopath.Location;
import psychopath.Locator;

/**
 * @version 2018/05/31 8:45:32
 */
class FileTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    /** The relative root path. */
    String relativeRoot = room.root.toString().replace(java.io.File.separatorChar, '/') + "/";

    /** The absolute root path. */
    String absoluteRoot = room.root.toAbsolutePath().toString().replace(java.io.File.separatorChar, '/') + "/";

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
    void base() {
        // relative
        assert relative("test").base().equals("test");
        assert relative("test.txt").base().equals("test");
        assert relative("test.dummy.log").base().equals("test.dummy");
        assert relative("text.").base().equals("text");
        assert relative(".gitignore").base().equals("");

        // absolute
        assert absolute("test").base().equals("test");
        assert absolute("test.txt").base().equals("test");
        assert absolute("test.dummy.log").base().equals("test.dummy");
        assert absolute("text.").base().equals("text");
        assert absolute(".gitignore").base().equals("");
    }

    @Test
    void extension() {
        // relative
        assert relative("test").extension().equals("");
        assert relative("test.txt").extension().equals("txt");
        assert relative("test.dummy.log").extension().equals("log");
        assert relative("text.").extension().equals("");
        assert relative(".gitignore").extension().equals("gitignore");

        // absolute
        assert absolute("test").extension().equals("");
        assert absolute("test.txt").extension().equals("txt");
        assert absolute("test.dummy.log").extension().equals("log");
        assert absolute("text.").extension().equals("");
        assert absolute(".gitignore").extension().equals("gitignore");
    }

    @Test
    void locateByNewBaseName() {
        // relative
        assert relative("test").base("new").path().equals(relativeRoot + "new");
        assert relative("test.txt").base("new").path().equals(relativeRoot + "new.txt");
        assert relative("test.dummy.log").base("new").path().equals(relativeRoot + "new.log");
        assert relative("text.").base("new").path().equals(relativeRoot + "new");
        assert relative(".gitignore").base("new").path().equals(relativeRoot + "new.gitignore");
        assert relative("dir/file").base("new").path().equals(relativeRoot + "dir/new");
        assert relative("root/dir/file").base("new").path().equals(relativeRoot + "root/dir/new");

        // absolute
        assert absolute("test").base("new").path().equals(absoluteRoot + "new");
        assert absolute("test.txt").base("new").path().equals(absoluteRoot + "new.txt");
        assert absolute("test.dummy.log").base("new").path().equals(absoluteRoot + "new.log");
        assert absolute("text.").base("new").path().equals(absoluteRoot + "new");
        assert absolute(".gitignore").base("new").path().equals(absoluteRoot + "new.gitignore");
        assert absolute("dir/file").base("new").path().equals(absoluteRoot + "dir/new");
        assert absolute("root/dir/file").base("new").path().equals(absoluteRoot + "root/dir/new");
    }

    @Test
    void locateByNewExtension() {
        // relative
        assert relative("test").extension("new").path().equals(relativeRoot + "test.new");
        assert relative("test.txt").extension("new").path().equals(relativeRoot + "test.new");
        assert relative("test.dummy.log").extension("new").path().equals(relativeRoot + "test.dummy.new");
        assert relative("text.").extension("new").path().equals(relativeRoot + "text.new");
        assert relative(".gitignore").extension("new").path().equals(relativeRoot + ".new");

        // absolute
        assert absolute("test").extension("new").path().equals(absoluteRoot + "test.new");
        assert absolute("test.txt").extension("new").path().equals(absoluteRoot + "test.new");
        assert absolute("test.dummy.log").extension("new").path().equals(absoluteRoot + "test.dummy.new");
        assert absolute("text.").extension("new").path().equals(absoluteRoot + "text.new");
        assert absolute(".gitignore").extension("new").path().equals(absoluteRoot + ".new");
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
        assert relative("a/b").parent().equals(relativeDirectory("a"));
        assert relative("a/b/c").parent().equals(relativeDirectory("a/b"));

        // absolute
        assert absolute("a/b").parent().equals(absoluteDirectory("a"));
        assert absolute("a/b/c").parent().equals(absoluteDirectory("a/b"));
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
    @Disabled
    void copyTo() {
        File file = at(room.locateFile("base", "ok"));
        Directory dest = Locator.directory(room.locateDirectory("dest"));
        assert dest.isEmpty() == true;

        file.copyTo(dest);
        assert dest.isEmpty() == false;
        File copied = dest.file("base");
        assert file.name().equals(copied.name());
    }

    @Test
    void newInputStream() throws IOException {
        File file = at(room.locateFile("test", "contents"));
        assert file.isPresent();
        assert file.size() != 0;

        InputStream stream = file.newInputStream();
        assert new String(stream.readAllBytes()).trim().equals("contents");
    }

    @Test
    void newOutputStream() throws Exception {
        File file = at(room.locateAbsent("test"));
        assert file.isAbsent();

        OutputStream stream = file.newOutputStream();
        stream.write("test".getBytes());
        stream.close();
        assert file.isPresent();
        assert file.size() != 0;
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private File relative(String path) {
        return Locator.file(room.locateAbsent(path));
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private File absolute(String path) {
        return Locator.file(room.locateAbsent(path).toAbsolutePath());
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private Directory relativeDirectory(String path) {
        return Locator.directory(room.locateAbsent(path));
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private Directory absoluteDirectory(String path) {
        return Locator.directory(room.locateAbsent(path).toAbsolutePath());
    }

    /**
     * Helper to locate {@link File}.
     * 
     * @param path
     * @return
     */
    private File at(Path path) {
        return Locator.file(path);
    }
}

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

import org.junit.jupiter.api.Test;

import psychopath.Location;

/**
 * @version 2018/05/31 8:45:32
 */
class FileTest {

    @Test
    void base() {
        assert Location.file("test").base().equals("test");
        assert Location.file("test.txt").base().equals("test");
        assert Location.file("test.dummy.log").base().equals("test.dummy");
        assert Location.file("text.").base().equals("text");
        assert Location.file(".gitignore").base().equals("");
    }

    @Test
    void extension() {
        assert Location.file("test").extension().equals("");
        assert Location.file("test.txt").extension().equals("txt");
        assert Location.file("test.dummy.log").extension().equals("log");
        assert Location.file("text.").extension().equals("");
        assert Location.file(".gitignore").extension().equals("gitignore");
    }
}

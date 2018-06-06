/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/06/06 11:53:43
 */
class FileTest {

    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    @Test
    void size() {
        assert Locator.file(room.locateFile("some", "ok")).size() == 4;
        assert Locator.file(room.locateFile("empty")).size() == 0;
        assert Locator.file(room.locateAbsent("not exist")).size() == 0;
    }
}

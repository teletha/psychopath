/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.operation;

import org.junit.jupiter.api.Test;

import psychopath.File;
import psychopath.LocationTestHelper;

/**
 * 
 */
class PackToTemporaryTest extends LocationTestHelper {

    @Test
    void file() {
        File archive = locateFile("file", "value").packToTemporary();

        assert matchZip(archive, $ -> {

        });
    }
}

/*
 * Copyright (C) 2019 psychopath Development Team
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
 * @version 2018/12/10 12:47:35
 */
class SizeTest extends LocationTestHelper {

    @Test
    void size() {
        assert locateFile("some", "ok").size() == 4;
        assert locateFile("empty").size() == 0;
        assert locateAbsent("not exist").size() == 0;
    }
}

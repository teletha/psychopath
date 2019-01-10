/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "psychopath", "0.8");

        require("com.github.teletha", "sinobu", "1.0");
        require("com.github.teletha", "antibug", "0.6").atTest();
        require("org.apache.commons", "commons-compress", "1.18");
        require("org.tukaani", "xz", "1.8");
        require("com.github.junrar", "junrar", "2.0.0");
        unrequire("org.apache.commons", "commons-vfs2");
        // unrequire("commons-logging", "commons-logging");
    }
}

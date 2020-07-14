/*
 * Copyright (C) 2020 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "psychopath", "1.0.0");

        require("com.github.teletha", "sinobu", "[2.0,)");
        require("com.github.teletha", "antibug").atTest();

        versionControlSystem("https://github.com/Teletha/Psychopath");
    }
}
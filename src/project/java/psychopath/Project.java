/*
 * Copyright (C) 2023 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import javax.lang.model.SourceVersion;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "psychopath", ref("version.txt"));

        require(SourceVersion.RELEASE_19, SourceVersion.RELEASE_17);

        require("com.github.teletha", "sinobu");
        require("com.github.teletha", "antibug").atTest();
    }
}
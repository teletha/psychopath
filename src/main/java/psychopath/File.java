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

import java.nio.file.Path;

/**
 * @version 2018/04/08 12:19:40
 */
public class File extends Location {

    /**
     * @param path
     */
    File(Path path) {
        super(path);
    }

    /**
     * Retrieve the extension of this {@link File}.
     * 
     * @return
     */
    public String extension() {
        String name = name();
        int index = name.lastIndexOf(".");
        return index == -1 ? "" : name.substring(index + 1);
    }
}

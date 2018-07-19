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

import java.nio.file.FileSystems;
import java.nio.file.Path;

import kiss.I;

/**
 * @version 2018/07/18 11:13:03
 */
class Archive extends Directory {

    /**
     * @param path
     */
    Archive(File file) {
        super(detect(file));

    }

    /**
     * Detect archive file system.
     * 
     * @param file A target archive file.
     * @return An archive.
     */
    private static Path detect(File file) {
        try {
            switch (file.extension()) {
            case "zip":
                return FileSystems.newFileSystem(file.path, null).getPath("/");

            default:
                // If this exception will be thrown, it is bug of this program.
                // So we must rethrow the wrapped error in here.
                throw new Error("Unkwown archive [" + file + "].");
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }
}

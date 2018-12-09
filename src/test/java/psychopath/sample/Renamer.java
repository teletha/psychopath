/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.sample;

import psychopath.Directory;
import psychopath.Location;
import psychopath.Locator;

/**
 * @version 2018/07/17 23:06:20
 */
public class Renamer {

    public static void main(String[] args) {
        Directory root = Locator.directory("e:\\");

        root.walkFiles("ワンピース*.zip").take(1).to(file -> {
            file.unpackTo(root.directory(file.base()))
                    .children()
                    .single()
                    .as(Directory.class)
                    .effectOnComplete(Location::delete)
                    .flatMap(Location::children)
                    .to(Location::moveUp);
        });
    }

    public static Directory unpack(Directory root, String pattern) {
        root.walkFiles(pattern).take(1).to(file -> {
            file.unpackTo(root.directory(file.base()))
                    .children()
                    .single()
                    .as(Directory.class)
                    .effectOnComplete(Location::delete)
                    .flatMap(Location::children)
                    .to(Location::moveUp);
        });

        return root;
    }
}

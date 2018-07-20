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

import psychopath.Directory;
import psychopath.Location;
import psychopath.Locator;

/**
 * @version 2018/07/17 23:06:20
 */
public class Renamer {

    public static void main(String[] args) {
        Directory root = Locator.directory("e:\\");

        root.walkFiles("*.zip")
                .take(1)
                .map(file -> file.unpackTo(root.directory(file.base())))
                .flatMap(Location::children)
                .single()
                .as(Directory.class)
                .effectOnComplete(Directory::delete)
                .flatMap(Directory::children)
                .to(Location::moveToParent);
    }
}

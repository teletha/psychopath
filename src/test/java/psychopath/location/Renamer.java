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

import java.util.List;

import psychopath.Directory;
import psychopath.Location;
import psychopath.Locator;

/**
 * @version 2018/07/17 23:06:20
 */
public class Renamer {

    public static void main(String[] args) {
        Directory root = Locator.directory("e:\\");

        root.walkFiles("*.rar").take(1).to(file -> {
            Directory destination = file.unpackTo(root.directory(file.base()));

            List<Location<?>> children = destination.children().toList();

            destination.children().share().as(Directory.class).flatMap(Directory::children).to(child -> child.moveTo(destination));

            if (children.size() == 1) {
                Location<?> wrapper = children.get(0);

                wrapper.asDirectory().flatMap(Directory::children).to(destination::moveFrom, wrapper::delete);
            }
        });
    }
}

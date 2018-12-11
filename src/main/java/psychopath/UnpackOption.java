/*
 * Copyright (C) 2018 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.util.LinkedList;
import java.util.List;

/**
 * @version 2018/12/09 12:58:14
 */
public enum UnpackOption {

    StripSingleDirectory {
        @Override
        public void process(Directory root) {
            Directory current = root;
            List<Location<?>> list = current.children().toList();
            LinkedList<Directory> remove = new LinkedList();

            while (list.size() == 1 && list.get(0) instanceof Directory) {
                current = (Directory) list.get(0);
                list = current.children().toList();
                remove.add(current);
            }

            list.forEach(file -> file.moveTo(root));
            remove.forEach(Directory::delete);
        }

        private List<Location<?>> strip(Location dir) {
            List<Location<?>> list = dir.children().toList();

            if (list.size() == 1 && list.get(0) instanceof Directory) {
                return strip(list.get(0));
            }
            return list;
        }
    };

    /**
     * Define option process.
     * 
     * @param root
     */
    public abstract void process(Directory root);
}

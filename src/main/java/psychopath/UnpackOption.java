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

import java.util.List;
import java.util.function.Consumer;

/**
 * @version 2018/12/09 12:58:14
 */
public enum UnpackOption {

    StripSingleDirectory(root -> {
        List<Location<?>> children = root.children().toList();
        
        root.repeatMap(r -> r.children().single().as(Directory.class)).flatMap(r -> r.children()).to(file -> {
            file.moveTo(root);
        });
        
        root.children().single().as(Directory.class);

        if (children.size() == 1) {
            children.g
        }
    });

    /** The processor. */
    private final Consumer<Directory> process;

    /**
     * Hide.
     * 
     * @param process
     */
    private UnpackOption(Consumer<Directory> process) {
        this.process = process;
    }
}

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

import java.util.ArrayList;
import java.util.List;

public class SelectOption {

    final List<String> patterns = new ArrayList();

    boolean ignoreRoot;

    String relativePath;

    public SelectOption glob(String... patterns) {
        if (patterns != null) {
            this.patterns.addAll(List.of(patterns));
        }
        return this;
    }

    public SelectOption ignoreRoot() {
        this.ignoreRoot = true;
        return this;
    }

    public SelectOption allocatIn(String relativePath) {
        if (relativePath != null) {
            this.relativePath = relativePath;
        }
        return this;
    }
}

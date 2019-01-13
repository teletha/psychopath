/*
 * Copyright (C) 2019 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

public interface UnpackOption {

    /**
     * Define option process.
     * 
     * @param root
     */
    public abstract void process(Directory root);
}

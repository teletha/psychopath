/*
 * Copyright (C) 2025 The PSYCHOPATH Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class SerializeTest {

    @Test
    void file() throws IOException, ClassNotFoundException {
        File location = Locator.file("test");
        assert clone(location).path().equals("test");
    }

    @Test
    void directory() throws IOException, ClassNotFoundException {
        Directory location = Locator.directory("test");
        assert clone(location).path().equals("test");
    }

    private <T> T clone(T value) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream writer = new ObjectOutputStream(out);
        writer.writeObject(value);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream reader = new ObjectInputStream(in);
        return (T) reader.readObject();
    }
}
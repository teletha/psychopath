/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath;

import java.lang.ProcessBuilder.Redirect;
import java.net.URL;

import kiss.I;

class Archiver7 {

    /** The shared archiver directory. */
    private static final Directory archiverDirectory = Locator.directory(System.getProperty("user.home"))
            .directory("psychopath-archiver")
            .absolutize();

    /** The shared archiver. */
    private static final File archiver = archiverDirectory.file("7z.exe");

    /** The temporary unpack location. */
    private static final Directory temporaryRoot = Locator.temporaryDirectory();

    static {
        if (archiver.isAbsent()) {
            // try to download
            try {
                URL in = new URL("https://github.com/Teletha/Psychopath/raw/master/7z.zip");

                Locator.temporaryFile("7z.zip").write(in.openStream()).unpackTo(archiverDirectory);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Unpack by command line.
     * 
     * @param in
     * @param out
     */
    static Directory unpack(File in) {
        Directory temp = temporaryRoot.directory("" + in.absolutize().path().hashCode() + in.lastModified()).absolutize();

        if (temp.isAbsent()) {
            try {
                ProcessBuilder builder = new ProcessBuilder(archiver.path(), //
                        "x", // extract option
                        "-y", // force to override
                        "-o\"" + temp.path() + "\"", // output directory
                        "\"" + in.absolutize().path() + "\"" // target to extract
                );

                builder.directory(archiverDirectory.asJavaFile())
                        .redirectOutput(Redirect.DISCARD)
                        .redirectError(Redirect.DISCARD)
                        .start()
                        .waitFor();
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
        return temp;
    }
}

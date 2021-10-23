/*
 * Copyright (C) 2021 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.operation;

import org.junit.jupiter.api.Test;

import psychopath.Directory;
import psychopath.File;
import psychopath.Folder;
import psychopath.LocationTestHelper;
import psychopath.Locator;

/**
 * 
 */
class TrackMovingToTest extends LocationTestHelper {

    @Test
    void file() {
        File file = locateFile("file", "value");
        long size = file.size();

        Directory dest = locateDirectory("destination");
        File moved = dest.file("file");
        assert moved.isAbsent();

        // operation
        file.trackMovingTo(dest).to(progress -> {
            assert progress.location == file;
            assert progress.totalFiles == 1;
            assert progress.totalSize == size;

            assert progress.completedFiles() == 0;
            assert progress.completedSize() == 0;

            assert progress.remainingFiles() == 1;
            assert progress.remainingSize() == size;

            assert progress.rateByFiles() == 0;
            assert progress.rateBySize() == 0;
        });

        assert moved.isPresent();
        assert moved.size() == size;
    }

    @Test
    void directory() {
        Directory dir = locateDirectory("dir", root -> {
            root.file("file1", "a");
            root.file("file2", "b");
            root.file("file3", "c");
        });
        long size = dir.file("file1").size();

        Directory dest = locateDirectory("destination");

        // operation
        dir.trackMovingTo(dest).to(progress -> {
            assert progress.totalFiles == 3;
            assert progress.totalSize == size * 3;

            if (progress.location.name().equals("file1")) {
                assert progress.completedFiles() == 0;
                assert progress.completedSize() == 0;
                assert progress.remainingFiles() == 3;
                assert progress.remainingSize() == size * 3;
                assert progress.rateByFiles() == 0;
                assert progress.rateBySize() == 0;
            }

            if (progress.location.name().equals("file2")) {
                assert progress.completedFiles() == 1;
                assert progress.completedSize() == size;
                assert progress.remainingFiles() == 2;
                assert progress.remainingSize() == size * 2;
                assert progress.rateByFiles() == 33;
                assert progress.rateBySize() == 33;
            }

            if (progress.location.name().equals("file3")) {
                assert progress.completedFiles() == 2;
                assert progress.completedSize() == size * 2;
                assert progress.remainingFiles() == 1;
                assert progress.remainingSize() == size;
                assert progress.rateByFiles() == 66;
                assert progress.rateBySize() == 66;
            }
        });
    }

    @Test
    void folder() {
        Directory dir = locateDirectory("dir", root -> {
            root.file("file1", "a");
            root.file("file2", "b");
            root.file("file3", "c");
        });

        Folder folder = Locator.folder().add(dir);

        Directory dest = locateDirectory("destination");

        // operation
        folder.trackMovingTo(dest).to(progress -> {
            assert progress.totalFiles == 3;
            assert progress.totalSize == 9;

            if (progress.location.name().equals("file1")) {
                assert progress.completedFiles() == 0;
                assert progress.completedSize() == 0;
                assert progress.remainingFiles() == 3;
                assert progress.remainingSize() == 9;
                assert progress.rateByFiles() == 0;
                assert progress.rateBySize() == 0;
            }

            if (progress.location.name().equals("file2")) {
                assert progress.completedFiles() == 1;
                assert progress.completedSize() == 3;
                assert progress.remainingFiles() == 2;
                assert progress.remainingSize() == 6;
                assert progress.rateByFiles() == 33;
                assert progress.rateBySize() == 33;
            }

            if (progress.location.name().equals("file3")) {
                assert progress.completedFiles() == 2;
                assert progress.completedSize() == 6;
                assert progress.remainingFiles() == 1;
                assert progress.remainingSize() == 3;
                assert progress.rateByFiles() == 66;
                assert progress.rateBySize() == 66;
            }
        });
    }
}
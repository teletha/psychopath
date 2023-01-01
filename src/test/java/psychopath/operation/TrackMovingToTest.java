/*
 * Copyright (C) 2023 The PSYCHOPATH Development Team
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
        int[] count = {0};
        long size = dir.file("file1").size();

        Directory dest = locateDirectory("destination");

        // operation
        dir.trackMovingTo(dest).to(progress -> {
            assert progress.totalFiles == 3;
            assert progress.totalSize == size * 3;

            assert progress.completedFiles() == count[0];
            assert progress.completedSize() == count[0] * size;
            assert progress.remainingFiles() == 3 - count[0];
            assert progress.remainingSize() == (3 - count[0]) * size;
            assert progress.rateByFiles() == count[0] * 33;
            assert progress.rateBySize() == count[0] * 33;

            count[0]++;
        });
    }

    @Test
    void folder() {
        Directory dir = locateDirectory("dir", root -> {
            root.file("file1", "a");
            root.file("file2", "b");
            root.file("file3", "c");
        });
        int[] count = {0};
        long size = dir.file("file1").size();

        Folder folder = Locator.folder().add(dir);

        Directory dest = locateDirectory("destination");

        // operation
        folder.trackMovingTo(dest).to(progress -> {
            assert progress.totalFiles == 3;
            assert progress.totalSize == size * 3;

            assert progress.completedFiles() == count[0];
            assert progress.completedSize() == count[0] * size;
            assert progress.remainingFiles() == 3 - count[0];
            assert progress.remainingSize() == (3 - count[0]) * size;
            assert progress.rateByFiles() == count[0] * 33;
            assert progress.rateBySize() == count[0] * 33;

            count[0]++;
        });
    }
}
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

/**
 * You can get detailed progress information on the process.
 */
public class Progress {

    /** Total number of files to be processed */
    public final int totalFiles;

    /** Number of files processed */
    private int completedFiles;

    /** Total data size (byte) to be processed */
    public final long totalSize;

    /** Processed data size (byte) */
    private long completedSize;

    /** The starting time. (nano) */
    public final long startingTime = System.nanoTime();

    /** The current processing file. */
    public Location location;

    private long size = -1;

    /**
     * @param totalFiles
     * @param totalSize
     */
    Progress(int totalFiles, long totalSize) {
        this.totalFiles = totalFiles;
        this.totalSize = totalSize;
    }

    /**
     * Update progress.
     * 
     * @param file
     * @return
     */
    Progress update(Location file) {
        if (size != -1) {
            completedFiles++;
            completedSize += size;
        }

        location = file;
        size = location.size();

        return this;
    }

    /**
     * Calculate the number of completed files.
     * 
     * @return
     */
    public int completedFiles() {
        return completedFiles;
    }

    /**
     * Calculate the data size (byte) of completed files.
     * 
     * @return
     */
    public long completedSize() {
        return completedSize;
    }

    /**
     * Calculate the number of remaining files.
     * 
     * @return
     */
    public int remainingFiles() {
        return totalFiles - completedFiles;
    }

    /**
     * Calculate the data size (byte) of remaining files.
     * 
     * @return
     */
    public long remainingSize() {
        return totalSize - completedSize;
    }

    /**
     * Calculate the progress rate by number of files. (0 ~ 100)
     * 
     * @return
     */
    public int rateByFiles() {
        return totalFiles == 0 ? 100 : Math.round(completedFiles * 100 / totalFiles);
    }

    /**
     * Calculate the progress rate by data size. (0 ~ 100)
     * 
     * @return
     */
    public int rateBySize() {
        return totalSize == 0 ? 100 : Math.round(completedSize * 100 / totalSize);
    }

    /**
     * Estimate the elapsed time of the current processing.
     * 
     * @return
     */
    public long elapsedTime() {
        return System.nanoTime() - startingTime;
    }

    /**
     * Estimate the remaining time of the current processing.
     * 
     * @return
     */
    public long remainingTime() {
        return completedSize == 0 ? 0 : elapsedTime() * remainingSize() / completedSize;
    }
}
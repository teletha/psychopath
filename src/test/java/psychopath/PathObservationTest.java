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

import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.Disposable;
import kiss.I;
import kiss.Observer;

/**
 * @version 2018/03/31 3:00:36
 */
public class PathObservationTest {

    @RegisterExtension
    public static final CleanRoom room = new CleanRoom();

    /** The event type. */
    private static final String Created = "ENTRY_CREATE";

    /** The event type. */
    private static final String Deleted = "ENTRY_DELETE";

    /** The event type. */
    private static final String Modified = "ENTRY_MODIFY";

    /** The file system event listener. */
    private EventQueue queue = new EventQueue();

    /** The disposable instances. */
    private List<Disposable> disposables = new ArrayList();

    @BeforeEach
    public void before() {
        disposables.clear();
    }

    @AfterEach
    public void after() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

    @Test
    public void modifyFile() throws Exception {
        Path path = room.locateFile("test");

        // observe
        observe(path);

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyFileMultiple() throws Exception {
        Path path = room.locateFile("multiple");

        // observe
        observe(path);

        // modify
        write(path);
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyMultipleFilesInSameDirectory() throws Exception {
        Path path1 = room.locateFile("sameDirectory1");
        Path path2 = room.locateFile("sameDirectory2");

        // observe
        observe(path1);
        observe(path2);

        // modify and verify
        write(path1);
        verify(path1, Modified);

        // modify
        write(path2);
        verify(path2, Modified);
    }

    @Test
    public void modifyMultipleFilesInSameDirectoryButObserveOnlyOne() throws Exception {
        Path path1 = room.locateFile("sameDirectoryOnlyOne1");
        Path path2 = room.locateFile("sameDirectoryOnlyOne2");

        // observe
        observe(path1); // only 1

        // modify
        write(path1);
        write(path2);

        // verify
        verify(path1, Modified);
    }

    @Test
    public void modifyMultipleFilesInSameDirectoryButDisposeOne() throws Exception {
        Path path1 = room.locateFile("sameDirectory1");
        Path path2 = room.locateFile("sameDirectory2");

        // observe
        Disposable disposable = observe(path1);
        observe(path2);

        // modify and verify
        write(path1);
        verify(path1, Modified);

        // modify and verify
        write(path2);
        verify(path2, Modified);

        disposable.dispose();

        // modify and verify
        write(path1);
        verifyNone();

        // modify and verify
        write(path2);
        verify(path2, Modified);
    }

    @Test
    public void createFile() throws Exception {
        Path path = room.locateAbsent("test");

        // observe
        observe(path);

        // create
        create(path);

        // verify events
        verify(path, Created);
    }

    @Test
    public void createFileInDeepDirectory() throws Exception {
        Path path = room.locateAbsent("directory/child/create");
        Files.createDirectories(path.getParent());

        // observe
        observe(path);

        // create
        create(path);

        // verify events
        verify(path, Created);
    }

    @Test
    public void deleteFile() throws Exception {
        Path path = room.locateFile("test");

        // observe
        observe(path);

        // delete
        delete(path);

        // verify events
        verify(path, Deleted);
    }

    @Test
    public void deleteFileInDeepDirectory() throws Exception {
        Path path = room.locateFile("directory/child/delete");

        // observe
        observe(path.getParent().getParent());

        // delete
        delete(path);

        // verify events
        verify(path, Deleted);
    }

    @Test
    public void modifyFileObserveFromDirectory() throws Exception {
        Path path = room.locateFile("directory/file");

        // observe root
        observe(path.getParent());

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyDirectoryObserveFromDirectory() throws Exception {
        Path path = room.locateDirectory("directory/child");

        // observe
        observe(path.getParent());

        // modify
        touch(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyFileObserveFromDeepDirectory() throws Exception {
        Path path = room.locateFile("directory/child/item/deep/file");

        // observe root
        observe(path.getParent().getParent().getParent());

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyDirectoryObserveFromDeepDirectory() throws Exception {
        Path path = room.locateDirectory("directory/child/descendant");

        // observe
        observe(path.getParent().getParent());

        // modify
        touch(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyFileInCreatedDirectory() throws Exception {
        Path directory = room.locateDirectory("directory");
        Path path = room.locateAbsent("directory/child/file");

        // observe
        observe(directory);

        // create sub directory
        Files.createDirectories(path.getParent());

        // verify events
        verify(path.getParent(), Created);

        // create file in created directory
        Files.createFile(path);

        // verify events
        verify(path, Created);
    }

    @Test
    public void createFileAndDirectoryInCreatedDirectory() throws Exception {
        Path root = room.locateDirectory("directory");
        Path directory = root.resolve("child/dir");
        Path file = directory.resolve("file");

        // observe
        observe(root);

        // create sub directory
        Files.createDirectories(directory);
        verify(directory.getParent(), Created);

        // create sub file
        create(file);
        verify(file, Created);
    }

    @Test
    public void absent() throws Exception {
        Path path = room.locateAbsent("absent/file");

        observe(path);
    }

    @Test
    public void observeTwice() throws Exception {
        Path path = room.locateFile("test");

        // observe
        Disposable disposable = observe(path);

        // modify
        write(path);

        // verify events
        verify(path, Modified);

        // dispose
        disposable.dispose();

        // modify
        write(path);

        // verify events
        verifyNone();

        // observe
        disposable = observe(path);

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void dispose() throws Exception {
        Path path = room.locateFile("test");

        // observe
        Disposable disposer = observe(path);

        // dispose
        disposer.dispose();

        // modify
        write(path);

        // verify events
        verifyNone();
    }

    @Test
    public void disposeTwice() throws Exception {
        Path path = room.locateFile("test");

        // observe
        Disposable disposer = observe(path);

        // dispose
        disposer.dispose();
        disposer.dispose();

        // modify
        write(path);

        // verify events
        verifyNone();
    }

    @Test
    public void pattern() throws Exception {
        Path path = room.locateDirectory("directory");

        // observe
        observe(path, "match");

        // create
        create(path.resolve("not-match"));

        // verify events
        verifyNone();

        // create
        Path file = path.resolve("match");
        create(file);

        // verify events
        verify(file, Created);
    }

    @Test
    public void patternWildcard() throws Exception {
        Path root = room.locateDirectory("directory");
        Path child = room.locateDirectory("directory/child");
        Path descendent = room.locateDirectory("directory/child/descendent");

        // observe
        observe(root, "*");

        // create
        Path file = root.resolve("match");
        create(file);

        // verify events
        verify(file, Created);

        // create
        create(descendent.resolve("not-match"));

        // verify events
        verifyNone();

        // write
        write(file);

        // verify events
        verify(file, Modified);

        // create directory
        Path dir = root.resolve("dynamic/child");
        Files.createDirectories(dir);
        verify(dir.getParent(), Created);

        // create file in created directory
        Path deep = dir.resolve("deep");
        create(deep);
        verifyNone();

        // delete
        delete(child);

        // verify events
        verify(child, Deleted);
    }

    @Test
    public void patternWildcards() throws Exception {
        Path root = room.locateDirectory("directory");
        Path descendent = room.locateDirectory("directory/child/descendent");

        // observe
        observe(root, "**");

        // create
        Path file = root.resolve("match");
        create(file);

        // verify events
        verify(file, Created);

        // create
        file = descendent.resolve("match");
        create(file);

        // verify events
        verify(file, Created);

        // write
        write(file);

        // verify events
        verify(file, Modified);
    }

    /**
     * <p>
     * Helper method to observe the specified path.
     * </p>
     */
    private Disposable observe(Path path) {
        Disposable disposable = PsychoPath.observe(path).to(queue);

        disposables.add(disposable);

        return disposable;
    }

    /**
     * <p>
     * Helper method to observe the specified path.
     * </p>
     */
    private Disposable observe(Path path, String pattern) {
        Disposable disposable = PsychoPath.observe(path, pattern).to(queue);

        disposables.add(disposable);

        return disposable;
    }

    /**
     * <p>
     * Helper method to create file with no data.
     * </p>
     */
    private void create(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * Helper method to modify file attribute.
     * </p>
     */
    private void touch(Path path) {
        try {
            Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to write file with some data.
     * </p>
     */
    private void write(Path path) {
        try {
            Files.write(path, Arrays.asList("write"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to delete file.
     * </p>
     */
    private void delete(Path path) {
        PsychoPath.delete(path);
    }

    /**
     * <p>
     * Verify events of the specified path.
     * </p>
     */
    private void verify(Path path, String... events) {
        for (String event : events) {
            try {
                Event retrieved = queue.poll(200, MILLISECONDS);

                if (retrieved == null) {
                    throw new AssertionError(event + " event doesn't rise in '" + path + "'.");
                } else {
                    assert Files.isSameFile(path, retrieved.path) : "Expected is " + path + "   but retrieved is " + retrieved.path;
                }
            } catch (InterruptedException e) {
                throw I.quiet(e);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        // remove following events
        try {
            Event retrieved = queue.poll(10, MILLISECONDS);

            while (retrieved != null) {
                retrieved = queue.poll(10, MILLISECONDS);
            }
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Heleper method to check no event is queued.
     * </p>
     */
    private void verifyNone() {
        try {
            Event event = queue.poll(10, MILLISECONDS);

            if (event != null) {
                throw new AssertionError("The unnecessary event is found. " + event);
            }
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2011/04/03 14:14:01
     */
    @SuppressWarnings("serial")
    private static class EventQueue extends SynchronousQueue<Event> implements Observer<WatchEvent<Path>> {

        private EventQueue() {
            super(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(WatchEvent<Path> value) {
            try {
                put(new Event(value.context(), value.kind().name()));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @version 2014/07/18 22:29:13
     */
    private static class Event {

        /** The event path. */
        private final Path path;

        /** The event type. */
        private final String type;

        private Event(Path path, String type) {
            this.path = path;
            this.type = type;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Event [path=" + path + ", type=" + type + "]";
        }
    }
}

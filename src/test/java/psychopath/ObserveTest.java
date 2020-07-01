/*
 * Copyright (C) 2020 psychopath Development Team
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
import java.nio.file.Files;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import antibug.powerassert.PowerAssertOff;
import kiss.Disposable;
import kiss.I;
import kiss.Observer;

class ObserveTest extends LocationTestHelper {

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
    void before() {
        disposables.clear();
    }

    @AfterEach
    void after() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

    @Test
    void modifyFile() {
        File file = locateFile("test");

        // observe
        observe(file);

        // modify
        write(file);

        // verify events
        verify(file, Modified);
    }

    @Test
    void modifyFileMultiple() {
        File file = locateFile("multiple");

        // observe
        observe(file);

        // modify
        write(file);
        write(file);

        // verify events
        verify(file, Modified);
    }

    @Test
    void modifyMultipleFilesInSameDirectory() {
        File file1 = locateFile("sameDirectory1");
        File file2 = locateFile("sameDirectory2");

        // observe
        observe(file1);
        observe(file2);

        // modify and verify
        write(file1);
        verify(file1, Modified);

        // modify
        write(file2);
        verify(file2, Modified);
    }

    @Test
    void modifyMultipleFilesInSameDirectoryButObserveOnlyOne() {
        File file1 = locateFile("sameDirectoryOnlyOne1");
        File file2 = locateFile("sameDirectoryOnlyOne2");

        // observe
        observe(file1); // only 1

        // modify
        write(file1);
        write(file2);

        // verify
        verify(file1, Modified);
    }

    @Test
    void modifyMultipleFilesInSameDirectoryButDisposeOne() {
        File file1 = locateFile("sameDirectory1");
        File file2 = locateFile("sameDirectory2");

        // observe
        Disposable disposable = observe(file1);
        observe(file2);

        // modify and verify
        write(file1);
        verify(file1, Modified);

        // modify and verify
        write(file2);
        verify(file2, Modified);

        disposable.dispose();

        // modify and verify
        write(file1);
        verifyNone();

        // modify and verify
        write(file2);
        verify(file2, Modified);
    }

    @Test
    void createFile() {
        File file = locateAbsent("test");

        // observe
        observe(file);

        // create
        file.create();

        // verify events
        verify(file, Created);
    }

    @Test
    void createFileInDeepDirectory() {
        File file = locateAbsent("directory/child/create");
        file.parent().create();

        // observe
        observe(file);

        // create
        file.create();

        // verify events
        verify(file, Created);
    }

    @Test
    void deleteFile() {
        File file = locateFile("test");

        // observe
        observe(file);

        // delete
        file.delete();

        // verify events
        verify(file, Deleted);
    }

    @Test
    void deleteFileInDeepDirectory() {
        File file = locateFile("directory/child/delete");

        // observe
        observe(file.parent().parent());

        // delete
        file.delete();

        // verify events
        verify(file, Deleted);
    }

    @Test
    void modifyFileObserveFromDirectory() {
        File file = locateFile("directory/file");

        // observe root
        observe(file.parent());

        // modify
        write(file);

        // verify events
        verify(file, Modified);
    }

    @Test
    void modifyDirectoryObserveFromDirectory() {
        Directory directory = locateDirectory("directory/child");

        // observe
        observe(directory.parent());

        // modify
        directory.touch();

        // verify events
        verify(directory, Modified);
    }

    @Test
    void modifyFileObserveFromDeepDirectory() {
        File file = locateFile("directory/child/item/deep/file");

        // observe root
        observe(file.parent().parent().parent());

        // modify
        write(file);

        // verify events
        verify(file, Modified);
    }

    @Test
    void modifyDirectoryObserveFromDeepDirectory() {
        Directory directory = locateDirectory("directory/child/descendant");

        // observe
        observe(directory.parent().parent());

        // modify
        directory.touch();

        // verify events
        verify(directory, Modified);
    }

    @Test
    @PowerAssertOff
    void modifyFileInCreatedDirectory() {
        Directory directory = locateDirectory("directory");
        File file = locateAbsent("directory/child/file");

        // observe
        observe(directory);

        // create sub directory
        file.parent().create();

        // verify events
        verify(file.parent(), Created);

        // create file in created directory
        file.create();

        // verify events
        verify(file, Created);
    }

    @Test
    void createFileAndDirectoryInCreatedDirectory() {
        Directory root = locateDirectory("directory");
        Directory directory = root.directory("child/dir");
        File file = directory.file("file");

        // observe
        observe(root);

        // create sub directory
        directory.create();
        verify(directory.parent(), Created);

        // create sub file
        file.create();
        verify(file, Created);
    }

    @Test
    void absent() {
        File file = locateAbsent("absent/file");

        observe(file);
    }

    @Test
    void observeTwice() {
        File file = locateFile("test");

        // observe
        Disposable disposable = observe(file);

        // modify
        write(file);

        // verify events
        verify(file, Modified);

        // dispose
        disposable.dispose();

        // modify
        write(file);

        // verify events
        verifyNone();

        // observe
        disposable = observe(file);

        // modify
        write(file);

        // verify events
        verify(file, Modified);
    }

    @Test
    void dispose() {
        File file = locateFile("test");

        // observe
        Disposable disposer = observe(file);

        // dispose
        disposer.dispose();

        // modify
        write(file);

        // verify events
        verifyNone();
    }

    @Test
    void disposeTwice() {
        File file = locateFile("test");

        // observe
        Disposable disposer = observe(file);

        // dispose
        disposer.dispose();
        disposer.dispose();

        // modify
        write(file);

        // verify events
        verifyNone();
    }

    @Test
    void pattern() {
        Directory directory = locateDirectory("directory");

        // observe
        observe(directory, "match");

        // create
        directory.file("not-match").create();

        // verify events
        verifyNone();

        // create
        File file = directory.file("match");
        file.create();

        // verify events
        verify(file, Created);
    }

    @Test
    void patternWildcard() {
        Directory root = locateDirectory("directory");
        Directory child = locateDirectory("directory/child");
        Directory descendent = locateDirectory("directory/child/descendent");

        // observe
        observe(root, "*");

        // create
        File file = root.file("match");
        file.create();

        // verify events
        verify(file, Created);

        // create
        descendent.file("not-match").create();

        // verify events
        verifyNone();

        // write
        write(file);

        // verify events
        verify(file, Modified);

        // create directory
        Directory dir = root.directory("dynamic/child");
        dir.create();
        verify(dir.parent(), Created);

        // create file in created directory
        File deep = dir.file("deep");
        deep.create();
        verifyNone();

        // delete
        child.delete();

        // verify events
        verify(child, Deleted);
    }

    @Test
    void patternWildcards() {
        Directory root = locateDirectory("directory");
        Directory descendent = locateDirectory("directory/child/descendent");

        // observe
        observe(root, "**");

        // create
        File file = root.file("match");
        file.create();

        // verify events
        verify(file, Created);

        // create
        file = descendent.file("match");
        file.create();

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
    private Disposable observe(Location location) {
        Disposable disposable = location.observe().to(queue);

        disposables.add(disposable);

        return disposable;
    }

    /**
     * <p>
     * Helper method to observe the specified path.
     * </p>
     */
    private Disposable observe(Directory location, String pattern) {
        Disposable disposable = location.observe(pattern).to(queue);

        disposables.add(disposable);

        return disposable;
    }

    /**
     * Helper method to write file with some data.
     */
    private void write(File file) {
        file.text("write");
    }

    /**
     * Verify events of the specified path.
     */
    private void verify(Location location, String... events) {
        for (String event : events) {
            try {
                Event retrieved = queue.poll(200, MILLISECONDS);

                if (retrieved == null) {
                    throw new AssertionError(event + " event doesn't rise in '" + location + "'.");
                } else {
                    assert Files
                            .isSameFile(location.path, retrieved.location.path) : "Expected is " + location + "   but retrieved is " + retrieved.location;
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
    private static class EventQueue extends SynchronousQueue<Event> implements Observer<WatchEvent<Location>> {

        private EventQueue() {
            super(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(WatchEvent<Location> value) {
            try {
                put(new Event(value.context(), value.kind().name()));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * 
     */
    private static class Event {

        /** The event location. */
        private final Location location;

        /** The event type. */
        private final String type;

        private Event(Location location, String type) {
            this.location = location;
            this.type = type;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Event [path=" + location + ", type=" + type + "]";
        }
    }
}
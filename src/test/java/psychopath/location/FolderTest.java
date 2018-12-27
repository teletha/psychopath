/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package psychopath.location;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import antibug.CleanRoom.FileSystemDSL;
import psychopath.Directory;
import psychopath.File;
import psychopath.Folder;
import psychopath.LocationTestHelper;
import psychopath.Locator;

class FolderTest extends LocationTestHelper {

    @Test
    void addPathString() {
        Folder folder = Locator.folder().add("absent").add("dir/child");
        assert folder.entries().toList().size() == 2;
    }

    @Test
    void addNullPathString() {
        Folder folder = Locator.folder().add((String) null);
        assert folder.entries().toList().size() == 0;
    }

    @Test
    void addPath() {
        Folder folder = Locator.folder().add(Path.of("absent")).add(Path.of("dir/child"));
        assert folder.entries().toList().size() == 2;
    }

    @Test
    void addNullPath() {
        Folder folder = Locator.folder().add((Path) null);
        assert folder.entries().toList().size() == 0;
    }

    @Test
    void addFolder() {
        Folder folder1 = Locator.folder().add("from1");
        Folder folder2 = Locator.folder().add("from2");

        Folder merged = Locator.folder().add(folder1).add(folder2);
        assert merged.entries().toList().size() == 2;

        // changing original sources doesn't effect meger folder
        folder1.add("no effect1");
        folder2.add("no effect2");
        assert merged.entries().toList().size() == 2;
    }

    @Test
    void addNullFolder() {
        Folder folder = Locator.folder().add((Folder) null);
        assert folder.entries().toList().size() == 0;
    }

    @Test
    void addFile() {
        Folder folder = Locator.folder().add(Locator.file("absent")).add(Locator.file("dir/child"));
        assert folder.entries().toList().size() == 2;
    }

    @Test
    void addNullFile() {
        Folder folder = Locator.folder().add((File) null);
        assert folder.entries().toList().size() == 0;
    }

    @Test
    void addDirectory() {
        Folder folder = Locator.folder().add(Locator.directory("absent")).add(Locator.directory("dir/child"));
        assert folder.entries().toList().size() == 2;
    }

    @Test
    void addNullDirectory() {
        Folder folder = Locator.folder().add((Directory) null);
        assert folder.entries().toList().size() == 0;
    }

    @Test
    void destinationPathString() {
        Folder folder = Locator.folder().add("lib", e -> e.add(locateFile("one.jar")).add(locateFile("other.jar")));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.file("one.jar");
                $.file("other.jar");
            });
        });
    }

    @Test
    void destinationPath() {
        Folder folder = Locator.folder().add(Path.of("lib"), e -> e.add(locateFile("one.jar")).add(locateFile("other.jar")));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.file("one.jar");
                $.file("other.jar");
            });
        });
    }

    @Test
    void destinationDirectory() {
        Folder folder = Locator.folder().add(Locator.directory("lib"), e -> e.add(locateFile("one.jar")).add(locateFile("other.jar")));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.file("one.jar");
                $.file("other.jar");
            });
        });
    }

    @Test
    void destinationAddDirectory() {
        Folder folder = Locator.folder().add(Locator.directory("lib"), e -> e.add(locateDirectory("sub", $ -> {
            $.file("one.jar");
            $.file("other.jar");
        })));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.dir("sub", () -> {
                    $.file("one.jar");
                    $.file("other.jar");
                });
            });
        });
    }

    @Test
    void destinationAddDirectoryPattern() {
        Folder folder = Locator.folder().add(Locator.directory("lib"), e -> e.add(locateDirectory("sub", $ -> {
            $.file("one.jar");
            $.file("other.jar");
            $.file("no-match.txt");
        }), "**.jar"));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.dir("sub", () -> {
                    $.file("one.jar");
                    $.file("other.jar");
                });
            });
        });
    }

    /**
     * Helper method to test copied, packed and moved file structures.
     * 
     * @param folder
     * @param expected
     * @return
     */
    private boolean matchDestination(Folder folder, Consumer<FileSystemDSL> expected) {
        assert matchCopyDestination(folder, expected);
        assert matchPackDestination(folder, expected);
        assert matchMoveDestination(folder, expected);

        return true;
    }

    /**
     * Helper method to test copied file structure.
     * 
     * @param folder
     * @param expected
     * @return
     */
    private boolean matchCopyDestination(Folder folder, Consumer<FileSystemDSL> expected) {
        assert match(folder.copyTo(locateDirectory("copy")), expected);

        return true;
    }

    /**
     * Helper method to test packed file structure.
     * 
     * @param folder
     * @param expected
     * @return
     */
    private boolean matchPackDestination(Folder folder, Consumer<FileSystemDSL> expected) {
        assert match(folder.packTo(locateFile("pack.zip")).unpackToTemporary(), expected);

        return true;
    }

    /**
     * Helper method to test moved file structure.
     * 
     * @param folder
     * @param expected
     * @return
     */
    private boolean matchMoveDestination(Folder folder, Consumer<FileSystemDSL> expected) {
        assert match(folder.moveTo(locateDirectory("move")), expected);

        return true;
    }

    @Test
    void addSignalDirectory() {
        Folder temporary = Locator.folder();

        Directory root = locateDirectory("root", $ -> {
            $.dir("child1", () -> {
                $.file("11.txt");
                $.file("12.txt");
            });
            $.dir("child2", () -> {
                $.file("21.txt");
                $.file("22.txt");
            });
            $.dir("sub3", () -> {
                $.file("31.txt");
                $.file("32.txt");
            });
        });

        temporary.add(root.walkDirectories("child*")).delete("*1.txt");

        assert match(root, $ -> {
            $.dir("child1", () -> {
                $.file("12.txt");
            });
            $.dir("child2", () -> {
                $.file("22.txt");
            });
            $.dir("sub3", () -> {
                $.file("31.txt");
                $.file("32.txt");
            });
        });
    }

    @Test
    void moveTo() {
        Folder temporary = Locator.folder();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        Directory destination = temporary.add(dir1).add(dir2).moveTo(locateDirectory("dir3"));

        assert dir1.isAbsent();
        assert dir2.isAbsent();
        assert match(destination, $ -> {
            $.dir("dir1", () -> {
                $.file("1.txt");
                $.file("2.txt");
                $.dir("3", () -> {
                    $.file("3.txt");
                });
            });
            $.dir("dir2", () -> {
                $.file("a.txt");
                $.file("b.txt");
                $.dir("c", () -> {
                    $.file("c.txt");
                });
            });
        });
    }

    @Test
    void copyTo() {
        Folder temporary = Locator.folder();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.txt");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        Directory destination = temporary.add(dir1).add(dir2).copyTo(locateDirectory("dir3"));

        assert dir1.isPresent();
        assert dir2.isPresent();
        assert match(destination, $ -> {
            $.dir("dir1", () -> {
                $.file("1.txt");
                $.file("2.txt");
                $.dir("3", () -> {
                    $.file("3.txt");
                });
            });
            $.dir("dir2", () -> {
                $.file("a.txt");
                $.file("b.txt");
                $.dir("c", () -> {
                    $.file("c.txt");
                });
            });
        });
    }

    @Test
    void copyToPatterns() {
        Folder temporary = Locator.folder();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.xml");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.xml");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        Directory destination = temporary.add(dir1).add(dir2).copyTo(locateDirectory("dir3"), "**", "!**.xml");

        assert dir1.isPresent();
        assert dir2.isPresent();
        assert match(destination, $ -> {
            $.file("1.txt");
            $.dir("3", () -> {
                $.file("3.txt");
            });
            $.file("a.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });
    }

    @Test
    void delete() {
        Folder temporary = Locator.folder();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.java");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.java");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        temporary.add(dir1, "**.text").add(dir2, "**.java").delete();

        assert match(dir1, $ -> {
            $.file("1.txt");
        });
        assert match(dir2, $ -> {
            $.file("a.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });
    }

    @Test
    void walk() {
        Folder temporary = Locator.folder();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.txt");
            $.file("2.java");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.txt");
            $.file("b.java");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        List<File> files = temporary.add(dir1).add(dir2).walkFiles().toList();
        assert files.size() == 6;
    }

    @Test
    void combinePattern() {
        Folder temporary = Locator.folder();

        Directory dir1 = locateDirectory("dir1", $ -> {
            $.file("1.xml");
            $.file("2.java");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.file("a.xml");
            $.file("b.java");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });

        temporary.add(dir1, "**.text").add(dir2, "**.java").delete("**.xml");

        assert match(dir1, $ -> {
            $.file("2.java");
        });
        assert match(dir2, $ -> {
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });
    }
}

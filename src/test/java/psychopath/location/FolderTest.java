/*
 * Copyright (C) 2019 psychopath Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath.location;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import antibug.CleanRoom.FileSystemDSL;
import kiss.I;
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
        Directory dir1 = locateDirectory("dir1", $ -> {
            $.dir("child1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("child2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.dir("sub1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("sub2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Folder folder = Locator.folder().add(dir1).add(dir2);

        assert matchDestination(folder, $ -> {
            $.dir("dir1", () -> {
                $.dir("child1", () -> {
                    $.file("11.jar");
                    $.file("12.txt");
                });
                $.dir("child2", () -> {
                    $.file("21.jar");
                    $.file("22.txt");
                });
            });
            $.dir("dir2", () -> {
                $.dir("sub1", () -> {
                    $.file("11.jar");
                    $.file("12.txt");
                });
                $.dir("sub2", () -> {
                    $.file("21.jar");
                    $.file("22.txt");
                });
            });
        });
    }

    @Test
    void addNullDirectory() {
        Folder folder = Locator.folder().add((Directory) null);
        assert folder.entries().toList().size() == 0;
    }

    @Test
    void addDirectoryWithAddPattern() {
        Directory dir1 = locateDirectory("dir1", $ -> {
            $.dir("child1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("child2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.dir("sub1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("sub2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Folder folder = Locator.folder().add(dir1, "**.jar").add(dir2, "**.txt");

        assert matchDestination(folder, $ -> {
            $.dir("dir1", () -> {
                $.dir("child1", () -> {
                    $.file("11.jar");
                });
                $.dir("child2", () -> {
                    $.file("21.jar");
                });
            });
            $.dir("dir2", () -> {
                $.dir("sub1", () -> {
                    $.file("12.txt");
                });
                $.dir("sub2", () -> {
                    $.file("22.txt");
                });
            });
        });
    }

    @Test
    @Disabled
    void addDirectoryWithAddPattern2() {
        Directory dir1 = locateDirectory("dir1", $ -> {
            $.dir("child1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("child2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.dir("sub1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("sub2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Folder folder = Locator.folder().add(dir1, "**").add(dir2, "**");

        assert matchDestination(folder, $ -> {
            $.dir("child1", () -> {
                $.file("11.jar");
            });
            $.dir("child2", () -> {
                $.file("21.jar");
            });
            $.dir("sub1", () -> {
                $.file("12.txt");
            });
            $.dir("sub2", () -> {
                $.file("22.txt");
            });
        });
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
    void addDirectorySignal() {
        Directory dir1 = locateDirectory("dir1", $ -> {
            $.dir("child1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("child2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Directory dir2 = locateDirectory("dir2", $ -> {
            $.dir("sub1", () -> {
                $.file("11.jar");
                $.file("12.txt");
            });
            $.dir("sub2", () -> {
                $.file("21.jar");
                $.file("22.txt");
            });
        });

        Folder folder = Locator.folder().add(I.signal(dir1, dir2));

        assert matchDestination(folder, $ -> {
            $.dir("dir1", () -> {
                $.dir("child1", () -> {
                    $.file("11.jar");
                    $.file("12.txt");
                });
                $.dir("child2", () -> {
                    $.file("21.jar");
                    $.file("22.txt");
                });
            });
            $.dir("dir2", () -> {
                $.dir("sub1", () -> {
                    $.file("11.jar");
                    $.file("12.txt");
                });
                $.dir("sub2", () -> {
                    $.file("21.jar");
                    $.file("22.txt");
                });
            });
        });
    }

    @Test
    void destinationPathString() {
        Folder folder = Locator.folder().addIn("lib", e -> e.add(locateFile("one.jar")).add(locateFile("other.jar")));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.file("one.jar");
                $.file("other.jar");
            });
        });
    }

    @Test
    void destinationPath() {
        Folder folder = Locator.folder().addIn(Path.of("lib"), e -> e.add(locateFile("one.jar")).add(locateFile("other.jar")));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.file("one.jar");
                $.file("other.jar");
            });
        });
    }

    @Test
    void destinationDirectory() {
        Folder folder = Locator.folder().addIn(Locator.directory("lib"), e -> e.add(locateFile("one.jar")).add(locateFile("other.jar")));

        assert matchDestination(folder, $ -> {
            $.dir("lib", () -> {
                $.file("one.jar");
                $.file("other.jar");
            });
        });
    }

    @Test
    void destinationAddDirectory() {
        Folder folder = Locator.folder().addIn(Locator.directory("lib"), e -> e.add(locateDirectory("sub", $ -> {
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
    void destinationAddDirectoryWithPattern() {
        Folder folder = Locator.folder().addIn("lib", e -> e.add(locateDirectory("sub", $ -> {
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
    void copy() {
        File file = locateFile("file.txt");
        Directory directory = locateDirectory("dir", $ -> {
            $.file("1.txt");
            $.file("2.java");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });

        Directory output = Locator.folder()
                .add(file)
                .add(directory)
                .add(directory, o -> o.glob("**").ignoreRoot())
                .copyTo(locateDirectory("output"));
        assert match(output, $ -> {
            $.file("file.txt");
            $.dir("dir", () -> {
                $.file("1.txt");
                $.file("2.java");
                $.dir("3", () -> {
                    $.file("3.txt");
                });
            });
            $.file("1.txt");
            $.file("2.java");
            $.dir("3", () -> {
                $.file("3.txt");
            });
        });
    }

    @Test
    void delete() {
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
        assert dir1.isPresent();
        assert dir2.isPresent();

        Locator.folder().add(dir1).add(dir2).delete();
        assert dir1.isAbsent();
        assert dir2.isAbsent();
    }

    @Test
    void deleteWithPattern() {
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

        Locator.folder().add(dir1).add(dir2).delete("**.txt");

        assert match(dir1, $ -> {
            $.file("2.java");
        });
        assert match(dir2, $ -> {
            $.file("b.java");
        });
    }

    @Test
    void deleteWithAddPattern() {
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

        Locator.folder().add(dir1, "**.txt").add(dir2, "**.java").delete();

        assert match(dir1, $ -> {
            $.file("2.java");
        });
        assert match(dir2, $ -> {
            $.file("a.txt");
            $.dir("c", () -> {
                $.file("c.txt");
            });
        });
    }

    @Test
    void deleteWithCombinePattern() {
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

        Locator.folder().add(dir1, "**.txt").add(dir2, "**.java").delete("**c.txt");

        assert match(dir1, $ -> {
            $.file("2.java");
        });
        assert match(dir2, $ -> {
            $.file("a.txt");
        });
    }

    @Test
    void walkFiles() {
        File file1 = locateFile("file1.txt");
        File file2 = locateFile("file2.txt");
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

        assert Locator.folder().add(file1).add(file2).add(dir1).add(dir2).walkFiles().toList().size() == 8;
        // pattern
        assert Locator.folder().add(file1).add(file2).add(dir1).add(dir2).walkFiles("**.java").toList().size() == 2;
        assert Locator.folder().add(file1).add(file2).add(dir1).add(dir2).walkFiles("**.txt", "!c/**").toList().size() == 5;
        // add pattern
        assert Locator.folder().add(dir1, "**.java").add(dir2, "**.txt").walkFiles().toList().size() == 3;
        assert Locator.folder().add(dir1, "!**.java").add(dir2, "!**.txt").walkFiles().toList().size() == 3;
        // combine pattern
        assert Locator.folder().add(dir1, "**.java").add(dir2, "**.txt").walkFiles().toList().size() == 3;
        assert Locator.folder().add(dir1, "!**.java").add(dir2, "!**.txt").walkFiles().toList().size() == 3;
    }
}

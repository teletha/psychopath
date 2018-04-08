/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package psychopath;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import kiss.I;

/**
 * @version 2018/03/31 3:00:31
 */
class PathOperationTestHelper {

    /**
     * <p>
     * Helper method to check {@link Path} equality as file.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean sameFile(Path one, Path other) {
        try {
            assert exist(one, other);
            assert file(one, other);
            assert sameLastModified(one, other);
            assert Files.size(one) == Files.size(other);
            assert checksum(one) == checksum(other);
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} equality as directory.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean sameDirectory(Path one, Path other) {
        try {
            assert exist(one, other);
            assert directory(one, other);
            assert sameLastModified(one, other);

            Iterator<Path> oneChildren = Files.list(one).iterator();
            Iterator<Path> otherChildren = Files.list(other).iterator();

            while (oneChildren.hasNext()) {
                assert otherChildren.hasNext();

                Path oneChild = oneChildren.next();
                Path otherChild = otherChildren.next();

                if (Files.isRegularFile(oneChild)) {
                    assert sameFile(oneChild, otherChild);
                } else if (Files.isDirectory(oneChild)) {
                    assert sameDirectory(oneChild, otherChild);
                }
            }
            assert !otherChildren.hasNext();
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} attributes.
     * </p>
     * 
     * @param paths A path set to check.
     * @return A test result.
     */
    protected static boolean sameLastModified(Path one, Path other) {
        try {
            assert Files.getLastModifiedTime(one).toInstant().getEpochSecond() == Files.getLastModifiedTime(other)
                    .toInstant()
                    .getEpochSecond();
        } catch (Exception e) {
            throw I.quiet(e);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to compute {@link Path} checksume.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static long checksum(Path path) {
        try {
            CRC32 crc = new CRC32();
            crc.update(Files.readAllBytes(path));

            return crc.getValue();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to check {@link Path} existence.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean exist(Path... paths) {
        for (Path path : paths) {
            assert Files.exists(path);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} existence.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean notExist(Path... paths) {
        for (Path path : paths) {
            assert Files.exists(path) == false;
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} kind.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean file(Path... paths) {
        for (Path path : paths) {
            if (isZip(path) && path.toString().equals("/")) {
                // archive root directory is as archive file
            } else {
                assert Files.isRegularFile(path);
            }
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} kind.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean directory(Path... paths) {
        for (Path path : paths) {
            assert Files.isDirectory(path);
        }
        return true;
    }

    /**
     * <p>
     * Create copy of the specified path.
     * </p>
     * 
     * @param path
     * @return
     */
    protected static Path snapshot(Path path) {
        try {
            Path temp = PsychoPath.locateTemporary();
            Files.createDirectories(temp);
            PsychoPath.copy(path, temp);
            return temp.resolve(path.getFileName());
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Collect children paths.
     * </p>
     * 
     * @param path
     * @return
     */
    protected static List<Path> children(Path path) {
        try {
            List<Path> list = new ArrayList();

            Files.walkFileTree(path, new FileVisitor<Path>() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir != path) list.add(dir);
                    return FileVisitResult.SKIP_SUBTREE;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    list.add(file);
                    return FileVisitResult.CONTINUE;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

            return list;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to check zip path.
     * </p>
     * 
     * @param path
     * @return
     */
    private static boolean isZip(Path path) {
        return path.getClass().getSimpleName().endsWith("ZipPath");
    }
}

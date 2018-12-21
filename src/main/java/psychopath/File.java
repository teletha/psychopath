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

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.StreamingNotSupportedException;

import kiss.I;
import kiss.Signal;
import kiss.WiseRunnable;

public class File extends Location<File> {

    /**
     * @param path
     */
    File(Path path) {
        super(path);
    }

    /**
     * Retrieve the base name of this {@link File}.
     * 
     * @return A base name.
     */
    public final String base() {
        String name = name();
        int index = name.lastIndexOf(".");
        return index == -1 ? name : name.substring(0, index);
    }

    /**
     * Locate {@link File} with the specified new base name, but extension is same.
     * 
     * @param newBaseName A new base name.
     * @return New located {@link File}.
     */
    public final File base(String newBaseName) {
        String extension = extension();
        return Locator.file(path.resolveSibling(extension.isEmpty() ? newBaseName : newBaseName + "." + extension));
    }

    /**
     * Retrieve the extension of this {@link File}.
     * 
     * @return An extension or empty if it has no extension.
     */
    public final String extension() {
        String name = name();
        int index = name.lastIndexOf(".");
        return index == -1 ? "" : name.substring(index + 1);
    }

    /**
     * Locate {@link File} with the specified new extension, but base name is same.
     * 
     * @param newExtension A new extension.
     * @return New located {@link File}.
     */
    public final File extension(String newExtension) {
        return Locator.file(path.resolveSibling(base() + "." + newExtension));
    }

    /**
     * Cast to archive.
     * 
     * @return
     */
    public Directory asArchive() {
        return new Directory(Archive.detectFileSystetm(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location<?>> children() {
        return Signal.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Location<?>> descendant() {
        return Signal.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Directory> asDirectory() {
        return Signal.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<File> asFile() {
        return I.signal(this);
    }

    public long lastModified() {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveTo(Directory destination) {
        if (isPresent()) {
            try {
                destination.create();
                Files.move(path, destination.file(name()).path, ATOMIC_MOVE, REPLACE_EXISTING);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void moveTo(File destination) {
        if (isPresent()) {
            try {
                destination.parent().create();
                Files.move(path, destination.path, REPLACE_EXISTING);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyTo(Directory destination) {
        if (isPresent()) {
            try {
                destination.create();
                Files.copy(path, destination.file(name()).path, REPLACE_EXISTING, COPY_ATTRIBUTES);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void copyTo(File destination) {
        if (isPresent()) {
            try {
                destination.parent().create();
                Files.copy(path, destination.path, REPLACE_EXISTING, COPY_ATTRIBUTES);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Copies all bytes from a file to an output stream.
     * <p>
     * If an I/O error occurs reading from the file or writing to the output stream, then it may do
     * so after some bytes have been read or written. Consequently the output stream may be in an
     * inconsistent state. It is strongly recommended that the output stream be promptly closed if
     * an I/O error occurs.
     * <p>
     * This method may block indefinitely writing to the output stream (or reading from the file).
     * The behavior for the case that the output stream is <i>asynchronously closed</i> or the
     * thread interrupted during the copy is highly output stream and file system provider specific
     * and therefore not specified.
     * <p>
     * Note that if the given output stream is {@link java.io.Flushable} then its
     * {@link java.io.Flushable#flush flush} method may need to invoked after this method completes
     * so as to flush any buffered output.
     *
     * @param destination the output stream to write to.
     * @return the number of bytes read or written
     * @throws IOException if an I/O error occurs when reading or writing
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public long copyTo(OutputStream destination) {
        try (OutputStream out = destination) {
            return Files.copy(path, out);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<WatchEvent<Location>> observe() {
        return parent().observe(name());
    }

    /**
     * Unpack archive file to the same directory that this {@link File} exists.
     * 
     * @param options A list of options.
     * @return An unpacked directory.
     */
    public Directory unpack(UnpackOption... options) {
        return unpackTo(absolutize().parent().directory(base()), options);
    }

    /**
     * Unpack archive file to the specified directory.
     * 
     * @param destination A destination directory to unpack.
     * @param options A list of options.
     * @return An unpacked directory.
     */
    public Directory unpackTo(Directory destination, UnpackOption... options) {
        return unpackTo(destination, e -> {
        }, options);
    }

    /**
     * Unpack archive file to the specified directory.
     * 
     * @param destination A destination directory to unpack.
     * @param listener An unpack event listener.
     * @param options A list of options.
     * @return An unpacked directory.
     */
    public Directory unpackTo(Directory destination, Consumer<File> listener, UnpackOption... options) {
        try (ArchiveInputStream in = new ArchiveStreamFactory()
                .createArchiveInputStream(extension().replaceAll("7z", "7z-override"), newInputStream())) {
            destination.create();

            ArchiveEntry entry = null;
            while ((entry = in.getNextEntry()) != null) {
                if (in.canReadEntryData(entry)) {
                    if (entry.isDirectory()) {
                        destination.directory(entry.getName()).create();
                    } else {
                        File file = destination.file(entry.getName());

                        try (OutputStream out = file.newOutputStream()) {
                            I.copy(in, out, false);
                            listener.accept(file);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }

        for (UnpackOption option : options) {
            option.process(destination);
        }
        return destination;
    }

    /**
     * Unpack archive file to the same directory that this {@link File} exists.
     * 
     * @param options A list of options.
     * @return An unpacked directory.
     */
    public Directory unpackToTemporary(UnpackOption... options) {
        return unpackTo(Locator.temporaryDirectory(), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create() {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileLock lock(WiseRunnable failed) {
        try {
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, CREATE, WRITE);

            return I.signal(channel)
                    .map(c -> c.tryLock())
                    .take(lock -> lock.isValid())
                    .retryWhen(NullPointerException.class, e -> e.effect(failed).wait(300, MILLISECONDS).take(10))
                    .to().v;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Opens a file, returning an input stream to read from the file. The stream will not be
     * buffered, and is not required to support the {@link InputStream#mark mark} or
     * {@link InputStream#reset reset} methods. The stream will be safe for access by multiple
     * concurrent threads. Reading commences at the beginning of the file. Whether the returned
     * stream is <i>asynchronously closeable</i> and/or <i>interruptible</i> is highly file system
     * provider specific and therefore not specified.
     * <p>
     * The {@code options} parameter determines how the file is opened. If no options are present
     * then it is equivalent to opening the file with the {@link StandardOpenOption#READ READ}
     * option. In addition to the {@code
     * READ} option, an implementation may also support additional implementation specific options.
     *
     * @param options options specifying how the file is opened
     * @return a new input stream
     * @throws IllegalArgumentException if an invalid combination of options is specified
     * @throws UnsupportedOperationException if an unsupported option is specified
     * @throws IOException if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public InputStream newInputStream(OpenOption... options) {
        try {
            return Files.newInputStream(path, options);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Opens or creates a file, returning an output stream that may be used to write bytes to the
     * file. The resulting stream will not be buffered. The stream will be safe for access by
     * multiple concurrent threads. Whether the returned stream is <i>asynchronously closeable</i>
     * and/or <i>interruptible</i> is highly file system provider specific and therefore not
     * specified.
     * <p>
     * This method opens or creates a file in exactly the manner specified by the
     * {@link #newByteChannel(Path,Set,FileAttribute[]) newByteChannel} method with the exception
     * that the {@link StandardOpenOption#READ READ} option may not be present in the array of
     * options. If no options are present then this method works as if the
     * {@link StandardOpenOption#CREATE CREATE}, {@link StandardOpenOption#TRUNCATE_EXISTING
     * TRUNCATE_EXISTING}, and {@link StandardOpenOption#WRITE WRITE} options are present. In other
     * words, it opens the file for writing, creating the file if it doesn't exist, or initially
     * truncating an existing {@link #isRegularFile regular-file} to a size of {@code 0} if it
     * exists.
     * <p>
     *
     * @param options options specifying how the file is opened
     * @return a new output stream
     * @throws IllegalArgumentException if {@code options} contains an invalid combination of
     *             options
     * @throws UnsupportedOperationException if an unsupported option is specified
     * @throws IOException if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkWrite(String) checkWrite} method is
     *             invoked to check write access to the file. The
     *             {@link SecurityManager#checkDelete(String) checkDelete} method is invoked to
     *             check delete access if the file is opened with the {@code DELETE_ON_CLOSE}
     *             option.
     */
    public OutputStream newOutputStream(OpenOption... options) {
        try {
            parent().create();
            return Files.newOutputStream(path, options);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Opens or creates a file, returning a seekable byte channel to access the file.
     * <p>
     * This method opens or creates a file in exactly the manner specified by the
     * {@link #newByteChannel(Path,Set,FileAttribute[]) newByteChannel} method.
     *
     * @param options options specifying how the file is opened
     * @return a new seekable byte channel
     * @throws IllegalArgumentException if the set contains an invalid combination of options
     * @throws UnsupportedOperationException if an unsupported open option is specified
     * @throws FileAlreadyExistsException if a file of that name already exists and the
     *             {@link StandardOpenOption#CREATE_NEW CREATE_NEW} option is specified <i>(optional
     *             specific exception)</i>
     * @throws IOException if an I/O error occurs
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the path if the file is opened for reading. The
     *             {@link SecurityManager#checkWrite(String) checkWrite} method is invoked to check
     *             write access to the path if the file is opened for writing. The
     *             {@link SecurityManager#checkDelete(String) checkDelete} method is invoked to
     *             check delete access if the file is opened with the {@code DELETE_ON_CLOSE}
     *             option.
     * @see java.nio.channels.FileChannel#open(Path,OpenOption[])
     */
    public SeekableByteChannel newByteChannel(OpenOption... options) {
        try {
            return Files.newByteChannel(path, options);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Reads all the bytes from a file. The method ensures that the file is closed when all bytes
     * have been read or an I/O error, or other runtime exception, is thrown.
     * <p>
     * Note that this method is intended for simple cases where it is convenient to read all bytes
     * into a byte array. It is not intended for reading in large files.
     *
     * @param path the path to the file
     * @return a byte array containing the bytes read from the file
     * @throws IOException if an I/O error occurs reading from the stream
     * @throws OutOfMemoryError if an array of the required size cannot be allocated, for example
     *             the file is larger that {@code 2GB}
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public byte[] bytes() {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /**
     * Reads all content from a file into a string, decoding from bytes to characters using the
     * {@link StandardCharsets#UTF_8 UTF-8} {@link Charset charset}. The method ensures that the
     * file is closed when all content have been read or an I/O error, or other runtime exception,
     * is thrown.
     * <p>
     * This method is equivalent to: {@code readString(path, StandardCharsets.UTF_8) }
     *
     * @return a String containing the content read from the file
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable
     *             byte sequence is read
     * @throws OutOfMemoryError if the file is extremely large, for example larger than {@code 2GB}
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public String text() {
        return text(StandardCharsets.UTF_8);
    }

    /**
     * Reads all characters from a file into a string, decoding from bytes to characters using the
     * specified {@linkplain Charset charset}. The method ensures that the file is closed when all
     * content have been read or an I/O error, or other runtime exception, is thrown.
     * <p>
     * This method reads all content including the line separators in the middle and/or at the end.
     * The resulting string will contain line separators as they appear in the file.
     *
     * @apiNote This method is intended for simple cases where it is appropriate and convenient to
     *          read the content of a file into a String. It is not intended for reading very large
     *          files.
     * @param cs the charset to use for decoding
     * @return a String containing the content read from the file
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable
     *             byte sequence is read
     * @throws OutOfMemoryError if the file is extremely large, for example larger than {@code 2GB}
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public String text(Charset charset) {
        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /**
     * Write lines of text to a file. Each line is a char sequence and is written to the file in
     * sequence with each line terminated by the platform's line separator, as defined by the system
     * property {@code
     * line.separator}. Characters are encoded into bytes using the specified charset.
     * <p>
     * The {@code options} parameter specifies how the file is created or opened. If no options are
     * present then this method works as if the {@link StandardOpenOption#CREATE CREATE},
     * {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and
     * {@link StandardOpenOption#WRITE WRITE} options are present. In other words, it opens the file
     * for writing, creating the file if it doesn't exist, or initially truncating an existing
     * {@link #isRegularFile regular-file} to a size of {@code 0}. The method ensures that the file
     * is closed when all lines have been written (or an I/O error or other runtime exception is
     * thrown). If an I/O error occurs then it may do so after the file has been created or
     * truncated, or after some bytes have been written to the file.
     * 
     * @param lines an object to iterate over the char sequences
     * @return the path
     * @throws IllegalArgumentException if {@code options} contains an invalid combination of
     *             options
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text
     *             cannot be encoded using the specified charset
     * @throws UnsupportedOperationException if an unsupported option is specified
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkWrite(String) checkWrite} method is
     *             invoked to check write access to the file. The
     *             {@link SecurityManager#checkDelete(String) checkDelete} method is invoked to
     *             check delete access if the file is opened with the {@code DELETE_ON_CLOSE}
     *             option.
     */
    public void text(String... lines) {
        text(StandardCharsets.UTF_8, lines);
    }

    /**
     * Write lines of text to a file. Each line is a char sequence and is written to the file in
     * sequence with each line terminated by the platform's line separator, as defined by the system
     * property {@code
     * line.separator}. Characters are encoded into bytes using the specified charset.
     * <p>
     * The {@code options} parameter specifies how the file is created or opened. If no options are
     * present then this method works as if the {@link StandardOpenOption#CREATE CREATE},
     * {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and
     * {@link StandardOpenOption#WRITE WRITE} options are present. In other words, it opens the file
     * for writing, creating the file if it doesn't exist, or initially truncating an existing
     * {@link #isRegularFile regular-file} to a size of {@code 0}. The method ensures that the file
     * is closed when all lines have been written (or an I/O error or other runtime exception is
     * thrown). If an I/O error occurs then it may do so after the file has been created or
     * truncated, or after some bytes have been written to the file.
     * 
     * @param lines an object to iterate over the char sequences
     * @return the path
     * @throws IllegalArgumentException if {@code options} contains an invalid combination of
     *             options
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text
     *             cannot be encoded using the specified charset
     * @throws UnsupportedOperationException if an unsupported option is specified
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkWrite(String) checkWrite} method is
     *             invoked to check write access to the file. The
     *             {@link SecurityManager#checkDelete(String) checkDelete} method is invoked to
     *             check delete access if the file is opened with the {@code DELETE_ON_CLOSE}
     *             option.
     */
    public void text(Iterable<String> lines) {
        text(StandardCharsets.UTF_8, lines);
    }

    /**
     * Write lines of text to a file. Each line is a char sequence and is written to the file in
     * sequence with each line terminated by the platform's line separator, as defined by the system
     * property {@code
     * line.separator}. Characters are encoded into bytes using the specified charset.
     * <p>
     * The {@code options} parameter specifies how the file is created or opened. If no options are
     * present then this method works as if the {@link StandardOpenOption#CREATE CREATE},
     * {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and
     * {@link StandardOpenOption#WRITE WRITE} options are present. In other words, it opens the file
     * for writing, creating the file if it doesn't exist, or initially truncating an existing
     * {@link #isRegularFile regular-file} to a size of {@code 0}. The method ensures that the file
     * is closed when all lines have been written (or an I/O error or other runtime exception is
     * thrown). If an I/O error occurs then it may do so after the file has been created or
     * truncated, or after some bytes have been written to the file.
     * 
     * @param charset the charset to use for encoding
     * @param lines an object to iterate over the char sequences
     * @return the path
     * @throws IllegalArgumentException if {@code options} contains an invalid combination of
     *             options
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text
     *             cannot be encoded using the specified charset
     * @throws UnsupportedOperationException if an unsupported option is specified
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkWrite(String) checkWrite} method is
     *             invoked to check write access to the file. The
     *             {@link SecurityManager#checkDelete(String) checkDelete} method is invoked to
     *             check delete access if the file is opened with the {@code DELETE_ON_CLOSE}
     *             option.
     */
    public void text(Charset charset, String... lines) {
        text(charset, List.of(lines));
    }

    /**
     * Write lines of text to a file. Each line is a char sequence and is written to the file in
     * sequence with each line terminated by the platform's line separator, as defined by the system
     * property {@code
     * line.separator}. Characters are encoded into bytes using the specified charset.
     * <p>
     * The {@code options} parameter specifies how the file is created or opened. If no options are
     * present then this method works as if the {@link StandardOpenOption#CREATE CREATE},
     * {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and
     * {@link StandardOpenOption#WRITE WRITE} options are present. In other words, it opens the file
     * for writing, creating the file if it doesn't exist, or initially truncating an existing
     * {@link #isRegularFile regular-file} to a size of {@code 0}. The method ensures that the file
     * is closed when all lines have been written (or an I/O error or other runtime exception is
     * thrown). If an I/O error occurs then it may do so after the file has been created or
     * truncated, or after some bytes have been written to the file.
     * 
     * @param charset the charset to use for encoding
     * @param lines an object to iterate over the char sequences
     * @return the path
     * @throws IllegalArgumentException if {@code options} contains an invalid combination of
     *             options
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text
     *             cannot be encoded using the specified charset
     * @throws UnsupportedOperationException if an unsupported option is specified
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkWrite(String) checkWrite} method is
     *             invoked to check write access to the file. The
     *             {@link SecurityManager#checkDelete(String) checkDelete} method is invoked to
     *             check delete access if the file is opened with the {@code DELETE_ON_CLOSE}
     *             option.
     */
    public void text(Charset charset, Iterable<String> lines) {
        try {
            Files.write(path, lines, charset);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /**
     * Read all lines from a file as a {@code Stream}. Unlike {@link #readAllLines(Path, Charset)
     * readAllLines}, this method does not read all lines into a {@code List}, but instead populates
     * lazily as the stream is consumed.
     * <p>
     * Bytes from the file are decoded into characters using the specified charset and the same line
     * terminators as specified by {@code
     * readAllLines} are supported.
     * <p>
     * The returned stream contains a reference to an open file. The file is closed by closing the
     * stream.
     * <p>
     * The file contents should not be modified during the execution of the terminal stream
     * operation. Otherwise, the result of the terminal stream operation is undefined.
     * <p>
     * After this method returns, then any subsequent I/O exception that occurs while reading from
     * the file or when a malformed or unmappable byte sequence is read, is wrapped in an
     * {@link UncheckedIOException} that will be thrown from the {@link java.util.stream.Stream}
     * method that caused the read to take place. In case an {@code IOException} is thrown when
     * closing the file, it is also wrapped as an {@code UncheckedIOException}.
     *
     * @apiNote This method must be used within a try-with-resources statement or similar control
     *          structure to ensure that the stream's open file is closed promptly after the
     *          stream's operations have completed.
     * @implNote This implementation supports good parallel stream performance for the standard
     *           charsets {@link StandardCharsets#UTF_8 UTF-8}, {@link StandardCharsets#US_ASCII
     *           US-ASCII} and {@link StandardCharsets#ISO_8859_1 ISO-8859-1}. Such
     *           <em>line-optimal</em> charsets have the property that the encoded bytes of a line
     *           feed ('\n') or a carriage return ('\r') are efficiently identifiable from other
     *           encoded characters when randomly accessing the bytes of the file.
     *           <p>
     *           For non-<em>line-optimal</em> charsets the stream source's spliterator has poor
     *           splitting properties, similar to that of a spliterator associated with an iterator
     *           or that associated with a stream returned from {@link BufferedReader#lines()}. Poor
     *           splitting properties can result in poor parallel stream performance.
     *           <p>
     *           For <em>line-optimal</em> charsets the stream source's spliterator has good
     *           splitting properties, assuming the file contains a regular sequence of lines. Good
     *           splitting properties can result in good parallel stream performance. The
     *           spliterator for a <em>line-optimal</em> charset takes advantage of the charset
     *           properties (a line feed or a carriage return being efficient identifiable) such
     *           that when splitting it can approximately divide the number of covered lines in
     *           half.
     * @return the lines from the file as a {@code Stream}
     * @throws IOException if an I/O error occurs opening the file
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public Signal<String> lines() {
        return lines(StandardCharsets.UTF_8);
    }

    /**
     * Read all lines from a file as a {@code Stream}. Unlike {@link #readAllLines(Path, Charset)
     * readAllLines}, this method does not read all lines into a {@code List}, but instead populates
     * lazily as the stream is consumed.
     * <p>
     * Bytes from the file are decoded into characters using the specified charset and the same line
     * terminators as specified by {@code
     * readAllLines} are supported.
     * <p>
     * The returned stream contains a reference to an open file. The file is closed by closing the
     * stream.
     * <p>
     * The file contents should not be modified during the execution of the terminal stream
     * operation. Otherwise, the result of the terminal stream operation is undefined.
     * <p>
     * After this method returns, then any subsequent I/O exception that occurs while reading from
     * the file or when a malformed or unmappable byte sequence is read, is wrapped in an
     * {@link UncheckedIOException} that will be thrown from the {@link java.util.stream.Stream}
     * method that caused the read to take place. In case an {@code IOException} is thrown when
     * closing the file, it is also wrapped as an {@code UncheckedIOException}.
     *
     * @apiNote This method must be used within a try-with-resources statement or similar control
     *          structure to ensure that the stream's open file is closed promptly after the
     *          stream's operations have completed.
     * @implNote This implementation supports good parallel stream performance for the standard
     *           charsets {@link StandardCharsets#UTF_8 UTF-8}, {@link StandardCharsets#US_ASCII
     *           US-ASCII} and {@link StandardCharsets#ISO_8859_1 ISO-8859-1}. Such
     *           <em>line-optimal</em> charsets have the property that the encoded bytes of a line
     *           feed ('\n') or a carriage return ('\r') are efficiently identifiable from other
     *           encoded characters when randomly accessing the bytes of the file.
     *           <p>
     *           For non-<em>line-optimal</em> charsets the stream source's spliterator has poor
     *           splitting properties, similar to that of a spliterator associated with an iterator
     *           or that associated with a stream returned from {@link BufferedReader#lines()}. Poor
     *           splitting properties can result in poor parallel stream performance.
     *           <p>
     *           For <em>line-optimal</em> charsets the stream source's spliterator has good
     *           splitting properties, assuming the file contains a regular sequence of lines. Good
     *           splitting properties can result in good parallel stream performance. The
     *           spliterator for a <em>line-optimal</em> charset takes advantage of the charset
     *           properties (a line feed or a carriage return being efficient identifiable) such
     *           that when splitting it can approximately divide the number of covered lines in
     *           half.
     * @param cs the charset to use for decoding
     * @return the lines from the file as a {@code Stream}
     * @throws IOException if an I/O error occurs opening the file
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public Signal<String> lines(Charset charset) {
        return new Signal<>((observer, disposer) -> {
            try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
                String line = null;
                while (disposer.isNotDisposed() && (line = reader.readLine()) != null) {
                    observer.accept(line);
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File convert(Path path) {
        return Locator.file(path);
    }

    /**
     * Detect archive file system.
     * 
     * @return An archive.
     */
    private ArchiveInputStream detect() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(System.getProperty("file.encoding"));

        try {
            try {
                return factory.createArchiveInputStream(extension(), new BufferedInputStream(newInputStream()));
            } catch (StreamingNotSupportedException e) {
                return factory.createArchiveInputStream(new BufferedInputStream(newInputStream()));
            }
        } catch (ArchiveException e) {
            throw I.quiet(e);
        }
    }
}

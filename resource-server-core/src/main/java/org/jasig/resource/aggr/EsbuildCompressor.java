/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.resource.aggr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compressor that uses esbuild for minifying JavaScript and CSS files.
 * Extracts the native esbuild binary from the esbuild-java-bundle-original
 * jar at runtime — no Node.js or npx required.
 */
public class EsbuildCompressor {
    private static final Logger logger = LoggerFactory.getLogger(EsbuildCompressor.class);

    private static final String ESBUILD_VERSION = "0.23.0";
    private static final int TIMEOUT_SECONDS = 30;

    private static volatile Path cachedBinary = null;

    /**
     * Compress JavaScript using esbuild.
     * Identifier minification is intentionally disabled to avoid breaking
     * property access patterns (e.g. layout.navigation.tabs.externalId).
     */
    public static void compressJavaScript(Reader reader, Writer writer) throws IOException {
        compressWithEsbuild(reader, writer, "js", "--minify-syntax", "--minify-whitespace");
    }

    /**
     * Compress CSS using esbuild.
     */
    public static void compressCss(Reader reader, Writer writer) throws IOException {
        compressWithEsbuild(reader, writer, "css", "--minify");
    }

    private static void compressWithEsbuild(Reader reader, Writer writer, String type, String... minifyFlags)
            throws IOException {
        final Path inputFile = Files.createTempFile("esbuild-input", "." + type);
        final Path outputFile = Files.createTempFile("esbuild-output", "." + type);

        try {
            try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(inputFile.toFile()), StandardCharsets.UTF_8)) {
                IOUtils.copy(reader, fw);
            }

            final Path binary = getOrExtractBinary();
            final String[] command = buildCommand(binary, inputFile, outputFile, minifyFlags);

            boolean succeeded = false;
            try {
                final Process process = new ProcessBuilder(command).start();
                final boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    logger.warn("esbuild timed out, falling back to uncompressed output");
                } else if (process.exitValue() != 0) {
                    logger.warn("esbuild failed (exit {}), falling back to uncompressed output: {}",
                            process.exitValue(), IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
                } else {
                    succeeded = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("esbuild process interrupted", e);
            }

            try (InputStreamReader fr = new InputStreamReader(new FileInputStream(succeeded ? outputFile.toFile() : inputFile.toFile()), StandardCharsets.UTF_8)) {
                IOUtils.copy(fr, writer);
            }
        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
        }
    }

    private static String[] buildCommand(Path binary, Path input, Path output, String[] minifyFlags) {
        final String[] cmd = new String[minifyFlags.length + 3];
        cmd[0] = binary.toString();
        cmd[1] = input.toString();
        System.arraycopy(minifyFlags, 0, cmd, 2, minifyFlags.length);
        cmd[cmd.length - 1] = "--outfile=" + output;
        return cmd;
    }

    /**
     * Returns the path to the esbuild binary, extracting it from the bundled
     * jar on first call and caching it for subsequent calls.
     */
    static Path getOrExtractBinary() throws IOException {
        if (cachedBinary != null && Files.isExecutable(cachedBinary)) {
            return cachedBinary;
        }
        synchronized (EsbuildCompressor.class) {
            if (cachedBinary != null && Files.isExecutable(cachedBinary)) {
                return cachedBinary;
            }
            cachedBinary = extractBinary();
            return cachedBinary;
        }
    }

    private static Path extractBinary() throws IOException {
        final String classifier = determineClassifier();
        final boolean isWindows = classifier.startsWith("win");
        final String tgzResource = "/" + classifier + "-" + ESBUILD_VERSION + ".tgz";
        final String binaryPathInTar = isWindows ? "package/esbuild.exe" : "package/bin/esbuild";
        final String binaryFileName = isWindows ? "esbuild.exe" : "esbuild";

        final InputStream resource = EsbuildCompressor.class.getResourceAsStream(tgzResource);
        if (resource == null) {
            throw new IOException("esbuild bundle not found on classpath: " + tgzResource);
        }

        final Path destDir = Files.createTempDirectory("esbuild-" + ESBUILD_VERSION);
        destDir.toFile().deleteOnExit();

        try (TarArchiveInputStream tar = new TarArchiveInputStream(new GZIPInputStream(resource))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                if (!entry.getName().equals(binaryPathInTar)) continue;

                final Path binary = destDir.resolve(binaryFileName);
                Files.copy(tar, binary);
                binary.toFile().deleteOnExit();

                if (!isWindows) {
                    Files.setPosixFilePermissions(binary, EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ,
                            PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_READ,
                            PosixFilePermission.OTHERS_EXECUTE));
                }
                logger.debug("Extracted esbuild binary to {}", binary);
                return binary;
            }
        }
        throw new IOException("esbuild binary not found in " + tgzResource);
    }

    private static String determineClassifier() {
        final String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        final String arch = System.getProperty("os.arch", "").toLowerCase(java.util.Locale.ROOT);
        if (os.contains("mac")) {
            return (arch.equals("aarch64") || arch.contains("arm")) ? "darwin-arm64" : "darwin-x64";
        } else if (os.contains("win")) {
            return arch.contains("64") ? "win32-x64" : "win32-ia32";
        } else {
            if (arch.equals("aarch64") || arch.equals("arm64")) return "linux-arm64";
            if (arch.contains("arm")) return "linux-arm";
            if (arch.contains("64")) return "linux-x64";
            return "linux-ia32";
        }
    }
}

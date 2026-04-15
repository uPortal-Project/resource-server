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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Compressor that uses esbuild for minifying JavaScript and CSS files.
 * Replaces YUI Compressor with modern, fast esbuild.
 */
public class EsbuildCompressor {
    private static final Log logger = LogFactory.getLog(EsbuildCompressor.class);
    
    private static final int TIMEOUT_SECONDS = 30;
    
    /**
     * Compress JavaScript using esbuild
     */
    public static void compressJavaScript(Reader reader, Writer writer) throws IOException {
        compressWithEsbuild(reader, writer, "js");
    }
    
    /**
     * Compress CSS using esbuild
     */
    public static void compressCss(Reader reader, Writer writer) throws IOException {
        compressWithEsbuild(reader, writer, "css");
    }
    
    private static void compressWithEsbuild(Reader reader, Writer writer, String type) throws IOException {
        // Create temporary files
        Path inputFile = Files.createTempFile("esbuild-input", "." + type);
        Path outputFile = Files.createTempFile("esbuild-output", "." + type);
        
        try {
            // Write input to temp file
            try (FileWriter fileWriter = new FileWriter(inputFile.toFile())) {
                IOUtils.copy(reader, fileWriter);
            }
            
            // Build esbuild command - on Windows, .cmd scripts require cmd /c
            // Use granular flags for JS to avoid renaming identifiers, which can break
            // property access patterns (e.g. layout.navigation.tabs.externalId).
            // CSS uses --minify since identifier renaming is not a concern there.
            final ProcessBuilder pb;
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                pb = type.equals("js")
                    ? new ProcessBuilder("cmd", "/c", "npx", "esbuild", inputFile.toString(),
                        "--minify-syntax", "--minify-whitespace", "--outfile=" + outputFile)
                    : new ProcessBuilder("cmd", "/c", "npx", "esbuild", inputFile.toString(),
                        "--minify", "--outfile=" + outputFile);
            } else {
                pb = type.equals("js")
                    ? new ProcessBuilder("npx", "esbuild", inputFile.toString(),
                        "--minify-syntax", "--minify-whitespace", "--outfile=" + outputFile)
                    : new ProcessBuilder("npx", "esbuild", inputFile.toString(),
                        "--minify", "--outfile=" + outputFile);
            }

            boolean esbuildSucceeded = false;
            try {
                Process process = pb.start();
                boolean finished = false;
                try {
                    finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("esbuild process interrupted", e);
                }

                if (!finished) {
                    process.destroyForcibly();
                    logger.warn("esbuild process timed out, falling back to uncompressed output");
                } else if (process.exitValue() != 0) {
                    String errorOutput = IOUtils.toString(process.getErrorStream(), "UTF-8");
                    logger.warn("esbuild failed (exit code " + process.exitValue() + "), falling back to uncompressed output: " + errorOutput);
                } else {
                    esbuildSucceeded = true;
                }
            } catch (IOException e) {
                logger.warn("esbuild not available, falling back to uncompressed output: " + e.getMessage());
            }

            // Re-read from inputFile since the original reader is already consumed
            try (FileReader fallbackReader = new FileReader(esbuildSucceeded ? outputFile.toFile() : inputFile.toFile())) {
                IOUtils.copy(fallbackReader, writer);
            }
            
        } finally {
            // Clean up temp files
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
        }
    }
}
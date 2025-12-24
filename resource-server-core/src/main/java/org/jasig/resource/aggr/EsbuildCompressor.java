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
    
    private static final String ESBUILD_COMMAND = "npx esbuild";
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
            
            // Build esbuild command
            ProcessBuilder pb = new ProcessBuilder(
                "npx", "esbuild",
                inputFile.toString(),
                "--minify",
                "--outfile=" + outputFile.toString()
            );
            
            // Execute esbuild
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
                throw new IOException("esbuild process timed out after " + TIMEOUT_SECONDS + " seconds");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // Log error output
                String errorOutput = IOUtils.toString(process.getErrorStream(), "UTF-8");
                logger.error("esbuild failed with exit code " + exitCode + ": " + errorOutput);
                
                // Fallback: copy input to output without compression
                logger.warn("Falling back to uncompressed output due to esbuild failure");
                // Re-read the input file since we can't reset the reader
                try (FileReader fallbackReader = new FileReader(inputFile.toFile())) {
                    IOUtils.copy(fallbackReader, writer);
                }
                return;
            }
            
            // Copy compressed output to writer
            try (FileReader fileReader = new FileReader(outputFile.toFile())) {
                IOUtils.copy(fileReader, writer);
            }
            
        } finally {
            // Clean up temp files
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
        }
    }
}
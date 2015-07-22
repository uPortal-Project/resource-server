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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility for determining the relative path between two files.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RelativePath {
    /**
     * break a path down into individual elements and add to a list.
     * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
     * @param file input file
     * @return a List collection with the individual elements of the path in reverse order
     */
    private static List<String> getPathList(File file) throws IOException {
        //Make sure the canonical file is being parsed
        file = file.getCanonicalFile();

        //Build a List of the parts of the path
        final List<String> pathParts = new LinkedList<String>();
        while (file != null) {
            pathParts.add(file.getName());
            file = file.getParentFile();
        }
        
        return pathParts;
    }

    /**
     * figure out a string representing the relative path of
     * 'f' with respect to 'r'
     * @param basePath home path
     * @param filePath path of file
     */
    private static String matchPathLists(List<String> basePath, List<String> filePath) {
        // start at the beginning of the lists
        // iterate while both lists are equal
        final StringBuilder relativePath = new StringBuilder();
        final ListIterator<String> basePathItr = basePath.listIterator(basePath.size() - 1);
        final ListIterator<String> filePathItr = filePath.listIterator(filePath.size() - 1);
        
        // first eliminate common root elements
        while (basePathItr.hasPrevious() && filePathItr.hasPrevious()) {
            if (!basePathItr.previous().equals(filePathItr.previous())) {
                basePathItr.next();
                filePathItr.next();
                break;
            }
        }

        // for each remaining level in the home path, add a ..
        for (; basePathItr.hasPrevious(); basePathItr.previous()) {
            relativePath.append("..").append(File.separator);
        }

        // for each level in the file path, add the path
        while (filePathItr.hasPrevious()) {
            relativePath.append(filePathItr.previous());
            if (filePathItr.hasPrevious()) {
                relativePath.append(File.separator);
            }
        }

        return relativePath.toString();
    }

    /**
     * get relative path of File 'f' with respect to 'home' directory
     * example : home = /a/b/c
     *           f    = /a/d/e/x.txt
     *           s = getRelativePath(home,f) = ../../d/e/x.txt
     * @param home base path, should be a directory, not a file, or it doesn't
    make sense
     * @param f file to generate path for
     * @return path from home to f as a string
     */
    public static String getRelativePath(File home, File f) throws IOException {
        final List<String> homelist = getPathList(home);
        final List<String> filelist = getPathList(f);
        return matchPathLists(homelist, filelist);
    }

}

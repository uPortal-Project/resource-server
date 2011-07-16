/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.resource.aggr.mojo;

import org.apache.maven.plugin.logging.Log;


/**
 * Adapts the commons-logging {@link org.apache.commons.logging.Log} interface to the Maven {@link Log} interface
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CommonsLogToMavenLog implements org.apache.commons.logging.Log {
    private final Log logger;

    public CommonsLogToMavenLog(Log logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return this.logger.isErrorEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public void trace(Object message) {
        this.logger.debug(String.valueOf(message));
    }

    @Override
    public void trace(Object message, Throwable t) {
        this.logger.debug(String.valueOf(message), t);
    }

    @Override
    public void debug(Object message) {
        this.logger.debug(String.valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable t) {
        this.logger.debug(String.valueOf(message), t);
    }

    @Override
    public void info(Object message) {
        this.logger.info(String.valueOf(message));
    }

    @Override
    public void info(Object message, Throwable t) {
        this.logger.info(String.valueOf(message), t);
    }

    @Override
    public void warn(Object message) {
        this.logger.warn(String.valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable t) {
        this.logger.warn(String.valueOf(message), t);
    }

    @Override
    public void error(Object message) {
        this.logger.error(String.valueOf(message));
    }

    @Override
    public void error(Object message, Throwable t) {
        this.logger.error(String.valueOf(message), t);
    }

    @Override
    public void fatal(Object message) {
        this.logger.error(String.valueOf(message));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        this.logger.error(String.valueOf(message), t);
    }
}

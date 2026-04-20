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
package org.jasig.resource.aggr.mojo;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Adapts the Maven {@link Log} interface to SLF4J's {@link Logger} interface.
 */
public class MavenLoggerToSlf4j implements Logger {
    private final Log mavenLog;
    private final String name;

    public MavenLoggerToSlf4j(Log mavenLog, String name) {
        this.mavenLog = mavenLog;
        this.name = name;
    }

    @Override public String getName() { return name; }

    @Override public boolean isTraceEnabled() { return mavenLog.isDebugEnabled(); }
    @Override public boolean isTraceEnabled(Marker m) { return mavenLog.isDebugEnabled(); }
    @Override public void trace(String msg) { mavenLog.debug(msg); }
    @Override public void trace(String fmt, Object arg) { mavenLog.debug(format(fmt, arg)); }
    @Override public void trace(String fmt, Object a, Object b) { log(mavenLog::debug, fmt, a, b); }
    @Override public void trace(String fmt, Object... args) { log(mavenLog::debug, fmt, args); }
    @Override public void trace(String msg, Throwable t) { mavenLog.debug(msg, t); }
    @Override public void trace(Marker m, String msg) { mavenLog.debug(msg); }
    @Override public void trace(Marker m, String fmt, Object arg) { mavenLog.debug(format(fmt, arg)); }
    @Override public void trace(Marker m, String fmt, Object a, Object b) { log(mavenLog::debug, fmt, a, b); }
    @Override public void trace(Marker m, String fmt, Object... args) { log(mavenLog::debug, fmt, args); }
    @Override public void trace(Marker m, String msg, Throwable t) { mavenLog.debug(msg, t); }

    @Override public boolean isDebugEnabled() { return mavenLog.isDebugEnabled(); }
    @Override public boolean isDebugEnabled(Marker m) { return mavenLog.isDebugEnabled(); }
    @Override public void debug(String msg) { mavenLog.debug(msg); }
    @Override public void debug(String fmt, Object arg) { mavenLog.debug(format(fmt, arg)); }
    @Override public void debug(String fmt, Object a, Object b) { log(mavenLog::debug, fmt, a, b); }
    @Override public void debug(String fmt, Object... args) { log(mavenLog::debug, fmt, args); }
    @Override public void debug(String msg, Throwable t) { mavenLog.debug(msg, t); }
    @Override public void debug(Marker m, String msg) { mavenLog.debug(msg); }
    @Override public void debug(Marker m, String fmt, Object arg) { mavenLog.debug(format(fmt, arg)); }
    @Override public void debug(Marker m, String fmt, Object a, Object b) { log(mavenLog::debug, fmt, a, b); }
    @Override public void debug(Marker m, String fmt, Object... args) { log(mavenLog::debug, fmt, args); }
    @Override public void debug(Marker m, String msg, Throwable t) { mavenLog.debug(msg, t); }

    @Override public boolean isInfoEnabled() { return mavenLog.isInfoEnabled(); }
    @Override public boolean isInfoEnabled(Marker m) { return mavenLog.isInfoEnabled(); }
    @Override public void info(String msg) { mavenLog.info(msg); }
    @Override public void info(String fmt, Object arg) { mavenLog.info(format(fmt, arg)); }
    @Override public void info(String fmt, Object a, Object b) { log(mavenLog::info, fmt, a, b); }
    @Override public void info(String fmt, Object... args) { log(mavenLog::info, fmt, args); }
    @Override public void info(String msg, Throwable t) { mavenLog.info(msg, t); }
    @Override public void info(Marker m, String msg) { mavenLog.info(msg); }
    @Override public void info(Marker m, String fmt, Object arg) { mavenLog.info(format(fmt, arg)); }
    @Override public void info(Marker m, String fmt, Object a, Object b) { log(mavenLog::info, fmt, a, b); }
    @Override public void info(Marker m, String fmt, Object... args) { log(mavenLog::info, fmt, args); }
    @Override public void info(Marker m, String msg, Throwable t) { mavenLog.info(msg, t); }

    @Override public boolean isWarnEnabled() { return mavenLog.isWarnEnabled(); }
    @Override public boolean isWarnEnabled(Marker m) { return mavenLog.isWarnEnabled(); }
    @Override public void warn(String msg) { mavenLog.warn(msg); }
    @Override public void warn(String fmt, Object arg) { mavenLog.warn(format(fmt, arg)); }
    @Override public void warn(String fmt, Object a, Object b) { log(mavenLog::warn, fmt, a, b); }
    @Override public void warn(String fmt, Object... args) { log(mavenLog::warn, fmt, args); }
    @Override public void warn(String msg, Throwable t) { mavenLog.warn(msg, t); }
    @Override public void warn(Marker m, String msg) { mavenLog.warn(msg); }
    @Override public void warn(Marker m, String fmt, Object arg) { mavenLog.warn(format(fmt, arg)); }
    @Override public void warn(Marker m, String fmt, Object a, Object b) { log(mavenLog::warn, fmt, a, b); }
    @Override public void warn(Marker m, String fmt, Object... args) { log(mavenLog::warn, fmt, args); }
    @Override public void warn(Marker m, String msg, Throwable t) { mavenLog.warn(msg, t); }

    @Override public boolean isErrorEnabled() { return mavenLog.isErrorEnabled(); }
    @Override public boolean isErrorEnabled(Marker m) { return mavenLog.isErrorEnabled(); }
    @Override public void error(String msg) { mavenLog.error(msg); }
    @Override public void error(String fmt, Object arg) { mavenLog.error(format(fmt, arg)); }
    @Override public void error(String fmt, Object a, Object b) { log(mavenLog::error, fmt, a, b); }
    @Override public void error(String fmt, Object... args) { log(mavenLog::error, fmt, args); }
    @Override public void error(String msg, Throwable t) { mavenLog.error(msg, t); }
    @Override public void error(Marker m, String msg) { mavenLog.error(msg); }
    @Override public void error(Marker m, String fmt, Object arg) { mavenLog.error(format(fmt, arg)); }
    @Override public void error(Marker m, String fmt, Object a, Object b) { log(mavenLog::error, fmt, a, b); }
    @Override public void error(Marker m, String fmt, Object... args) { log(mavenLog::error, fmt, args); }
    @Override public void error(Marker m, String msg, Throwable t) { mavenLog.error(msg, t); }

    private static String format(String fmt, Object... args) {
        if (args == null || args.length == 0) return fmt;
        return org.slf4j.helpers.MessageFormatter.arrayFormat(fmt, args).getMessage();
    }

    private static org.slf4j.helpers.FormattingTuple tuple(String fmt, Object... args) {
        if (args == null || args.length == 0) return org.slf4j.helpers.MessageFormatter.format(fmt, (Object) null);
        return org.slf4j.helpers.MessageFormatter.arrayFormat(fmt, args);
    }

    @FunctionalInterface
    private interface LogWithThrowable { void log(String msg, Throwable t); }

    private static void log(LogWithThrowable sink, String fmt, Object... args) {
        final org.slf4j.helpers.FormattingTuple t = tuple(fmt, args);
        if (t.getThrowable() != null) sink.log(t.getMessage(), t.getThrowable());
        else sink.log(t.getMessage(), (Throwable) null);
    }
}

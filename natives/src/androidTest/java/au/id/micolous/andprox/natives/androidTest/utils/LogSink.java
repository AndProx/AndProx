/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */
package au.id.micolous.andprox.natives.androidTest.utils;

import android.test.suitebuilder.annotation.Suppress;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;

import java.util.LinkedList;

import au.id.micolous.andprox.natives.Natives;

/**
 * Allows unit tests to capture PrintAndLog outputs.
 */
@Suppress
public class LogSink implements Natives.PrinterArgs {
    private LinkedList<String> mLogLines;
    private StringBuilder mPrint;

    public void reset() {
        mLogLines = new LinkedList<>();
        mPrint = new StringBuilder();
    }

    public LogSink() {
        reset();
        Natives.registerPrintHandler(this);
    }

    @Override
    public void onPrintAndLog(String log) {
        mLogLines.add(log);
    }

    @Override
    public void onPrint(String msg) {
        mPrint.append(msg);
    }

    /**
     * Finds a "needle" in the log lines. Returns null if not found. Case sensitive.
     * @param needle Case sensitive string to match on.
     * @return Complete log line that matched.
     */
    public String findInLogLines(CharSequence needle) {
        for (String l : mLogLines) {
            if (l.contains(needle)) {
                return l;
            }
        }

        return null;
    }

    public String printBuffer() {
        return mPrint.toString();
    }
}

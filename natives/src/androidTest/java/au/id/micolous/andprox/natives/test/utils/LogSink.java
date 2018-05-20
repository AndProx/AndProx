package au.id.micolous.andprox.natives.test.utils;

import android.test.suitebuilder.annotation.Suppress;

import java.util.LinkedList;

import au.id.micolous.andprox.natives.Natives;

/**
 * Allows unit tests to capture PrintAndLog outputs.
 */
@Suppress
public class LogSink implements Natives.PrinterArgs {
    protected LinkedList<String> mLogLines;

    public void reset() {
        mLogLines = new LinkedList<>();
    }

    public LogSink() {
        reset();
        Natives.registerPrintAndLogHandler(this);
    }

    @Override
    public void onPrint(String log) {
        mLogLines.add(log);
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
}

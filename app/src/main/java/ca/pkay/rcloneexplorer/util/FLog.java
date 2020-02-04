package ca.pkay.rcloneexplorer.util;

import android.util.Log;
import ca.pkay.rcloneexplorer.BuildConfig;

import java.util.IllegalFormatException;

/**
 * A simple wrapper around {@link Log} to enable easier debugging without
 * slowing down all app functions.
 */
public abstract class FLog {

    private static final FLog logger;
    /**
     * Use this log tag to set a lower than default (<=INFO) log tag for all
     * app log messages.
     * <br><br><b>Example</b><br>
     *     <code>adb shell setprop log.tag.APP_MIN VERBOSE</code>
     */
    public static final String LOGGING_MIN_LEVEL_TAG = "APP_MIN";

    static {
        if (BuildConfig.DEBUG) {
            logger = new DevLogger();
        } else {
            logger = new ProdLogger();
        }
    }

    public static void v(String tag, String message, Object... args) {
        if (logger.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, applyFormatting(message, args));
        }
    }

    public static void d(String tag, String message, Object... args) {
        if (logger.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, applyFormatting(message, args));
        }
    }

    public static void i(String tag, String message, Object... args) {
        if (logger.isLoggable(tag, Log.INFO)) {
            Log.i(tag, applyFormatting(message, args));
        }
    }

    public static void w(String tag, String message, Object... args) {
        if (logger.isLoggable(tag, Log.WARN)) {
            Log.w(tag, applyFormatting(message, args));
        }
    }

    public static void w(String tag, String message, Exception e, Object... args) {
        if (logger.isLoggable(tag, Log.WARN)) {
            Log.w(tag, applyFormatting(message, args), e);
        }
    }

    public static void e(String tag, String s, Object... args) {
        if (logger.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, applyFormatting(s, args));
        }
    }

    public static void e(String tag, String message, Throwable e, Object... args) {
        if (logger.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, applyFormatting(message, args), e);
        }
    }

    abstract boolean isLoggable(String tag, int level);

    private static String applyFormatting(String message, Object... args) {
        if (args.length == 0) {
            return message;
        } else {
            try {
                return String.format(message, args);
            } catch (IllegalFormatException e) {
                // We really shouldn't crash here even if there is a format
                // error since this is usally used without in error logging
                // itself.
                return message;
            }
        }
    }

    private static final class DevLogger extends FLog {

        @Override
        boolean isLoggable(String tag, int level) {
            return Log.isLoggable(tag, level) || level != Log.INFO && Log.isLoggable(LOGGING_MIN_LEVEL_TAG, level);
        }
    }

    private static final class ProdLogger extends FLog {

        @Override
        boolean isLoggable(String tag, int level) {
            switch (level) {
                case Log.VERBOSE:
                case Log.DEBUG:
                    return false;
                default:
                    return Log.isLoggable(tag, level);
            }
        }
    }
}

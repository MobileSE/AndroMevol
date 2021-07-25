package android.text.format;

import android.content.Context;
import android.net.NetworkUtils;
import android.net.ProxyInfo;

public final class Formatter {
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;

    public static String formatFileSize(Context context, long number) {
        return formatFileSize(context, number, false);
    }

    public static String formatShortFileSize(Context context, long number) {
        return formatFileSize(context, number, true);
    }

    private static String formatFileSize(Context context, long number, boolean shorter) {
        String value;
        if (context == null) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        float result = (float) number;
        int suffix = 17039467;
        if (result > 900.0f) {
            suffix = 17039468;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039469;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039470;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039471;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039472;
            result /= 1024.0f;
        }
        if (result < 1.0f) {
            value = String.format("%.2f", Float.valueOf(result));
        } else if (result < 10.0f) {
            if (shorter) {
                value = String.format("%.1f", Float.valueOf(result));
            } else {
                value = String.format("%.2f", Float.valueOf(result));
            }
        } else if (result >= 100.0f) {
            value = String.format("%.0f", Float.valueOf(result));
        } else if (shorter) {
            value = String.format("%.0f", Float.valueOf(result));
        } else {
            value = String.format("%.2f", Float.valueOf(result));
        }
        return context.getResources().getString(17039473, value, context.getString(suffix));
    }

    @Deprecated
    public static String formatIpAddress(int ipv4Address) {
        return NetworkUtils.intToInetAddress(ipv4Address).getHostAddress();
    }

    public static String formatShortElapsedTime(Context context, long millis) {
        long secondsLong = millis / 1000;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (secondsLong >= 86400) {
            days = (int) (secondsLong / 86400);
            secondsLong -= (long) (SECONDS_PER_DAY * days);
        }
        if (secondsLong >= 3600) {
            hours = (int) (secondsLong / 3600);
            secondsLong -= (long) (hours * SECONDS_PER_HOUR);
        }
        if (secondsLong >= 60) {
            minutes = (int) (secondsLong / 60);
            secondsLong -= (long) (minutes * 60);
        }
        int seconds = (int) secondsLong;
        if (days >= 2) {
            return context.getString(17039474, Integer.valueOf(days + ((hours + 12) / 24)));
        } else if (days > 0) {
            if (hours == 1) {
                return context.getString(17039476, Integer.valueOf(days), Integer.valueOf(hours));
            }
            return context.getString(17039475, Integer.valueOf(days), Integer.valueOf(hours));
        } else if (hours >= 2) {
            return context.getString(17039477, Integer.valueOf(hours + ((minutes + 30) / 60)));
        } else if (hours > 0) {
            if (minutes == 1) {
                return context.getString(17039479, Integer.valueOf(hours), Integer.valueOf(minutes));
            }
            return context.getString(17039478, Integer.valueOf(hours), Integer.valueOf(minutes));
        } else if (minutes >= 2) {
            return context.getString(17039480, Integer.valueOf(minutes + ((seconds + 30) / 60)));
        } else if (minutes > 0) {
            if (seconds == 1) {
                return context.getString(17039482, Integer.valueOf(minutes), Integer.valueOf(seconds));
            }
            return context.getString(17039481, Integer.valueOf(minutes), Integer.valueOf(seconds));
        } else if (seconds == 1) {
            return context.getString(17039484, Integer.valueOf(seconds));
        } else {
            return context.getString(17039483, Integer.valueOf(seconds));
        }
    }
}

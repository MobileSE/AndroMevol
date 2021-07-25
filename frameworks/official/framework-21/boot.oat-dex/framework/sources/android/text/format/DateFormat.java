package android.text.format;

import android.content.Context;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public class DateFormat {
    @Deprecated
    public static final char AM_PM = 'a';
    @Deprecated
    public static final char CAPITAL_AM_PM = 'A';
    @Deprecated
    public static final char DATE = 'd';
    @Deprecated
    public static final char DAY = 'E';
    @Deprecated
    public static final char HOUR = 'h';
    @Deprecated
    public static final char HOUR_OF_DAY = 'k';
    @Deprecated
    public static final char MINUTE = 'm';
    @Deprecated
    public static final char MONTH = 'M';
    @Deprecated
    public static final char QUOTE = '\'';
    @Deprecated
    public static final char SECONDS = 's';
    @Deprecated
    public static final char STANDALONE_MONTH = 'L';
    @Deprecated
    public static final char TIME_ZONE = 'z';
    @Deprecated
    public static final char YEAR = 'y';
    private static boolean sIs24Hour;
    private static Locale sIs24HourLocale;
    private static final Object sLocaleLock = new Object();

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002b, code lost:
        r1 = java.text.DateFormat.getTimeInstance(1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        if ((r1 instanceof java.text.SimpleDateFormat) == false) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0041, code lost:
        if (((java.text.SimpleDateFormat) r1).toPattern().indexOf(72) < 0) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0043, code lost:
        r4 = "24";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0045, code lost:
        r6 = android.text.format.DateFormat.sLocaleLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0047, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        android.text.format.DateFormat.sIs24HourLocale = r0;
        android.text.format.DateFormat.sIs24Hour = r4.equals("24");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0052, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0059, code lost:
        r4 = "12";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005c, code lost:
        r4 = "12";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return android.text.format.DateFormat.sIs24Hour;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean is24HourFormat(android.content.Context r7) {
        /*
        // Method dump skipped, instructions count: 105
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.format.DateFormat.is24HourFormat(android.content.Context):boolean");
    }

    public static String getBestDateTimePattern(Locale locale, String skeleton) {
        return ICU.getBestDateTimePattern(skeleton, locale);
    }

    public static java.text.DateFormat getTimeFormat(Context context) {
        return new SimpleDateFormat(getTimeFormatString(context));
    }

    public static String getTimeFormatString(Context context) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        return is24HourFormat(context) ? d.timeFormat24 : d.timeFormat12;
    }

    public static java.text.DateFormat getDateFormat(Context context) {
        return getDateFormatForSetting(context, Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT));
    }

    public static java.text.DateFormat getDateFormatForSetting(Context context, String value) {
        return new SimpleDateFormat(getDateFormatStringForSetting(context, value));
    }

    private static String getDateFormatStringForSetting(Context context, String value) {
        String value2;
        if (value != null) {
            int month = value.indexOf(77);
            int day = value.indexOf(100);
            int year = value.indexOf(121);
            if (month >= 0 && day >= 0 && year >= 0) {
                String template = context.getString(17039443);
                if (year >= month || year >= day) {
                    if (month < day) {
                        if (day < year) {
                            value2 = String.format(template, "MM", "dd", "yyyy");
                        } else {
                            value2 = String.format(template, "MM", "yyyy", "dd");
                        }
                    } else if (month < year) {
                        value2 = String.format(template, "dd", "MM", "yyyy");
                    } else {
                        value2 = String.format(template, "dd", "yyyy", "MM");
                    }
                } else if (month < day) {
                    value2 = String.format(template, "yyyy", "MM", "dd");
                } else {
                    value2 = String.format(template, "yyyy", "dd", "MM");
                }
                return value2;
            }
        }
        return LocaleData.get(context.getResources().getConfiguration().locale).shortDateFormat4;
    }

    public static java.text.DateFormat getLongDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(1);
    }

    public static java.text.DateFormat getMediumDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(2);
    }

    public static char[] getDateFormatOrder(Context context) {
        return ICU.getDateFormatOrder(getDateFormatString(context));
    }

    private static String getDateFormatString(Context context) {
        return getDateFormatStringForSetting(context, Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT));
    }

    public static CharSequence format(CharSequence inFormat, long inTimeInMillis) {
        return format(inFormat, new Date(inTimeInMillis));
    }

    public static CharSequence format(CharSequence inFormat, Date inDate) {
        Calendar c = new GregorianCalendar();
        c.setTime(inDate);
        return format(inFormat, c);
    }

    public static boolean hasSeconds(CharSequence inFormat) {
        return hasDesignator(inFormat, 's');
    }

    public static boolean hasDesignator(CharSequence inFormat, char designator) {
        if (inFormat == null) {
            return false;
        }
        int length = inFormat.length();
        int i = 0;
        while (i < length) {
            int count = 1;
            int c = inFormat.charAt(i);
            if (c == 39) {
                count = skipQuotedText(inFormat, i, length);
            } else if (c == designator) {
                return true;
            }
            i += count;
        }
        return false;
    }

    private static int skipQuotedText(CharSequence s, int i, int len) {
        if (i + 1 < len && s.charAt(i + 1) == '\'') {
            return 2;
        }
        int count = 1;
        int i2 = i + 1;
        while (i2 < len) {
            if (s.charAt(i2) == '\'') {
                count++;
                if (i2 + 1 >= len || s.charAt(i2 + 1) != '\'') {
                    return count;
                }
                i2++;
            } else {
                i2++;
                count++;
            }
        }
        return count;
    }

    public static CharSequence format(CharSequence inFormat, Calendar inDate) {
        String replacement;
        SpannableStringBuilder s = new SpannableStringBuilder(inFormat);
        LocaleData localeData = LocaleData.get(Locale.getDefault());
        int len = inFormat.length();
        int i = 0;
        while (i < len) {
            int count = 1;
            int c = s.charAt(i);
            if (c == 39) {
                count = appendQuotedText(s, i, len);
                len = s.length();
            } else {
                while (i + count < len && s.charAt(i + count) == c) {
                    count++;
                }
                switch (c) {
                    case 65:
                    case 97:
                        replacement = localeData.amPm[inDate.get(9) + 0];
                        break;
                    case 69:
                    case 99:
                        replacement = getDayOfWeekString(localeData, inDate.get(7), count, c);
                        break;
                    case 72:
                    case 107:
                        replacement = zeroPad(inDate.get(11), count);
                        break;
                    case 75:
                    case 104:
                        int hour = inDate.get(10);
                        if (c == 104 && hour == 0) {
                            hour = 12;
                        }
                        replacement = zeroPad(hour, count);
                        break;
                    case 76:
                    case 77:
                        replacement = getMonthString(localeData, inDate.get(2), count, c);
                        break;
                    case 100:
                        replacement = zeroPad(inDate.get(5), count);
                        break;
                    case 109:
                        replacement = zeroPad(inDate.get(12), count);
                        break;
                    case 115:
                        replacement = zeroPad(inDate.get(13), count);
                        break;
                    case 121:
                        replacement = getYearString(inDate.get(1), count);
                        break;
                    case 122:
                        replacement = getTimeZoneString(inDate, count);
                        break;
                    default:
                        replacement = null;
                        break;
                }
                if (replacement != null) {
                    s.replace(i, i + count, (CharSequence) replacement);
                    count = replacement.length();
                    len = s.length();
                }
            }
            i += count;
        }
        if (inFormat instanceof Spanned) {
            return new SpannedString(s);
        }
        return s.toString();
    }

    private static String getDayOfWeekString(LocaleData ld, int day, int count, int kind) {
        boolean standalone = kind == 99;
        return count == 5 ? standalone ? ld.tinyStandAloneWeekdayNames[day] : ld.tinyWeekdayNames[day] : count == 4 ? standalone ? ld.longStandAloneWeekdayNames[day] : ld.longWeekdayNames[day] : standalone ? ld.shortStandAloneWeekdayNames[day] : ld.shortWeekdayNames[day];
    }

    private static String getMonthString(LocaleData ld, int month, int count, int kind) {
        boolean standalone = kind == 76;
        if (count == 5) {
            return standalone ? ld.tinyStandAloneMonthNames[month] : ld.tinyMonthNames[month];
        }
        if (count == 4) {
            return standalone ? ld.longStandAloneMonthNames[month] : ld.longMonthNames[month];
        }
        if (count == 3) {
            return standalone ? ld.shortStandAloneMonthNames[month] : ld.shortMonthNames[month];
        }
        return zeroPad(month + 1, count);
    }

    private static String getTimeZoneString(Calendar inDate, int count) {
        boolean dst;
        TimeZone tz = inDate.getTimeZone();
        if (count < 2) {
            return formatZoneOffset(inDate.get(16) + inDate.get(15), count);
        }
        if (inDate.get(16) != 0) {
            dst = true;
        } else {
            dst = false;
        }
        return tz.getDisplayName(dst, 0);
    }

    private static String formatZoneOffset(int offset, int count) {
        int offset2 = offset / 1000;
        StringBuilder tb = new StringBuilder();
        if (offset2 < 0) {
            tb.insert(0, "-");
            offset2 = -offset2;
        } else {
            tb.insert(0, "+");
        }
        tb.append(zeroPad(offset2 / 3600, 2));
        tb.append(zeroPad((offset2 % 3600) / 60, 2));
        return tb.toString();
    }

    private static String getYearString(int year, int count) {
        if (count <= 2) {
            return zeroPad(year % 100, 2);
        }
        return String.format(Locale.getDefault(), "%d", Integer.valueOf(year));
    }

    private static int appendQuotedText(SpannableStringBuilder s, int i, int len) {
        if (i + 1 >= len || s.charAt(i + 1) != '\'') {
            int count = 0;
            s.delete(i, i + 1);
            int len2 = len - 1;
            while (i < len2) {
                if (s.charAt(i) != '\'') {
                    i++;
                    count++;
                } else if (i + 1 >= len2 || s.charAt(i + 1) != '\'') {
                    s.delete(i, i + 1);
                    return count;
                } else {
                    s.delete(i, i + 1);
                    len2--;
                    count++;
                    i++;
                }
            }
            return count;
        }
        s.delete(i, i + 1);
        return 1;
    }

    private static String zeroPad(int inValue, int inMinDigits) {
        return String.format(Locale.getDefault(), "%0" + inMinDigits + "d", Integer.valueOf(inValue));
    }
}

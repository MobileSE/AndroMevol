package com.android.internal.inputmethod;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class InputMethodUtils {
    public static final boolean DEBUG = false;
    private static final Locale ENGLISH_LOCALE = new Locale("en");
    public static final int NOT_A_SUBTYPE_ID = -1;
    private static final String NOT_A_SUBTYPE_ID_STR = String.valueOf(-1);
    private static final Locale[] SEARCH_ORDER_OF_FALLBACK_LOCALES = {Locale.ENGLISH, Locale.US, Locale.UK};
    public static final String SUBTYPE_MODE_ANY = null;
    public static final String SUBTYPE_MODE_KEYBOARD = "keyboard";
    public static final String SUBTYPE_MODE_VOICE = "voice";
    private static final String TAG = "InputMethodUtils";
    private static final String TAG_ASCII_CAPABLE = "AsciiCapable";
    private static final String TAG_ENABLED_WHEN_DEFAULT_IS_NOT_ASCII_CAPABLE = "EnabledWhenDefaultIsNotAsciiCapable";

    private InputMethodUtils() {
    }

    public static String getStackTrace() {
        StringBuilder sb = new StringBuilder();
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            StackTraceElement[] frames = e.getStackTrace();
            for (int j = 1; j < frames.length; j++) {
                sb.append(frames[j].toString() + "\n");
            }
            return sb.toString();
        }
    }

    public static String getApiCallStack() {
        String apiCallStack = "";
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            StackTraceElement[] frames = e.getStackTrace();
            for (int j = 1; j < frames.length; j++) {
                String tempCallStack = frames[j].toString();
                if (!TextUtils.isEmpty(apiCallStack) && tempCallStack.indexOf("Transact(") >= 0) {
                    break;
                }
                apiCallStack = tempCallStack;
            }
            return apiCallStack;
        }
    }

    public static boolean isSystemIme(InputMethodInfo inputMethod) {
        return (inputMethod.getServiceInfo().applicationInfo.flags & 1) != 0;
    }

    @Deprecated
    public static boolean isSystemImeThatHasEnglishKeyboardSubtype(InputMethodInfo imi) {
        if (!isSystemIme(imi)) {
            return false;
        }
        return containsSubtypeOf(imi, ENGLISH_LOCALE.getLanguage(), SUBTYPE_MODE_KEYBOARD);
    }

    public static Locale getFallbackLocaleForDefaultIme(ArrayList<InputMethodInfo> imis, Context context) {
        Locale[] arr$ = SEARCH_ORDER_OF_FALLBACK_LOCALES;
        for (Locale fallbackLocale : arr$) {
            for (int i = 0; i < imis.size(); i++) {
                InputMethodInfo imi = imis.get(i);
                if (isSystemIme(imi) && imi.isDefault(context) && containsSubtypeOf(imi, fallbackLocale, false, SUBTYPE_MODE_KEYBOARD)) {
                    return fallbackLocale;
                }
            }
        }
        return null;
    }

    private static boolean isSystemAuxilialyImeThatHasAutomaticSubtype(InputMethodInfo imi) {
        if (!(isSystemIme(imi) && imi.isAuxiliaryIme())) {
            return false;
        }
        int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            if (imi.getSubtypeAt(i).overridesImplicitlyEnabledSubtype()) {
                return true;
            }
        }
        return false;
    }

    public static Locale getSystemLocaleFromContext(Context context) {
        try {
            return context.getResources().getConfiguration().locale;
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    public static ArrayList<InputMethodInfo> getDefaultEnabledImes(Context context, boolean isSystemReady, ArrayList<InputMethodInfo> imis) {
        ArrayList<InputMethodInfo> retval;
        Locale fallbackLocale = getFallbackLocaleForDefaultIme(imis, context);
        if (!isSystemReady) {
            retval = new ArrayList<>();
            for (int i = 0; i < imis.size(); i++) {
                InputMethodInfo imi = imis.get(i);
                if (isSystemIme(imi) && imi.isDefault(context) && isImeThatHasSubtypeOf(imi, fallbackLocale, false, SUBTYPE_MODE_KEYBOARD)) {
                    retval.add(imi);
                }
            }
        } else {
            Locale systemLocale = getSystemLocaleFromContext(context);
            retval = new ArrayList<>();
            boolean systemLocaleKeyboardImeFound = false;
            for (int i2 = 0; i2 < imis.size(); i2++) {
                InputMethodInfo imi2 = imis.get(i2);
                if (isSystemIme(imi2) && imi2.isDefault(context)) {
                    boolean isSystemLocaleKeyboardIme = isImeThatHasSubtypeOf(imi2, systemLocale, false, SUBTYPE_MODE_KEYBOARD);
                    if (isSystemLocaleKeyboardIme || isImeThatHasSubtypeOf(imi2, fallbackLocale, false, SUBTYPE_MODE_ANY)) {
                        retval.add(imi2);
                    }
                    systemLocaleKeyboardImeFound |= isSystemLocaleKeyboardIme;
                }
            }
            if (!systemLocaleKeyboardImeFound) {
                for (int i3 = 0; i3 < imis.size(); i3++) {
                    InputMethodInfo imi3 = imis.get(i3);
                    if (isSystemIme(imi3) && imi3.isDefault(context) && !isImeThatHasSubtypeOf(imi3, fallbackLocale, false, SUBTYPE_MODE_KEYBOARD) && isImeThatHasSubtypeOf(imi3, systemLocale, true, SUBTYPE_MODE_ANY)) {
                        retval.add(imi3);
                    }
                }
            }
            int i4 = 0;
            while (true) {
                if (i4 >= retval.size()) {
                    for (int i5 = 0; i5 < imis.size(); i5++) {
                        InputMethodInfo imi4 = imis.get(i5);
                        if (isSystemAuxilialyImeThatHasAutomaticSubtype(imi4)) {
                            retval.add(imi4);
                        }
                    }
                } else if (retval.get(i4).isAuxiliaryIme()) {
                    break;
                } else {
                    i4++;
                }
            }
        }
        return retval;
    }

    public static boolean isImeThatHasSubtypeOf(InputMethodInfo imi, Locale locale, boolean ignoreCountry, String mode) {
        if (locale == null) {
            return false;
        }
        return containsSubtypeOf(imi, locale, ignoreCountry, mode);
    }

    @Deprecated
    public static boolean isValidSystemDefaultIme(boolean isSystemReady, InputMethodInfo imi, Context context) {
        if (!isSystemReady || !isSystemIme(imi)) {
            return false;
        }
        if (imi.getIsDefaultResourceId() != 0) {
            try {
                if (imi.isDefault(context) && containsSubtypeOf(imi, context.getResources().getConfiguration().locale.getLanguage(), SUBTYPE_MODE_ANY)) {
                    return true;
                }
            } catch (Resources.NotFoundException e) {
            }
        }
        if (imi.getSubtypeCount() != 0) {
            return false;
        }
        Slog.w(TAG, "Found no subtypes in a system IME: " + imi.getPackageName());
        return false;
    }

    public static boolean containsSubtypeOf(InputMethodInfo imi, Locale locale, boolean ignoreCountry, String mode) {
        int N = imi.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            InputMethodSubtype subtype = imi.getSubtypeAt(i);
            if (ignoreCountry) {
                if (!new Locale(getLanguageFromLocaleString(subtype.getLocale())).getLanguage().equals(locale.getLanguage())) {
                    continue;
                }
                if (mode != SUBTYPE_MODE_ANY || TextUtils.isEmpty(mode) || mode.equalsIgnoreCase(subtype.getMode())) {
                    return true;
                }
            } else {
                if (!TextUtils.equals(subtype.getLocale(), locale.toString())) {
                    continue;
                }
                if (mode != SUBTYPE_MODE_ANY) {
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static boolean containsSubtypeOf(InputMethodInfo imi, String language, String mode) {
        int N = imi.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            InputMethodSubtype subtype = imi.getSubtypeAt(i);
            if (subtype.getLocale().startsWith(language) && (mode == SUBTYPE_MODE_ANY || TextUtils.isEmpty(mode) || mode.equalsIgnoreCase(subtype.getMode()))) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<InputMethodSubtype> getSubtypes(InputMethodInfo imi) {
        ArrayList<InputMethodSubtype> subtypes = new ArrayList<>();
        int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            subtypes.add(imi.getSubtypeAt(i));
        }
        return subtypes;
    }

    public static ArrayList<InputMethodSubtype> getOverridingImplicitlyEnabledSubtypes(InputMethodInfo imi, String mode) {
        ArrayList<InputMethodSubtype> subtypes = new ArrayList<>();
        int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            InputMethodSubtype subtype = imi.getSubtypeAt(i);
            if (subtype.overridesImplicitlyEnabledSubtype() && subtype.getMode().equals(mode)) {
                subtypes.add(subtype);
            }
        }
        return subtypes;
    }

    public static InputMethodInfo getMostApplicableDefaultIME(List<InputMethodInfo> enabledImes) {
        if (enabledImes == null || enabledImes.isEmpty()) {
            return null;
        }
        int i = enabledImes.size();
        int firstFoundSystemIme = -1;
        while (i > 0) {
            i--;
            InputMethodInfo imi = enabledImes.get(i);
            if (isSystemImeThatHasEnglishKeyboardSubtype(imi) && !imi.isAuxiliaryIme()) {
                return imi;
            }
            if (firstFoundSystemIme < 0 && isSystemIme(imi) && !imi.isAuxiliaryIme()) {
                firstFoundSystemIme = i;
            }
        }
        return enabledImes.get(Math.max(firstFoundSystemIme, 0));
    }

    public static boolean isValidSubtypeId(InputMethodInfo imi, int subtypeHashCode) {
        return getSubtypeIdFromHashCode(imi, subtypeHashCode) != -1;
    }

    public static int getSubtypeIdFromHashCode(InputMethodInfo imi, int subtypeHashCode) {
        if (imi != null) {
            int subtypeCount = imi.getSubtypeCount();
            for (int i = 0; i < subtypeCount; i++) {
                if (subtypeHashCode == imi.getSubtypeAt(i).hashCode()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static ArrayList<InputMethodSubtype> getImplicitlyApplicableSubtypesLocked(Resources res, InputMethodInfo imi) {
        InputMethodSubtype lastResortKeyboardSubtype;
        InputMethodSubtype applicableSubtype;
        List<InputMethodSubtype> subtypes = getSubtypes(imi);
        String systemLocale = res.getConfiguration().locale.toString();
        if (TextUtils.isEmpty(systemLocale)) {
            return new ArrayList<>();
        }
        String systemLanguage = res.getConfiguration().locale.getLanguage();
        HashMap<String, InputMethodSubtype> applicableModeAndSubtypesMap = new HashMap<>();
        int N = subtypes.size();
        for (int i = 0; i < N; i++) {
            InputMethodSubtype subtype = subtypes.get(i);
            if (subtype.overridesImplicitlyEnabledSubtype()) {
                String mode = subtype.getMode();
                if (!applicableModeAndSubtypesMap.containsKey(mode)) {
                    applicableModeAndSubtypesMap.put(mode, subtype);
                }
            }
        }
        if (applicableModeAndSubtypesMap.size() > 0) {
            return new ArrayList<>(applicableModeAndSubtypesMap.values());
        }
        for (int i2 = 0; i2 < N; i2++) {
            InputMethodSubtype subtype2 = subtypes.get(i2);
            String locale = subtype2.getLocale();
            String mode2 = subtype2.getMode();
            if (getLanguageFromLocaleString(locale).equals(systemLanguage) && systemLocale.startsWith(locale) && ((applicableSubtype = applicableModeAndSubtypesMap.get(mode2)) == null || (!systemLocale.equals(applicableSubtype.getLocale()) && systemLocale.equals(locale)))) {
                applicableModeAndSubtypesMap.put(mode2, subtype2);
            }
        }
        InputMethodSubtype keyboardSubtype = applicableModeAndSubtypesMap.get(SUBTYPE_MODE_KEYBOARD);
        ArrayList<InputMethodSubtype> applicableSubtypes = new ArrayList<>(applicableModeAndSubtypesMap.values());
        if (keyboardSubtype != null && !keyboardSubtype.containsExtraValueKey(TAG_ASCII_CAPABLE)) {
            for (int i3 = 0; i3 < N; i3++) {
                InputMethodSubtype subtype3 = subtypes.get(i3);
                if (SUBTYPE_MODE_KEYBOARD.equals(subtype3.getMode()) && subtype3.containsExtraValueKey(TAG_ENABLED_WHEN_DEFAULT_IS_NOT_ASCII_CAPABLE)) {
                    applicableSubtypes.add(subtype3);
                }
            }
        }
        if (keyboardSubtype != null || (lastResortKeyboardSubtype = findLastResortApplicableSubtypeLocked(res, subtypes, SUBTYPE_MODE_KEYBOARD, systemLocale, true)) == null) {
            return applicableSubtypes;
        }
        applicableSubtypes.add(lastResortKeyboardSubtype);
        return applicableSubtypes;
    }

    private static List<InputMethodSubtype> getEnabledInputMethodSubtypeList(Context context, InputMethodInfo imi, List<InputMethodSubtype> enabledSubtypes, boolean allowsImplicitlySelectedSubtypes) {
        if (allowsImplicitlySelectedSubtypes && enabledSubtypes.isEmpty()) {
            enabledSubtypes = getImplicitlyApplicableSubtypesLocked(context.getResources(), imi);
        }
        return InputMethodSubtype.sort(context, 0, imi, enabledSubtypes);
    }

    public static String getLanguageFromLocaleString(String locale) {
        int idx = locale.indexOf(95);
        return idx < 0 ? locale : locale.substring(0, idx);
    }

    public static InputMethodSubtype findLastResortApplicableSubtypeLocked(Resources res, List<InputMethodSubtype> subtypes, String mode, String locale, boolean canIgnoreLocaleAsLastResort) {
        if (subtypes == null || subtypes.size() == 0) {
            return null;
        }
        if (TextUtils.isEmpty(locale)) {
            locale = res.getConfiguration().locale.toString();
        }
        String language = getLanguageFromLocaleString(locale);
        boolean partialMatchFound = false;
        InputMethodSubtype applicableSubtype = null;
        InputMethodSubtype firstMatchedModeSubtype = null;
        int N = subtypes.size();
        int i = 0;
        while (true) {
            if (i >= N) {
                break;
            }
            InputMethodSubtype subtype = subtypes.get(i);
            String subtypeLocale = subtype.getLocale();
            String subtypeLanguage = getLanguageFromLocaleString(subtypeLocale);
            if (mode == null || subtypes.get(i).getMode().equalsIgnoreCase(mode)) {
                if (firstMatchedModeSubtype == null) {
                    firstMatchedModeSubtype = subtype;
                }
                if (locale.equals(subtypeLocale)) {
                    applicableSubtype = subtype;
                    break;
                } else if (!partialMatchFound && language.equals(subtypeLanguage)) {
                    applicableSubtype = subtype;
                    partialMatchFound = true;
                }
            }
            i++;
        }
        return (applicableSubtype != null || !canIgnoreLocaleAsLastResort) ? applicableSubtype : firstMatchedModeSubtype;
    }

    public static boolean canAddToLastInputMethod(InputMethodSubtype subtype) {
        if (subtype != null && subtype.isAuxiliary()) {
            return false;
        }
        return true;
    }

    public static void setNonSelectedSystemImesDisabledUntilUsed(PackageManager packageManager, List<InputMethodInfo> enabledImis) {
        String[] systemImesDisabledUntilUsed = Resources.getSystem().getStringArray(R.array.config_disabledUntilUsedPreinstalledImes);
        if (!(systemImesDisabledUntilUsed == null || systemImesDisabledUntilUsed.length == 0)) {
            SpellCheckerInfo currentSpellChecker = TextServicesManager.getInstance().getCurrentSpellChecker();
            for (String packageName : systemImesDisabledUntilUsed) {
                boolean enabledIme = false;
                int j = 0;
                while (true) {
                    if (j >= enabledImis.size()) {
                        break;
                    } else if (packageName.equals(enabledImis.get(j).getPackageName())) {
                        enabledIme = true;
                        break;
                    } else {
                        j++;
                    }
                }
                if (!enabledIme && (currentSpellChecker == null || !packageName.equals(currentSpellChecker.getPackageName()))) {
                    ApplicationInfo ai = null;
                    try {
                        ai = packageManager.getApplicationInfo(packageName, 32768);
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.w(TAG, "NameNotFoundException: " + packageName, e);
                    }
                    if (ai != null) {
                        if ((ai.flags & 1) != 0) {
                            setDisabledUntilUsed(packageManager, packageName);
                        }
                    }
                }
            }
        }
    }

    private static void setDisabledUntilUsed(PackageManager packageManager, String packageName) {
        int state = packageManager.getApplicationEnabledSetting(packageName);
        if (state == 0 || state == 1) {
            packageManager.setApplicationEnabledSetting(packageName, 4, 0);
        }
    }

    public static CharSequence getImeAndSubtypeDisplayName(Context context, InputMethodInfo imi, InputMethodSubtype subtype) {
        CharSequence imiLabel = imi.loadLabel(context.getPackageManager());
        if (subtype == null) {
            return imiLabel;
        }
        CharSequence[] charSequenceArr = new CharSequence[2];
        charSequenceArr[0] = subtype.getDisplayName(context, imi.getPackageName(), imi.getServiceInfo().applicationInfo);
        charSequenceArr[1] = TextUtils.isEmpty(imiLabel) ? "" : " - " + ((Object) imiLabel);
        return TextUtils.concat(charSequenceArr);
    }

    public static boolean checkIfPackageBelongsToUid(AppOpsManager appOpsManager, int uid, String packageName) {
        try {
            appOpsManager.checkPackage(uid, packageName);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static class InputMethodSettings {
        private static final char INPUT_METHOD_SEPARATER = ':';
        private static final char INPUT_METHOD_SUBTYPE_SEPARATER = ';';
        private int[] mCurrentProfileIds = new int[0];
        private int mCurrentUserId;
        private String mEnabledInputMethodsStrCache;
        private final TextUtils.SimpleStringSplitter mInputMethodSplitter = new TextUtils.SimpleStringSplitter(INPUT_METHOD_SEPARATER);
        private final ArrayList<InputMethodInfo> mMethodList;
        private final HashMap<String, InputMethodInfo> mMethodMap;
        private final Resources mRes;
        private final ContentResolver mResolver;
        private final TextUtils.SimpleStringSplitter mSubtypeSplitter = new TextUtils.SimpleStringSplitter(INPUT_METHOD_SUBTYPE_SEPARATER);

        private static void buildEnabledInputMethodsSettingString(StringBuilder builder, Pair<String, ArrayList<String>> pair) {
            builder.append((String) pair.first);
            Iterator i$ = ((ArrayList) pair.second).iterator();
            while (i$.hasNext()) {
                builder.append(INPUT_METHOD_SUBTYPE_SEPARATER).append(i$.next());
            }
        }

        public InputMethodSettings(Resources res, ContentResolver resolver, HashMap<String, InputMethodInfo> methodMap, ArrayList<InputMethodInfo> methodList, int userId) {
            setCurrentUserId(userId);
            this.mRes = res;
            this.mResolver = resolver;
            this.mMethodMap = methodMap;
            this.mMethodList = methodList;
        }

        public void setCurrentUserId(int userId) {
            this.mCurrentUserId = userId;
        }

        public void setCurrentProfileIds(int[] currentProfileIds) {
            synchronized (this) {
                this.mCurrentProfileIds = currentProfileIds;
            }
        }

        public boolean isCurrentProfile(int userId) {
            boolean z = true;
            synchronized (this) {
                if (userId != this.mCurrentUserId) {
                    int i = 0;
                    while (true) {
                        if (i >= this.mCurrentProfileIds.length) {
                            z = false;
                            break;
                        } else if (userId == this.mCurrentProfileIds[i]) {
                            break;
                        } else {
                            i++;
                        }
                    }
                }
            }
            return z;
        }

        public List<InputMethodInfo> getEnabledInputMethodListLocked() {
            return createEnabledInputMethodListLocked(getEnabledInputMethodsAndSubtypeListLocked());
        }

        public List<Pair<InputMethodInfo, ArrayList<String>>> getEnabledInputMethodAndSubtypeHashCodeListLocked() {
            return createEnabledInputMethodAndSubtypeHashCodeListLocked(getEnabledInputMethodsAndSubtypeListLocked());
        }

        public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(Context context, InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
            List<InputMethodSubtype> enabledSubtypes = getEnabledInputMethodSubtypeListLocked(imi);
            if (allowsImplicitlySelectedSubtypes && enabledSubtypes.isEmpty()) {
                enabledSubtypes = InputMethodUtils.getImplicitlyApplicableSubtypesLocked(context.getResources(), imi);
            }
            return InputMethodSubtype.sort(context, 0, imi, enabledSubtypes);
        }

        public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(InputMethodInfo imi) {
            List<Pair<String, ArrayList<String>>> imsList = getEnabledInputMethodsAndSubtypeListLocked();
            ArrayList<InputMethodSubtype> enabledSubtypes = new ArrayList<>();
            if (imi != null) {
                Iterator i$ = imsList.iterator();
                while (true) {
                    if (!i$.hasNext()) {
                        break;
                    }
                    Pair<String, ArrayList<String>> imsPair = i$.next();
                    InputMethodInfo info = this.mMethodMap.get(imsPair.first);
                    if (info != null && info.getId().equals(imi.getId())) {
                        int subtypeCount = info.getSubtypeCount();
                        for (int i = 0; i < subtypeCount; i++) {
                            InputMethodSubtype ims = info.getSubtypeAt(i);
                            Iterator i$2 = ((ArrayList) imsPair.second).iterator();
                            while (i$2.hasNext()) {
                                if (String.valueOf(ims.hashCode()).equals((String) i$2.next())) {
                                    enabledSubtypes.add(ims);
                                }
                            }
                        }
                    }
                }
            }
            return enabledSubtypes;
        }

        public void enableAllIMEsIfThereIsNoEnabledIME() {
            if (TextUtils.isEmpty(getEnabledInputMethodsStr())) {
                StringBuilder sb = new StringBuilder();
                int N = this.mMethodList.size();
                for (int i = 0; i < N; i++) {
                    InputMethodInfo imi = this.mMethodList.get(i);
                    Slog.i(InputMethodUtils.TAG, "Adding: " + imi.getId());
                    if (i > 0) {
                        sb.append(INPUT_METHOD_SEPARATER);
                    }
                    sb.append(imi.getId());
                }
                putEnabledInputMethodsStr(sb.toString());
            }
        }

        public List<Pair<String, ArrayList<String>>> getEnabledInputMethodsAndSubtypeListLocked() {
            ArrayList<Pair<String, ArrayList<String>>> imsList = new ArrayList<>();
            String enabledInputMethodsStr = getEnabledInputMethodsStr();
            if (!TextUtils.isEmpty(enabledInputMethodsStr)) {
                this.mInputMethodSplitter.setString(enabledInputMethodsStr);
                while (this.mInputMethodSplitter.hasNext()) {
                    this.mSubtypeSplitter.setString(this.mInputMethodSplitter.next());
                    if (this.mSubtypeSplitter.hasNext()) {
                        ArrayList<String> subtypeHashes = new ArrayList<>();
                        String imeId = this.mSubtypeSplitter.next();
                        while (this.mSubtypeSplitter.hasNext()) {
                            subtypeHashes.add(this.mSubtypeSplitter.next());
                        }
                        imsList.add(new Pair<>(imeId, subtypeHashes));
                    }
                }
            }
            return imsList;
        }

        public void appendAndPutEnabledInputMethodLocked(String id, boolean reloadInputMethodStr) {
            if (reloadInputMethodStr) {
                getEnabledInputMethodsStr();
            }
            if (TextUtils.isEmpty(this.mEnabledInputMethodsStrCache)) {
                putEnabledInputMethodsStr(id);
            } else {
                putEnabledInputMethodsStr(this.mEnabledInputMethodsStrCache + INPUT_METHOD_SEPARATER + id);
            }
        }

        public boolean buildAndPutEnabledInputMethodsStrRemovingIdLocked(StringBuilder builder, List<Pair<String, ArrayList<String>>> imsList, String id) {
            boolean isRemoved = false;
            boolean needsAppendSeparator = false;
            for (Pair<String, ArrayList<String>> ims : imsList) {
                if (((String) ims.first).equals(id)) {
                    isRemoved = true;
                } else {
                    if (needsAppendSeparator) {
                        builder.append(INPUT_METHOD_SEPARATER);
                    } else {
                        needsAppendSeparator = true;
                    }
                    buildEnabledInputMethodsSettingString(builder, ims);
                }
            }
            if (isRemoved) {
                putEnabledInputMethodsStr(builder.toString());
            }
            return isRemoved;
        }

        private List<InputMethodInfo> createEnabledInputMethodListLocked(List<Pair<String, ArrayList<String>>> imsList) {
            ArrayList<InputMethodInfo> res = new ArrayList<>();
            for (Pair<String, ArrayList<String>> ims : imsList) {
                InputMethodInfo info = this.mMethodMap.get(ims.first);
                if (info != null) {
                    res.add(info);
                }
            }
            return res;
        }

        private List<Pair<InputMethodInfo, ArrayList<String>>> createEnabledInputMethodAndSubtypeHashCodeListLocked(List<Pair<String, ArrayList<String>>> imsList) {
            ArrayList<Pair<InputMethodInfo, ArrayList<String>>> res = new ArrayList<>();
            for (Pair<String, ArrayList<String>> ims : imsList) {
                InputMethodInfo info = this.mMethodMap.get(ims.first);
                if (info != null) {
                    res.add(new Pair<>(info, ims.second));
                }
            }
            return res;
        }

        private void putEnabledInputMethodsStr(String str) {
            Settings.Secure.putStringForUser(this.mResolver, "enabled_input_methods", str, this.mCurrentUserId);
            this.mEnabledInputMethodsStrCache = str;
        }

        public String getEnabledInputMethodsStr() {
            this.mEnabledInputMethodsStrCache = Settings.Secure.getStringForUser(this.mResolver, "enabled_input_methods", this.mCurrentUserId);
            return this.mEnabledInputMethodsStrCache;
        }

        private void saveSubtypeHistory(List<Pair<String, String>> savedImes, String newImeId, String newSubtypeId) {
            StringBuilder builder = new StringBuilder();
            boolean isImeAdded = false;
            if (!TextUtils.isEmpty(newImeId) && !TextUtils.isEmpty(newSubtypeId)) {
                builder.append(newImeId).append(INPUT_METHOD_SUBTYPE_SEPARATER).append(newSubtypeId);
                isImeAdded = true;
            }
            for (Pair<String, String> ime : savedImes) {
                String imeId = (String) ime.first;
                String subtypeId = (String) ime.second;
                if (TextUtils.isEmpty(subtypeId)) {
                    subtypeId = InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                }
                if (isImeAdded) {
                    builder.append(INPUT_METHOD_SEPARATER);
                } else {
                    isImeAdded = true;
                }
                builder.append(imeId).append(INPUT_METHOD_SUBTYPE_SEPARATER).append(subtypeId);
            }
            putSubtypeHistoryStr(builder.toString());
        }

        private void addSubtypeToHistory(String imeId, String subtypeId) {
            List<Pair<String, String>> subtypeHistory = loadInputMethodAndSubtypeHistoryLocked();
            Iterator i$ = subtypeHistory.iterator();
            while (true) {
                if (!i$.hasNext()) {
                    break;
                }
                Pair<String, String> ime = i$.next();
                if (((String) ime.first).equals(imeId)) {
                    subtypeHistory.remove(ime);
                    break;
                }
            }
            saveSubtypeHistory(subtypeHistory, imeId, subtypeId);
        }

        private void putSubtypeHistoryStr(String str) {
            Settings.Secure.putStringForUser(this.mResolver, "input_methods_subtype_history", str, this.mCurrentUserId);
        }

        public Pair<String, String> getLastInputMethodAndSubtypeLocked() {
            return getLastSubtypeForInputMethodLockedInternal(null);
        }

        public String getLastSubtypeForInputMethodLocked(String imeId) {
            Pair<String, String> ime = getLastSubtypeForInputMethodLockedInternal(imeId);
            if (ime != null) {
                return (String) ime.second;
            }
            return null;
        }

        private Pair<String, String> getLastSubtypeForInputMethodLockedInternal(String imeId) {
            List<Pair<String, ArrayList<String>>> enabledImes = getEnabledInputMethodsAndSubtypeListLocked();
            for (Pair<String, String> imeAndSubtype : loadInputMethodAndSubtypeHistoryLocked()) {
                String imeInTheHistory = (String) imeAndSubtype.first;
                if (TextUtils.isEmpty(imeId) || imeInTheHistory.equals(imeId)) {
                    String subtypeHashCode = getEnabledSubtypeHashCodeForInputMethodAndSubtypeLocked(enabledImes, imeInTheHistory, (String) imeAndSubtype.second);
                    if (!TextUtils.isEmpty(subtypeHashCode)) {
                        return new Pair<>(imeInTheHistory, subtypeHashCode);
                    }
                }
            }
            return null;
        }

        private String getEnabledSubtypeHashCodeForInputMethodAndSubtypeLocked(List<Pair<String, ArrayList<String>>> enabledImes, String imeId, String subtypeHashCode) {
            List<InputMethodSubtype> implicitlySelectedSubtypes;
            for (Pair<String, ArrayList<String>> enabledIme : enabledImes) {
                if (((String) enabledIme.first).equals(imeId)) {
                    ArrayList<String> explicitlyEnabledSubtypes = (ArrayList) enabledIme.second;
                    InputMethodInfo imi = this.mMethodMap.get(imeId);
                    if (explicitlyEnabledSubtypes.size() != 0) {
                        Iterator i$ = explicitlyEnabledSubtypes.iterator();
                        while (i$.hasNext()) {
                            String s = i$.next();
                            if (s.equals(subtypeHashCode)) {
                                try {
                                    if (InputMethodUtils.isValidSubtypeId(imi, Integer.valueOf(subtypeHashCode).intValue())) {
                                        return s;
                                    }
                                    return InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                                } catch (NumberFormatException e) {
                                    return InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                                }
                            }
                        }
                    } else if (!(imi == null || imi.getSubtypeCount() <= 0 || (implicitlySelectedSubtypes = InputMethodUtils.getImplicitlyApplicableSubtypesLocked(this.mRes, imi)) == null)) {
                        int N = implicitlySelectedSubtypes.size();
                        for (int i = 0; i < N; i++) {
                            if (String.valueOf(implicitlySelectedSubtypes.get(i).hashCode()).equals(subtypeHashCode)) {
                                return subtypeHashCode;
                            }
                        }
                    }
                    return InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                }
            }
            return null;
        }

        private List<Pair<String, String>> loadInputMethodAndSubtypeHistoryLocked() {
            ArrayList<Pair<String, String>> imsList = new ArrayList<>();
            String subtypeHistoryStr = getSubtypeHistoryStr();
            if (!TextUtils.isEmpty(subtypeHistoryStr)) {
                this.mInputMethodSplitter.setString(subtypeHistoryStr);
                while (this.mInputMethodSplitter.hasNext()) {
                    this.mSubtypeSplitter.setString(this.mInputMethodSplitter.next());
                    if (this.mSubtypeSplitter.hasNext()) {
                        String subtypeId = InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                        String imeId = this.mSubtypeSplitter.next();
                        if (this.mSubtypeSplitter.hasNext()) {
                            subtypeId = this.mSubtypeSplitter.next();
                        }
                        imsList.add(new Pair<>(imeId, subtypeId));
                    }
                }
            }
            return imsList;
        }

        private String getSubtypeHistoryStr() {
            return Settings.Secure.getStringForUser(this.mResolver, "input_methods_subtype_history", this.mCurrentUserId);
        }

        public void putSelectedInputMethod(String imeId) {
            Settings.Secure.putStringForUser(this.mResolver, "default_input_method", imeId, this.mCurrentUserId);
        }

        public void putSelectedSubtype(int subtypeId) {
            Settings.Secure.putIntForUser(this.mResolver, "selected_input_method_subtype", subtypeId, this.mCurrentUserId);
        }

        public String getDisabledSystemInputMethods() {
            return Settings.Secure.getStringForUser(this.mResolver, "disabled_system_input_methods", this.mCurrentUserId);
        }

        public String getSelectedInputMethod() {
            return Settings.Secure.getStringForUser(this.mResolver, "default_input_method", this.mCurrentUserId);
        }

        public boolean isSubtypeSelected() {
            return getSelectedInputMethodSubtypeHashCode() != -1;
        }

        private int getSelectedInputMethodSubtypeHashCode() {
            try {
                return Settings.Secure.getIntForUser(this.mResolver, "selected_input_method_subtype", this.mCurrentUserId);
            } catch (Settings.SettingNotFoundException e) {
                return -1;
            }
        }

        public boolean isShowImeWithHardKeyboardEnabled() {
            return Settings.Secure.getIntForUser(this.mResolver, "show_ime_with_hard_keyboard", 0, this.mCurrentUserId) == 1;
        }

        public void setShowImeWithHardKeyboard(boolean show) {
            Settings.Secure.putIntForUser(this.mResolver, "show_ime_with_hard_keyboard", show ? 1 : 0, this.mCurrentUserId);
        }

        public int getCurrentUserId() {
            return this.mCurrentUserId;
        }

        public int getSelectedInputMethodSubtypeId(String selectedImiId) {
            InputMethodInfo imi = this.mMethodMap.get(selectedImiId);
            if (imi == null) {
                return -1;
            }
            return InputMethodUtils.getSubtypeIdFromHashCode(imi, getSelectedInputMethodSubtypeHashCode());
        }

        public void saveCurrentInputMethodAndSubtypeToHistory(String curMethodId, InputMethodSubtype currentSubtype) {
            String subtypeId = InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
            if (currentSubtype != null) {
                subtypeId = String.valueOf(currentSubtype.hashCode());
            }
            if (InputMethodUtils.canAddToLastInputMethod(currentSubtype)) {
                addSubtypeToHistory(curMethodId, subtypeId);
            }
        }

        public HashMap<InputMethodInfo, List<InputMethodSubtype>> getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(Context context) {
            HashMap<InputMethodInfo, List<InputMethodSubtype>> enabledInputMethodAndSubtypes = new HashMap<>();
            for (InputMethodInfo imi : getEnabledInputMethodListLocked()) {
                enabledInputMethodAndSubtypes.put(imi, getEnabledInputMethodSubtypeListLocked(context, imi, true));
            }
            return enabledInputMethodAndSubtypes;
        }
    }
}

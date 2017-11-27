package jaygoo.library.m3u8downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Collections;
import java.util.Set;

public class SPHelper {

    private static final String NULL_KEY = "NULL_KEY";
    private static final String TAG_NAME = "M3U8PreferenceHelper";

    private static SharedPreferences PREFERENCES;


    public static void init(Context context) {
        PREFERENCES = context.getSharedPreferences(TAG_NAME, Context.MODE_PRIVATE);
    }

    public static void onSetPrefBoolSetting(String Tag, Boolean Value, Context activityContext) {
        if (Tag != null && Value != null && activityContext != null) {
            SharedPreferences settings = activityContext.getSharedPreferences(TAG_NAME, 0);
            settings.edit().putBoolean(Tag, Value).commit();
        }
    }

    private static String checkKeyNonNull(String key) {
        if (key == null) {
            Log.e(NULL_KEY, "Key is null!!!");
            return NULL_KEY;
        }
        return key;
    }

    private static SharedPreferences.Editor newEditor() {
        return PREFERENCES.edit();
    }

    public static void putBoolean(@NonNull String key, boolean value) {
        newEditor().putBoolean(checkKeyNonNull(key), value).apply();
    }

    public static boolean getBoolean(@NonNull String key, boolean defValue) {
        return PREFERENCES.getBoolean(checkKeyNonNull(key), defValue);
    }

    public static void putInt(@NonNull String key, int value) {
        newEditor().putInt(checkKeyNonNull(key), value).apply();
    }

    public static int getInt(@NonNull String key, int defValue) {
        return PREFERENCES.getInt(checkKeyNonNull(key), defValue);
    }

    public static void putLong(@NonNull String key, long value) {
        newEditor().putLong(checkKeyNonNull(key), value).apply();
    }

    public static long getLong(@NonNull String key, long defValue) {
        return PREFERENCES.getLong(checkKeyNonNull(key), defValue);
    }

    public static void putFloat(@NonNull String key, float value) {
        newEditor().putFloat(checkKeyNonNull(key), value).apply();
    }

    public static float getFloat(@NonNull String key, float defValue) {
        return PREFERENCES.getFloat(checkKeyNonNull(key), defValue);
    }

    public static void putString(@NonNull String key, @Nullable String value) {
        newEditor().putString(checkKeyNonNull(key), value).apply();
    }

    public static String getString(@NonNull String key, @Nullable String defValue) {
        return PREFERENCES.getString(checkKeyNonNull(key), defValue);
    }

    public static void putStringSet(@NonNull String key, @Nullable Set<String> values) {
        newEditor().putStringSet(checkKeyNonNull(key), values).apply();
    }

    public static Set<String> getStringSet(@NonNull String key, @Nullable Set<String> defValues) {
        Set<String> result = PREFERENCES.getStringSet(checkKeyNonNull(key), defValues);
        return result == null ? null : Collections.unmodifiableSet(result);
    }

    public static void increaseCount(String key) {
        int count = getInt(key, 0);
        putInt(key, ++count);
    }

    public static void remove(String key) {
        newEditor().remove(key).apply();
    }

    public static void clearPreference() {
        newEditor().clear().commit();
    }

}

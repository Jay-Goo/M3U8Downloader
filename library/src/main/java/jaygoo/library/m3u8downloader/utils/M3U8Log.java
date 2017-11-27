package jaygoo.library.m3u8downloader.utils;

import android.util.Log;

import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/21
 * 描    述: M3U8日志系统
 * ================================================
 */
public class M3U8Log {

    private static String TAG = "M3U8Log";

    public static void d(String msg){
        if (M3U8DownloaderConfig.isDebugMode()) Log.d(TAG, msg);
    }

    public static void e(String msg){
        if (M3U8DownloaderConfig.isDebugMode()) Log.e(TAG, msg);
    }


}

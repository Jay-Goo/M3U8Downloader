package jaygoo.library.m3u8downloader;

import jaygoo.library.m3u8downloader.bean.M3U8Task;


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/17
 * 描    述: M3U8Downloader 监听器
 * ================================================
 */
public abstract class OnM3U8DownloadListener {

    //切片下载
    public void onDownloadItem(M3U8Task task, long itemFileSize, int totalTs, int curTs) {

    }

    public void onDownloadSuccess(M3U8Task task) {

    }

    public void onDownloadPause(M3U8Task task) {

    }

    public void onDownloadPending(M3U8Task task) {

    }

    /**
     * 异步回调，不可以直接在UI线程调用
     * @param task
     */
    public void onDownloadProgress(M3U8Task task) {

    }

    public void onDownloadPrepare(M3U8Task task) {

    }

    /**
     * 线程环境无法保证，不可以直接在UI线程调用
     * @param task
     */
    public void onDownloadError(M3U8Task task, Throwable errorMsg) {

    }

}

package jaygoo.library.m3u8downloader;


import jaygoo.library.m3u8downloader.bean.BaseListener;
import jaygoo.library.m3u8downloader.bean.M3U8;

/**
 * 下载监听
 * Created by HDL on 2017/8/10.
 */

interface OnDownloadListener extends BaseListener {
    /**
     * 下载m3u8文件.
     * 注意：这个方法是异步的（子线程中执行），所以不能在此方法中回调，其他方法为主线程中回调
     *
     * @param totalFileSize
     * @param itemFileSize 单个文件的大小
     * @param totalTs      ts总数
     * @param curTs        当前下载完成的ts个数
     */
    void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs);

    /**
     * 下载成功
     */
    void onSuccess(M3U8 m3U8);

    /**
     * 当前已经下载的文件大小
     *
     * @param curLength
     */
    void onProgress(long curLength);
}

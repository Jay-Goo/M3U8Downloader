package jaygoo.library.m3u8downloader;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/12/14
 * 描    述: 删除任务缓存监听器
 * ================================================
 */
public interface OnDeleteTaskListener extends BaseListener {
    /**
     * 开始的时候回调
     */
    void onStart();

    /**
     * 非UI线程
     */
    void onSuccess();

    /**
     * 非UI线程
     */
    void onFail();
}

package jaygoo.library.m3u8downloader;

public interface BaseListener {
    /**
     * 开始的时候回调
     */
    void onStart();

    /**
     * 错误的时候回调
     *
     * @param errorMsg
     */
    void onError(Throwable errorMsg);
}

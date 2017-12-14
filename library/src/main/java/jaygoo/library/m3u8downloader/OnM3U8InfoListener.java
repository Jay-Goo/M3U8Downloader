package jaygoo.library.m3u8downloader;


import jaygoo.library.m3u8downloader.bean.M3U8;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/17
 * 描    述: 获取M3U8信息
 * ================================================
 */
public interface OnM3U8InfoListener extends BaseListener{

    /**
     * 开始的时候回调
     */
    @Override
    void onStart();

    /**
     * 获取成功的时候回调
     * 异步回调，不可以直接在UI线程调用
     */
    void onSuccess(M3U8 m3U8);

    /**
     *
     * 错误的时候回调
     * 异步回调，不可以直接在UI线程调用
     * @param errorMsg
     */
    @Override
    void onError(Throwable errorMsg);
}

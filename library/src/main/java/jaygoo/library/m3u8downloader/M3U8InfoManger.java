package jaygoo.library.m3u8downloader;


import java.io.IOException;

import jaygoo.library.m3u8downloader.bean.M3U8;
import jaygoo.library.m3u8downloader.utils.MUtils;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/15
 * 描    述: 获取M3U8信息的管理器
 * ================================================
 */
public class M3U8InfoManger {
    private static M3U8InfoManger mM3U8InfoManger;
    private OnM3U8InfoListener onM3U8InfoListener;

    private M3U8InfoManger() {
    }

    public static M3U8InfoManger getInstance() {
        synchronized (M3U8InfoManger.class) {
            if (mM3U8InfoManger == null) {
                mM3U8InfoManger = new M3U8InfoManger();
            }
        }
        return mM3U8InfoManger;
    }

    /**
     * 获取m3u8信息
     *
     * @param url
     * @param onM3U8InfoListener
     */
    public synchronized void getM3U8Info(final String url, OnM3U8InfoListener onM3U8InfoListener) {
        this.onM3U8InfoListener = onM3U8InfoListener;
        onM3U8InfoListener.onStart();
        new Thread() {
            @Override
            public void run() {
                try {
                    M3U8 m3u8 = MUtils.parseIndex(url);
                    handlerSuccess(m3u8);
                } catch (IOException e) {
                    handlerError(e);
                }
            }
        }.start();

    }

    /**
     * 通知异常
     *
     * @param e
     */
    private void handlerError(Throwable e) {
        onM3U8InfoListener.onError(e);
    }

    /**
     * 通知成功
     *
     * @param m3u8
     */
    private void handlerSuccess(M3U8 m3u8) {
        onM3U8InfoListener.onSuccess(m3u8);
    }
}

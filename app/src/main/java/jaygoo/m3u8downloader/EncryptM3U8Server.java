package jaygoo.m3u8downloader;

import jaygoo.library.m3u8downloader.M3U8EncryptHelper;
import jaygoo.library.m3u8downloader.utils.M3U8Log;
import jaygoo.local.server.M3U8HttpServer;


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/12/14
 * 描    述: 支持加密的M3U8Server
 * ================================================
 */
public class EncryptM3U8Server extends M3U8HttpServer {

    public void onPause(final String encryptKey){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    M3U8EncryptHelper.encryptTsFilesName(encryptKey,filesDir);
                } catch (Exception e) {
                    M3U8Log.e("M3u8Server onPause"+e.getMessage());
                }
            }
        }).start();

    }

    public void onResume(final String encryptKey){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    M3U8EncryptHelper.decryptTsFilesName(encryptKey,filesDir);
                } catch (Exception e) {
                    M3U8Log.e("M3u8Server onResume"+e.getMessage());
                }
            }
        }).start();

    }
}

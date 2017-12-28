package jaygoo.library.m3u8downloader.server;

import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;

import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.M3U8EncryptHelper;
import jaygoo.library.m3u8downloader.utils.M3U8Log;


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/12/18
 * 描    述: 加密版m3u8 httpServer
 * ================================================
 */
public class EncryptM3U8Server extends M3U8HttpServer {

    public void encrypt(){
        if (TextUtils.isEmpty(filesDir) || isEncrypt(filesDir))return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    M3U8EncryptHelper.encryptTsFilesName(
                            M3U8Downloader.getInstance().getEncryptKey()
                            ,filesDir
                    );
                } catch (Exception e) {
                    M3U8Log.e("M3u8Server encrypt: "+e.getMessage());
                }
            }
        }).start();

    }

    public void decrypt(){
        if (TextUtils.isEmpty(filesDir) || !isEncrypt(filesDir))return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    M3U8EncryptHelper.decryptTsFilesName(
                            M3U8Downloader.getInstance().getEncryptKey()
                            ,filesDir
                    );
                } catch (Exception e) {
                    M3U8Log.e("M3u8Server decrypt: "+e.getMessage());
                }
            }
        }).start();

    }

    /**
     * 文件夹文件是否已经加密,防止重复加密或解密
     * @param dirPath
     * @return
     */
    private boolean isEncrypt(String dirPath){
        try {
            File dirFile = new File(dirPath);
            if (dirFile.exists() && dirFile.isDirectory()){
                File[] files = dirFile.listFiles();
                for (int i = 0; i < files.length; i++) {// 遍历目录下所有的文件
                    if (files[i].getName().contains(".ts"))return false;
                }
            }
        }catch (Exception e){
        }
        return true;
    }

}

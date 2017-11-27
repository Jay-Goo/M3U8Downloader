package jaygoo.library.m3u8downloader.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;
import jaygoo.library.m3u8downloader.M3U8EncryptHelper;
import jaygoo.library.m3u8downloader.bean.M3U8;
import jaygoo.library.m3u8downloader.utils.M3U8Log;

import static jaygoo.library.m3u8downloader.server.NanoHTTPD.Response.Status;


public class M3u8Server extends NanoHTTPD {
    private static NanoHTTPD server;
    public static final int PORT = 8686;
    private static String filesDir = null;

    public static String createLocalUrl(String filePath){
        if (filePath != null) filesDir = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        return String.format("http://127.0.0.1:%d%s", PORT,filePath);
    }

    /**
     * 启动服务
     */
    public static void execute() {
        try {
            server = M3u8Server.class.newInstance();
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ioe) {
            M3U8Log.e("M3u8Server 启动服务失败：\n" + ioe);
            System.exit(-1);
        } catch (Exception e) {
            M3U8Log.e("M3u8Server 启动服务失败：\n" + e);
            System.exit(-1);
        }

        M3U8Log.d("M3u8Server 服务启动成功：\n");

        try {
            System.in.read();
        } catch (Throwable ignored) {
        }
    }

    public static void onPause(final String encryptKey){
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

    public static void onResume(final String encryptKey){
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

    /**
     * 关闭服务
     */
    public static void finish() {
        if(server != null){
            server.stop();
            M3U8Log.d("M3u8Server 服务已经关闭：\n");
            server = null;
        }
    }

    public M3u8Server() {
        super(PORT);
    }

    @Override
    public Response serve(IHTTPSession session) {

        String url = String.valueOf(session.getUri());

        M3U8Log.d("M3u8Server 请求URL：" + url);

        File file = new File(url);

        if(file.exists()){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "文件不存在：" + url);
            }
            // ts文件
            String mimeType = "video/mpeg";
            if(url.contains(".m3u8")){
                // m3u8文件
                mimeType = "video/x-mpegURL";
            }
            return newChunkedResponse(Status.OK, mimeType, fis);
        } else {
            return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "文件不存在：" + url);
        }
    }
}

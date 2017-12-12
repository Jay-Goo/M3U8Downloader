package jaygoo.library.m3u8downloader.server;


import android.net.Uri;

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
    private NanoHTTPD server;
    private static final int DEFAULT_PORT = 8686;
    private String filesDir = null;

    public String createLocalUrl(String url){
        Uri uri = Uri.parse(url);
        M3U8Log.d("uri: "+uri);
        String scheme = uri.getScheme();
        M3U8Log.d("scheme: "+scheme);
        String filePath ;
        if (null != scheme) {
            filePath = uri.toString();
        } else {
            filePath = uri.getPath();
        }
        if (filePath != null){
            filesDir = filePath.substring(0, filePath.lastIndexOf("/") + 1);
           return String.format("http://127.0.0.1:%d%s", myPort, filePath);
        }
        return null;
    }

    /**
     * 启动服务
     */
    public void execute() {
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

    /**
     * 关闭服务
     */
    public void finish() {
        if(server != null){
            server.stop();
            M3U8Log.d("M3u8Server 服务已经关闭：\n");
            server = null;
        }
    }

    public M3u8Server() {
        super(DEFAULT_PORT);
    }

    public M3u8Server(int port) {
        super(port);
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

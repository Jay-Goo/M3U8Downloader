package jaygoo.library.m3u8downloader.server;

import android.net.Uri;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/12/21
 * 描    述:
 * ================================================
 */
public class M3U8HttpServer extends NanoHTTPD {

    private NanoHTTPD server;
    private static final int DEFAULT_PORT = 8686;
    public String filesDir = null;
    private FileInputStream fis;

    public M3U8HttpServer() {
        super(DEFAULT_PORT);
    }

    public M3U8HttpServer(int port) {
        super(port);
    }

    public String createLocalHttpUrl(String filePath){
        Uri uri = Uri.parse(filePath);
        String scheme = uri.getScheme();
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
            server = M3U8HttpServer.class.newInstance();
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        } catch (IOException ioe) {
            System.exit(-1);
        } catch (Exception e) {
            System.exit(-1);
        }

    }


    /**
     * 关闭服务
     */
    public void finish() {
        if(server != null){
            server.stop();
            server = null;
        }
    }


    @Override
    public Response handle(IHTTPSession session) {
        return super.handle(session);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String url = String.valueOf(session.getUri());

        File file = new File(url);

        Response response = newFixedLengthResponse(Status.NOT_FOUND, "text/html", "文件不存在：" + url);
        if(file.exists()){

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

            try {
                response = newFixedLengthResponse(Status.OK, mimeType, fis, fis.available());
            } catch (IOException e) {

            }

        }

        return response;
    }

}


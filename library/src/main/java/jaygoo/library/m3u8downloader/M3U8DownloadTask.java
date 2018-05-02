package jaygoo.library.m3u8downloader;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jaygoo.library.m3u8downloader.bean.M3U8;
import jaygoo.library.m3u8downloader.bean.M3U8Ts;
import jaygoo.library.m3u8downloader.utils.M3U8Log;
import jaygoo.library.m3u8downloader.utils.MUtils;
/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/17
 * 描    述: 单独M3U8下载任务
 * ================================================
 */
class M3U8DownloadTask {
    private static final int WHAT_ON_ERROR = 1001;
    private static final int WHAT_ON_PROGRESS = 1002;
    private static final int WHAT_ON_SUCCESS = 1003;
    private static final int WHAT_ON_START_DOWNLOAD = 1004;
    private OnTaskDownloadListener onTaskDownloadListener;
    //加密Key，默认为空，不加密
    private String encryptKey = null;
    private String m3u8FileName = "local.m3u8";
    //文件保存的路径
    private String saveDir;
    //当前下载完成的文件个数
    private volatile int curTs = 0;
    //总文件的个数
    private volatile int totalTs = 0;
    //单个文件的大小
    private volatile long itemFileSize = 0;
    //所有文件的大小
    private volatile long totalFileSize = 0;
    private volatile boolean isStartDownload = true;
    /**
     * 当前已经在下完成的大小
     */
    private long curLength = 0;
    /**
     * 任务是否正在运行中
     */
    private boolean isRunning = false;
    /**
     * 线程池最大线程数，默认为3
     */
    private int threadCount = 3;
    /**
     * 读取超时时间
     */
    private int readTimeout = 30 * 60 * 1000;
    /**
     * 链接超时时间
     */
    private int connTimeout = 10 * 1000;
    /**
     * 定时任务
     */
    private Timer netSpeedTimer;
    private ExecutorService executor;//线程池
    private M3U8 currentM3U8;

    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_ON_ERROR:
                    onTaskDownloadListener.onError((Throwable) msg.obj);
                    break;

                case WHAT_ON_START_DOWNLOAD:
                    onTaskDownloadListener.onStartDownload(totalTs, curTs);
                    break;

                case WHAT_ON_PROGRESS:
                    onTaskDownloadListener.onDownloading(totalFileSize, itemFileSize, totalTs, curTs);
                    break;

                case WHAT_ON_SUCCESS:
                    if (netSpeedTimer != null) {
                        netSpeedTimer.cancel();
                    }
                    onTaskDownloadListener.onSuccess(currentM3U8);
                    break;
            }
            return true;
        }
    });

    public M3U8DownloadTask(){
        connTimeout = M3U8DownloaderConfig.getConnTimeout();
        readTimeout = M3U8DownloaderConfig.getReadTimeout();
        threadCount = M3U8DownloaderConfig.getThreadCount();
    }

    /**
     * 开始下载
     *
     * @param url
     * @param onTaskDownloadListener
     */
    public void download(final String url, OnTaskDownloadListener onTaskDownloadListener) {
        saveDir = MUtils.getSaveFileDir(url);
        M3U8Log.d("start download ,SaveDir: "+ saveDir);
        this.onTaskDownloadListener = onTaskDownloadListener;
        if (!isRunning()) {
            getM3U8Info(url);
        } else {
            handlerError(new Throwable("Task running"));
        }
    }


    public void setEncryptKey(String encryptKey){
        this.encryptKey = encryptKey;
    }

    public String getEncryptKey(){
        return encryptKey;
    }


    /**
     * 获取任务是否正在执行
     *
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 先获取m3u8信息
     *
     * @param url
     */
    private void getM3U8Info(String url) {

        M3U8InfoManger.getInstance().getM3U8Info(url, new OnM3U8InfoListener() {
            @Override
            public void onSuccess(final M3U8 m3U8) {
                currentM3U8 = m3U8;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            startDownload(m3U8);
                            if (executor != null) {
                                executor.shutdown();//下载完成之后要关闭线程池
                            }
                            while (executor != null && !executor.isTerminated()) {
                                //等待中
                                Thread.sleep(100);
                            }
                            if (isRunning) {
                                File m3u8File = MUtils.createLocalM3U8(new File(saveDir), m3u8FileName, currentM3U8);
                                currentM3U8.setM3u8FilePath(m3u8File.getPath());
                                currentM3U8.setDirFilePath(saveDir);
                                currentM3U8.getFileSize();
                                mHandler.sendEmptyMessage(WHAT_ON_SUCCESS);
                                isRunning = false;
                            }
                        } catch (InterruptedIOException e) {
                            //被中断了，使用stop时会抛出这个，不需要处理
                            return;
                        } catch (IOException e) {
                            handlerError(e);
                            return;
                        } catch (InterruptedException e) {
                            handlerError(e);
                            return;
                        } catch (Exception e) {
                            handlerError(e);
                        }
                    }
                }.start();
            }

            @Override
            public void onStart() {
                onTaskDownloadListener.onStart();
            }

            @Override
            public void onError(Throwable errorMsg) {
                handlerError(errorMsg);
            }
        });
    }

    /**
     * 开始下载
     * 关于断点续传，每个任务会根据url进行生成相应Base64目录
     * 如果任务已经停止、开始下载之前，下一次会判断相关任务目录中已经下载完成的ts文件是否已经下载过了，下载了就不再下载
     * @param m3U8
     */
    private void startDownload(final M3U8 m3U8) {
        final File dir = new File(saveDir);
        //没有就创建
        if (!dir.exists()) {
            dir.mkdirs();
        }
        totalTs = m3U8.getTsList().size();
        if (executor != null) {
            executor.shutdownNow();
        }
//        //等待线程池完全关闭
//        while (executor != null && !executor.isTerminated()) {
//            //等待中
//            try {
//                M3U8Log.d("startDownload wait executor shutDown!");
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                M3U8Log.e(e.getMessage());
//            }
//        }
        M3U8Log.d("executor is shutDown ! Downloading !");
        //初始化值
        curTs = 1;
        isRunning = true;
        isStartDownload = true;
        executor = null;

        executor = Executors.newFixedThreadPool(threadCount);
        final String basePath = m3U8.getBasePath();
        netSpeedTimer = new Timer();
        netSpeedTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onTaskDownloadListener.onProgress(curLength);
            }
        }, 0, 1500);

        for (final M3U8Ts m3U8Ts : m3U8.getTsList()) {//循环下载
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    File file;
                    try {
                        String fileName = M3U8EncryptHelper.encryptFileName(encryptKey, m3U8Ts.obtainEncodeTsFileName());
                        file = new File(dir + File.separator + fileName);
                    } catch (Exception e) {
                        file = new File(dir + File.separator + m3U8Ts.getUrl());
                    }

                    if (!file.exists()) {//下载过的就不管了

                        FileOutputStream fos = null;
                        InputStream inputStream = null;
                        try {
                            URL url = new URL(m3U8Ts.obtainFullUrl(basePath));
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(connTimeout);
                            conn.setReadTimeout(readTimeout);
                            if (conn.getResponseCode() == 200) {
                                if (isStartDownload){
                                    isStartDownload = false;
                                    mHandler.sendEmptyMessage(WHAT_ON_START_DOWNLOAD);
                                }
                                inputStream = conn.getInputStream();
                                fos = new FileOutputStream(file);//会自动创建文件
                                int len = 0;
                                byte[] buf = new byte[8 * 1024 * 1024];
                                while ((len = inputStream.read(buf)) != -1) {
                                    curLength += len;
                                    fos.write(buf, 0, len);//写入流中
                                }
                            } else {
                                handlerError(new Throwable(String.valueOf(conn.getResponseCode())));
                            }
                        } catch (MalformedURLException e) {
                            handlerError(e);
                        } catch (IOException e) {
                            handlerError(e);
                        } catch (Exception e) {
                            handlerError(e);
                        }
                        finally
                        {//关流
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                }
                            }
                        }

                        itemFileSize = file.length();
                        m3U8Ts.setFileSize(itemFileSize);
                        mHandler.sendEmptyMessage(WHAT_ON_PROGRESS);
                        curTs++;
                    }else {
                        curTs ++;
                        itemFileSize = file.length();
                        m3U8Ts.setFileSize(itemFileSize);
                    }
                }
            });
        }
    }


    /**
     * 通知异常
     *
     * @param e
     */
    private void handlerError(Throwable e) {
        if (!"Task running".equals(e.getMessage())) {
            stop();
        }
        //不提示被中断的情况
        if ("thread interrupted".equals(e.getMessage())) {
            return;
        }
        Message msg = Message.obtain();
        msg.obj = e;
        msg.what = WHAT_ON_ERROR;
        mHandler.sendMessage(msg);
    }

    /**
     * 停止任务
     */
    public void stop() {
        if (netSpeedTimer != null) {
            netSpeedTimer.cancel();
            netSpeedTimer = null;
        }
        isRunning = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public File getM3u8File(String url){
        try {
            return new File(MUtils.getSaveFileDir(url), m3u8FileName);
        }catch (Exception e){
            M3U8Log.e(e.getMessage());
        }
        return null;
    }

}

package jaygoo.library.m3u8downloader;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jaygoo.library.m3u8downloader.bean.M3U8;
import jaygoo.library.m3u8downloader.bean.M3U8Task;
import jaygoo.library.m3u8downloader.bean.M3U8TaskState;
import jaygoo.library.m3u8downloader.utils.M3U8Log;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/17
 * 描    述: M3U8下载器
 * ================================================
 */
public class M3U8Downloader {

    private long currentTime;
    private M3U8Task currentM3U8Task;
    private Queue<String> downLoadQueue;
    private static M3U8Downloader instance;
    private M3U8DownloadTask m3U8DownLoadTask;
    private List<String> pauseList = new ArrayList<>();
    private OnM3U8DownloadListener onM3U8DownloadListener;

    private M3U8Downloader() {
        downLoadQueue = new LinkedList<>();
        m3U8DownLoadTask = new M3U8DownloadTask();
    }

    public static M3U8Downloader getInstance(){
        if (instance == null){
            instance = new M3U8Downloader();
        }
        return instance;
    }


    /**
     * 防止快速点击引起ThreadPoolExecutor 频繁创建销毁引起crash
     * @return
     */
    private boolean isQuicklyClick(){
        boolean result = false;
        if (System.currentTimeMillis() - currentTime <= 100){
            result = true;
        }
        currentTime = System.currentTimeMillis();
        return result;
    }

    /**
     * 忽略当前任务将其移至队列尾部.开始下一个任务
     */
    private void ignoreDownloadTask() {
        if (null != downLoadQueue.poll() && downLoadQueue.size() > 0){
            download(downLoadQueue.element());
        }
    }

    /**
     * 下载下一个任务，直到任务全部完成
     */
    private void downloadNextTask() {
        if (null != downLoadQueue.poll() && downLoadQueue.size() > 0){
            startDownloadTask(downLoadQueue.element());
        }else {
            //所有任务都完成了
            if (onM3U8DownloadListener != null && pauseList.size() == 0){
                onM3U8DownloadListener.onAllTaskComplete();
            }
        }
    }

    /**
     * 插队任务
     * @param url
     */
    private void insertDownloadTask(String url){
        //停止当前任务
        m3U8DownLoadTask.stop();
        //依次出队，直至找到要插队的任务
        while (!url.equals(downLoadQueue.element())){
            downLoadQueue.poll();
        }
        //开始当前任务
        if (downLoadQueue.size() > 0) {
            startDownloadTask(downLoadQueue.element());
        }
    }

    private void pendingTask(M3U8Task task){
        task.setState(M3U8TaskState.PENDING);
        if (onM3U8DownloadListener != null){
            onM3U8DownloadListener.onDownloadPending(task);
        }
    }

    /**
     * 暂停，如果此任务正在下载则暂停，否则无反应
     * @param url
     */
    public void pause(String url){
        if (TextUtils.isEmpty(url) || isQuicklyClick())return;
        m3U8DownLoadTask.stop();
        pauseList.add(url);
        currentM3U8Task.setState(M3U8TaskState.PAUSE);
        if (onM3U8DownloadListener != null){
            onM3U8DownloadListener.onDownloadPause(currentM3U8Task);
        }
        if (downLoadQueue.size() > 0 && url.equals(downLoadQueue.element())){
            downloadNextTask();
        }
    }

    /**
     * 下载任务，如果当前任务在下载列表中则认为是插队，否则入队等候下载
     * @param url
     */
    public void download(String url){
        if (TextUtils.isEmpty(url) || isQuicklyClick())return;
        if (downLoadQueue.contains(url)){
            pendingTask(currentM3U8Task);
            insertDownloadTask(url);
        }else {
            pendingTask(new M3U8Task(url));
            downLoadQueue.offer(url);
            startDownloadTask(url);
        }
    }

    /**
     * 检查m3u8文件是否存在
     * @param url
     * @return
     */
    public boolean checkM3U8IsExist(String url){
        return m3U8DownLoadTask.getM3u8File(url).exists();
    }

    /**
     * 得到m3u8文件路径
     * @param url
     * @return
     */
    public String getM3U8Path(String url){
        return m3U8DownLoadTask.getM3u8File(url).getPath();
    }

    public boolean isRunning(){
        return downLoadQueue.size() > 0 && m3U8DownLoadTask.isRunning();
    }

    public List<String> getPauseList(){
     return pauseList;
    }

    public boolean isTaskDownloading(String url){
        return !TextUtils.isEmpty(url)
                && downLoadQueue.size() > 0
                && url.equals(downLoadQueue.element())
                && m3U8DownLoadTask.isRunning();
    }


    public void setOnM3U8DownloadListener(OnM3U8DownloadListener onM3U8DownloadListener) {
        this.onM3U8DownloadListener = onM3U8DownloadListener;
    }

    public void setEncryptKey(String encryptKey){
        m3U8DownLoadTask.setEncryptKey(encryptKey);
    }

    public String getEncryptKey(){
        return m3U8DownLoadTask.getEncryptKey();
    }

    private void startDownloadTask(String url){
        if (m3U8DownLoadTask.isRunning())return;
        try {
            if (pauseList.contains(url))pauseList.remove(url);
            m3U8DownLoadTask.download(url, onDownloadListener);
        }catch (Exception e){
            M3U8Log.e("startDownloadTask Error:"+e.getMessage());
        }
    }

    private OnDownloadListener onDownloadListener = new OnDownloadListener() {
        private long lastLength;
        private float downloadProgress;

        @Override
        public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
           if (!m3U8DownLoadTask.isRunning())return;
            M3U8Log.d("onDownloading: "+totalFileSize+"|"+itemFileSize+"|"+totalTs+"|"+curTs);
            currentM3U8Task.setState(M3U8TaskState.DOWNLOADING);
            downloadProgress = 1.0f * curTs / totalTs;

            if (onM3U8DownloadListener != null){
                onM3U8DownloadListener.onDownloadItem(currentM3U8Task, itemFileSize, totalTs, curTs);
            }
        }

        @Override
        public void onSuccess(M3U8 m3U8) {
            m3U8DownLoadTask.stop();
            currentM3U8Task.setState( M3U8TaskState.SUCCESS);
            if (onM3U8DownloadListener != null) {
                onM3U8DownloadListener.onDownloadSuccess(currentM3U8Task);
            }
            M3U8Log.d("m3u8 Downloader onSuccess: "+ m3U8);
            downloadNextTask();

        }

        @Override
        public void onProgress(long curLength) {
            if (curLength - lastLength > 0) {
                currentM3U8Task.setProgress(downloadProgress);
                currentM3U8Task.setSpeed(curLength - lastLength);
                if (onM3U8DownloadListener != null ){
                    onM3U8DownloadListener.onDownloadProgress(currentM3U8Task);
                }
                lastLength = curLength;
            }
        }

        @Override
        public void onStart() {
            currentM3U8Task = new M3U8Task(downLoadQueue.peek());
            currentM3U8Task.setState(M3U8TaskState.PREPARE);
            if (onM3U8DownloadListener != null){
                onM3U8DownloadListener.onDownloadPrepare(currentM3U8Task);
            }
            M3U8Log.d("onDownloadPrepare: "+ currentM3U8Task.getUrl());
        }

        @Override
        public void onError(Throwable errorMsg) {
            ignoreDownloadTask();
            currentM3U8Task.setState(M3U8TaskState.ERROR);
            if (onM3U8DownloadListener != null){
                onM3U8DownloadListener.onDownloadError(currentM3U8Task, errorMsg);
            }
            M3U8Log.e("onError: "+ errorMsg.getMessage());
        }

    };

}

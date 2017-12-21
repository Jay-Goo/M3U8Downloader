package jaygoo.library.m3u8downloader;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;

import jaygoo.library.m3u8downloader.bean.M3U8;
import jaygoo.library.m3u8downloader.bean.M3U8Task;
import jaygoo.library.m3u8downloader.bean.M3U8TaskState;
import jaygoo.library.m3u8downloader.utils.M3U8Log;
import jaygoo.library.m3u8downloader.utils.MUtils;

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
    private DownloadQueue downLoadQueue;
    private M3U8DownloadTask m3U8DownLoadTask;
    private OnM3U8DownloadListener onM3U8DownloadListener;

    private M3U8Downloader() {
        downLoadQueue = new DownloadQueue();
        m3U8DownLoadTask = new M3U8DownloadTask();
    }

    private static class SingletonHolder{
        static M3U8Downloader instance = new M3U8Downloader();
    }

    public static M3U8Downloader getInstance(){
        return SingletonHolder.instance;
    }


    /**
     * 防止快速点击引起ThreadPoolExecutor频繁创建销毁引起crash
     * @return
     */
    private boolean isQuicklyClick(){
        boolean result = false;
        if (System.currentTimeMillis() - currentTime <= 100){
            result = true;
            M3U8Log.d("is too quickly click!");
        }
        currentTime = System.currentTimeMillis();
        return result;
    }


    /**
     * 下载下一个任务，直到任务全部完成
     */
    private void downloadNextTask() {
        startDownloadTask(downLoadQueue.poll());
    }

    private void pendingTask(M3U8Task task){
        task.setState(M3U8TaskState.PENDING);
        if (onM3U8DownloadListener != null){
            onM3U8DownloadListener.onDownloadPending(task);
        }
    }


    /**
     * 下载任务
     * 如果当前任务在下载列表中则认为是暂停
     * 否则入队等候下载
     * @param url
     */
    public void download(String url){
        if (TextUtils.isEmpty(url) || isQuicklyClick())return;
        M3U8Task task = new M3U8Task(url);
        if (downLoadQueue.contains(task)){
            task = downLoadQueue.getTask(url);
            if (task.getState() == M3U8TaskState.PAUSE || task.getState() == M3U8TaskState.ERROR){
                startDownloadTask(task);
            }else {
                pause(url);
            }
        }else {
            downLoadQueue.offer(task);
            startDownloadTask(task);


        }
    }

    /**
     * 暂停，如果此任务正在下载则暂停，否则无反应
     * 只支持单一任务暂停，多任务暂停请使用{@link #pause(java.util.List)}
     * @param url
     */
    public void pause(String url){
        if (TextUtils.isEmpty(url))return;
        M3U8Task task = downLoadQueue.getTask(url);
        if (task != null) {
            task.setState(M3U8TaskState.PAUSE);

            if (onM3U8DownloadListener != null) {
                onM3U8DownloadListener.onDownloadPause(task);
            }

            if (url.equals(currentM3U8Task.getUrl())) {
                m3U8DownLoadTask.stop();
                downloadNextTask();
            } else {
                downLoadQueue.remove(task);
            }
        }
    }

    /**
     * 批量暂停
     * @param urls
     */
    public void pause(List<String> urls){
        if (urls == null || urls.size() == 0)return;
        boolean isCurrentTaskPause = false;
        for (String url : urls){
            if (downLoadQueue.contains(new M3U8Task(url))){
                M3U8Task task = downLoadQueue.getTask(url);
                if (task != null){
                    task.setState(M3U8TaskState.PAUSE);
                    if (onM3U8DownloadListener != null){
                        onM3U8DownloadListener.onDownloadPause(task);
                    }
                    if (task.equals(currentM3U8Task)){
                        m3U8DownLoadTask.stop();
                        isCurrentTaskPause = true;
                    }
                    downLoadQueue.remove(task);
                }
            }
        }
        if (isCurrentTaskPause)startDownloadTask(downLoadQueue.peek());
    }

    /**
     * 检查m3u8文件是否存在
     * @param url
     * @return
     */
    public boolean checkM3U8IsExist(String url){
        try {
            return m3U8DownLoadTask.getM3u8File(url).exists();
        }catch (Exception e){
            M3U8Log.e(e.getMessage());
        }
        return false;
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
        return m3U8DownLoadTask.isRunning();
    }


    /**
     *  if task is the current task , it will return true
     * @param url
     * @return
     */
    public boolean isCurrentTask(String url){
        return !TextUtils.isEmpty(url)
                && downLoadQueue.peek() != null
                && downLoadQueue.peek().getUrl().equals(url);
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

    private void startDownloadTask(M3U8Task task){
        if (task == null)return;
        pendingTask(task);
        if (!downLoadQueue.isHead(task)){
            M3U8Log.d("start download task, but task is running: " + task.getUrl());
            return;
        }

        if (task.getState() == M3U8TaskState.PAUSE){
            M3U8Log.d("start download task, but task has pause: " + task.getUrl());
            return;
        }
        try {
            currentM3U8Task = task;
            M3U8Log.d("====== start downloading ===== " + task.getUrl());
            m3U8DownLoadTask.download(task.getUrl(), onTaskDownloadListener);
        }catch (Exception e){
            M3U8Log.e("startDownloadTask Error:"+e.getMessage());
        }
    }

    /**
     * 取消任务
     * @param url
     */
    public void cancel(String url){
        pause(url);
    }

    /**
     * 批量取消任务
     * @param urls
     */
    public void cancel(List<String> urls){
        pause(urls);
    }

    /**
     * 取消任务,删除缓存
     * @param url
     */
    public void cancelAndDelete(final String url, @Nullable final OnDeleteTaskListener listener){
        pause(url);
        if (listener != null) {
            listener.onStart();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDelete = MUtils.clearDir(new File(MUtils.getSaveFileDir(url)));
                if (listener != null) {
                    if (isDelete) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }
            }
        }).start();
    }

    /**
     *  批量取消任务,删除缓存
     * @param urls
     * @param listener
     */
    public void cancelAndDelete(final List<String> urls, @Nullable final OnDeleteTaskListener listener){
        pause(urls);
        if (listener != null) {
            listener.onStart();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDelete = true;
                for (String url : urls){
                    isDelete = isDelete && MUtils.clearDir(new File(MUtils.getSaveFileDir(url)));
                }
                if (listener != null) {
                    if (isDelete) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }
            }
        }).start();
    }

    private OnTaskDownloadListener onTaskDownloadListener = new OnTaskDownloadListener() {
        private long lastLength;
        private float downloadProgress;

        @Override
        public void onStartDownload(int totalTs, int curTs) {
            M3U8Log.d("onStartDownload: "+totalTs+"|"+curTs);

            currentM3U8Task.setState(M3U8TaskState.DOWNLOADING);
            downloadProgress = 1.0f * curTs / totalTs;
        }

        @Override
        public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
            if (!m3U8DownLoadTask.isRunning())return;
            M3U8Log.d("onDownloading: "+totalFileSize+"|"+itemFileSize+"|"+totalTs+"|"+curTs);

            downloadProgress = 1.0f * curTs / totalTs;

            if (onM3U8DownloadListener != null){
                onM3U8DownloadListener.onDownloadItem(currentM3U8Task, itemFileSize, totalTs, curTs);
            }
        }

        @Override
        public void onSuccess(M3U8 m3U8) {
            m3U8DownLoadTask.stop();
            currentM3U8Task.setM3U8(m3U8);
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
            currentM3U8Task.setState(M3U8TaskState.PREPARE);
            if (onM3U8DownloadListener != null){
                onM3U8DownloadListener.onDownloadPrepare(currentM3U8Task);
            }
            M3U8Log.d("onDownloadPrepare: "+ currentM3U8Task.getUrl());
        }

        @Override
        public void onError(Throwable errorMsg) {
            if (errorMsg.getMessage() != null && errorMsg.getMessage().contains("ENOSPC")){
                currentM3U8Task.setState(M3U8TaskState.ENOSPC);
            }else {
                currentM3U8Task.setState(M3U8TaskState.ERROR);
            }
            if (onM3U8DownloadListener != null) {
                onM3U8DownloadListener.onDownloadError(currentM3U8Task, errorMsg);
            }
            M3U8Log.e("onError: " + errorMsg.getMessage());
            downloadNextTask();
        }

    };

}

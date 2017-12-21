package jaygoo.library.m3u8downloader;

import java.util.ArrayList;
import java.util.List;

import jaygoo.library.m3u8downloader.bean.M3U8Task;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/12/14
 * 描    述: 自定义下载队列，采用ArrayList，扩展一些操作，非线程安全
 * ================================================
 */
class DownloadQueue {

    private List<M3U8Task> queue;

    public DownloadQueue(){
        queue = new ArrayList<>();
    }

    /**
     * 入队
     * @param task
     */
    public void offer(M3U8Task task){
        queue.add(task);
    }

    /**
     * 队头元素出队，并返回队头元素
     * @return
     */
    public M3U8Task poll(){
        try {
            if (queue.size() >= 2){
                queue.remove(0);
                return queue.get(0);
            }else if (queue.size() == 1){
                queue.remove(0);
            }
        }catch (Exception e){
        }
        return null;
    }

    /**
     * 返回队头元素
     * @return
     */
    public M3U8Task peek(){
        try {
            if (queue.size() >= 1){
                return queue.get(0);
            }
        }catch (Exception e){
        }
        return null;
    }

    /**
     * 移除元素
     * @param task
     * @return 是否成功移除
     */
    public boolean remove(M3U8Task task){
        if (contains(task)){
            return queue.remove(task);
        }
        return false;
    }

    /**
     * 判断队列中是否含有此元素
     * @param task
     * @return
     */
    public boolean contains(M3U8Task task){
        return queue.contains(task);
    }

    /**
     * 通过url 返回队列中任务元素
     * @param url
     * @return
     */
    public M3U8Task getTask(String url){
        try {
            for (int i = 0; i < queue.size(); i++){
                if (queue.get(i).getUrl().equals(url)){
                    return queue.get(i);
                }
            }
        }catch (Exception e){
        }

        return null;
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    public int size(){
        return queue.size();
    }

    public boolean isHead(String url){
        return isHead(new M3U8Task(url));
    }

    public boolean isHead(M3U8Task task){
        return task.equals(peek());
    }
}

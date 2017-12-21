package jaygoo.library.m3u8downloader.bean;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/22
 * 描    述:
 * ================================================
 */
public class M3U8TaskState {
    public static final int DEFAULT = 0;//默认状态
    public static final int PENDING = -1;//下载排队
    public static final int PREPARE = 1;//下载准备中
    public static final int DOWNLOADING = 2;//下载中
    public static final int SUCCESS = 3;//下载完成
    public static final int ERROR = 4;//下载出错
    public static final int PAUSE = 5;//下载暂停
    public static final int ENOSPC = 6;//空间不足

}

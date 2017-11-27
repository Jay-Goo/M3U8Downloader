package jaygoo.library.m3u8downloader.bean;

import android.support.annotation.NonNull;

/**
 * m3u8切片类
 * Created by HDL on 2017/7/24.
 */

public class M3U8Ts implements Comparable<M3U8Ts> {
    private String file;
    private long fileSize;
    private float seconds;

    public M3U8Ts(String file, float seconds) {
        this.file = file;
        this.seconds = seconds;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public float getSeconds() {
        return seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return file + " (" + seconds + "sec)";
    }

    /**
     * 获取时间
     */
    public long getLongDate() {
        try {
            return Long.parseLong(file.substring(0, file.lastIndexOf(".")));
        }catch (NumberFormatException e){
            return 0;
        }
    }

    @Override
    public int compareTo(@NonNull M3U8Ts o) {
        return file.compareTo(o.file);
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}

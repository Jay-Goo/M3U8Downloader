# M3U8Downloader
M3U8下载器，支持多线程下载、断点续传、后台下载、本地播放解决方案、m3u8加密解决方案
# 依赖

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
dependencies {
	        compile 'com.github.Jay-Goo:M3U8Downloader:V1.0.8'
	}
```

# 用法
### 配置
* default Config: 
```
M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                ;
```
* Custom Config: 
```
M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                .setConnTimeout(10000)
                .setReadTimeout(10000)
                .setThreadCount(3)
                .setDebugMode(true)
                ;
```

### 单例
`M3U8Downloader.getInstance()`

### 下载
  `download(String url)`

### 暂停
 `pause(String url)`

### 取消任务、取消任务并删除文件缓存
  `cancel(String url)`

`cancelAndDelete(final String url, final OnDeleteTaskListener listener)`

### 下载监听

```
        M3U8Downloader.getInstance().setOnM3U8DownloadListener(onM3U8DownloadListener);

private OnM3U8DownloadListener onM3U8DownloadListener = new OnM3U8DownloadListener() {

        @Override
        public void onDownloadItem(M3U8Task task, long itemFileSize, int totalTs, int curTs) {
            super.onDownloadItem(task, itemFileSize, totalTs, curTs);
            //下载切片监听，非UI线程
        }

        @Override
        public void onDownloadSuccess(M3U8Task task) {
            super.onDownloadSuccess(task);
            //下载成功
        }

        @Override
        public void onDownloadPending(M3U8Task task) {
            super.onDownloadPending(task);
            //加入队列，任务挂起
        }

        @Override
        public void onDownloadPause(M3U8Task task) {
            super.onDownloadPause(task);
            //任务暂停
        }

        @Override
        public void onDownloadProgress(final M3U8Task task) {
            super.onDownloadProgress(task);
            //下载进度，非UI线程
        }

        @Override
        public void onDownloadPrepare(final M3U8Task task) {
            super.onDownloadPrepare(task);
            //准备下载
        }

        @Override
        public void onDownloadError(final M3U8Task task, Throwable errorMsg) {
            super.onDownloadError(task, errorMsg);
            //下载错误，非UI线程
        }

    };
```

### M3U8Task

```
    private String url; //下载链接
    private int state = M3U8TaskState.DEFAULT; //下载状态
    private long speed; //下载速度
    private float progress; //下载进度
    private M3U8 m3U8; //下载成功后得到
```
### 加密
加密后视频切片文件名乱序，并移除后缀。
```
//设置密匙
M3U8Downloader.getInstance().setEncryptKey(encryptKey);
//获取随机密匙
AES128Utils.getAESKey()
```

更多加密解密 [M3U8EncryptHelper](https://github.com/Jay-Goo/M3U8Downloader/blob/master/library/src/main/java/jaygoo/library/m3u8downloader/M3U8EncryptHelper.java)

### 本地播放
普通未加密请使用 [M3U8HttpServer](https://github.com/Jay-Goo/M3U8HttpServer) 

 加密解密请使用 [M3U8EncryptHelper](https://github.com/Jay-Goo/M3U8Downloader/blob/master/library/src/main/java/jaygoo/library/m3u8downloader/M3U8EncryptHelper.java)
 
具体使用样例参考  [FullScreenActivity](https://github.com/Jay-Goo/M3U8Downloader/blob/master/app/src/main/java/jaygoo/m3u8downloader/FullScreenActivity.java)

## 联系我

- Email： 1015121748@qq.com
- QQ Group: 573830030 有时候工作很忙没空看邮件和Issue,大家可以通过QQ群联系我
<div style="text-align: center;">
<img src="https://github.com/Jay-Goo/RangeSeekBar/blob/master/Gif/qq.png" style="margin: 0 auto;" height="250px"/>
</div>

## 一杯咖啡

大家都知道开源是件很辛苦的事情，这个项目也是我工作之余完成的，平时工作很忙，但大家提的需求基本上我都尽量满足，如果这个项目帮助你节省了大量时间，你很喜欢，你可以给我一杯咖啡的鼓励，不在于钱多钱少，关键是你的这份鼓励所带给我的力量~
<div style="text-align: center;">
<img src="https://github.com/Jay-Goo/RangeSeekBar/blob/master/Gif/pay.png" height="200px"/>
</div>

# 致谢
[huangdali - M3U8Manger](https://github.com/huangdali/M3U8Manger)

package jaygoo.m3u8downloader;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;
import jaygoo.library.m3u8downloader.OnM3U8DownloadListener;
import jaygoo.library.m3u8downloader.bean.M3U8Task;
import jaygoo.library.m3u8downloader.utils.AES128Utils;
import jaygoo.library.m3u8downloader.utils.M3U8Log;

public class MainActivity extends AppCompatActivity {
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    M3U8Task[] taskList = new M3U8Task[6];
    private VideoListAdapter adapter;
    private String dirPath;
    private String encryptKey = "63F06F99D823D33AAB89A0A93DECFEE0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAppPermissions();
        try {
            M3U8Log.d("AES BASE64 Random Key:"+AES128Utils.getAESKey() );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        dirPath = StorageUtils.getCacheDirectory(this).getPath()+"/m3u8Downloader";
        //common config !
        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                .setDebugMode(true)
        ;

        // add listener
        M3U8Downloader.getInstance().setOnM3U8DownloadListener(onM3U8DownloadListener);
        M3U8Downloader.getInstance().setEncryptKey(encryptKey);
        initData();
        adapter = new VideoListAdapter(this, R.layout.list_item, taskList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = taskList[position].getUrl();
                if (M3U8Downloader.getInstance().checkM3U8IsExist(url)){
                    Toast.makeText(getApplicationContext(),"本地文件已下载，正在播放中！！！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,FullScreenActivity.class);
                    intent.putExtra("M3U8_URL",M3U8Downloader.getInstance().getM3U8Path(url));
                    startActivity(intent);
                }else {
                    if (M3U8Downloader.getInstance().isTaskDownloading(url)) {
                        M3U8Downloader.getInstance().pause(url);
                    } else {
                        M3U8Downloader.getInstance().download(url);
                    }
                }
            }
        });
    }

    private void initData(){
//        MUtils.clearDir(new File(dirPath));
        M3U8Task bean0 = new M3U8Task("https://media6.smartstudy.com/52/9c/10732/4/dest.m3u8");
        M3U8Task bean1 = new M3U8Task("https://media6.smartstudy.com/b2/75/2475/4/dest.m3u8");
        M3U8Task bean2 = new M3U8Task("https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8");
        M3U8Task bean3 = new M3U8Task("http://hls.ciguang.tv/hdtv/video.m3u8");
        M3U8Task bean4 = new M3U8Task("http://hcjs2ra2rytd8v8np1q.exp.bcevod.com/mda-hegtjx8n5e8jt9zv/mda-hegtjx8n5e8jt9zv.m3u8");
        M3U8Task bean5 = new M3U8Task("https://media6.smartstudy.com/55/34/2542/4/dest.m3u8");
        taskList[0] = bean0;
        taskList[1] = bean1;
        taskList[2] = bean2;
        taskList[3] = bean3;
        taskList[4] = bean4;
        taskList[5] = bean5;
    }

    private OnM3U8DownloadListener onM3U8DownloadListener = new OnM3U8DownloadListener() {

        @Override
        public void onDownloadSuccess(M3U8Task task) {
            super.onDownloadSuccess(task);
            adapter.notifyChanged(taskList, task);
        }

        @Override
        public void onDownloadPending(M3U8Task task) {
            super.onDownloadPending(task);
            adapter.notifyChanged(taskList, task);
        }

        @Override
        public void onDownloadPause(M3U8Task task) {
            super.onDownloadPause(task);
            adapter.notifyChanged(taskList, task);
        }

        @Override
        public void onDownloadProgress(final M3U8Task task) {
            super.onDownloadProgress(task);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyChanged(taskList, task);
                }
            });

        }

        @Override
        public void onDownloadPrepare(final M3U8Task task) {
            super.onDownloadPrepare(task);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyChanged(taskList, task);
                }
            });
        }

        @Override
        public void onDownloadError(M3U8Task task, Throwable errorMsg) {
            super.onDownloadError(task, errorMsg);
            adapter.notifyChanged(taskList, task);
        }

        @Override
        public void onAllTaskComplete() {
            super.onAllTaskComplete();
            Toast.makeText(getApplicationContext(),"文件全部下载完成！！！！", Toast.LENGTH_LONG).show();

        }
    };

    private void requestAppPermissions() {
        Dexter.withActivity(this)
                .withPermissions(PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            initView();
                            Toast.makeText(getApplicationContext(),"权限获取成功",Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(getApplicationContext(),"权限获取失败",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                })
                .check();
    }
}

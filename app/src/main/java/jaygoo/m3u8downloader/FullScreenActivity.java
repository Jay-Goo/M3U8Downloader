package jaygoo.m3u8downloader;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import jaygoo.library.m3u8downloader.server.EncryptM3U8Server;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/21
 * 描    述:
 * ================================================
 */
public class FullScreenActivity extends Activity{

    private StandardGSYVideoPlayer videoPlayer;
    private EncryptM3U8Server m3u8Server = new EncryptM3U8Server();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        videoPlayer = (StandardGSYVideoPlayer)findViewById(R.id.videoView);
        String url = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            url = bundle.getString("M3U8_URL");
        }
        m3u8Server.execute();
        videoPlayer.getCurrentPlayer().setUp(m3u8Server.createLocalHttpUrl(url),false,"");
        videoPlayer.startWindowFullscreen(this,false,false);

        videoPlayer.setBackFromFullScreenListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        videoPlayer.getCurrentPlayer().startPlayLogic();

    }

    @Override
    protected void onResume() {
        super.onResume();
        m3u8Server.decrypt();
    }

    @Override
    protected void onPause() {
        super.onPause();
        m3u8Server.encrypt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m3u8Server.finish();
        //释放所有
        videoPlayer.getCurrentPlayer().release();
        GSYVideoPlayer.releaseAllVideos();
        videoPlayer.setStandardVideoAllCallBack(null);
    }
}

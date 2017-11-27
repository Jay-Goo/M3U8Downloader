package jaygoo.m3u8downloader;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import jaygoo.library.m3u8downloader.server.M3u8Server;
import jaygoo.library.m3u8downloader.utils.M3U8Log;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

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
    private OrientationUtils orientationUtils;
    private String encryptKey = "63F06F99D823D33AAB89A0A93DECFEE0";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        videoPlayer = (StandardGSYVideoPlayer)findViewById(R.id.videoView);
        orientationUtils = new OrientationUtils(this, videoPlayer);
        orientationUtils.resolveByClick();
        videoPlayer.startWindowFullscreen(this,false,false);
        List<VideoOptionModel> optionModels = new ArrayList<>();
        optionModels.add(new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
                "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp"));
        GSYVideoManager.instance().setOptionModelList(optionModels);
        String url = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            url = bundle.getString("M3U8_URL");
        }
        Uri uri = Uri.parse(url);
        M3U8Log.d("uri: "+uri);
        String scheme = uri.getScheme();
        M3U8Log.d("scheme: "+scheme);
        String mVideoSource;
        if (null != scheme) {
            mVideoSource = uri.toString();
        } else {
            mVideoSource = uri.getPath();
        }
        M3U8Log.d("mVideoSource: "+mVideoSource);
        String mSource = M3u8Server.createLocalUrl(mVideoSource);
        M3U8Log.d("mSource: "+mSource);
        M3u8Server.execute();
        videoPlayer.setUp(mSource,false,"");
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        videoPlayer.startPlayLogic();

    }

    @Override
    protected void onResume() {
        super.onResume();
        M3u8Server.onResume(encryptKey);
    }

    @Override
    protected void onPause() {
        super.onPause();
        M3u8Server.onPause(encryptKey);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        M3u8Server.finish();
        //释放所有
        videoPlayer.getCurrentPlayer().release();
        GSYVideoPlayer.releaseAllVideos();
        videoPlayer.setStandardVideoAllCallBack(null);
    }
}

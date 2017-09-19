package com.icheyy.webrtcdemo.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.icheyy.webrtcdemo.ProxyRenderer;
import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.base.BaseAppActivity;
import com.icheyy.webrtcdemo.bean.RemoterPeer;
import com.icheyy.webrtcdemo.bean.PeerConnectionClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;


public class CallActivity extends BaseAppActivity {

    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String EXTRA_IS_CALLED = "com.icheyy.webrtc.IS_CALLED";//是否是呼叫方唤起

    public static final String EXTRA_USER_NAME = "com.icheyy.webrtc.USER_NAME";
    public static final String EXTRA_CALLER_NAME = "com.icheyy.webrtc.CALLER_NAME";
    public static final String EXTRA_VIDEO_CALL = "com.icheyy.webrtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "com.icheyy.webrtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "com.icheyy.webrtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "com.icheyy.webrtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "com.icheyy.webrtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "com.icheyy.webrtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "com.icheyy.webrtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "com.icheyy.webrtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "com.icheyy.webrtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "com.icheyy.webrtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "com.icheyy.webrtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "com.icheyy.webrtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "com.icheyy.webrtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "com.icheyy.webrtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "com.icheyy.webrtc.AECDUMP";
    public static final String EXTRA_OPENSLES_ENABLED = "com.icheyy.webrtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "com.icheyy.webrtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "com.icheyy.webrtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "com.icheyy.webrtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL = "com.icheyy.webrtc.ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "com.icheyy.webrtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "com.icheyy.webrtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "com.icheyy.webrtc.TRACING";
    public static final String EXTRA_CMDLINE = "com.icheyy.webrtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "com.icheyy.webrtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "com.icheyy.webrtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "com.icheyy.webrtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "com.icheyy.webrtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "com.icheyy.webrtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "com.icheyy.webrtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "com.icheyy.webrtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "com.icheyy.webrtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "com.icheyy.webrtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "com.icheyy.webrtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "com.icheyy.webrtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "com.icheyy.webrtc.NEGOTIATED";
    public static final String EXTRA_ID = "com.icheyy.webrtc.ID";


    private final static int VIDEO_CALL_SENT = 666;
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;


    //    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    //    private GLSurfaceView vsv;
    //    private VideoRenderer.Callbacks localRender;
    //    private VideoRenderer.Callbacks remoteRender;


    //==========================================
    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<VideoRenderer.Callbacks>();
    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final ProxyRenderer localProxyRenderer = new ProxyRenderer();
    private EglBase rootEglBase;
    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};
    private VideoTrack mRemoteVideoTrack;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // Check for mandatory permissions.  检查权限
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }


        final Intent intent = getIntent();
        // Get Intent parameters. 取得用户名
        String callerName = intent.getStringExtra(EXTRA_CALLER_NAME);
        Log.d(TAG, "toCall: calleeName:: " + callerName);
        if (TextUtils.isEmpty(callerName)) {
            Log.e(TAG, "toCall: Ooops...this username cannot be empty, please try again");
            return;
        }

        // pc init
        // Camera settings(顺序问题)
        pcClient.setListener(mRtcListener);
        pcClient.start(rootEglBase.getEglBaseContext());


        Boolean isCalled = intent.getBooleanExtra(EXTRA_IS_CALLED, false);
        if (!isCalled) {//是呼叫方
            toCall(callerName);
        }

    }


    private void initViews() {
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);
        remoteRenderers.add(remoteProxyRenderer);

        // Create video renderers.
        rootEglBase = EglBase.create();
        pipRenderer.init(rootEglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        fullscreenRenderer.init(rootEglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        setSwappedFeeds(true /* isSwappedFeeds */);

        remoteRenderers.add(remoteProxyRenderer);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            toHangUp();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }




    public void toCall(String remoterId) {
        Log.d(TAG, "toCall: ====================");
        pcClient.setRemoterId(remoterId);
        sendCall(remoterId);

    }

    public void toHangUp() {
        Log.d(TAG, "toHangUp: ====================");
        RemoterPeer remoterPeer = pcClient.getRemoterPeer();
        if(remoterPeer != null) {
            sendHangUp(remoterPeer);
            PeerConnection pc = remoterPeer.getPeerConnection();
            pc.close();
        }

        if (!isSwappedFeeds)
            setSwappedFeeds(true);

    }


    @Override
    public void onPause() {
//        if (pcClient != null) {
//            pcClient.onPause();
//        }
        if (pcClient != null) {
            pcClient.stopVideoSource();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pcClient != null) {
            pcClient.startVideoSource();
        }
        pcClient.setListener(mRtcListener);

    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        disconnect();
        pcClient.removeRemoterPeer();
        super.onDestroy();
    }

    private PeerConnectionClient.RtcListener mRtcListener = new PeerConnectionClient.RtcListener() {

        @Override
        public void onConnectSocketFinish(boolean result) {

        }

        @Override
        public void onStatusChanged(final String id, PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "onStatusChanged: id:: " + id + ", " + iceConnectionState);
            switch (iceConnectionState) {
                case DISCONNECTED:
                case CLOSED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplication().getApplicationContext(), id + " 通话关闭", Toast.LENGTH_LONG).show();
                        }
                    });
                    if (!isSwappedFeeds)
                        setSwappedFeeds(true);
                    pipRenderer.clearImage();
                    mRemoteVideoTrack = null;
                    remoteProxyRenderer.setTarget(null);
                    finish();
                    break;
            }
        }

        @Override
        public void onLocalStream(MediaStream localStream, VideoTrack track) {
            Log.d(TAG, "onLocalStream localStream.videoTracks:: " + localStream.videoTracks);
            Log.d(TAG, "onLocalStream localProxyRenderer:: " + localProxyRenderer);
            if (localStream.videoTracks == null)
                return;
            if (track == null)
                return;
            track.addRenderer(new VideoRenderer(localProxyRenderer));

        }

        @Override
        public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
            Log.d(TAG, "onAddRemoteStream");
            if (isSwappedFeeds)
                setSwappedFeeds(false);

            mRemoteVideoTrack = remoteStream.videoTracks.get(0);
            mRemoteVideoTrack.setEnabled(true);
            for (VideoRenderer.Callbacks remoteRender : remoteRenderers) {
                mRemoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
            }

        }

        @Override
        public void onRemoveRemoteStream() {
            Log.d(TAG, "onRemoveRemoteStream");

            if (!isSwappedFeeds)
                setSwappedFeeds(true);
            pipRenderer.clearImage();
            mRemoteVideoTrack = null;
            remoteProxyRenderer.setTarget(null);

            finish();

        }
    };

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyRenderer.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        remoteProxyRenderer.setTarget(null);
        localProxyRenderer.setTarget(null);
        if (pipRenderer != null) {
            pipRenderer.clearImage();
            pipRenderer.release();
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.clearImage();
            fullscreenRenderer.release();
        }
    }


    private void sendHangUp(RemoterPeer remoterPeer) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "leave");
            msg.put("connectedUser", remoterPeer.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pcClient.sendMessage(msg);
    }

    private void sendCall(String remoterId) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "call");
            msg.put("connectedUser", remoterId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pcClient.sendMessage(msg);
    }



}
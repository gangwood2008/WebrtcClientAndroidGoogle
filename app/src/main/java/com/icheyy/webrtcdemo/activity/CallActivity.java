package com.icheyy.webrtcdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.icheyy.webrtcdemo.ProxyRenderer;
import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.WebRTCClient;
import com.icheyy.webrtcdemo.base.BaseAppActivity;

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

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import okhttp3.OkHttpClient;

//import org.webrtc.VideoRendererGui;

public class CallActivity extends BaseAppActivity implements WebRTCClient.RtcListener {

    private static final String TAG = CallActivity.class.getSimpleName();


    public static final String EXTRA_USER_NAME = "com.icheyy.webrtc.USERNAME";
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

    private EditText mEtName;
    private EditText mEtCalleeName;
    //    private GLSurfaceView vsv;
//    private VideoRenderer.Callbacks localRender;
//    private VideoRenderer.Callbacks remoteRender;

    public static Handler mHandler = new Handler();
    private String mSocketAddress;
    private WebRTCClient pcClient;

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



        final Intent intent = getIntent();



        // Get Intent parameters. 取得用户名
        String userName = intent.getStringExtra(EXTRA_USER_NAME);
        Log.d(TAG, "user name is: " + userName);
        if (userName == null || userName.length() == 0) {
            logAndToast("用户名为空");
            Log.e(TAG, "Incorrect user name in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

//        init();
//        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
//        vsv.setPreserveEGLContextOnPause(true);
//        vsv.setKeepScreenOn(true);
//        VideoRendererGui.setView(vsv, new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "run in VideoRendererGui.setView");
//                init();
//            }
//        });

        // local and remote render
//        remoteRender = VideoRendererGui.create(
//                REMOTE_X, REMOTE_Y,
//                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
//        localRender = VideoRendererGui.create(
//                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyRenderer.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    private static final String VIDEO_CODEC_VP8 = "VP8";



    private void initViews() {
        mEtName = (EditText) findViewById(R.id.et_name);
        mEtCalleeName = (EditText) findViewById(R.id.et_calleeName);
    }

    public void click2Join(View view) {
        Log.i(TAG, "click2Join: ====================");
        String name = mEtName.getText().toString();
        Log.d(TAG, "click2Join: name:: " + name);
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "click2Join: Ooops...this username cannot be empty, please try again");
            return;
        }
        pcClient.setSelfId(name);

        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "join");
            msg.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pcClient.sendMessage(msg);
        pcClient.removeAllPeers();
        startCam();
        pcClient.addPeer(name, 0);
    }

    public void click2Call(View view) {
        Log.d(TAG, "click2Call: ====================");
        String calleeName = mEtCalleeName.getText().toString();
        Log.d(TAG, "click2Call: calleeName:: " + calleeName);
        pcClient.setConnectedId(calleeName);
        if (TextUtils.isEmpty(calleeName)) {
            Log.e(TAG, "click2Call: Ooops...this username cannot be empty, please try again");
        }

        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "call");
            msg.put("connectedUser", calleeName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pcClient.sendMessage(msg);
        pcClient.addPeer(calleeName, 1);
    }

    public void startCam() {
        // Camera settings
        pcClient.start("android_test", rootEglBase.getEglBaseContext());
    }

    public void click2HangUp(View view) {
        Log.d(TAG, "click2HangUp: ====================");
        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "leave");
            msg.put("connectedUser", pcClient.getSelfId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pcClient.sendMessage(msg);
        if (!isSwappedFeeds) setSwappedFeeds(true);
        pcClient.handleLeave();
    }

    @Override
    public void onPause() {
        if (pcClient != null) {
            pcClient.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pcClient != null) {
            pcClient.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pcClient != null) {
            pcClient.stopVideoSource();
        }
    }

    @Override
    public void onDestroy() {
        if (pcClient != null) {
            pcClient.onDestroy();
        }
        disconnect();


        super.onDestroy();
    }

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
                        Toast.makeText(CallActivity.this, id + " DISCONNECTED", Toast.LENGTH_SHORT).show();
                    }
                });
                if (!isSwappedFeeds) setSwappedFeeds(true);
                pipRenderer.clearImage();
                mRemoteVideoTrack = null;
                remoteProxyRenderer.setTarget(null);
                break;
        }
    }

    @Override
    public void onLocalStream(MediaStream localStream, VideoTrack track) {
        Log.d(TAG, "onLocalStream localStream.videoTracks:: " + localStream.videoTracks);
        Log.d(TAG, "onLocalStream localProxyRenderer:: " + localProxyRenderer);
        if (localStream.videoTracks == null) return;
        if (track == null) return;
        track.addRenderer(new VideoRenderer(localProxyRenderer));

//        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
//                scalingType);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        Log.d(TAG, "onAddRemoteStream");
        if (isSwappedFeeds) setSwappedFeeds(false);

        mRemoteVideoTrack = remoteStream.videoTracks.get(0);
        mRemoteVideoTrack.setEnabled(true);
        for (VideoRenderer.Callbacks remoteRender : remoteRenderers) {
            mRemoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
        }

//        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
//        VideoRendererGui.update(remoteRender,
//                REMOTE_X, REMOTE_Y,
//                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);
//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
//                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
//                scalingType);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        Log.d(TAG, "onRemoveRemoteStream");

        if (!isSwappedFeeds) setSwappedFeeds(true);
        pipRenderer.clearImage();
        mRemoteVideoTrack = null;
        remoteProxyRenderer.setTarget(null);

//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
//                scalingType);
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

    private IO.Options getIOOptions() {
        SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
        X509TrustManager x509TrustManager = getX509TrustManager();
        if (sslSocketFactory == null || x509TrustManager == null) return null;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .sslSocketFactory(sslSocketFactory, x509TrustManager)
                .build();

        // default settings for all sockets
        IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
        IO.setDefaultOkHttpCallFactory(okHttpClient);

        // set as an option
        IO.Options opts = new IO.Options();
        opts.callFactory = okHttpClient;
        opts.webSocketFactory = okHttpClient;
        return opts;
    }

    private X509TrustManager getX509TrustManager() {
        TrustManager[] trustManagers = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SSLSocketFactory getSSLSocketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext == null ? null : sslContext.getSocketFactory();
    }



}
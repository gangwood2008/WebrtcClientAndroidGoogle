package com.icheyy.webrtcdemo.bean;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.icheyy.webrtcdemo.PeerConnectionParameters;
import com.icheyy.webrtcdemo.activity.CallActivity;
import com.icheyy.webrtcdemo.base.BaseAppActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import io.socket.client.IO;

//import org.webrtc.VideoCapturerAndroid;

public class PeerConnectionClient {
    private final static String TAG = "PeerConnectionClient";

    private Handler mHandler = BaseAppActivity.mHandler;
    private Context mContext;
    private PeerConnectionFactory factory;
    // 本地连接接口
    private io.socket.client.Socket mSocket;
    private String mSelfId;
    private String mRemoterId;
    private RemoterPeer mRemoterPeer;
    // 本地连接参数和媒体信息
    private PeerConnectionParameters pcParams;

    private VideoSource videoSource;
    private VideoCapturer videoCapturer;
    private MediaStream mLocalMS;
    private MediaConstraints pcConstraints = new MediaConstraints();

    private RtcListener mListener;
    private RtcSignallingListener mSignallingListener;





    /**
     * Implement this interface to be notified of events.
     */
    public interface RtcListener {
        /**
         * socket连接完成
         * @param result
         */
        void onConnectSocketFinish(boolean result);

        /**
         * rtc状态改变
         * @param id
         * @param iceConnectionState
         */
        void onStatusChanged(String id, PeerConnection.IceConnectionState iceConnectionState);

        /**
         * 加载本地视频
         * @param localStream
         * @param track
         */
        void onLocalStream(MediaStream localStream, VideoTrack track);

        /**
         * 加载远程视频
         * @param remoteStream
         * @param endPoint
         */
        void onAddRemoteStream(MediaStream remoteStream, int endPoint);

        /**
         * 移除视频
         */
        void onRemoveRemoteStream();
    }

    public interface RtcSignallingListener {
        void onshow(JSONObject jsonAllUsers);
    }


    public PeerConnectionClient(RtcListener listener, RtcSignallingListener signallingListener, String host, IO.Options options,
                                PeerConnectionParameters params, final Context context) {
        Log.i(TAG, ">>>>>>> PeerConnectionClient: host:: " + host);
        mContext = context;
        mListener = listener;
        pcParams = params;
        this.mSignallingListener = signallingListener;


        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        mSocket = getSocket(host, options);

        // creatPeerConnectionFactory
        PeerConnectionFactory.initializeAndroidGlobals(
                context/*上下文，可自定义监听*/, pcParams.videoCodecHwAcceleration/*是否支持硬件加速*/);
        PeerConnectionFactory.Options opt = null;
        if (pcParams.loopback) {
            opt = new PeerConnectionFactory.Options();
            opt.networkIgnoreMask = 0;
        }
        factory = new PeerConnectionFactory(opt);
    }

    /**
     * 初始化本地socket
     */
    private io.socket.client.Socket getSocket(String host, IO.Options options) {

        try {
            //            mSocket = IO.socket(host);
            final io.socket.client.Socket mSocket = IO.socket(host, options);
            mSocket.on("message", onMessage);
            mSocket.connect();
            Log.d(TAG, "onCreate: mSocket.connect() finish");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: mSocket.connected():: " + mSocket.connected());
                    mListener.onConnectSocketFinish(mSocket.connected());
                }
            }, 2000);

            return mSocket;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Boolean> mAllUsers;

    private io.socket.emitter.Emitter.Listener onMessage = new io.socket.emitter.Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "call in onMessage: data:: \n" + data.toString());

            try {
                String event = (String) data.get("event");
                if (TextUtils.equals("show", event)) {
                    handleShow(data);
                } else if (TextUtils.equals("join", event)) {
                    handleJoin(data);
                } else if (TextUtils.equals("call", event)) {
                    handleCall(data);
                } else if (TextUtils.equals("accept", event)) {
                    handleAccept(data);
                } else if (TextUtils.equals("offer", event)) {
                    handleOffer(data);
                } else if (TextUtils.equals("candidate", event)) {
                    handleCandidate(data);
                } else if (TextUtils.equals("msg", event)) {
                    handleMsg(data);
                } else if (TextUtils.equals("answer", event)) {
                    handleAnswer(data);
                } else if (TextUtils.equals("leave", event)) {
                    handleLeave();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void handleShow(JSONObject data) throws JSONException {
        JSONObject allUsers = data.getJSONObject("allUsers");
        if (mSignallingListener != null) {
            mSignallingListener.onshow(allUsers);
        }
        Iterator<String> keys = allUsers.keys();
        if (mAllUsers == null) {
            mAllUsers = new HashMap<>();
        }
        while (keys.hasNext()) {
            String key = keys.next();
            mAllUsers.put(key, (Boolean) allUsers.get(key));
        }
        Log.i(TAG, "handleShow: " + mAllUsers);
    }

    private void handleJoin(JSONObject data) throws JSONException {
        boolean isSuccess = (boolean) data.get("success");

        Log.d(TAG, "handleJoin: isSuccess:: " + isSuccess);
        if (!isSuccess) {
            final String message = (String) data.get("message");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleCall(JSONObject data) throws JSONException {
        final String name = (String) data.get("name");
        Log.d(TAG, "handleCall: name:: " + name);
        mRemoterId = name;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle("视频邀请")
                        .setMessage("来自 " + name + " 的视频邀请，是否接收？")
                        .setPositiveButton("接收", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        createRemotePeer(mRemoterId);
                                        sendAccept(mRemoterId, true);
                                    }
                                }, 1000);// 确保CallActivity初始化完毕，mLocalMS初始化完毕


                                startCallActivity(mRemoterId);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendAccept(mRemoterId, false);
                                dialog.cancel();
                            }
                        }).create();
                dialog.show();
            }
        });
    }



    private void handleAccept(JSONObject data) throws JSONException {
        boolean isAccept = (boolean) data.get("accept");
        Log.d(TAG, "handleAccept: isAccept:: " + isAccept);
        if (isAccept) {
            createRemotePeer(mRemoterId);
            Log.i(TAG, "handleAccept: mCallerId:: " + mRemoterPeer.getId());
            Log.d(TAG, "handleAccept: peer:: " + mRemoterId);
            Log.d(TAG, "handleAccept: peerConn:: " + mRemoterPeer.getPeerConnection());
            mRemoterPeer.getPeerConnection().createOffer(mRemoterPeer, pcConstraints);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "对方已拒绝", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void handleOffer(JSONObject data) throws JSONException {
        String name = (String) data.get("name");
        JSONObject offer = (JSONObject) data.get("offer");
        Log.d(TAG, "handleOffer: name:: " + name);
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(offer.getString("type")),
                offer.getString("sdp")
        );
        mRemoterPeer.getPeerConnection().setRemoteDescription(mRemoterPeer, sdp);
        mRemoterPeer.getPeerConnection().createAnswer(mRemoterPeer, pcConstraints);
    }

    private void handleAnswer(JSONObject data) throws JSONException {
        JSONObject answer = (JSONObject) data.get("answer");
        Log.d(TAG, "handleAnswer: answer:: " + answer);
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(answer.getString("type")),
                answer.getString("sdp")
        );
        mRemoterPeer.getPeerConnection().setRemoteDescription(mRemoterPeer, sdp);
    }

    private void handleCandidate(JSONObject data) throws JSONException {
        JSONObject candidate = (JSONObject) data.get("candidate");
        Log.d(TAG, "handleCandidate: candidate:: " + candidate);
        mRemoterPeer.getPeerConnection().addIceCandidate(new IceCandidate(candidate.getString("sdpMid"),
                candidate.getInt("sdpMLineIndex"), candidate.getString("candidate")));
    }

    public void handleLeave() {
        Log.d(TAG, "handleLeave: ");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "对方已挂断", Toast.LENGTH_LONG).show();
            }
        });

        if (mListener != null) {
            mListener.onRemoveRemoteStream();
        }
        removeRemoterPeer();
    }

    private void handleMsg(JSONObject data) throws JSONException {
        String message = (String) data.get("message");
        Log.i(TAG, "handleMsg: message:: " + message);
    }


    public void removeRemoterPeer() {
        if(mRemoterPeer != null) {
            mRemoterPeer.dispose();
            mRemoterPeer = null;
        }
    }


    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (videoSource != null) {
            Log.d(TAG, "VideoSource dispose");
            videoSource.dispose();
            videoSource = null;
        }

        if (factory != null) {
            Log.d(TAG, "PeerConnectionFactory dispose");
            factory.dispose();
            factory = null;
        }


        if (mSocket != null) {
            Log.d(TAG, "Socket dispose");
            closeSocket();
            mSocket = null;
        }


    }

    private void closeSocket() {
        if(mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
    }

    /**
     * Start the client.
     * <p>
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     */
    public void start(EglBase.Context renderEGLContext) {
        initPeerConnectFactory(renderEGLContext);
    }

    private void initPeerConnectFactory(EglBase.Context renderEGLContext) {

        mLocalMS = factory.createLocalMediaStream("ARDAMS");
        Log.i(TAG, "initPeerConnectFactory: mLocalMS:: " + mLocalMS);

        VideoTrack track = null;
        if (pcParams.videoCallEnabled) {
            factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(pcParams.videoHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(pcParams.videoWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));

            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
            Log.d(TAG, "initPeerConnectFactory: videoCapturer:: " + videoCapturer);
            if (videoCapturer == null)
                return;
            videoSource = factory.createVideoSource(videoCapturer/*, videoConstraints*/);
            startVideoSource();
            track = factory.createVideoTrack("ARDAMSv0", videoSource);
            mLocalMS.addTrack(track);
            track.setEnabled(true);
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        mLocalMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));

        Log.d(TAG, "initPeerConnectFactory: track:: " + track);
        mListener.onLocalStream(mLocalMS, track);
    }

    private boolean videoCapturerStopped = true;

    public void stopVideoSource() {
        if (videoCapturer != null && !videoCapturerStopped) {
            Log.d(TAG, "Stop video source.");
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
            }
            videoCapturer.dispose();
            videoCapturerStopped = true;
        }
    }

    public void startVideoSource() {
        if (videoCapturer != null && videoCapturerStopped) {
            Log.d(TAG, "Restart video source.");
            videoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
            videoCapturerStopped = false;
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }



    private void createRemotePeer(String name) {
        if (mRemoterPeer == null) {
            mRemoterPeer = new RemoterPeer(name, mSocket,mLocalMS);
            if (factory == null) {
                Log.e(TAG, "faile createPeer " + name + ", PeerConnectionFactory is null");
                return;
            }
            PeerConnection pc = factory.createPeerConnection(getRTCConfig(), pcConstraints, mRemoterPeer);
            mRemoterPeer.setPeerConnection(pc);
            mRemoterPeer.setRTCListener(mListener);
        }
    }

    public void toJoin(String name) {
        Log.d(TAG, "toJoin: Local name:: " + name);
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "toJoin: Ooops...this username cannot be empty, please try again");
            return;
        }

        sendJoin(name);
        mSelfId = name;

    }

    private void sendJoin(String name) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "join");
            msg.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(msg);
    }
    private void sendAccept(String toName, boolean isAccept) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "accept");
            msg.put("connectedUser", toName);
            msg.put("accept", isAccept);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onClick: sendAccept:: " + msg.toString());
        mSocket.send(msg.toString());
    }


    public void sendMessage(String msg) {
        if (mSocket != null)
            mSocket.send(msg);
    }

    public void sendMessage(JSONObject jsonObject) {
        if (jsonObject == null)
            return;
        Log.i(TAG, "sendMessage: " + jsonObject.toString());
        sendMessage(jsonObject.toString());
    }

    private PeerConnection.RTCConfiguration getRTCConfig() {
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer("turn:call.icheyy.top:3478", "cheyy", "cheyy"));
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        return rtcConfig;
    }

    private void startCallActivity(String toName) {
        Intent intent = new Intent(mContext, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_USER_NAME, mSelfId);
        intent.putExtra(CallActivity.EXTRA_CALLER_NAME, toName);
        intent.putExtra(CallActivity.EXTRA_IS_CALLED, true);

        mContext.startActivity(intent);
    }


    public void setListener(RtcListener listener) {
        mListener = listener;
    }

    public RemoterPeer getRemoterPeer() {
        return mRemoterPeer;
    }

    public void setRemoterId(String remoterId) {
        mRemoterId = remoterId;
    }


}

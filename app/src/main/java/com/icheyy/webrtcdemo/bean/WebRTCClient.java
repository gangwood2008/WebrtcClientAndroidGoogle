package com.icheyy.webrtcdemo.bean;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.icheyy.webrtcdemo.PeerConnectionParameters;
import com.icheyy.webrtcdemo.activity.CallActivity;
import com.icheyy.webrtcdemo.helper.PeerManager;

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
import java.util.Map;

import io.socket.client.IO;

//import org.webrtc.VideoCapturerAndroid;

public class WebRTCClient {
    private final static String TAG = "WebRTCClient";

    private PeerManager mPM;

    //    private PeerConnectionFactory factory;
    //    private final static int MAX_PEER = 2;
    //    private boolean[] endPoints = new boolean[MAX_PEER];
    //    private HashMap<String, Peer> peers = new HashMap<>();

    // 本地连接接口
    private io.socket.client.Socket mSocket;
    private String mSelfId/*, mConnectedId*/;


    // 本地连接参数和媒体信息
    private PeerConnectionParameters pcParams;

    //    private MediaStream localMS;
    private VideoSource videoSource;
    private VideoCapturer videoCapturer;
    private MediaStream localMS;
    private MediaConstraints pcConstraints = new MediaConstraints();


    private RtcListener mListener;
    private RtcSignallingListener mSignallingListener;

    /**
     * Implement this interface to be notified of events.
     */
    public interface RtcListener {
        void onConnectSocketFinish(boolean result);

        void onStatusChanged(String id, PeerConnection.IceConnectionState iceConnectionState);

        void onLocalStream(MediaStream localStream, VideoTrack track);

        void onAddRemoteStream(MediaStream remoteStream, int endPoint);

        void onRemoveRemoteStream(int endPoint);
    }

    public interface RtcSignallingListener {
        void onshow(JSONObject jsonAllUsers);


    }

    public void setSignallingListener(RtcSignallingListener signallingListener) {
        mSignallingListener = signallingListener;
    }

    //    public void sendMessage(String msg) {
    //        mSocket.send(msg);
    //    }
    //
    //    public void sendMessage(JSONObject jsonObject) {
    //        if (jsonObject == null) return;
    //        Log.i(TAG, "sendMessage: " + jsonObject.toString());
    //        mSocket.send(jsonObject.toString());
    //    }

    //    private class Peer implements SdpObserver, PeerConnection.Observer {
    //        private PeerConnection pc;
    //        private String id;
    //        private int endPoint;
    //
    //        @Override
    //        public void onCreateSuccess(final SessionDescription sdp) {// createOffer/createAnswer成功回调此方法
    //            if (sdp == null) return;
    //            Log.d(TAG, "onCreateSuccess: sdp.description:: \n" + sdp.description);
    //            Log.i(TAG, "onCreateSuccess: sdp.type.canonicalForm():: " + sdp.type.canonicalForm());
    //
    //            try {
    //                JSONObject payload = new JSONObject();
    //                payload.put("type", sdp.type.canonicalForm());
    //                payload.put("sdp", sdp.description);
    //
    //                JSONObject msg = new JSONObject();
    //                msg.put("event", sdp.type.canonicalForm());
    //                msg.put("connectedUser", mConnectedId);
    //                msg.put(sdp.type.canonicalForm(), payload);
    //                sendMessage(msg);
    //                pc.setLocalDescription(Peer.this, sdp);
    //            } catch (JSONException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //
    //        @Override
    //        public void onSetSuccess() {
    //        }
    //
    //        @Override
    //        public void onCreateFailure(String s) {
    //            Log.e(TAG, "onCreateFailure: " + s);
    //        }
    //
    //        @Override
    //        public void onSetFailure(String s) {
    //        }
    //
    //        @Override
    //        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
    //        }
    //
    //        @Override
    //        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
    //            Log.i(TAG, "onIceConnectionChange: id:: " + id);
    //            Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);
    //            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
    //                if (peers != null) {
    //                    removePeer(id);
    //                }
    //            }
    //            if (mListener != null) {
    //                mListener.onStatusChanged(id, iceConnectionState);
    //            }
    //        }
    //
    //        @Override
    //        public void onIceConnectionReceivingChange(boolean b) {
    //            //===================================
    //            Log.d(TAG, "IceConnectionReceiving changed to " + b);
    //        }
    //
    //        @Override
    //        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
    //        }
    //
    //        @Override
    //        public void onIceCandidate(final IceCandidate candidate) {
    //            if (candidate == null) return;
    //            Log.d(TAG, "onIceCandidate: \ncandidate.sdpMLineIndex:: " + candidate.sdpMLineIndex +
    //                    "\ncandidate.sdpMid:: " + candidate.sdpMid);
    //            Log.d(TAG, "onIceCandidate: candidate.sdp:: \n" + candidate.sdp);
    //
    //            try {
    //                JSONObject payload = new JSONObject();
    //                payload.put("sdpMLineIndex", candidate.sdpMLineIndex);
    //                payload.put("sdpMid", candidate.sdpMid);
    //                payload.put("candidate", candidate.sdp);
    //
    //                JSONObject msg = new JSONObject();
    //                msg.put("event", "candidate");
    //                msg.put("connectedUser", mConnectedId);
    //                msg.put("candidate", payload);
    //                sendMessage(msg);
    //            } catch (JSONException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //
    //        @Override
    //        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
    //            //====================================
    //            Log.d(TAG, "onIceCandidatesRemoved: ");
    //        }
    //
    //        @Override
    //        public void onAddStream(MediaStream mediaStream) {
    //            Log.d(TAG, "onAddStream " + mediaStream.label());
    //
    //            if (mediaStream.videoTracks.size() == 1) {
    //                mListener.onAddRemoteStream(mediaStream, endPoint + 1);
    //            }
    //
    ////            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
    ////            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
    //        }
    //
    //        @Override
    //        public void onRemoveStream(MediaStream mediaStream) {
    //            Log.d(TAG, "onRemoveStream " + mediaStream.label());
    //            removePeer(id);
    //        }
    //
    //        @Override
    //        public void onDataChannel(DataChannel dataChannel) {
    //        }
    //
    //        @Override
    //        public void onRenegotiationNeeded() {
    //        }
    //
    //        @Override
    //        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
    //            //================================
    //            Log.d(TAG, "onAddTrack: ");
    //        }
    //
    //        public Peer(String id, int endPoint) {
    //            Log.d(TAG, "new Peer: " + id + " " + endPoint);
    //
    //
    //            PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
    //            // TCP candidates are only useful when connecting to a server that supports
    //            // ICE-TCP.
    //            rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
    //            rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
    //            rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
    //            rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
    //            // Use ECDSA encryption.
    //            rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
    //            this.pc = factory.createPeerConnection(rtcConfig, pcConstraints, this);
    //
    ////            this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
    //
    //            this.id = id;
    //            this.endPoint = endPoint;
    //
    //            Log.d(TAG, "Peer: localMS:: " + localMS);
    //            if(localMS != null) {
    //                pc.addStream(localMS);
    //            }
    //
    ////            if (mListener != null) {
    ////                mListener.onStatusChanged(id + " CONNECTING");
    ////            }
    //        }
    //
    //        @Override
    //        public String toString() {
    //            return "Peer{pc: " + pc + ", id: " + id + ", endPoint: " + endPoint + "}";
    //        }
    //    }


    //    public Peer addPeer(String id, int endPoint) {
    //        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    //        iceServers.add(new PeerConnection.IceServer("turn:call.icheyy.top","cheyy","cheyy"));
    //        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
    //        // TCP candidates are only useful when connecting to a server that supports
    //        // ICE-TCP.
    //        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
    //        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
    //        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
    //        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
    //        // Use ECDSA encryption.
    //        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
    //
    //        Peer peer = new Peer(id, endPoint, localMS,mSocket);
    //
    //        peer.setPeerConnection(factory.createPeerConnection(rtcConfig, pcConstraints, peer));
    //        peers.put(id, peer);
    //        endPoints[endPoint] = true;
    //        return peer;
    //    }
    //
    //
    //    public void removePeer(String id) {
    //        Peer peer = peers.get(id);
    //        if (peer == null) return;
    //        mListener.onRemoveRemoteStream(peer.getEndPoint());
    //        //        peer.pc.close();
    //        peers.remove(peer.getId());
    //        endPoints[peer.getEndPoint()] = false;
    //    }
    //
    //    public void removeAllPeers() {
    //        //        for (Map.Entry<String, Peer> entry : peers.entrySet()) {
    //        //            String id = entry.getKey();
    //        //            Peer peer = peers.get(id);
    //        ////            mListener.onRemoveRemoteStream(peer.endPoint);
    //        //            peer.pc.close();
    //        //            endPoints[peer.endPoint] = false;
    //        //        }
    //        peers.clear();
    //    }
    //


    private Handler mHandler = CallActivity.mHandler;
    private Context mContext;


    public WebRTCClient(RtcListener listener, RtcSignallingListener signallingListener, String host, IO.Options options,
                        PeerConnectionParameters params, /*EGLContext mEGLContext, */final Context context) {
        Log.i(TAG, ">>>>>>> WebRTCClient: host:: " + host);
        mContext = context;
        mListener = listener;
        pcParams = params;
        this.mSignallingListener = signallingListener;


        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        mSocket = initNativeSocket(context, host, options);

        mPM = new PeerManager(context, pcParams);
    }

    /**
     * 初始化本地socket
     *
     * @param context
     */
    private io.socket.client.Socket initNativeSocket(Context context, String host, IO.Options options) {

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
        getSelfPeer().setCallerId(name);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle("视频邀请")
                        .setMessage("来自 " + name + " 的视频邀请，是否接受？")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject msg = new JSONObject();
                                try {
                                    msg.put("event", "accept");
                                    msg.put("connectedUser", mSelfId);
                                    msg.put("accept", true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "onClick: sendAccept:: " + msg.toString());
                                mSocket.send(msg.toString());
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject msg = new JSONObject();
                                try {
                                    msg.put("event", "accept");
                                    msg.put("connectedUser", mSelfId);
                                    msg.put("accept", false);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "onClick: sendReject:: " + msg.toString());
                                mSocket.send(msg.toString());
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
            String mConnectedId = getSelfPeer().getCallerId();
            if (!mPM.containPeer(mConnectedId)) {
                mPM.addPeer(mConnectedId, 1, localMS, pcConstraints, mSocket);
            }
            Peer peer = mPM.getPeer(mConnectedId);
            peer.setCallerId(mSelfId);
            Log.i(TAG, "handleAccept: mConnectedId:: " + mConnectedId);
            Log.d(TAG, "handleAccept: peer:: " + peer);
            Log.d(TAG, "handleAccept: peerConn:: " + peer.getPeerConnection());
            peer.getPeerConnection().createOffer(peer, pcConstraints);
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
        Peer peer = mPM.getPeer(name);
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(offer.getString("type")),
                offer.getString("sdp")
        );
        peer.getPeerConnection().setRemoteDescription(peer, sdp);
        peer.getPeerConnection().createAnswer(peer, pcConstraints);
    }

    private void handleCandidate(JSONObject data) throws JSONException {
        JSONObject candidate = (JSONObject) data.get("candidate");
        Log.d(TAG, "handleCandidate: candidate:: " + candidate);
        String mConnectedId = getSelfPeer().getCallerId();
        Peer peer = mPM.getPeer(mConnectedId);
        peer.getPeerConnection().addIceCandidate(new IceCandidate(candidate.getString("sdpMid"),
                candidate.getInt("sdpMLineIndex"), candidate.getString("candidate")));
    }

    private void handleMsg(JSONObject data) throws JSONException {
        String message = (String) data.get("message");
        Log.i(TAG, "handleMsg: message:: " + message);
    }

    private void handleAnswer(JSONObject data) throws JSONException {
        JSONObject answer = (JSONObject) data.get("answer");
        Log.d(TAG, "handleAnswer: answer:: " + answer);
        String mConnectedId = getSelfPeer().getCallerId();
        Peer peer = mPM.getPeer(mConnectedId);
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(answer.getString("type")),
                answer.getString("sdp")
        );
        peer.getPeerConnection().setRemoteDescription(peer, sdp);
    }

    public void handleLeave() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "Chat finished! ", Toast.LENGTH_SHORT).show();
            }
        });

        if (mListener != null) {
            mListener.onRemoveRemoteStream(1);
        }

        mPM.removeAllPeers();
        //        mListener = null;
        getSelfPeer().setCallerId(null);

        //        alert("通话已结束");
        //        connectedUser = null;
        //        this.remote_video = "";
        //        peerConn.close();
        //        peerConn.onicecandidate = null;
        //        peerConn.onaddstream = null;
        //        if (peerConn.signalingState == 'closed') {
        //            this.initCreate();
        //        }
    }

    private void closeSocket() {
        mSocket.disconnect();
        mSocket.close();
    }

    /**
     * Call this method in Activity.onPause()
     */
    public void onPause() {
        //        if (videoSource != null) videoSource.stop();
    }

    /**
     * Call this method in Activity.onResume()
     */
    public void onResume() {
        //        if (videoSource != null) videoSource.restart();
    }

    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {

        if (videoSource != null) {
            videoSource.dispose();
        }

        if (mSocket != null) {
            closeSocket();
        }
        mPM.dispose();
    }



    /**
     * Start the client.
     * <p>
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     *
     */
    public void start(EglBase.Context renderEGLContext) {
        setCamera(renderEGLContext);
        getSelfPeer().setStream(localMS);
    }

    private void setCamera(EglBase.Context renderEGLContext) {

        PeerConnectionFactory factory = mPM.getFactory();
        localMS = factory.createLocalMediaStream("ARDAMS");
        Log.i(TAG, "setCamera: localMS:: " + localMS);
        VideoTrack track = null;
        if (pcParams.videoCallEnabled) {
            factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(pcParams.videoHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(pcParams.videoWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));

            //            VideoCapturer videoCapturer = getVideoCapturer();
            //            VideoCapturer videoCapturer = createCameraCapturer(new Camera2Enumerator(mContext));
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
            Log.d(TAG, "setCamera: videoCapturer:: " + videoCapturer);
            if (videoCapturer == null)
                return;
            videoSource = factory.createVideoSource(videoCapturer/*, videoConstraints*/);
            startVideoSource();
            track = factory.createVideoTrack("ARDAMSv0", videoSource);
            localMS.addTrack(track);
            track.setEnabled(true);
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));

        Log.d(TAG, "setCamera: track:: " + track);
        mListener.onLocalStream(localMS, track);
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

    //    private VideoCapturer getVideoCapturer() {
    //        String frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
    //        Log.d(TAG, "getVideoCapturer: frontCameraDeviceName:: " + frontCameraDeviceName);
    //
    //        if (frontCameraDeviceName == null) {
    //            String[] deviceNames = VideoCapturerAndroid.getDeviceNames();
    //            if (deviceNames == null || deviceNames.length == 0) return null;
    //            for (String devName : deviceNames) {
    //                Log.i(TAG, "getVideoCapturer: devName:: " + devName);
    //            }
    //            frontCameraDeviceName = deviceNames[0];
    //        }
    //        //权限没开，则此处报错 E/VideoCapturerAndroid: InitStatics failed
    //        return VideoCapturerAndroid.create(frontCameraDeviceName);
    //    }


    public void toJoin(String name) {
        Log.i(TAG, "toJoin: ====================");
        Log.d(TAG, "toJoin: name:: " + name);
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "toJoin: Ooops...this username cannot be empty, please try again");
            return;
        }
        mSelfId = name;

        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "join");
            msg.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(msg);
        mPM.removeAllPeers();
        mPM.addPeer(name, 0,localMS,pcConstraints, mSocket);
    }

    public void sendMessage(String msg) {
        mSocket.send(msg);
    }

    public void sendMessage(JSONObject jsonObject) {
        if (jsonObject == null)
            return;
        Log.i(TAG, "sendMessage: " + jsonObject.toString());
        mSocket.send(jsonObject.toString());
    }


    public void setListener(RtcListener listener) {
        mListener = listener;
    }


    public String getSelfId() {
        return mSelfId;
    }

    public void setSelfId(String selfId) {
        mSelfId = selfId;
    }

    public Peer getSelfPeer() {
        return mPM.getPeer(mSelfId);
    }


}

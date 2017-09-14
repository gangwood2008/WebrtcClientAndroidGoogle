package com.icheyy.webrtcdemo.bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;



/**
 * Created by Dell on 2017/9/13.
 */

public class Peer implements SdpObserver, PeerConnection.Observer {
    private static final String TAG = Peer.class.getSimpleName();

    /**
     * 连接通道
     */
    private PeerConnection mConnection;
    /**
     * 用户名
     */
    private String mId;

    /**
     * 远端用户名
     */
    private String mCallerId;
    /**
     * 端口号
     */
    private int endPoint;

    private io.socket.client.Socket mSocket;
    private PeerObserver mObserver;
    private WebRTCClient.RtcListener mRtcListener;

    public interface PeerObserver {
        /**
         * 本peer需要移除
         * @param id
         */
        void onRemove(String id);
    }

    public Peer(String id, int endPoint, io.socket.client.Socket socket, PeerObserver observer) {
        Log.d(TAG, "new Peer: " + id + " " + endPoint);

        this.mId = id;
        this.endPoint = endPoint;
        mSocket = socket;
        mObserver = observer;
    }

    public void setRTCListener(WebRTCClient.RtcListener listener) {
        mRtcListener = listener;
    }

    public void setStream(MediaStream ms) {
        Log.d(TAG, "Peer: localMS:: " + ms);
        if (ms != null) {
            mConnection.addStream(ms);
        }
    }

    public void setPeerConnection(PeerConnection pc) {
        this.mConnection = pc;
    }

    public void close() {
        mConnection.dispose();
        mConnection.close();
    }


    //-------------------------------------SdpObserver interface start---------------------------------------------------------------

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    @Override
    public void onCreateSuccess(final SessionDescription sdp) {// createOffer/createAnswer成功回调此方法
        if (sdp == null)
            return;
        Log.d(TAG, "onCreateSuccess: sdp.description:: \n" + sdp.description);
        Log.i(TAG, "onCreateSuccess: sdp.type.canonicalForm():: " + sdp.type.canonicalForm());
        Log.i(TAG, "onCreateSuccess: mId " + mId);
        Log.i(TAG, "onCreateSuccess: mCallerId " + mCallerId);

        try {
            JSONObject payload = new JSONObject();
            payload.put("type", sdp.type.canonicalForm());
            payload.put("sdp", sdp.description);

            JSONObject msg = new JSONObject();
            msg.put("event", sdp.type.canonicalForm());
            msg.put("connectedUser", mCallerId);
            msg.put(sdp.type.canonicalForm(), payload);
            sendMessage(msg);
            mConnection.setLocalDescription(this, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSetSuccess() {
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "onCreateFailure: " + s);
    }

    @Override
    public void onSetFailure(String s) {
    }

    //-------------------------------------SdpObserver interface end---------------------------------------------------------------


    //-------------------------------------PeerConnection.Observer interface start---------------------------------------------------------------

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.i(TAG, "onIceConnectionChange: mId:: " + mId);
        Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);

        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED && mObserver != null) {
            mObserver.onRemove(mId);
        }
                        if (mRtcListener != null) {
                            mRtcListener.onStatusChanged(mId, iceConnectionState);
                        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "IceConnectionReceiving changed to " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        if (candidate == null)
            return;
        Log.d(TAG, "onIceCandidate: \ncandidate.sdpMLineIndex:: " + candidate.sdpMLineIndex +
                "\ncandidate.sdpMid:: " + candidate.sdpMid);
        Log.d(TAG, "onIceCandidate: candidate.sdp:: \n" + candidate.sdp);

        try {
            JSONObject payload = new JSONObject();
            payload.put("sdpMLineIndex", candidate.sdpMLineIndex);
            payload.put("sdpMid", candidate.sdpMid);
            payload.put("candidate", candidate.sdp);

            JSONObject msg = new JSONObject();
            msg.put("event", "candidate");
            msg.put("connectedUser", mCallerId);
            msg.put("candidate", payload);
            sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        //====================================
        Log.d(TAG, "onIceCandidatesRemoved: ");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "onAddStream " + mediaStream.label());

        if (mediaStream.videoTracks.size() == 1) {
            mRtcListener.onAddRemoteStream(mediaStream, endPoint + 1);
        }

        //            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
        //            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream " + mediaStream.label());
        if (mObserver != null)
            mObserver.onRemove(mId);

       close();
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
    }

    @Override
    public void onRenegotiationNeeded() {
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        //================================
        Log.d(TAG, "onAddTrack: ");
    }
    //-------------------------------------PeerConnection.Observer interface end---------------------------------------------------------------


    public void sendMessage(String msg) {
        mSocket.send(msg);
    }

    public void sendMessage(JSONObject jsonObject) {
        if (jsonObject == null)
            return;
        Log.i(TAG, "sendMessage: " + jsonObject.toString());
        mSocket.send(jsonObject.toString());
    }


    public String getId() {
        return mId;
    }

    public int getEndPoint() {
        return endPoint;
    }

    public PeerConnection getPeerConnection() {
        return mConnection;
    }

    public String getCallerId() {
        return mCallerId;
    }

    public void setCallerId(String callerId) {
        mCallerId = callerId;
    }


    @Override
    public String toString() {
        return "Peer{mConnection: " + mConnection + ", mId: " + mId + ", endPoint: " + endPoint + "}";
    }
}
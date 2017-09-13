package com.icheyy.webrtcdemo.bean;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import static android.content.ContentValues.TAG;

/**
 * Created by Dell on 2017/9/13.
 */

public class Peer implements SdpObserver, PeerConnection.Observer {
    /**
     * 连接通道
     */
    private PeerConnection pc;
    /**
     * 用户名
     */
    private String id;
    /**
     * 端口号
     */
    private int endPoint;


    public Peer(String id, int endPoint, MediaStream localMS) {
        Log.d(TAG, "new Peer: " + id + " " + endPoint);

        this.id = id;
        this.endPoint = endPoint;
        setStream(localMS);
    }

    public void setStream(MediaStream ms) {
        Log.d(TAG, "Peer: localMS:: " + ms);
        if(ms != null) {
            pc.addStream(ms);
        }
    }

    public void setPeerConnection(PeerConnection pc) {
        this.pc = pc;
    }


    @Override
    public void onCreateSuccess(final SessionDescription sdp) {// createOffer/createAnswer成功回调此方法
        if (sdp == null) return;
        Log.d(TAG, "onCreateSuccess: sdp.description:: \n" + sdp.description);
        Log.i(TAG, "onCreateSuccess: sdp.type.canonicalForm():: " + sdp.type.canonicalForm());
//TODO
//        try {
//            JSONObject payload = new JSONObject();
//            payload.put("type", sdp.type.canonicalForm());
//            payload.put("sdp", sdp.description);
//
//            JSONObject msg = new JSONObject();
//            msg.put("event", sdp.type.canonicalForm());
//            msg.put("connectedUser", mConnectedId);
//            msg.put(sdp.type.canonicalForm(), payload);
//            sendMessage(msg);
//            pc.setLocalDescription(Peer.this, sdp);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.i(TAG, "onIceConnectionChange: id:: " + id);
        Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);

        //TODO
//        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
//            if (peers != null) {
//                removePeer(id);
//            }
//        }
//        if (mListener != null) {
//            mListener.onStatusChanged(id, iceConnectionState);
//        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        //===================================
        Log.d(TAG, "IceConnectionReceiving changed to " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        if (candidate == null) return;
        Log.d(TAG, "onIceCandidate: \ncandidate.sdpMLineIndex:: " + candidate.sdpMLineIndex +
                "\ncandidate.sdpMid:: " + candidate.sdpMid);
        Log.d(TAG, "onIceCandidate: candidate.sdp:: \n" + candidate.sdp);

        //TODO

//        try {
//            JSONObject payload = new JSONObject();
//            payload.put("sdpMLineIndex", candidate.sdpMLineIndex);
//            payload.put("sdpMid", candidate.sdpMid);
//            payload.put("candidate", candidate.sdp);
//
//            JSONObject msg = new JSONObject();
//            msg.put("event", "candidate");
//            msg.put("connectedUser", mConnectedId);
//            msg.put("candidate", payload);
//            sendMessage(msg);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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
            //TODO
//            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
        }

        //            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
        //            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream " + mediaStream.label());
        //TODO
//        removePeer(id);
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


    public String getId() {
        return id;
    }

    public int getEndPoint() {
        return endPoint;
    }

    public PeerConnection getPeerConnection() {
        return pc;
    }


    @Override
    public String toString() {
        return "Peer{pc: " + pc + ", id: " + id + ", endPoint: " + endPoint + "}";
    }
}
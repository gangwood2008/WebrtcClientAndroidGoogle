package com.icheyy.webrtcdemo.helper;

import android.content.Context;
import android.util.Log;

import com.icheyy.webrtcdemo.PeerConnectionParameters;
import com.icheyy.webrtcdemo.bean.Peer;

import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Dell on 2017/9/14.
 */

public class PeerManager {


    private PeerConnectionFactory factory;
    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private HashMap<String, Peer> peers = new HashMap<>();


    public PeerManager(Context context, PeerConnectionParameters pcParams) {
        //        PeerConnectionFactory.initializeAndroidGlobals(listener, //上下文，可自定义监听
        //                true,//是否初始化音频
        //                true,//是否初始化视频
        //                params.videoCodecHwAcceleration,//是否支持硬件加速
        //                mEGLContext);//是否支持硬件渲染
        PeerConnectionFactory.initializeAndroidGlobals(
                context, pcParams.videoCodecHwAcceleration);
        //        factory = new PeerConnectionFactory();
        PeerConnectionFactory.Options opt = null;
        if (pcParams.loopback) {
            opt = new PeerConnectionFactory.Options();
            opt.networkIgnoreMask = 0;
        }
        factory = new PeerConnectionFactory(opt);


    }

    private Peer.PeerObserver mObserver = new Peer.PeerObserver() {
        @Override
        public void onRemove(String id) {
            removePeer(id);
        }
    };


    public Peer addPeer(String id, int endPoint, MediaStream localMS, MediaConstraints pcConstraints, io.socket.client.Socket mSocket) {
        Peer peer = new Peer(id, endPoint, mSocket, mObserver);
        peer.setPeerConnection(factory.createPeerConnection(getRTCConfig(), pcConstraints, peer));
        peer.setStream(localMS);
        peers.put(id, peer);
        endPoints[endPoint] = true;
        return peer;
    }

    public boolean containPeer(String id) {
        Log.d(TAG, "handleAccept: peers:: " + peers);
        return peers.containsKey(id);
    }

    public Peer getPeer(String id) {
        return peers.get(id);
    }

    public void removePeer(String id) {
        Peer peer = peers.get(id);
        if (peer == null)
            return;
        //TODO
        //        mListener.onRemoveRemoteStream(peer.getEndPoint());
        peer.close();
        peers.remove(peer.getId());
        endPoints[peer.getEndPoint()] = false;
    }

    public void removeAllPeers() {
        for (Map.Entry<String, Peer> entry : peers.entrySet()) {
            String id = entry.getKey();
            Peer peer = peers.get(id);
            //            mListener.onRemoveRemoteStream(peer.endPoint);
            peer.close();
            endPoints[peer.getEndPoint()] = false;
        }
        peers.clear();
    }

    private PeerConnection.RTCConfiguration getRTCConfig() {
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer("turn:call.icheyy.top", "cheyy", "cheyy"));
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

    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++)
            if (!endPoints[i])
                return i;
        return MAX_PEER;
    }



    public PeerConnectionFactory getFactory() {
        return factory;
    }

    public void dispose() {
        for (Peer peer : peers.values()) {
            peer.close();
        }
        factory.dispose();
    }
}

package com.icheyy.webrtcdemo.helper;

import android.util.Log;

import com.icheyy.webrtcdemo.bean.Peer;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Dell on 2017/9/14.
 */

public class PeerManager {

    private static final String TAG = PeerManager.class.getSimpleName();

    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private HashMap<String, Peer> peers = new HashMap<>();



//    private Peer.PeerObserver mObserver = new Peer.PeerObserver() {
//        @Override
//        public void onRemove(String id) {
//            removePeer(id);
//        }
//    };


    public Peer addPeer(Peer peer) {
        Log.d(TAG, "addPeer name is " + peer.getId());
//        peer.setObserver(mObserver);
        peers.put(peer.getId(), peer);
        endPoints[peer.getEndPoint()] = true;
        return peer;
    }

    public boolean containPeer(String id) {
        return peers.containsKey(id);
    }

    public Peer getPeer(String id) {
        return peers.get(id);
    }

    public void removePeer(String id) {
        if(!containPeer(id)) {
            Log.e(TAG, "removePeer: " + id + " not contain");
            return;
        }
        Log.d(TAG, "removePeer: " + id);
        Peer peer = peers.get(id);
        //TODO
        //        mListener.onRemoveRemoteStream(peer.getEndPoint());
        endPoints[peer.getEndPoint()] = false;
        peer.dispose();
        peers.remove(peer.getId());
    }

    private void removeAllPeers() {
        Log.d(TAG, "removeAllPeers");
        for (Map.Entry<String, Peer> entry : peers.entrySet()) {
            String id = entry.getKey();
            Peer peer = peers.get(id);
            //            mListener.onRemoveRemoteStream(peer.endPoint);
            Log.d(TAG, "remove Peer " + id);
            peer.dispose();
            endPoints[peer.getEndPoint()] = false;
        }
        peers.clear();
    }


    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++)
            if (!endPoints[i])
                return i;
        return MAX_PEER;
    }




    public void dispose() {
        Log.d(TAG, "dispose");
        removeAllPeers();
    }
}

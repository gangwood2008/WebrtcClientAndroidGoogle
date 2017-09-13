package com.icheyy.webrtcdemo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.bean.Caller;

import java.util.List;

public class CallersItemLvAdapter extends BaseAdapter {

    private Activity mActivity;
    private List<Caller> mContentList;

    public CallersItemLvAdapter(Activity activity, List<Caller> contentList) {
        this.mActivity = activity;
        this.mContentList = contentList;
    }

    @Override
    public int getCount() {
        return mContentList == null ? 0 : mContentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mContentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DetailsHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_callers_list, null);
            holder = new DetailsHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DetailsHolder) convertView.getTag();
        }

        Caller caller = mContentList.get(position);
        holder.tv_caller_name.setText(caller.getName());
        if(caller.getStatus())
            holder.tv_caller_status.setText("在线");
        else
            holder.tv_caller_status.setText("正在通话");

        return convertView;
    }


    class DetailsHolder {
        private TextView tv_caller_name;
        private TextView tv_caller_status;

        public DetailsHolder(View itemView) {
            tv_caller_name = (TextView) itemView.findViewById(R.id.tv_caller_name);
            tv_caller_status = (TextView) itemView.findViewById(R.id.tv_caller_status);
        }
    }
}

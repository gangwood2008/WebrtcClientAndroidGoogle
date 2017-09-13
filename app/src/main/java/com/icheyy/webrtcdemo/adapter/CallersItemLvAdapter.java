package com.icheyy.webrtcdemo.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CallersItemLvAdapter extends BaseAdapter {

    private Activity mActivity;
    private List<String> mContentList;

    public CallersItemLvAdapter(Activity activity, List<String> contentList) {
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
//            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_details_list, null);
            holder = new DetailsHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DetailsHolder) convertView.getTag();
        }
        holder.tv_item_content.setText(mContentList.get(position));

        return convertView;
    }

    public void update() {
        notifyDataSetChanged();
    }

    class DetailsHolder {
        private TextView tv_item_content;

        public DetailsHolder(View itemView) {
//            tv_item_content = (TextView) itemView.findViewById(R.id.tv_item_content);
        }
    }
}

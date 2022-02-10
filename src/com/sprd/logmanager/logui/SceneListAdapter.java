package com.sprd.logmanager.logui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sprd.logmanager.R;
import com.sprd.logmanager.database.LogSceneManager;

public class SceneListAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<SceneInfo> mSceneInfoList = new ArrayList<SceneInfo>();

    public SceneListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public SceneListAdapter(Context context, ArrayList<SceneInfo> sceneList) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mSceneInfoList = sceneList;
    }

    public void setSceneListAdapter(ArrayList<SceneInfo> sceneList) {
        mSceneInfoList = sceneList;
        notifyDataSetChanged();
    }

    public void queryDataSetChanged() {
        mSceneInfoList = (ArrayList<SceneInfo>) LogSceneManager.getInstance().getAllSceneInfoList(mContext);
        notifyDataSetChanged();
    }

    public void addItem(String name, boolean check) {
        if(TextUtils.isEmpty(name)) {
            return;
        }
        SceneInfo item = new SceneInfo();
        item.setSceneName(name);
        item.setCurrentChecked(check);
        mSceneInfoList.add(item);
        notifyDataSetChanged();
    }

    public void addItem(SceneInfo item) {
        if(item == null || TextUtils.isEmpty(item.getSceneName())) {
            return;
        }
        mSceneInfoList.add(item);
        notifyDataSetChanged();
    }

    public void clearData() {
        mSceneInfoList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSceneInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSceneInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.ylog_scene_list_item, null);
            holder.sceneInfo = (TextView)convertView.findViewById(R.id.sceneInfo);
            holder.mAddButton = (ImageButton)convertView.findViewById(R.id.btn_add);
            holder.radioButton = (RadioButton)convertView.findViewById(R.id.radioButton);
            holder.mDivider = (ImageView)convertView.findViewById(R.id.divider);
            holder.mEditDetails = (ImageView)convertView.findViewById(R.id.editDetails);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.sceneInfo.setText(mSceneInfoList.get(position).getSceneName());
        holder.radioButton.setChecked(mSceneInfoList.get(position).getCurrentChecked());
        Log.d("huasong", "position:" + position + " mSceneInfoList.get(position).getSceneName():" + mSceneInfoList.get(position).getSceneName());
        if(LogSceneManager.getInstance().getSceneString(mContext, SceneInfo.SCENE_CUSTOMER).equals(mSceneInfoList.get(position).getSceneName())) {
            holder.mAddButton.setVisibility(View.VISIBLE);
            holder.radioButton.setVisibility(View.GONE);
//            holder.mAddButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LogSceneManager.getInstance().createSceneDialog(SceneListAdapter.this);
//                }
//            });
        } else {
            holder.mAddButton.setVisibility(View.GONE);
            holder.radioButton.setVisibility(View.VISIBLE);
        }
        if(position > SceneInfo.SCENE_DEFAULT_COUNT) {
            final String name = mSceneInfoList.get(position).getSceneName();
            holder.mDivider.setVisibility(View.VISIBLE);
            holder.mEditDetails.setVisibility(View.VISIBLE);
            holder.mEditDetails.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(mContext, UserDefinedActivity.class);
                    intent.putExtra(LogSceneManager.EXTRA_CUSTOM_NAME, name);
                    ((SceneSettingActivity)mContext).startActivityForResult(intent, 0);
                }
            });
        } else {
            holder.mDivider.setVisibility(View.INVISIBLE);
            holder.mEditDetails.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView sceneInfo;
        public ImageButton mAddButton;
        public RadioButton radioButton;
        public ImageView mDivider;
        public ImageView mEditDetails;
    }
}

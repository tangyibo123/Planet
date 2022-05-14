package com.tangyibo.planet.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tangyibo.framework.R;

import com.moxun.tagcloudlib.view.TagsAdapter;
import com.tangyibo.framework.helper.GlideHelper;
import com.tangyibo.planet.model.PlanetModel;

import java.util.List;

public class CloudTagAdapter extends TagsAdapter {

    private Context mContext;
    private List<PlanetModel> mList;
    private LayoutInflater inflater;

    public CloudTagAdapter(Context mContext, List<PlanetModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        PlanetModel model = mList.get(position);
        View mView = null;
        ViewHolder viewHolder = null;
        if (mView == null) {
            viewHolder = new ViewHolder();
            //初始化View
            mView = inflater.inflate(R.layout.layout_star_view_item, null);
            //初始化控件
            viewHolder.iv_star_icon = mView.findViewById(R.id.iv_star_icon);
            viewHolder.tv_star_name = mView.findViewById(R.id.tv_star_name);
            mView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) mView.getTag();
        }
        if (!TextUtils.isEmpty(model.getPhotoUrl())) {
            GlideHelper.loadSmollUrl(mContext, model.getPhotoUrl(), 30, 30, viewHolder.iv_star_icon);
        } else {
            viewHolder.iv_star_icon.setImageResource(R.drawable.img_star_icon);
        }
        viewHolder.tv_star_name.setText(model.getNickName());
        return mView;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }

    class ViewHolder {
        private ImageView iv_star_icon;
        private TextView tv_star_name;
    }
}

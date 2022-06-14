package com.tangyibo.framework.tag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class NOPTagsAdapter extends TagAdapter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        return null;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getPopularity(int position) {
        return 0;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }
}

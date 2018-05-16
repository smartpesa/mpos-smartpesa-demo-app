package com.smartpesa.smartpesa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.helpers.MenuItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MenuAdapter extends BaseAdapter {
    private Context mContext;
    List<MenuItem> menuList;

    public MenuAdapter(Context c, List<MenuItem> list) {
        mContext = c;
        menuList = list;
    }

    @Override
    public int getCount() {
        return menuList.size();
    }

    @Override
    public Object getItem(int position) {
        return menuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.row_menu, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MenuItem menuItem = menuList.get(position);

        holder.textView.setText(menuItem.textResId);
        holder.imageView.setImageResource(menuItem.imgResId);

        return convertView;
    }

    public static class ViewHolder {
        @Bind(R.id.grid_text) public TextView textView;
        @Bind(R.id.grid_image) public ImageView imageView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

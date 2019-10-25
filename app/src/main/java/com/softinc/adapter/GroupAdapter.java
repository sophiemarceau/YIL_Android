package com.softinc.adapter;

/**
 * Created by sophiemarceau_qu on 15/5/22.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMGroup;
import com.softinc.youelink.R;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<EMGroup> {

    private LayoutInflater inflater;
    private String str;

    public GroupAdapter(Context context, int res, List<EMGroup> groups) {
        super(context, res, groups);
        this.inflater = LayoutInflater.from(context);
        str = context.getResources().getString(R.string.The_new_group_chat);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 0;
        }else if(position == getCount() - 1){
            return 1;
        }else{
            return 2;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(getItemViewType(position) == 0){
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.search_bar_with_padding, null);
            }
            final EditText query = (EditText) convertView.findViewById(R.id.query);
            final ImageButton clearSearch = (ImageButton) convertView.findViewById(R.id.search_clear);
            query.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    getFilter().filter(s);
                    if (s.length() > 0) {
                        clearSearch.setVisibility(View.VISIBLE);
                    } else {
                        clearSearch.setVisibility(View.INVISIBLE);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                }
            });
            clearSearch.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    query.getText().clear();
                }
            });
        }
        else if(getItemViewType(position) == 1){
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.row_add_group, null);
//            }
//            ((ImageView)convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.roominfo_add_btn);
//            ((TextView)convertView.findViewById(R.id.name)).setText(str);
//            convertView.setVisibility(View.GONE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_group, null);
            }

            ((TextView)convertView.findViewById(R.id.name)).setText(getItem(position-1).getGroupName());
        } else {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_group, null);
            }

            ((TextView)convertView.findViewById(R.id.name)).setText(getItem(position-1).getGroupName());

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

}

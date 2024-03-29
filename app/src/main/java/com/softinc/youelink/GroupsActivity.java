package com.softinc.youelink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.softinc.adapter.GroupAdapter;

import java.util.List;

/**
 * Created by sophiemarceau_qu on 15/5/22.
 */
public class GroupsActivity extends BaseActivity {
    private  static  final String TAG ="GroupActivity";
    private ListView groupListView;
    protected List<EMGroup> grouplist;
    private GroupAdapter groupAdapter;
    private InputMethodManager inputMethodManager;
    public static GroupsActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_groups);

        instance = this;
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        grouplist =	EMGroupManager.getInstance().getAllGroups();


//        Log.e(TAG,"grouplist------->"+grouplist);

        groupListView = (ListView)findViewById(R.id.list);
        groupAdapter = new GroupAdapter(this, 1, grouplist);
        groupListView.setAdapter(groupAdapter);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position == groupAdapter.getCount() - 1) {
//                    //新建群聊
//                    startActivityForResult(new Intent(GroupsActivity.this, NewGroupActivity.class), 0);
//                } else {

                    //进入群聊
                    Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
                    // it is group chat
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                    intent.putExtra("groupId", groupAdapter.getItem(position - 1).getGroupId());
                    startActivityForResult(intent, 0);
                }
//            }

        });
        groupListView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
    }

    /**
     * 进入公开群聊列表
     */
    public void onPublicGroups(View view){
        startActivity(new Intent(this, PublicGroupsActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        grouplist = EMGroupManager.getInstance().getAllGroups();
        groupAdapter = new GroupAdapter(this, 1, grouplist);
        groupListView.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    /**
     * 返回
     * @param view
     */
    public void back(View view){
        finish();
    }
}


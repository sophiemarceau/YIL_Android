package com.softinc.youelink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMGroupManager;

/**
 * Created by sophiemarceau_qu on 15/6/9.
 */
public class SetGroupMessageActivity  extends BaseActivity implements View.OnClickListener  {

    private RelativeLayout rl_switch_block_groupmsg;
//    private RelativeLayout receivemessage_switch_block_groupmsg;
    /**
     * 屏蔽群消息imageView
     */
    private ImageView on_switch_block_groupmsg;
    private ImageView off_switch_block_groupmsg;
    private  TextView lineView;
    /**
     * 接受群消息imageView
     */
//    private ImageView  on_re_switch_block_groupmsg;
//    private ImageView  off_re_switch_block_groupmsg;
    private ProgressDialog progressDialog;
    private Button saveBtn;
    private String groupId;




    private  Boolean pushFlag;
    private  Boolean switch_block_Flag;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_groupmessage_set);
        rl_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.rl_switch_block_groupmsg);
        on_switch_block_groupmsg = (ImageView) findViewById(R.id.iv_switch_block_groupmsg);
        off_switch_block_groupmsg = (ImageView) findViewById(R.id.iv_switch_unblock_groupmsg);
        rl_switch_block_groupmsg.setOnClickListener(this);

//        receivemessage_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.receivemessage_switch_block_groupmsg);
//        on_re_switch_block_groupmsg = (ImageView) findViewById(R.id.re_switch_block_groupmsg);
//        off_re_switch_block_groupmsg = (ImageView) findViewById(R.id.re_switch_unblock_groupmsg);
//        receivemessage_switch_block_groupmsg.setOnClickListener(this);
//        lineView =(TextView)findViewById(R.id.group_line);
        groupId = getIntent().getStringExtra("groupId");
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.rl_switch_block_groupmsg: // 屏蔽群组

                if (on_switch_block_groupmsg.getVisibility() == View.INVISIBLE) {

                    on_switch_block_groupmsg.setVisibility(View.VISIBLE);
                    off_switch_block_groupmsg.setVisibility(View.INVISIBLE);
//                    receivemessage_switch_block_groupmsg.setVisibility(View.GONE);
//                    lineView.setVisibility(View.GONE);

                    switch_block_Flag =Boolean.TRUE;
                    pushFlag =Boolean.FALSE;

                } else {
                    on_switch_block_groupmsg.setVisibility(View.INVISIBLE);
                    off_switch_block_groupmsg.setVisibility(View.VISIBLE);
//                    receivemessage_switch_block_groupmsg.setVisibility(View.VISIBLE);
//                    lineView.setVisibility(View.VISIBLE);

                    switch_block_Flag =Boolean.FALSE;
                }
                break;

//            case R.id.receivemessage_switch_block_groupmsg: // 清空聊天记录
//                if (on_re_switch_block_groupmsg.getVisibility() == View.VISIBLE) {
//
//                    on_re_switch_block_groupmsg.setVisibility(View.INVISIBLE);
//                    off_re_switch_block_groupmsg.setVisibility(View.VISIBLE);
//                    pushFlag =Boolean.TRUE;
//
//                }else {
//
//                    on_re_switch_block_groupmsg.setVisibility(View.VISIBLE);
//                    off_re_switch_block_groupmsg.setVisibility(View.INVISIBLE);
//                    pushFlag =Boolean.FALSE;
//                }
//
//                break;

//            case R.id.rl_blacklist: // 黑名单列表
//                startActivity(new Intent(GroupDetailsActivity.this, SetGroupMessageActivity.class).putExtra("groupId", groupId));
//                break;
            default:
                break;
        }
    }







    public void save(View view){
        String st6 = getResources().getString(R.string.Is_unblock);
        final String st7 = getResources().getString(R.string.remove_group_of);
//        setResult(RESULT_OK,new Intent().putExtra("data", editText.getText().toString()));
        if (switch_block_Flag){
            if (progressDialog == null) {
                        progressDialog = new ProgressDialog(SetGroupMessageActivity.this);
                        progressDialog.setCanceledOnTouchOutside(false);
                    }
                    progressDialog.setMessage(st6);
                    progressDialog.show();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                EMGroupManager.getInstance().blockGroupMessage(groupId);
                                runOnUiThread(new Runnable() {
                                    public void run() {

                                        progressDialog.dismiss();
                                        finish();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), st7, 1).show();
                                    }
                                });

                            }
                        }
                    }).start();
        }else{
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(SetGroupMessageActivity.this);
                progressDialog.setCanceledOnTouchOutside(false);
            }
            progressDialog.setMessage(st6);
            progressDialog.show();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        EMGroupManager.getInstance().unblockGroupMessage(groupId);
                        runOnUiThread(new Runnable() {
                            public void run() {

                                progressDialog.dismiss();
                                finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), st7, 1).show();
                                progressDialog.dismiss();


                            }
                        });

                    }
                }
            }).start();

        }
//        if (pushFlag){
//
//            if (progressDialog == null) {
//                progressDialog = new ProgressDialog(SetGroupMessageActivity.this);
//                progressDialog.setCanceledOnTouchOutside(false);
//            }
//            progressDialog.setMessage(st6);
//            progressDialog.show();
//            new Thread(new Runnable() {
//                public void run() {
//                    try {
//                        EMGroupManager.getInstance().as;
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//
//                                progressDialog.dismiss();
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                progressDialog.dismiss();
//                                Toast.makeText(getApplicationContext(), st7, 1).show();
//                            }
//                        });
//
//                    }
//                }
//            }).start();
//        }else {
//
//        }

    }
}

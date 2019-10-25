package com.softinc.youelink;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.common.ECI;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.config.GlobalData;
import com.softinc.engine.UserEngine;
import com.softinc.utils.FileUploadHelper;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.utils.StoreHelper;
import com.softinc.utils.StringHelper;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.jivesoftware.smack.util.collections.KeyValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MineDetailActivity extends Activity {

    private Button btnEditBase;
    private Button btnEditDetail;

    private EditText txtNickName;
    private TextView txtSex;
    private TextView txtBirth;
    private TextView tv_phone;
    private EditText tv_mail;
    private EditText tv_interest;

    private Spinner spArea;
    private Spinner spJob;

    private List<Item> lst_area = new ArrayList<>();
    private List<Item> lst_job = new ArrayList<>();

    private Integer sel_area_id = 0;
    private Integer sel_job_id = 0;

    private ImageView iv_def;

    private Title tv_name;

    private String[] items = new String[]{"本地上传", "我要拍照"};

    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;

    private static final String IMAGE_FILE_NAME = "faceImage.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_detail);

        initView();
        initViewData();
        initSpinnerData();
        initViewEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //结果码不等于取消时候
        if (resultCode != RESULT_CANCELED) {

            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (StoreHelper.hasSdcard()) {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File tempFile = new File(path, IMAGE_FILE_NAME);
                        startPhotoZoom(Uri.fromFile(tempFile));
                    } else {
                        Toast.makeText(MineDetailActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case RESULT_REQUEST_CODE: //图片缩放完成后
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        btnEditBase = (Button) findViewById(R.id.btnEditBase);
        //btnEditDetail = (Button) findViewById(R.id.btnEditDetail);

        txtNickName = (EditText) findViewById(R.id.txtNickName);
        txtSex = (TextView) findViewById(R.id.txtSex);
        txtBirth = (TextView) findViewById(R.id.txtBirth);
        //txtArea = (EditText) findViewById(R.id.txtArea);
        //txtCa = (EditText) findViewById(R.id.txtCa);

        iv_def = (ImageView) findViewById(R.id.iv_def);
        iv_def.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showEditPortrait();
                return false;
            }
        });

        tv_name = (Title) findViewById(R.id.tv_name);

        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_mail = (EditText) findViewById(R.id.tv_mail);

        tv_interest = (EditText) findViewById(R.id.tv_interest);

        spArea = (Spinner) findViewById(R.id.spArea);
        spJob = (Spinner) findViewById(R.id.spCa);
    }

    private void initViewData() {
        txtNickName.setText(MyApplication.user.getNick());
        if (MyApplication.user.getGender().equals("1")) {
            txtSex.setText("男");
        } else {
            txtSex.setText("女");
        }

        txtBirth.setText(MyApplication.user.getBrithday());
        txtBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(MineDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                        String date = format.format(calendar.getTime());

                        txtBirth.setText(date);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        if (!TextUtils.isEmpty(MyApplication.user.iconPath) && !"null".equals(MyApplication.user.iconPath)) {
            ImageLoader.getInstance().displayImage(MyApplication.user.iconPath, iv_def);
        }

        tv_name = (Title) findViewById(R.id.tv_name);
        tv_name.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                MineDetailActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });

        tv_phone.setText(MyApplication.user.getPhoneNumber());

        if (!StringHelper.isEmptyOrNull(MyApplication.user.getMail())) {
            tv_mail.setText(MyApplication.user.getMail());
        }

        if (!StringHelper.isEmptyOrNull(MyApplication.user.getHobbies())) {
            tv_interest.setText(MyApplication.user.getHobbies());
        }
    }

    private void initSpinnerData() {
        MyApplication.client.get(Constant.URL_AREA_LIST, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray items = response.getJSONArray("Data");

                        Integer sel_index = 0;
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject data = (JSONObject) items.get(i);
                            Item item = new Item(data.getInt("ID"), data.getString("Name"));
                            if (item.getId().toString().equals(MyApplication.user.getAreaId())) {
                                sel_index = i;
                            }
                            lst_area.add(item);
                        }

                        spArea.setAdapter(new ItemAdapter(getApplicationContext(), lst_area));
                        spArea.setSelection(sel_index);
                    } else {
                        Toast.makeText(MineDetailActivity.this, response.getString("Message"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });

        MyApplication.client.get(Constant.URL_JOB_LIST, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray items = response.getJSONArray("Data");

                        Integer sel_index = 0;
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject data = (JSONObject) items.get(i);
                            Item item = new Item(data.getInt("ID"), data.getString("Name"));
                            if (item.getId().toString().equals(MyApplication.user.getAreaId())) {
                                sel_index = i;
                            }
                            lst_job.add(item);
                        }

                        spJob.setAdapter(new ItemAdapter(getApplicationContext(), lst_job));
                        spJob.setSelection(sel_index);
                    } else {
                        Toast.makeText(MineDetailActivity.this, response.getString("Message"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViewEvents() {
        btnEditBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.put("nickName", txtNickName.getText());
                params.put("brithday", txtBirth.getText());
                params.put("areaId", sel_area_id);
                params.put("jobId", sel_job_id);
                params.put("email", tv_mail.getText());
                params.put("hobbies", tv_interest.getText());

                MyApplication.client.post(Constant.URL_SET_USER_INFO, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            Boolean success = response.getBoolean("Result");
                            if (!success) {
                                String msg = response.getString("Infomation");
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            } else {
                                MyApplication.user.setMail(tv_mail.getText().toString());
                                MyApplication.user.setHobbies(tv_interest.getText().toString());
                                Toast.makeText(getApplicationContext(), "更新成功！", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.console().printf(responseString);
                        Toast.makeText(getApplicationContext(), "访问异常！", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        spArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sel_area_id = lst_area.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sel_job_id = lst_job.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showEditPortrait() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("设置头像")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*"); // 设置文件类型
                                intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (StoreHelper.hasSdcard()) {
                                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                                    File file = new File(path, IMAGE_FILE_NAME);
                                    intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                                }

                                startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 340);
        intent.putExtra("outputY", 340);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(this.getResources(), photo);
            iv_def.setImageDrawable(drawable);

            RequestParams params = new RequestParams();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
            params.put("file", inputStream, "icon.png", "image/png");
            params.put("uid", MyApplication.uid);

            MyApplication.client.post(Constant.URL_UPLOAD_ICON, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (ResponseUtils.isResultOK(response)) {
                        Toast.makeText(MineDetailActivity.this, "头像更新成功！", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(MineDetailActivity.this, responseString, Toast.LENGTH_SHORT).show();
                    ////PromptUtils.showNoNetWork(CompleteUserInfo2Activity.this);
                }
            });

//            File file = new File(data.getData().getPath());
//            String result = FileUploadHelper.uploadFile(file, Constant.URL_UPLOAD_ICON + "?uid=" + MyApplication.uid);
//            if (result.equals("1")) {
//                Toast.makeText(MineDetailActivity.this, "更新头像成功！", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void editMineInfo(String nickName, Integer gender, String birth, Integer areaId, Integer jobId) {
        RequestParams params = new RequestParams();
        params.put("nickName", nickName);
        params.put("gender", gender);
        params.put("brithday", birth);
        params.put("areaId", areaId);
        params.put("jobId", jobId);
        MyApplication.client.get(Constant.URL_SET_USER_INFO, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        Toast.makeText(MineDetailActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class ItemAdapter extends BaseAdapter {
        private List<Item> mList;
        private Context mContext;

        public ItemAdapter(Context context, List<Item> list) {
            this.mContext = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return this.mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
            convertView = _LayoutInflater.inflate(R.layout.activity_mine_detail_spinner_item, null);
            if (convertView != null) {
                TextView tv_item_value = (TextView) convertView.findViewById(R.id.tv_item_value);
                tv_item_value.setText(mList.get(position).getName());
            }
            return convertView;
        }
    }

    class Item {
        private Integer id;
        private String name;

        public Item(Integer id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

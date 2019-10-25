package com.softinc.youelink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.softinc.application.MyApplication;
import com.softinc.config.GlobalData;
import com.softinc.utils.ResponseUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.softinc.config.Constant;
import com.softinc.utils.PromptUtils;

public class CompleteUserInfo2Activity extends Activity implements View.OnClickListener {

    private static final String TAG = "CompleteInfo2";

    private static final int TAKE_PHOTO = 1;
    private static final int CUT_PHOTO = 2;
    private static final int PICK_PHOTO = 3;

    private Bitmap userIcon = null;
    // 创建一个以当前时间为名称的文件
    File tempFile = new File(Environment.getExternalStorageDirectory(), getPhotoFileName());

    private ImageView iv_title_left;
    private ImageView iv_takephoto;
    private Button bt_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_user_info_2);

        iv_title_left = (ImageView) this.findViewById(R.id.iv_title_left);
        iv_takephoto = (ImageView) this.findViewById(R.id.iv_takephoto);
        bt_next = (Button) this.findViewById(R.id.bt_next);

        iv_takephoto.setOnClickListener(this);
        bt_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_takephoto://选取图片
                showDialogForPhoto();
                break;

            case R.id.bt_next:
                nextClick();
                break;
        }
    }

    /**
     * 点击下一步,上传头像
     */
    private void nextClick() {
        if (userIcon == null) {
            PromptUtils.showToast(this, "请先上传头像");
            return;
        }

        RequestParams params = new RequestParams();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        userIcon.compress(Bitmap.CompressFormat.PNG, 100, baos);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        params.put("file", inputStream, "icon.png", "image/png");
        params.put("uid", GlobalData.currentUser.id);
        Log.d(TAG, "uid 是" + GlobalData.currentUser.id);

        MyApplication.client.post(Constant.URL_UPLOAD_ICON, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "上传返回:" + response);
                if (ResponseUtils.isResultOK(response)) {
                    Log.d(TAG, "上传成功");

                    getCurrentUser(GlobalData.currentUser.id);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d(TAG, "上传错误:" + responseString);
//                Log.e(TAG, "返回错误", throwable);
                PromptUtils.showNoNetWork(CompleteUserInfo2Activity.this);
            }
        });
    }

    /**
     * 弹出对话框选择获取图片的方式
     */
    private void showDialogForPhoto() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("请选择上传方式")
                .setItems(new String[]{"相册", "拍照"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {//相册
                            Intent intent = new Intent(Intent.ACTION_PICK, null);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent, PICK_PHOTO);
                        } else if (which == 1) {//拍照
                            // 调用系统的拍照功能
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // 指定调用相机拍照后照片的储存路径
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                            startActivityForResult(intent, TAKE_PHOTO);
                        }
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "拍照返回码" + resultCode);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case TAKE_PHOTO:
                cutImage(Uri.fromFile(tempFile), 150);
                break;

            case PICK_PHOTO:
                if (data != null)
                    cutImage(data.getData(), 150);
                break;

            case CUT_PHOTO:
                if (data != null)
                    setImageToView(data);
                break;
        }
    }

    /**
     * 剪切好的图片显示出来
     *
     * @param picdata
     */
    private void setImageToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            userIcon = bundle.getParcelable("data");
            iv_takephoto.setImageBitmap(userIcon);
        }
    }

    private void cutImage(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CUT_PHOTO);
    }

    /**
     * 使用系统当前日期加以调整作为照片的名称
     *
     * @return
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    private void getCurrentUser(String uid) {
        RequestParams params = new RequestParams();
        params.put("uid", uid);

        MyApplication.client.get(Constant.URL_GET_USER_BY_ID, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject userObj = response.getJSONObject("Data");

                    MyApplication.user.iconPath = "http://123.57.217.223/youelink/upload/userpic/" + userObj.getString("UserPic");
                    MyApplication.user.setNick(userObj.getString("NickName"));
                    MyApplication.user.id = userObj.getString("UID");
                    MyApplication.user.setUserLevel(userObj.getString("UserLevel"));
                    MyApplication.user.CreditPoint = userObj.getString("CreditPoint");
                    MyApplication.user.setBrithday(userObj.getString("Brithday"));
                    MyApplication.user.setJobId(userObj.getString("JobID"));
                    MyApplication.user.setJobName(userObj.getString("JobName"));
                    MyApplication.user.setAreaId(userObj.getString("AreaID"));
                    MyApplication.user.setAreaName(userObj.getString("AreaName"));
                    MyApplication.user.setGender(userObj.getString("Gender"));
                    MyApplication.user.setBuddyRequest(userObj.getString("BuddyRequest"));

                    startActivity(new Intent(CompleteUserInfo2Activity.this, HomeActivity.class));
                    CompleteUserInfo2Activity.this.finish();

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
}

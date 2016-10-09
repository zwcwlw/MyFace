package com.zwcwlw.myface;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements View.OnClickListener {
    private Paint mPaint;
    private static final int PICK_CODE = 0x110;
    private ImageView mPhoto;
    private Button mGetImg;
    private Button mDetect;
    private TextView mTip;
    private String mCurrentPhotoStr;
    private View mWaitting;
    private Bitmap mPhotoImg;
    private static final int MSG_SUCESS = 0x111;
    private static final int MSG_ERROR = 0x112;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SUCESS:
                    mWaitting.setVisibility(View.GONE);
                    JSONObject jsonObject = (JSONObject) msg.obj;
                    prepareREBitmap(jsonObject);
                    mPhoto.setImageBitmap(mPhotoImg);
                    break;
                case MSG_ERROR:
                    mWaitting.setVisibility(View.GONE);
                    String text = (String) msg.obj;
                    if (TextUtils.isEmpty(text)) {
                        mTip.setText("ERROR");
                    } else {
                        mTip.setText(text);
                    }
                    break;
            }

        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();//加载控件
        initEvents();//加载数据
        //初始化
        mPaint = new Paint();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * 从json解析获取图片
     *
     * @param js
     */
    private void prepareREBitmap(JSONObject js) {
        Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(), mPhotoImg.getHeight(), mPhotoImg.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mPhotoImg, 0, 0, null);
        try {
            JSONArray faces = js.getJSONArray("face");
            int faceCount = faces.length();
            mTip.setText("数量" + faceCount);
            for (int i = 0; i < faceCount; i++) {
                //拿到face对象
                JSONObject face = faces.getJSONObject(i);
                JSONObject posjs = face.getJSONObject("position");
                float x = (float) posjs.getJSONObject("center").getDouble("x");
                float y = (float) posjs.getJSONObject("center").getDouble("y");
                float w = (float) posjs.getDouble("width");
                float h = (float) posjs.getDouble("height");
                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();
                w = w / 100 * bitmap.getWidth();
                h = h / 100 * bitmap.getHeight();
                //画人脸框box四根线
                //设置框颜色
                mPaint.setColor(Color.RED);
                //设置宽度
                mPaint.setStrokeWidth(3);
                //画四根线
                canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2, mPaint);
                canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y + h / 2, x + w / 2, y + h / 2, mPaint);
                int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = face.getJSONObject("attrbute").getJSONObject("gender").getString("value");
                Bitmap ageBitmap = buildAgeBitmap(age, "Male".equals(gender));
                int ageHeight = ageBitmap.getHeight();
                int ageWidth = ageBitmap.getWidth();
                if (bitmap.getWidth() < mPhoto.getWidth() && bitmap.getHeight() < mPhoto.getHeight()) {
                    float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhoto.getWidth(), bitmap.getHeight() * 1.0f / mPhoto.getHeight());
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int) (ageWidth * ratio), (int) (ageHeight * ratio), false);
                }
                canvas.drawBitmap(ageBitmap, x - ageBitmap.getWidth() / 2, y - h / 2 - ageBitmap.getHeight(), null);

                mPhotoImg = bitmap;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap buildAgeBitmap(int age, boolean isMale) {
        TextView tv = (TextView) mWaitting.findViewById(R.id.tv_age_gender);
        tv.setText(age + "");
        if (isMale) {
            tv.setText("男");
           // tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.mipmap.man));
        } else {
            tv.setText("女");
           // tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.mipmap.woman));
        }
        tv.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
        tv.destroyDrawingCache();
        return bitmap;
    }


    private void initEvents() {
        mGetImg.setOnClickListener(this);
        mDetect.setOnClickListener(this);
    }


    private void initViews() {
        mPhoto = (ImageView) findViewById(R.id.iv_photo);
        mDetect = (Button) findViewById(R.id.bt_detect);
        mGetImg = (Button) findViewById(R.id.bt_getimg);
        mWaitting = findViewById(R.id.fl_waitting);
        mTip = (TextView) findViewById(R.id.tv_text);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_detect:
                mWaitting.setVisibility(View.VISIBLE);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_CODE);
                break;
            case R.id.bt_getimg:
                mWaitting.setVisibility(View.VISIBLE);
                if (mCurrentPhotoStr!=null&&mCurrentPhotoStr.equals("")){
                    reSizePhoto();
                }else {
                    mPhotoImg=BitmapFactory.decodeResource(getResources(),R.drawable.test1);
                }
                FaceDetect.detect(mPhotoImg, new FaceDetect.Callback() {
                    @Override
                    public void success(JSONObject result) {
                        Message msg = Message.obtain();
                        msg.what = MSG_SUCESS;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg = Message.obtain();
                        msg.what = MSG_ERROR;
                        msg.obj = exception;
                        mHandler.sendMessage(msg);
                    }
                });
                break;
        }
    }

    /**
     * 获取图片路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurrentPhotoStr = cursor.getString(idx);
                cursor.close();
                reSizePhoto();
                mPhoto.setImageBitmap(mPhotoImg);
                mTip.setText("人脸识别——>");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 压缩图片不能超过3M
     */
    private void reSizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoStr, options);
        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);
        options.inSampleSize = (int) Math.ceil(ratio);
        options.inJustDecodeBounds = false;
        mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
    }


}

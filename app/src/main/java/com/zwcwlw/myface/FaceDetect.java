package com.zwcwlw.myface;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2016/10/8.
 */
public class FaceDetect {
    public interface Callback{
        void success(JSONObject result);
        void error(FaceppParseException exception);
    }
    public static void detect(final Bitmap bm,final Callback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //request创建请求
                try {
                    HttpRequests httpRequests=new HttpRequests(Constant.KEY,Constant.SECRET,true,true);
                    Bitmap bmSmall=Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight());
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] arrays = baos.toByteArray();
                    PostParameters postParameters = new PostParameters();
                    postParameters.setImg(arrays);
                    JSONObject jsonObject = httpRequests.detectionDetect(postParameters);
                    Log.e("TAG",jsonObject.toString());
                    if (callback!=null){
                        callback.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callback!=null){
                        callback.error(e);
                    }
                }

            }
        }).start();

    }
}

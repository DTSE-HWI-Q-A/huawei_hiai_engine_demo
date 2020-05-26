package com.huawei.hiaicodedemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.sr.ImageSuperResolution;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.ImageResult;
import com.huawei.hiai.vision.visionkit.image.sr.SuperResolutionConfiguration;
import com.huawei.hiaicodedemo.BaseActivity;
import com.huawei.hiaicodedemo.R;
import com.huawei.hiaicodedemo.utils.AssetsFileUtil;
import com.huawei.hiaicodedemo.widget.TitleBar;

public class ImageSuperActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = ImageSuperActivity.class.getSimpleName();
    private final static int SUPERRESOLUTION_RESULT = 110;
    private android.widget.ImageView superOrigin;
    private android.widget.ImageView superImage;
    private Bitmap bitmap;
    private TitleBar titleBar;
    private Bitmap bmp;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUPERRESOLUTION_RESULT:
                    superImage.setImageBitmap(bmp);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initHiAI();
    }

    @Override
    protected void init() {


    }

    /**
     * init HiAI interface
     */
    private void initHiAI() {
        /** Initialize with the VisionBase static class and asynchronously get the connection of the service */
        VisionBase.init(this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                /** This callback method is invoked when the service connection is successful; you can do the initialization of the detector class, mark the service connection status, and so on */
            }

            @Override
            public void onServiceDisconnect() {
                /** When the service is disconnected, this callback method is called; you can choose to reconnect the service here, or to handle the exception*/
            }
        });


    }

    /**
     * Capability Interfaces
     *
     * @return
     */
    private void setHiAi() {
        /** Define class detector, the context of this project is the input parameter */
        ImageSuperResolution superResolution = new ImageSuperResolution(this);
        /** Define the frame, put the bitmap that needs to detect the image into the frame*/
        Frame frame = new Frame();
        /** BitmapFactory.decodeFile input resource file path*/
        //Bitmap bitmap = BitmapFactory.decodeFile(null);
        frame.setBitmap(bitmap);
        /** Define and set super-resolution parameters*/
        SuperResolutionConfiguration paras = new SuperResolutionConfiguration(
                SuperResolutionConfiguration.SISR_SCALE_3X,
                SuperResolutionConfiguration.SISR_QUALITY_HIGH);
        superResolution.setSuperResolutionConfiguration(paras);
        /** Run super-resolution and get result of processing */
        ImageResult result = superResolution.doSuperResolution(frame, null);
        /** After the results are processed to get bitmap*/
        Bitmap bmp = result.getBitmap();
        /** Note: The result and the Bitmap in the result must be NULL, but also to determine whether the returned error code is 0 (0 means no error)*/


        this.bmp = bmp;
        handler.sendEmptyMessage(SUPERRESOLUTION_RESULT);
    }

    /**
     * Release
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    protected int layout() {
        return R.layout.activity_image_super;
    }

    private void initView() {
        superOrigin = findViewById(R.id.super_origin);
        superImage = findViewById(R.id.super_image);
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        findViewById(R.id.btn_album).setOnClickListener(this);
        findViewById(R.id.btn_material).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_material:
                selectMaterial("material/image_super_resolution");
                break;
            case R.id.btn_album:
                selectImage();
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == REQUEST_PHOTO) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                } catch (Exception e) {
                    e.printStackTrace();
                    toast("Exception:" + e.getMessage());

                }
            } else if (requestCode == REQUEST_SELECT_MATERIAL_CODE) {
                bitmap = AssetsFileUtil.getBitmapByFilePath(this, data.getStringExtra(KEY_FILE_PATH));
            }
            if (bitmap != null) {
                setBitmap();
            } else {
                toast(getString(R.string.isr_toast_1));
            }
        }

    }

    private void setBitmap() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        Log.e(TAG, "width:" + width + ";height:" + height);
        if (width <= 800 && height <= 600) {
            superOrigin.setImageBitmap(bitmap);
            new Thread() {
                @Override
                public void run() {
                    setHiAi();
                }
            }.start();
        } else {
            toast(getString(R.string.isr_toast_2));
        }
    }

}

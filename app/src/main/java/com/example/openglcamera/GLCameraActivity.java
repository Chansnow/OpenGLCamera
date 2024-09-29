package com.example.openglcamera;

import android.Manifest;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.example.openglcamera.camera.CameraHelper;
import com.example.openglcamera.camera.CameraListener;
import com.example.openglcamera.widget.glsurface.GLUtil;
import com.example.openglcamera.widget.glsurface.RoundCameraGLSurfaceView;
import com.example.openglcamera.util.ImageUtil;
import com.example.openglcamera.widget.RoundTextureView;

public class GLCameraActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, CameraListener{

    private static final String TAG = "CameraActivity";
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    private CameraHelper cameraHelper;
    //原始数据显示的控件
    private RoundTextureView textureView;

    //默认打开的CAMERA
    private static final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //需要的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    //原始预览尺寸
    private Camera.Size previewSize;
    //正方形预览数据的宽度/高度
    private int squarePreviewSize;
    //被裁剪的区域，默认实现取最中心部分
    private Rect cropRect = null;
    //正方形预览数据
    private byte[] squareNV21;
    //正方形预览的控件
    private RoundCameraGLSurfaceView roundCameraGLSurfaceView;
    //美颜参数
    private int skinSoftenLevel = 0; // 默认值
    private int skinBrightenLevel = 0; // 默认值
    private int eyeEnlargmentLevel = 0; // 默认值
    private int noseHighlightLevel = 0; // 默认值
    private int faceSlenderLevel = 0; // 默认值
    private int skinSoftenType = 0; // 默认值

    private FrameLayout overlayLayout;
    private ImageView beautyButton;
    private ImageView cameraButton;
    private SeekBar skinBrightenSeekBar;
    private SeekBar skinSoftenSeekBar;
    private SeekBar eyeEnlargmentSeekBar;
    private SeekBar noseHighlightSeekBar;
    private SeekBar faceSlenderSeekBar;
    private SeekBar skinSoftenTypeSeekBar;

    static {
        System.loadLibrary("native-lib");
    }
    private native byte[] processPreviewData(byte[] data, int width, int height, int pitch);
    private native void setBeautyParameters(int skinSoften, int skinBrighten, int eyeEnlargment, int noseHighlight, int faceSlender, int skinSoftenType);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
    }

    private void initView() {
        textureView = findViewById(R.id.texture_preview);
        roundCameraGLSurfaceView = findViewById(R.id.camera_gl_surface_view);
        overlayLayout = findViewById(R.id.overlayLayout);
        beautyButton = findViewById(R.id.btn_camera_beauty);
        cameraButton = findViewById(R.id.btn_camera_switch);
        /**
         * {@link GLUtil#FRAG_SHADER_NORMAL} 正常效果
         * {@link GLUtil#FRAG_SHADER_GRAY} 灰度效果
         * {@link GLUtil#FRAG_SHADER_SCULPTURE} 浮雕效果
         */
//        roundCameraGLSurfaceView.setFragmentShaderCode(GLUtil.FRAG_SHADER_SCULPTURE);
        roundCameraGLSurfaceView.setFragmentShaderCode(GLUtil.FRAG_SHADER_NORMAL);
        roundCameraGLSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        beautyButton.setOnClickListener(v -> {
            // Toggle the visibility of the overlay layout
            if (overlayLayout.getVisibility() == View.VISIBLE) {
                overlayLayout.setVisibility(View.GONE);
            } else {
                overlayLayout.setVisibility(View.VISIBLE);
            }
        });
        // 初始化滑块
        initializeSeekBars();

        cameraButton.setOnClickListener(v -> {
            cameraHelper.switchCamera();
        });

    }

    private void initializeSeekBars() {
        skinSoftenSeekBar = findViewById(R.id.skinSoftenSeekBar);
        skinBrightenSeekBar = findViewById(R.id.skinBrightenSeekBar);
        eyeEnlargmentSeekBar = findViewById(R.id.eyeEnlargmentSeekBar);
        noseHighlightSeekBar = findViewById(R.id.noseHighlightSeekBar);
        faceSlenderSeekBar = findViewById(R.id.faceSlenderSeekBar);
        skinSoftenTypeSeekBar = findViewById(R.id.skinSoftenTypeSeekBar);

        skinSoftenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                skinSoftenLevel = progress;
                Log.i(TAG, "Successfully Change Parameters: skinSoften = " + skinSoftenLevel);
                applyBeautySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        skinBrightenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                skinBrightenLevel = progress;
                Log.i(TAG, "Successfully Change Parameters: skinBrighten = " + skinBrightenLevel);
                applyBeautySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        eyeEnlargmentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                eyeEnlargmentLevel = progress;
                Log.i(TAG, "Successfully Change Parameters: eyeEnlargment = " + eyeEnlargmentLevel);
                applyBeautySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        noseHighlightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                noseHighlightLevel = progress;
                Log.i(TAG, "Successfully Change Parameters: noseHighlight = " + noseHighlightLevel);
                applyBeautySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        faceSlenderSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                faceSlenderLevel = progress;
                Log.i(TAG, "Successfully Change Parameters: faceSlender = " + faceSlenderLevel);
                applyBeautySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        skinSoftenTypeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                skinSoftenType = progress;
                Log.i(TAG, "Successfully Change Parameters: skinSoftenType = " + skinSoftenType);
                applyBeautySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void applyBeautySettings() {
        // 调用 native 方法，传递滑块值
        setBeautyParameters(skinSoftenLevel, skinBrightenLevel, eyeEnlargmentLevel, noseHighlightLevel, faceSlenderLevel, skinSoftenType);
    }

    void initCamera() {
        cameraHelper = new CameraHelper.Builder()
                .cameraListener(this)
                .specificCameraId(CAMERA_ID)
                .previewOn(textureView)
                .previewViewSize(new Point(roundCameraGLSurfaceView.getLayoutParams().width, roundCameraGLSurfaceView.getLayoutParams().height))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        cameraHelper.start();
    }

    @Override
    protected void onRequestPermissionResult(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initCamera();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 在布局完成时，
     * 将CameraGLSurfaceView设置为圆形，并将其宽高缩小到屏幕宽度的1/2；
     * 将TextureView的宽度缩小到屏幕宽度的1/2，高度等比缩放
     */
    @Override
    public void onGlobalLayout() {
        roundCameraGLSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        if (previewSize != null) {
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            float previewRatio = (float) previewSize.width / (float) previewSize.height;
            float screenRatio = (float) screenWidth / (float) screenHeight;

            int glSurfaceViewWidth, glSurfaceViewHeight;

            if (previewRatio > screenRatio) {
                // 预览比屏幕更宽，调整高度
                glSurfaceViewHeight = screenHeight;
                glSurfaceViewWidth = (int) (screenHeight * previewRatio);
            } else {
                // 预览比屏幕更高，调整宽度
                glSurfaceViewWidth = screenWidth;
                glSurfaceViewHeight = (int) (screenWidth / previewRatio);
            }

            // 设置GLSurfaceView的布局参数
            ConstraintLayout.LayoutParams glSurfaceViewLayoutParams = (ConstraintLayout.LayoutParams) roundCameraGLSurfaceView.getLayoutParams();
            glSurfaceViewLayoutParams.width = glSurfaceViewWidth;
            glSurfaceViewLayoutParams.height = glSurfaceViewHeight;
            roundCameraGLSurfaceView.setLayoutParams(glSurfaceViewLayoutParams);
        }


        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initCamera();
        }
    }

    /**
     * 停止预览
     */
    @Override
    protected void onPause() {
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        super.onPause();
    }

    /**
     * 继续预览
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (cameraHelper != null) {
            cameraHelper.start();
        }
    }


    @Override
    public void onCameraOpened(Camera camera, final int cameraId, final int displayOrientation, boolean isMirror) {
        previewSize = camera.getParameters().getPreviewSize();
        //正方形预览区域的边长
        squarePreviewSize = Math.min(previewSize.width, previewSize.height);
        //裁剪的区域
        cropRect = new Rect((previewSize.width - squarePreviewSize) / 2, 0,
                (previewSize.width - (previewSize.width - squarePreviewSize) / 2), squarePreviewSize);
        squareNV21 = new byte[squarePreviewSize * squarePreviewSize * 3 / 2];
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.width + "x" + previewSize.height);
        //在相机打开时，添加右上角的view用于显示原始数据和预览数据
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将预览控件和预览尺寸比例保持一致，避免拉伸
                {
                    ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();
                    //横屏
                    if (displayOrientation % 180 == 0) {
                        layoutParams.height = layoutParams.width * previewSize.height / previewSize.width;
                    }
                    //竖屏
                    else {
                        layoutParams.height = layoutParams.width * previewSize.width / previewSize.height;
                    }
                    textureView.setLayoutParams(layoutParams);
                }
                roundCameraGLSurfaceView.init(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT, displayOrientation, squarePreviewSize, squarePreviewSize);
                addNotificationView();

            }
        });
    }

    /**
     * 添加显示原始预览帧和画面中的预览帧数据
     */
    private void addNotificationView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @Override
    public void onPreview(final byte[] nv21, Camera camera) {
        // 获取相机的预览宽度和高度
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        int width = previewSize.width;
        int height = previewSize.height;

        // 计算图像的行跨度（pitch）
        int pitch = width; // NV21格式中Y的跨度就是宽度

        // 调用 JNI 方法处理图像
        byte[] processedNV21 = processPreviewData(nv21, width, height, pitch);
        setBeautyParameters(skinSoftenLevel, skinBrightenLevel, eyeEnlargmentLevel, noseHighlightLevel, faceSlenderLevel, skinSoftenType);
        //裁剪指定的图像区域
        ImageUtil.cropNV21(processedNV21, this.squareNV21, previewSize.width, previewSize.height, cropRect);
        //刷新GLSurfaceView
        roundCameraGLSurfaceView.refreshFrameNV21(this.squareNV21);
    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed: ");
    }

    @Override
    public void onCameraError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {

    }

    @Override
    protected void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
        }
        super.onDestroy();
    }
}
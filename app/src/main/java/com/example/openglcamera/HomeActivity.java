package com.example.openglcamera;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class HomeActivity extends BaseActivity {
    private static final int REQUEST_CODE_USING_CAMERA_AND_OPENGL = 3;
    private static final String[] CAMERA_PERMISSION = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void usingCameraAndOpenGL(View view) {
        if (checkPermissions(CAMERA_PERMISSION)) {
            startActivity(new Intent(this, GLCameraActivity.class));
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, REQUEST_CODE_USING_CAMERA_AND_OPENGL);
        }
    }

    @Override
    protected void onRequestPermissionResult(int requestCode, boolean isAllGranted) {
        super.onRequestPermissionResult(requestCode, isAllGranted);
        if (isAllGranted) {
            switch (requestCode) {
                case REQUEST_CODE_USING_CAMERA_AND_OPENGL:
                    usingCameraAndOpenGL(null);
                    break;
                default:
                    break;
            }
        }else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
    }
}

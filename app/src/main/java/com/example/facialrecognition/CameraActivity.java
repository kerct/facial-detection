package com.example.facialrecognition;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class CameraActivity extends AppCompatActivity {
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private ImageSurfaceView mImageSurfaceView;
    private Camera camera;
    private ConstraintLayout cameraPreviewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraPreviewLayout = findViewById(R.id.cameraLayout);

        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            // Permission has already been granted
            openCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // if request is cancelled, the result arrays are empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    openCamera();
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    private Camera checkDeviceCamera() {
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    private void openCamera() {
        camera = checkDeviceCamera();

        if(camera != null) {
            camera.setDisplayOrientation(90);

            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);

            mImageSurfaceView = new ImageSurfaceView(CameraActivity.this, camera);
            cameraPreviewLayout.addView(mImageSurfaceView);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
            builder.setMessage("Cannot access camera")
                    .setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}

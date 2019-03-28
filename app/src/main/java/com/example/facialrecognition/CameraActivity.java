package com.example.facialrecognition;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.facialrecognition.components.CameraSource;
import com.example.facialrecognition.components.CameraSourcePreview;
import com.example.facialrecognition.components.GraphicOverlay;
import com.example.facialrecognition.facedetection.FaceContourDetectorProcessor;
import com.example.facialrecognition.facedetection.FaceDetectionProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final String FACE_DETECTION = "Face Detection";
    private static final String FACE_CONTOUR = "Face Contour";
    private static final String TAG = "CameraActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public static class PlaceholderFragment extends Fragment
            implements ActivityCompat.OnRequestPermissionsResultCallback {

        private CameraSource cameraSource = null;
        private CameraSourcePreview preview;
        private GraphicOverlay graphicOverlay;
        private boolean facingBack = true;
        private String selectedModel = FACE_DETECTION;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "on create view");
            View rootView;
            if(getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                rootView = inflater.inflate(R.layout.fragment_face_detection, container, false);
                selectedModel = FACE_DETECTION;
            }
            else {
                rootView = inflater.inflate(R.layout.fragment_face_contour, container, false);
                selectedModel = FACE_CONTOUR;
            }

            preview = rootView.findViewById(R.id.preview);
            if (preview == null) {
                Log.d(TAG, "Preview is null");
            }
            graphicOverlay = rootView.findViewById(R.id.overlay);
            if (graphicOverlay == null) {
                Log.d(TAG, "graphicOverlay is null");
            }

            FloatingActionButton fab = rootView.findViewById(R.id.fab);
            if (fab == null) {
                Log.d(TAG, "fab is null");
            }
            if (Camera.getNumberOfCameras() == 1) {
                fab.hide();
            }
            else{
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cameraSource != null) {
                            if (facingBack) {
                                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                                facingBack = false;
                            } else {
                                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                                facingBack = true;
                            }
                        }
                        preview.stop();
                        startCameraSource();
                    }
                });
            }

            if (allPermissionsGranted()) {
                Log.d(TAG, "all permissions granted");
                createCameraSource(selectedModel);
            } else {
                getRuntimePermissions();
            }

            return rootView;
        }

        private void createCameraSource(String model) {
            // If there's no existing cameraSource, create one.
            if (cameraSource == null) {
                cameraSource = new CameraSource(this.getActivity(), graphicOverlay);
            }

            try {
                switch (model) {
                    case FACE_DETECTION:
                        Log.i(TAG, "Using Face Detector Processor");
                        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(getResources()));
                        break;
                    case FACE_CONTOUR:
                        Log.i(TAG, "Using Face Contour Detector Processor");
                        cameraSource.setMachineLearningFrameProcessor(new FaceContourDetectorProcessor());
                        break;
                    default:
                        Log.e(TAG, "Unknown model: " + model);
                }
            } catch (Exception e) {
                Log.e(TAG, "cannot create camera source: " + model);
            }
        }

        private void startCameraSource() {
            if (cameraSource != null) {
                try {
                    if (preview == null) {
                        Log.d(TAG, "resume: Preview is null");
                    }
                    if (graphicOverlay == null) {
                        Log.d(TAG, "resume: graphOverlay is null");
                    }
                    preview.start(cameraSource, graphicOverlay);
                    Log.d(TAG, "resume: camera started");
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source.", e);
                    cameraSource.release();
                    cameraSource = null;
                }
            }
        }

        private void changeCameraSource(String model) {
            preview.stop();
            if (allPermissionsGranted()) {
                createCameraSource(model);
                startCameraSource();
            } else {
                getRuntimePermissions();
            }
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser) {
                if(preview != null) {
                    changeCameraSource(selectedModel);
                }
                else {
                    Log.d(TAG, "preview is null");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            changeCameraSource(selectedModel);
                        }
                    }, 1000);
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "onResume");
            startCameraSource();
        }

        @Override
        public void onPause() {
            super.onPause();
            preview.stop();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (cameraSource != null) {
                cameraSource.release();
            }
        }

        private String[] getRequiredPermissions() {
            try {
                PackageInfo info =
                        this.getActivity().getPackageManager()
                                .getPackageInfo(this.getActivity().getPackageName(), PackageManager.GET_PERMISSIONS);
                String[] ps = info.requestedPermissions;
                if (ps != null && ps.length > 0) {
                    return ps;
                } else {
                    return new String[0];
                }
            } catch (Exception e) {
                return new String[0];
            }
        }

        private boolean allPermissionsGranted() {
            for (String permission : getRequiredPermissions()) {
                if (!isPermissionGranted(this.getActivity(), permission)) {
                    return false;
                }
            }
            return true;
        }

        private void getRuntimePermissions() {
            List<String> allNeededPermissions = new ArrayList<>();
            for (String permission : getRequiredPermissions()) {
                if (!isPermissionGranted(this.getActivity(), permission)) {
                    allNeededPermissions.add(permission);
                }
            }

            if (!allNeededPermissions.isEmpty()) {
                requestPermissions(allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            Log.i(TAG, "Permission granted!");
            if (allPermissionsGranted()) {
                createCameraSource(selectedModel);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        private static boolean isPermissionGranted(Context context, String permission) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: " + permission);
                return true;
            }
            Log.i(TAG, "Permission NOT granted: " + permission);
            return false;
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

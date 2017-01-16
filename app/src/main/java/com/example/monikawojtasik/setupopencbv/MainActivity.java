package com.example.monikawojtasik.setupopencbv;

import android.app.Activity;
import android.support.v4.view.MotionEventCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    private DetectionService handDetectionService;

    private DrawingService drawingService;

    private JavaCameraView javaCameraView;

    private Mat rgbaMat;

    private Mat temp;

    public static final int BACKGROUND_MODE = 0;

    public static final int DETECTION_MODE = 2;

    public static final int DETECTION_MODE_V2 = 3;

    private int mode = BACKGROUND_MODE;

    static {
        System.loadLibrary("MyCLib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    javaCameraView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            int action = MotionEventCompat.getActionMasked(event);
                            switch (action) {
                                case (MotionEvent.ACTION_DOWN):
                                    Log.d(TAG, "Action was DOWN");
                                    String toastStr = null;
                                    if (mode == BACKGROUND_MODE) {
                                        mode = DETECTION_MODE;
                                        toastStr = "Detection mode!!";
                                    } else if (mode == DETECTION_MODE) {
                                        mode = DETECTION_MODE_V2;
                                        toastStr = "Detection mode v2!";
                                    } else if (mode == DETECTION_MODE_V2) {
                                        mode = BACKGROUND_MODE;
                                        toastStr = "Background Mode!";
                                    }
                                    Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_LONG).show();
                                    return true;
                                case (MotionEvent.ACTION_MOVE):
                                    Log.d(TAG, "Action was MOVE");
                                    return true;
                                case (MotionEvent.ACTION_UP):
                                    Log.d(TAG, "Action was UP");
                                    return true;
                                case (MotionEvent.ACTION_CANCEL):
                                    Log.d(TAG, "Action was CANCEL");
                                    return true;
                                case (MotionEvent.ACTION_OUTSIDE):
                                    Log.d(TAG, "Movement occurred outside bounds " +
                                            "of current screen element");
                                    return true;
                                default:
                                    return true;
                            }
                        }
                    });
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCv loaded successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "OpenCv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        Log.i(TAG, "On cameraview started!");

        if (handDetectionService == null)
            handDetectionService = new DetectionService();

        if (drawingService == null)
            drawingService = new DrawingService();

        if (rgbaMat == null)
            rgbaMat = new Mat();

        if (temp == null)
            temp = new Mat();

    }

    @Override
    public void onCameraViewStopped() {
        Log.i(TAG, "On cameraview stopped!");
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgbaMat = inputFrame.rgba();
        if (mode == DETECTION_MODE) {
            Log.i(TAG, "DETECTION_MODE");
            Hand hand = handDetectionService.detectHand(rgbaMat);
            return drawingService.drawHand(hand, rgbaMat);
        } else if (mode == DETECTION_MODE_V2) {
            Hand hand2 = handDetectionService.detectHand(rgbaMat);
            drawingService.drawHand(hand2, rgbaMat);
            Log.i(TAG, "DETECTION_MODE_V2");
            int width = rgbaMat.width();
            int height = rgbaMat.height();
            int pocz = width / 4;
            int kon = pocz * 2;
            Rect cropped = new Rect(pocz, 0, kon, height);
            temp = new Mat();
            Mat srodek = new Mat(rgbaMat, cropped);
            Core.transpose(srodek, srodek);
            Core.flip(srodek, srodek, 1);
            temp.push_back(srodek);
            temp.push_back(srodek);
            Core.transpose(temp, temp);
            Core.flip(temp, temp, 0);
            return temp;
        } else {
            return rgbaMat;
        }
    }
}

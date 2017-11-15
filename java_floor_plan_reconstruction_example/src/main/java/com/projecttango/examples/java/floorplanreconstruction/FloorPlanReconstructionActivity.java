/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projecttango.examples.java.floorplanreconstruction;

import com.google.atap.tango.reconstruction.TangoFloorplanLevel;
import com.google.atap.tango.reconstruction.TangoPolygon;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.google.tango.support.TangoSupport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.lang.Math.tan;
import static java.lang.Thread.sleep;

/**
 * An example showing how to use the 3D reconstruction floor planning features to create a
 * floor plan in Java.
 * <p/>
 * This sample uses the APIs that extract a set of simplified 2D polygons and renders them on a
 * SurfaceView. The device orientation is used to automatically translate and rotate the map.
 * <p/>
 * Rendering is done in a simplistic way, using the canvas API over a SurfaceView.
 */
public class FloorPlanReconstructionActivity extends Activity implements FloorplanView
        .DrawingCallback {
    private static final String TAG = FloorPlanReconstructionActivity.class.getSimpleName();

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int CAMERA_PERMISSION_CODE = 0;

    /*
    testing height between two floors
     */
    private static final float FLOOR_HEIGHT = 4.4f;
    private TangoFloorplanner mTangoFloorplanner;
    private Tango mTango;
    private TangoConfig mConfig;
    private boolean mIsConnected = false;
    private boolean mIsPaused;
    private Button mPauseButton;
    //private FloorplanView //mFloorplanView;
    private TextView mAreaText;
    private TextView mHeightText;
    private TextView mDistanceText;
    private TextView mFloorText;
    private int mDisplayRotation = 0;
    private final int DESTINATION = Integer.parseInt(MainActivity.getDestination());
    private final int START = Integer.parseInt(MainActivity.getStartingPoint());
    private float mMinAreaSpace = 0;

    private double currFloor = 0;
    private float startDevToFloorDistance;
    private boolean isStarted;

    private ImageView up;
    private ImageView left;
    private ImageView right;
    private ImageView down;
    private ImageView stop;

    private boolean clearClicked = false;
    private boolean isSet = false;
    private boolean setRadians = false;
    private float minFloor;
    private float maxFloor;

    // the pointBuffer for get average depth
    private FloatBuffer pointBuffer;
    // the buffer for the current numpoints
    private int numPoints;
    // used in getAverageDepth
    private float averageDepth;

    //used for testing heading, maybe used if works.
    private String headingAngle;

    //maximum radians and distance
    private double maxDistance;
    private double maxRadians;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_navigation);
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.min_area_space, typedValue, true);
        mMinAreaSpace = typedValue.getFloat();

        mPauseButton = (Button) findViewById(R.id.pause_button);
        // //mFloorplanView = (FloorplanView) findViewById(R.id.floorplan);
        ////mFloorplanView.registerCallback(this);
        mAreaText = (TextView) findViewById(R.id.area_text);
        mHeightText = (TextView) findViewById(R.id.height_text);
        mDistanceText = (TextView) findViewById(R.id.floordistance_text);
        mFloorText = (TextView) findViewById(R.id.floor_text);
        //currFloor = Integer.parseInt(MainActivity.getStartingPoint());
        mFloorText.setText("" + (int) currFloor);
        isStarted = false;

        up = (ImageView) findViewById(R.id.imageView8);
        left = (ImageView) findViewById(R.id.imageView7);
        right = (ImageView) findViewById(R.id.imageView6);
        down = (ImageView) findViewById(R.id.imageView5);
        stop = (ImageView) findViewById(R.id.imageView4);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        setDisplayRotation();
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            }, null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check and request camera permission at run time.
        if (checkAndRequestPermissions()) {
            bindTangoService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        synchronized (this) {
            try {
                if (mTangoFloorplanner != null) {
                    mTangoFloorplanner.stopFloorplanning();
                    mTangoFloorplanner.resetFloorplan();
                    mTangoFloorplanner.release();
                    mTangoFloorplanner = null;
                }
                if (mTango != null) {
                    mTango.disconnect();
                }
                mIsConnected = false;
                mIsPaused = true;
            } catch (TangoErrorException e) {
                Log.e(TAG, getString(R.string.exception_tango_error), e);
            }
        }
    }

    /**
     * Initialize Tango Service as a normal Android Service.
     */
    private void bindTangoService() {
        // Initialize Tango Service as a normal Android Service.
        // Since we call mTango.disconnect() in onPause, this will unbind Tango Service,
        // so every time onResume gets called we should create a new Tango object.
        mTango = new Tango(FloorPlanReconstructionActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready,
            // this Runnable will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only
            // when there are no UI thread changes involved.
            @Override
            public void run() {
                synchronized (FloorPlanReconstructionActivity.this) {
                    try {
                        mConfig = setupTangoConfig(mTango);
                        mTango.connect(mConfig);
                        startupTango();
                        TangoSupport.initialize(mTango);
                        mIsConnected = true;
                        mIsPaused = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pauseOrResumeFloorplanning(mIsPaused);
                            }
                        });
                        setDisplayRotation();
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, getString(R.string.exception_out_of_date), e);
                        showsToastAndFinishOnUiThread(R.string.exception_out_of_date);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.exception_tango_error), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_error);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, getString(R.string.exception_tango_invalid), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_invalid);
                    }
                }
            }
        });
    }

    /**
     * Sets up the Tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setupTangoConfig(Tango tango) {
        // Use default configuration for Tango Service, plus color camera, low latency
        // IMU integration, depth, smooth pose and dataset recording.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        // NOTE: Low latency integration is necessary to achieve a precise alignment of virtual
        // objects with the RGB image and produce a good AR effect.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        return config;
    }

    /**
     * Set up the callback listeners for the Tango Service and obtain other parameters required
     * after Tango connection.
     * Listen to updates from the point cloud.
     */
    private void startupTango() {
        mTangoFloorplanner = new TangoFloorplanner(new TangoFloorplanner
                .OnFloorplanAvailableListener() {
            @Override
            public void onFloorplanAvailable(List<TangoPolygon> polygons,
                                             List<TangoFloorplanLevel> levels) {
                //mFloorplanView.setFloorplan(polygons);
                updateFloorAndCeiling(levels);
                calculateAndUpdateArea(polygons);
            }
        });
        // Set camera intrinsics to TangoFloorplanner.
        mTangoFloorplanner.setDepthCameraCalibration(mTango.getCameraIntrinsics
                (TangoCameraIntrinsics.TANGO_CAMERA_DEPTH));

        mTangoFloorplanner.startFloorplanning();

        // Connect listeners to Tango Service and forward point cloud and camera information to
        // TangoFloorplanner.
        List<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(TangoPoseData tangoPoseData) {
                // We are not using onPoseAvailable for this app.
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {
                // We are not using onXyzIjAvailable for this app.
            }

            @Override
            public void onFrameAvailable(int i) {
                // We are not using onFrameAvailable for this app.
            }

            @Override
            public void onTangoEvent(TangoEvent tangoEvent) {
                // We are not using onTangoEvent for this app.
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {
                pointBuffer = tangoPointCloudData.points;
                numPoints = tangoPointCloudData.numPoints;
//                getAveragedDepth(pointBuffer, tangoPointCloudData.numPoints);
                mTangoFloorplanner.onPointCloudAvailable(tangoPointCloudData);
            }
        });
    }


    /**
     * Method called each time right before the floorplan is drawn. It allows use of the Tango
     * Service to get the device position and orientation.
     */
    @Override
    public void onPreDrawing() {
        try {
            // Synchronize against disconnecting while using the service.
            synchronized (FloorPlanReconstructionActivity.this) {
                // Don't execute any Tango API actions if we're not connected to
                // the service.
                if (!mIsConnected) {
                    return;
                }

                // Calculate the device pose in OpenGL engine (Y+ up).
                TangoPoseData devicePose = TangoSupport.getPoseAtTime(0.0,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE,
                        TangoSupport.ENGINE_OPENGL,
                        TangoSupport.ENGINE_OPENGL,
                        mDisplayRotation);

                if (devicePose.statusCode == TangoPoseData.POSE_VALID) {
                    // Extract position and rotation around Z.
                    float[] devicePosition = devicePose.getTranslationAsFloats();
                    float[] deviceOrientation = devicePose.getRotationAsFloats();
                    float yawRadians = yRotationFromQuaternion(deviceOrientation[0],
                            deviceOrientation[1], deviceOrientation[2],
                            deviceOrientation[3]);

                    //mFloorplanView.updateCameraMatrix(devicePosition[0], devicePosition[2],
                    //yawRadians);
                } else {
                    Log.w(TAG, "Can't get last device pose");
                }
            }
        } catch (TangoErrorException e) {
            Log.e(TAG, "Tango error while querying device pose.", e);
        } catch (TangoInvalidException e) {
            Log.e(TAG, "Tango exception while querying device pose.", e);
        }
    }

    /**
     * Calculates the rotation around Y (yaw) from the given quaternion.
     */
    private static float yRotationFromQuaternion(float x, float y, float z, float w) {
        return (float) Math.atan2(2 * (w * y - x * z), w * (w + x) - y * (z + y));
    }

    /**
     * Calculate the total explored space area and update the text field with that information.
     */
    private void calculateAndUpdateArea(List<TangoPolygon> polygons) {
        double area = 0;
        for (TangoPolygon polygon : polygons) {
            if (polygon.layer == TangoPolygon.TANGO_3DR_LAYER_SPACE) {
                // If there is more than one free space polygon, only count those
                // that have an area larger than two square meters to suppress unconnected
                // areas (which might occur in front of windows).
                if (area == 0 || (polygon.area > mMinAreaSpace || polygon.area < 0)) {
                    area += polygon.area;
                }
            }
        }
        final String areaText = String.format("%.2f", area);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAreaText.setText(areaText);
            }
        });
    }

    /**
     * Given the Floorplan levels, calculate the ceiling height and the current distance from the
     * device to the floor.
     */
    private void updateFloorAndCeiling(List<TangoFloorplanLevel> levels) {
        if (levels.size() > 0) {
            // Currently only one level is supported by the floorplanning API.
            if (clearClicked && !isSet) {
                TangoFloorplanLevel level = levels.get(0);
                minFloor = level.minZ;

                maxFloor = level.minZ + FLOOR_HEIGHT;
                isSet = true;
            }

            TangoFloorplanLevel level = levels.get(0);
            float ceilingHeight = level.maxZ - level.minZ;

            final String ceilingHeightText = String.format("%.2f", ceilingHeight);
            // Query current device pose and calculate the distance from it to the floor.
            final TangoPoseData devicePose;
            // Synchronize against disconnecting while using the service.
            synchronized (FloorPlanReconstructionActivity.this) {
                // Don't execute any Tango API actions if we're not connected to
                // the service.
                if (!mIsConnected) {
                    return;
                }
                devicePose = TangoSupport.getPoseAtTime(0.0,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE,
                        TangoSupport.ENGINE_OPENGL,
                        TangoSupport.ENGINE_OPENGL,
                        mDisplayRotation);
            }
            final float devToFloorDistance;
            if (isSet) {
                if (START < DESTINATION) {
                    devToFloorDistance = devicePose.getTranslationAsFloats()[1] - minFloor;
                } else if (START > DESTINATION) {
                    devToFloorDistance = minFloor - devicePose.getTranslationAsFloats()[1];
                } else {
                    devToFloorDistance = devicePose.getTranslationAsFloats()[1] - minFloor;
                }

                if (!isStarted) {
                    startDevToFloorDistance = devToFloorDistance;
                    isStarted = true;
                }
                final String distanceText = String.format("%.2f", devToFloorDistance);
                // Get the average Depth of points that is currently in front of the camera
                averageDepth = getAveragedDepth(pointBuffer, numPoints);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    /*
                     * Testing if floor changes
                    */
                        //going up
                        if (START < DESTINATION) {
                            currFloor = (abs(devToFloorDistance - startDevToFloorDistance) / FLOOR_HEIGHT) + START;
                        }
                        //going down
                        else if (START > DESTINATION) {
                            currFloor = START - (abs(devToFloorDistance - startDevToFloorDistance) / FLOOR_HEIGHT);
                        }
                        //stay
                        else {
                            currFloor = START;
                        }

                        if (averageDepth < 0.95 && averageDepth > 0.3) {
                            //VOICE: go forward, you have reached the platform
                            ShowPic("UP");
                            //clear the parameter
                            maxRadians = 0;
                            maxDistance = 0;
                        } else if (averageDepth < 0.3) {
                            //VOICE: stop now and scan left and right
                            ShowPic("STOP");

                            //record max distance and radians pairs on left and right
                            getHeading();
                            //determine the turn
                            String turn = determineHeading();
                            if(turn.equals("LEFT")){
                                ShowPic("LEFT");
                            }
                            else if(turn.equals("RIGHT")){
                                ShowPic("RIGHT");
                            }
                            else{
                                ShowPic("STOP");
                            }
                            //
                        } else {
                            if (abs(currFloor - DESTINATION) < 0.1) {
                                ShowPic("STOP");
                            } else if (currFloor < DESTINATION) {
                                ShowPic("UP");
                            } else {
                                ShowPic("DOWN");
                            }
                        }

                        mFloorText.setText(String.valueOf(round(currFloor)));

                        mHeightText.setText(ceilingHeightText);
                        //mDistanceText.setText(distanceText);
                        mDistanceText.setText(String.valueOf(headingAngle));
                    }
                });
            }
        }

    }

    public void onPauseButtonClick(View v) {
        mIsPaused = !mIsPaused;
        pauseOrResumeFloorplanning(mIsPaused);
    }

    @UiThread
    private void pauseOrResumeFloorplanning(boolean isPaused) {
        if (!isPaused) {
            mTangoFloorplanner.startFloorplanning();
            mPauseButton.setText("Pause");
        } else {
            mTangoFloorplanner.stopFloorplanning();
            mPauseButton.setText("Resume");
        }
    }

    public void onClearButtonClicked(View v) {
        mTangoFloorplanner.resetFloorplan();
        clearClicked = true;
    }

    /**
     * Set the display rotation.
     */
    @SuppressLint("WrongConstant")
    private void setDisplayRotation() {
        Display display = getWindowManager().getDefaultDisplay();
        mDisplayRotation = display.getRotation();
    }

    /**
     * Check to see if we have the necessary permissions for this app; ask for them if we don't.
     *
     * @return True if we have the necessary permissions, false if we don't.
     */
    private boolean checkAndRequestPermissions() {
        if (!hasCameraPermission()) {
            requestCameraPermission();
            return false;
        }
        return true;
    }

    /**
     * Check to see if we have the necessary permissions for this app.
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the necessary permissions for this app.
     */
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION)) {
            showRequestPermissionRationale();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION},
                    CAMERA_PERMISSION_CODE);
        }
    }

    /**
     * If the user has declined the permission before, we have to explain that the app needs this
     * permission.
     */
    private void showRequestPermissionRationale() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Java Floorplan Reconstruction Example requires camera permission")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(FloorPlanReconstructionActivity.this,
                                new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
                    }
                })
                .create();
        dialog.show();
    }

    /**
     * Display toast on UI thread.
     *
     * @param resId The resource id of the string resource to use. Can be formatted text.
     */
    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FloorPlanReconstructionActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (hasCameraPermission()) {
            bindTangoService();
        } else {
            Toast.makeText(this, "Java Floorplan Reconstruction Example requires camera permission",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void ShowPic(String direction) {
        switch (direction.toUpperCase()) {
            case "STOP":
                down.setAlpha(0.0f);
                right.setAlpha(0.0f);
                left.setAlpha(0.0f);
                up.setAlpha(0.0f);
                stop.setAlpha(1.0f);
                break;
            case "LEFT":
                down.setAlpha(0.0f);
                right.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                up.setAlpha(0.0f);
                left.setAlpha(1.0f);
                break;
            case "RIGHT":
                down.setAlpha(0.0f);
                left.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                up.setAlpha(0.0f);
                right.setAlpha(1.0f);
                break;
            case "UP":
                down.setAlpha(0.0f);
                left.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                right.setAlpha(0.0f);
                up.setAlpha(1.0f);
                break;
            default: //down
                right.setAlpha(0.0f);
                left.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                up.setAlpha(0.0f);
                down.setAlpha(1.0f);
                break;
        }
    }

    /**
     * Calculates the average depth from a point cloud buffer.
     *
     * @param pointCloudBuffer
     * @param numPoints
     * @return Average depth.
     */
    private float getAveragedDepth(FloatBuffer pointCloudBuffer, int numPoints) {
        float totalZ = 0;
        float averageZ = 0;
        if (numPoints != 0) {
            int numFloats = 4 * numPoints;
            for (int i = 2; i < numFloats; i = i + 4) {
                totalZ = totalZ + pointCloudBuffer.get(i);
            }
            averageZ = totalZ / numPoints;
        }
        return averageZ;
    }

    private String determineHeading() {
        //get current pose
        TangoPoseData devicePose = TangoSupport.getPoseAtTime(0.0,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE,
                TangoSupport.ENGINE_OPENGL,
                TangoSupport.ENGINE_OPENGL,
                mDisplayRotation);
        float[] deviceOrientation = devicePose.getRotationAsFloats();
        float yawRadians1 = yRotationFromQuaternion(deviceOrientation[0],
                deviceOrientation[1], deviceOrientation[2],
                deviceOrientation[3]);

        // positive number
        // left section [-3.14, x - 3.14 - 0.5] [x + 0.5, 3.14]
        // right section [x - 3.14 + 0.5, 0][0, x - 0.5]
        if (yawRadians1 >= 0) {
            if ((maxRadians > -3.14 && maxRadians < yawRadians1 - 3.14 - 0.5) || (maxRadians > yawRadians1 + 0.5 && maxRadians < 3.14)) {
                headingAngle = "left";
            } else if ((maxRadians < 0 && maxRadians > yawRadians1 - 3.14 + 0.5) || (maxRadians > 0 && maxRadians < yawRadians1 - 0.5)) {
                headingAngle = "right";
            } else {
                headingAngle = "others";
            }
        }
        // negative number
        // left section [x + 0.5, 0] [0 , x + 3.14 - 0.5]
        // right section [-3.14, x - 0.5] [x+3.14 + 0.5, 3.14]
        else {
            if ((maxRadians < 0 && maxRadians > yawRadians1 + 0.5) || (maxRadians > 0 && maxRadians < yawRadians1 + 3.14 - 0.5)) {
                headingAngle = "left";
            } else if ((maxRadians > -3.14 && maxRadians < yawRadians1 - 0.5) || ((maxRadians > yawRadians1 + 3.14 + 0.5) && maxRadians < 3.14)) {
                headingAngle = "right";
            } else {
                headingAngle = "others";
            }
        }

        return headingAngle;
    }

    /**
     * Get the heading or the turn direction of the user
     *
     * @return 'l' / 'r' / others
     * left  right  hasn't rotated yet
     */
    private void getHeading() {

        Log.d("thread", "trying, I am trying to get heading");
        new Thread(new Runnable() {
            @Override
            public void run() {
                TangoPoseData devicePose = TangoSupport.getPoseAtTime(0.0,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE,
                        TangoSupport.ENGINE_OPENGL,
                        TangoSupport.ENGINE_OPENGL,
                        mDisplayRotation);

                float[] deviceOrientation = devicePose.getRotationAsFloats();
                float yawRadians1 = yRotationFromQuaternion(deviceOrientation[0],
                        deviceOrientation[1], deviceOrientation[2],
                        deviceOrientation[3]);

                averageDepth = getAveragedDepth(pointBuffer, numPoints);
                if(averageDepth > maxDistance){
                    maxDistance = averageDepth;
                    maxRadians = yawRadians1;
                }
            }
        }).start();
    }
}


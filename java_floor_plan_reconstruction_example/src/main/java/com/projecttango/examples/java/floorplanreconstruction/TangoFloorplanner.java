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

import com.google.atap.tango.reconstruction.Tango3dReconstruction;
import com.google.atap.tango.reconstruction.Tango3dReconstructionConfig;
import com.google.atap.tango.reconstruction.TangoFloorplanLevel;
import com.google.atap.tango.reconstruction.TangoPolygon;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.google.tango.support.TangoPointCloudManager;
import com.google.tango.support.TangoSupport;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.nio.FloatBuffer;
import java.util.List;

import static java.lang.Math.*;

/**
 * Uses the Tango Service data to build a floor plan 2D. Provides higher level functionality
 * built on top of the {@code Tango3dReconstruction}.
 * Given a point cloud, it will report a callback with the floorplan polygons.
 * It abstracts all the needed thread management and pose requesting logic.
 */
public class TangoFloorplanner extends Tango.OnTangoUpdateListener {

    private static final String TAG = TangoFloorplanner.class.getSimpleName();
    private final TangoPointCloudManager mPointCloudBuffer;

    private Tango3dReconstruction mTango3dReconstruction = null;
    private OnFloorplanAvailableListener mCallback = null;
    private HandlerThread mHandlerThread = null;
    private volatile Handler mHandler = null;

    private volatile boolean mIsFloorplanningActive = false;

    private Runnable mRunnableCallback = null;

   /**
     * Callback for when meshes are available.
     */
    public interface OnFloorplanAvailableListener {
        void onFloorplanAvailable(List<TangoPolygon> polygons, List<TangoFloorplanLevel> levels);
    }

    public TangoFloorplanner(OnFloorplanAvailableListener callback) {
        mCallback = callback;
        Tango3dReconstructionConfig config = new Tango3dReconstructionConfig();
        // Configure the 3D reconstruction library to work in "floorplan" mode.
        config.putBoolean("use_floorplan", true);
        config.putBoolean("generate_color", false);
        // Simplify the detected countours by allowing a maximum error of 5cm.
        config.putDouble("floorplan_max_error", 0.05);
        mTango3dReconstruction = new Tango3dReconstruction(config);
        mPointCloudBuffer = new TangoPointCloudManager();

        mHandlerThread = new HandlerThread("mesherCallback");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        if (callback != null) {
            /**
             * This runnable processes the saved point clouds and meshes and triggers the
             * onFloorplanAvailable callback with the generated {@code TangoPolygon} instances.
             */
            mRunnableCallback = new Runnable() {
                @Override
                public void run() {
                    List<TangoPolygon> polygons;
                    List<TangoFloorplanLevel> levels;
                    // Synchronize access to mTango3dReconstruction. This runs in TangoFloorplanner
                    // thread.
                    synchronized (TangoFloorplanner.this) {
                        if (!mIsFloorplanningActive) {
                            return;
                        }

                        if (mPointCloudBuffer.getLatestPointCloud() == null) {
                            return;
                        }

                       // Get the latest point cloud data.
                        TangoPointCloudData cloudData = mPointCloudBuffer.getLatestPointCloud();
                       // Hongyi Wang was here 11/6/2017
//                        int numPoints = cloudData.numPoints;
//                        FloatBuffer points = cloudData.points;
//                        int numFloats = 4 * numPoints;
//                        Log.v("!!!!!NumPoints!", String.valueOf(numPoints));
//                        Log.v("!!!!!!!!!!Time!!!!!!", String.valueOf(cloudData.timestamp));
//                        float[] a = new float[4];
//                        for (int i = 2; i < numFloats; i++) {
//                            int mod = i % 4;
//                            switch (mod) {
//                                case 1:
//                                    // Log.v("Y", String.valueOf(points.get(i)));
//                                    a[1] = (points.get(i));
//                                    break;
//                                case 2:
//                                    // Log.v("Z", String.valueOf(points.get(i)));
//                                    a[2] = (points.get(i));
//                                    break;
//                                case 3:
//                                    a[3] = (points.get(i));
//                                    //Log.v("C", String.valueOf(points.get(i)));
//                                    break;
//                                case 0:
//                                    a[0] = (points.get(i));
//                                     //Log.v("X", String.valueOf(points.get(i)));
//                                 break;
//                            }
////                            if ((int)cloudData.timestamp % 5 == 0) {
//                                // Log.v("point", Arrays.toString(a));
////                            }
//                        }

                        TangoPoseData depthPose = TangoSupport.getPoseAtTime(cloudData.timestamp,
                                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                                TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH,
                                TangoSupport.ENGINE_TANGO,
                                TangoSupport.ENGINE_TANGO,
                                TangoSupport.ROTATION_IGNORED);
                        if (depthPose.statusCode != TangoPoseData.POSE_VALID) {
                            Log.e(TAG, "couldn't extract a valid depth pose");
                            return;
                        }

//                        double x = depthPose.getRotationAsFloats()[0];
//                        double y = depthPose.getRotationAsFloats()[1];
//                        double z = depthPose.getRotationAsFloats()[2];
//                        double w = depthPose.getRotationAsFloats()[3];

                        //start hold phone to front

                        // first determine he is on the platform
                        //go up front distance change dramatically under 1m
                        //go down less than 1m.

                        //turn around and measure left and right
                        //determine turning


                        //proceed until reach 0.5m of wall
                        //turn as needed

                        //continue the climb




//                        float[] devicePosition = devicePose.getTranslationAsFloats();
//                        float[] deviceOrientation = devicePose.getRotationAsFloats();
//                        float yawRadians = yRotationFromQuaternion(deviceOrientation[0],
//                                deviceOrientation[1], deviceOrientation[2],
//                                deviceOrientation[3]);

                        // Update the mesh and floorplan representation.
                        mTango3dReconstruction.updateFloorplan(cloudData, depthPose);

                        // Extract the full set of floorplan polygons.
                        polygons = mTango3dReconstruction.extractFloorplan();

                        // Extract the full set of floorplan levels.
                        levels = mTango3dReconstruction.extractFloorplanLevels();
                    }
                    // Provide the new floorplan polygons to the app via callback.
                    mCallback.onFloorplanAvailable(polygons, levels);
                }
            };
        }
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void release() {
        mIsFloorplanningActive = false;
        mTango3dReconstruction.release();
    }

    public void startFloorplanning() {
        mIsFloorplanningActive = true;
    }

    public void stopFloorplanning() {
        mIsFloorplanningActive = false;
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void resetFloorplan() {
        mTango3dReconstruction.clear();
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void setDepthCameraCalibration(TangoCameraIntrinsics calibration) {
        mTango3dReconstruction.setDepthCameraCalibration(calibration);
    }

    @Override
    public void onPoseAvailable(TangoPoseData var1) {

    }

    @Override
    public void onXyzIjAvailable(final TangoXyzIjData var1) {
        // do nothing.
    }

    /**
     * Receives the depth point cloud. This method retrieves and stores the depth camera pose
     * and point cloud to use when updating the {@code Tango3dReconstruction}.
     *
     * @param tangoPointCloudData the depth point cloud.
     */
    @Override
    public void onPointCloudAvailable(final TangoPointCloudData tangoPointCloudData) {
        if (!mIsFloorplanningActive || tangoPointCloudData == null ||
                tangoPointCloudData.points == null) {
            return;
        }
        mPointCloudBuffer.updatePointCloud(tangoPointCloudData);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mRunnableCallback);
    }

    @Override
    public void onFrameAvailable(int var1) {

    }

    @Override
    public void onTangoEvent(TangoEvent var1) {

    }
}

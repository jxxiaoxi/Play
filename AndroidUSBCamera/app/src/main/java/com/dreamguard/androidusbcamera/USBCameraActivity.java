package com.dreamguard.androidusbcamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dreamguard.api.CameraType;
import com.dreamguard.api.KDXCamera;
import com.dreamguard.widget.UVCCameraTextureView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class USBCameraActivity extends AppCompatActivity {


    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;
    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;
    private boolean isRecording = false;
    private Button captureButton;
    private static final String TAG = "USBCameraActivity";

    private KDXCamera camera;

    @Bind(R.id.camera_view)
    protected UVCCameraTextureView mCameraView;


    @OnClick(R.id.open) void open(){
        if(!camera.isCameraOpened()){
            boolean ret = camera.open(0);
            if(!ret){
                Toast.makeText(USBCameraActivity.this, "NO_USB_DEVICE", Toast.LENGTH_SHORT).show();
            }else {
                camera.setPreviewSize(PREVIEW_WIDTH,PREVIEW_HEIGHT);
                camera.setPreviewTexture(mCameraView.getSurfaceTexture());
                camera.startPreview();
            }
        }
    }
    @OnClick(R.id.close) void close(){
        if(camera.isCameraOpened()){
           camera.close();
        }
    }
    @OnClick(R.id.captureStill) void captureStill(){
        if(camera.isCameraOpened()){
            Toast.makeText(USBCameraActivity.this, "Captured", Toast.LENGTH_SHORT).show();
            camera.captureStill();
        }
    }
    @OnClick(R.id.record) void record(){
        if(camera.isCameraOpened() && !camera.isRecording()){
            Toast.makeText(USBCameraActivity.this, "startRecording", Toast.LENGTH_SHORT).show();
            camera.startRecording();
        }
        if(camera.isCameraOpened() && camera.isRecording()){
            Toast.makeText(USBCameraActivity.this, "stopRecording", Toast.LENGTH_SHORT).show();

            camera.stopRecording();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera);
        ButterKnife.bind(this);

        mCameraView.setAspectRatio(PREVIEW_WIDTH / (float)PREVIEW_HEIGHT);

        camera = new KDXCamera();
        camera.init(this);
        camera.setCameraType(CameraType.C3D_SBS);
        checkPermission();

        initCamera2();

    }


    private void initCamera2() {
        mPreview = (TextureView) findViewById(R.id.surface_view);
        captureButton = (Button) findViewById(R.id.button_capture);
    }


    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAPTURE_SECURE_VIDEO_OUTPUT,
                    Manifest.permission.CAPTURE_VIDEO_OUTPUT
            };
            requestPermissions(permissions, 123);
        }
    }

    public void onCaptureClick(View view) {
        if (isRecording) {
            try {
                Log.e(TAG, "MediaRecorder stop ");
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);

                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                e.printStackTrace();
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            if (mCamera != null)
                mCamera.lock();         // take camera access back from MediaRecorder

            setCaptureButtonText(getString(R.string.start_recording));
            isRecording = false;
            releaseCamera();
        } else {
            new MediaPrepareTask().execute(null, null, null);
        }
    }

    private void setCaptureButtonText(String title) {
        captureButton.setText(title);
    }


    private void releaseMediaRecorder() {
        Log.e(TAG, "MediaRecorder releaseMediaRecorder");
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        Log.e(TAG, "MediaRecorder releaseCamera");
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean prepareVideoRecorder() {
        Log.e(TAG, "MediaRecorder prepareVideoRecorder");
        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;
        Log.e(TAG,"videoFrameWidth :  "+optimalSize.width+ "   ;height  : "+optimalSize.height + "  ;ecode : "+profile.videoCodec);

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        Log.e(TAG, "MediaRecorder outputFile : " + mOutputFile.getPath());
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                Log.e(TAG, "MediaRecorder start ");
                mMediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                USBCameraActivity.this.finish();
            }
            // inform the user that recording has started
            Log.e(TAG,"onPostExecute ");
            setCaptureButtonText(getString(R.string.stop_recording));

        }
    }




    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onPause() {
        camera.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }


}

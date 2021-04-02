package com.example.nursingjournal;

import android.content.Context;

import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera camera=null;
    int surfaceWidth, surfaceHeight;
    public CameraSurfaceView(Context context, AttributeSet attr) {
        super(context,attr);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera=Camera.open();
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (Exception e){
            showToast("카메라 기능을 사용할 수 없습니다.");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
                camera.setDisplayOrientation(90);
            } else {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setRotation(90);
                camera.setParameters(parameters);
            }
            camera.setPreviewDisplay(holder);
        }catch(IOException e){
            camera.release();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;
    }

    public boolean capture(Camera.PictureCallback handler){
        if (camera != null){
            camera.takePicture(null,null, handler);
            return true;
        } else {
            return false;
        }
    }

    void showToast(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}

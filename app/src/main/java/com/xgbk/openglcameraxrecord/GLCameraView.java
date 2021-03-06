package com.xgbk.openglcameraxrecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.core.util.Consumer;


import com.xgbk.openglcameraxrecord.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLCameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private static final String LOG_TAG = "OpenGLCameraX";

    private Executor executor = Executors.newSingleThreadExecutor();

    private int textureId;
    private SurfaceTexture surfaceTexture;

    private int vPosition;
    private int vCoord;
    private int programId;

    private int textureMatrixId;
    private float[] textureMatrix = new float[16];

    protected FloatBuffer mGLVertexBuffer;
    protected FloatBuffer mGLTextureBuffer;

    public GLCameraView(Context context) {
        this(context, null);
    }

    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);

        setRenderer(this);
        // 设置非连续渲染
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    public void attachPreview(Preview preview) {
        preview.setSurfaceProvider(new Preview.SurfaceProvider() {
            @Override
            public void onSurfaceRequested(@NonNull SurfaceRequest request) {
                Surface surface = new Surface(surfaceTexture);
                request.provideSurface(surface, executor, new Consumer<SurfaceRequest.Result>() {
                    @Override
                    public void accept(SurfaceRequest.Result result) {
                        surface.release();
                        surfaceTexture.release();
                        Log.v(LOG_TAG, "--accept------");
                    }
                });
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int[] ids = new int[1];

        // OpenGL相关
        GLES20.glGenTextures(1, ids, 0);
        textureId = ids[0];
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this::onFrameAvailable);

        String vertexShader = OpenGLUtils.readRawTextFile(getContext(), R.raw.camera_vertex);
        String fragmentShader = OpenGLUtils.readRawTextFile(getContext(), R.raw.camera_frag);
        programId = OpenGLUtils.loadProgram(vertexShader, fragmentShader);

        vPosition = GLES20.glGetAttribLocation(programId, "vPosition");
        vCoord = GLES20.glGetAttribLocation(programId, "vCoord");

        textureMatrixId = GLES20.glGetUniformLocation(programId, "textureMatrix");

        // 4个顶点，每个顶点有两个浮点型，每个浮点型占4个字节
        mGLVertexBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLVertexBuffer.clear();
        // 顶点坐标
        float[] VERTEX = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLVertexBuffer.put(VERTEX);

        // 纹理坐标
        mGLTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.clear();

        // 正常的纹理贴图坐标，但是贴出的图是上下颠倒的，所以需要修改一下
//        float[] TEXTURE = {
//                0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f
//        };

        // 修复上下颠倒后的纹理贴图坐标
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLTextureBuffer.put(TEXTURE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // 清屏
        GLES20.glClearColor(1, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 更新纹理
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(textureMatrix);
        GLES20.glUseProgram(programId);

        //变换矩阵
        GLES20.glUniformMatrix4fv(textureMatrixId, 1, false, textureMatrix, 0);

        // 传递坐标数据
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        // 传递纹理坐标
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }
}


package io.agora.tutorials.helper;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBeautyFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;

/**
 * @author liya
 * @version V1.0
 * @ClassName:
 * @Package io.agora.tutorials.helper
 * @Description:
 * @date 2017-12-07 15:07
 */

public class CustomizedGPUImageRenderer extends GLSurfaceView implements
        GLSurfaceView.Renderer{
    private final static String LOG_TAG = CustomizedGPUImageRenderer.class.getSimpleName();
    private static final boolean DBG = true;

    private static final int PICTURE_WIDTH = 1280;
    private static final int PICTURE_HEIGHT = 720;

    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;

    private boolean isPreviewing;
    private boolean isSetViewHidden = false;

    private Context mContext;
    private Camera camera;

    private EGLContext mEGLCurrentContext;

    private GPUImage gpuImage;

    private OnEGLContextListener mOnEGLContextHandler;
    private OnFrameAvailableListener mOnFrameAvailableHandler;

    public CustomizedGPUImageRenderer(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CustomizedGPUImageRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init() {
        isPreviewing = false;
        gpuImage = new GPUImage(mContext);
        setEGLContextFactory(new MyContextFactory(this));
        gpuImage.setGLSurfaceView(this);
        GPUImageFilterGroup magicFilterGroup = new GPUImageFilterGroup();
        magicFilterGroup.addFilter(new GPUImageBeautyFilter());
        gpuImage.setFilter(magicFilterGroup);
        gpuImage.setOnFrameAvailableHandler(new GPUImageRenderer.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(int textureID, int rotation, float[] transform) {
                if (mOnFrameAvailableHandler != null) {
                    mOnFrameAvailableHandler.onFrameAvailable(textureID, mEGLCurrentContext,
                            rotation, transform);
                }
            }
        });
        gpuImage.setOnSTFrameAvailableHandler(new GPUImageRenderer.OnSTFrameAvailableListener() {
            @Override
            public void onSTFrameAvailable() {
                requestRender();
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        if (DBG) {
            Log.d(LOG_TAG, "onSurfaceCreated " + gl10);
        }
        Log.d(LOG_TAG, "onSurfaceCreated " + gl10 + " end");
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        if (DBG) {
            Log.d(LOG_TAG, "onSurfaceChanged " + gl10 + " " + width + " " + height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (DBG) {
            Log.d(LOG_TAG, "onDrawFrame ");
        }
    }

    public void setOnEGLContextHandler(OnEGLContextListener listener) {
        this.mOnEGLContextHandler = listener;
    }

    public void setOnFrameAvailableHandler(OnFrameAvailableListener listener) {
        this.mOnFrameAvailableHandler = listener;
    }

    public void initCamera(int cameraId) {
        try {
            if (camera == null) {
                camera = Camera.open(cameraId);

                if (mOnEGLContextHandler != null) {
                    //if (mEGLCurrentContext != null) {
                        mOnEGLContextHandler.onEGLContextReady(mEGLCurrentContext);
                    //}
                }

                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);
                int rotation = ((WindowManager) (getContext()
                        .getSystemService(Context.WINDOW_SERVICE)))
                        .getDefaultDisplay()
                        .getRotation();

                int degrees = 0;
                switch (rotation) {
                    case Surface.ROTATION_0:
                        degrees = 0;
                        break;
                    case Surface.ROTATION_90:
                        degrees = 90;
                        break;
                    case Surface.ROTATION_180:
                        degrees = 180;
                        break;
                    case Surface.ROTATION_270:
                        degrees = 270;
                        break;
                }

                int result;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (info.orientation + degrees) % 360;
                    result = (360 - result) % 360;
                } else {
                    result = (info.orientation - degrees + 360) % 360;
                }

                camera.setDisplayOrientation(result);
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();

                int width = 0;
                int height = 0;
                for (Camera.Size size : sizeList) {
                    if (size.width * size.height <= PICTURE_HEIGHT * PICTURE_WIDTH) {
                        if (size.width * size.height > width * height) {
                            width = size.width;
                            height = size.height;
                        }
                    }
                }
                parameters.setPictureSize(width, height);

                sizeList = parameters.getSupportedPreviewSizes();
                width = 0;
                height = 0;
                for (Camera.Size size : sizeList) {
                    if (size.width * size.height <= PREVIEW_WIDTH * PREVIEW_HEIGHT) {
                        if (size.width * size.height > width * height) {
                            width = size.width;
                            height = size.height;
                        }
                    }
                }

                parameters.setPreviewSize(width, height);
                if (cameraId == 0) {
                    List<String> supportedFocusModes = parameters.getSupportedFocusModes();
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                }
                camera.setParameters(parameters);

                if (!isPreviewing) {
                    isPreviewing = true;
                    //camera.startPreview();
                    gpuImage.setUpCamera(camera, cameraId == 0 ? result : 270, cameraId > 0, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setViewHiddenStatus(boolean isHidden) {
        this.isSetViewHidden = isHidden;
    }

    public void onDestroy() {
        isPreviewing = false;

        mEGLCurrentContext = null;

        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
        }

        camera = null;
    }

    public interface OnFrameAvailableListener {
        void onFrameAvailable(int texture, EGLContext eglContext, int rotation, float[] transform);
    }

    public interface OnEGLContextListener {
        void onEGLContextReady(EGLContext eglContext);
    }

    private static class MyContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private CustomizedGPUImageRenderer mRenderer;

        public MyContextFactory(CustomizedGPUImageRenderer renderer) {
            Log.d(LOG_TAG, "MyContextFactory " + renderer);
            this.mRenderer = renderer;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.d(LOG_TAG, "createContext " + egl + " " + display + " " + eglConfig);
            checkEglError("before createContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

            EGLContext ctx;

            if (mRenderer.mEGLCurrentContext == null) {
                mRenderer.mEGLCurrentContext = egl.eglCreateContext(display, eglConfig,
                        EGL10.EGL_NO_CONTEXT, attrib_list);
                ctx = mRenderer.mEGLCurrentContext;
            } else {
                ctx = mRenderer.mEGLCurrentContext;
            }
            checkEglError("after createContext", egl);
            return ctx;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            Log.d(LOG_TAG, "destroyContext " + egl + " " + display + " " + context + " " + mRenderer.mEGLCurrentContext);
            if (mRenderer.mEGLCurrentContext == null) {
                egl.eglDestroyContext(display, context);
            }
        }

        private static void checkEglError(String prompt, EGL10 egl) {
            int error;
            while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
                Log.d(LOG_TAG, String.format(Locale.US, "%s: EGL error: 0x%x", prompt, error));
            }
        }
    }
}

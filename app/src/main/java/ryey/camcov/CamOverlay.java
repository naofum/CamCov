/*
 * Copyright (c) 2016 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of CamCov.
 *
 * CamCov is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CamCov is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CamCov.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.camcov;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

public class CamOverlay extends TextureView implements TextureView.SurfaceTextureListener {

    public static final float DEFAULT_ALPHA = 0.5F;

    static WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);

    protected static View container = null;

    Camera camera = null;

    {
        setSurfaceTextureListener(this);
    }

    public static View show(final Context context) {
        if (container == null) {
            container = new FrameLayout(context) {
                {
                    View v = new CamOverlay(context);
                    addView(v);
                }
            };
        }

        params.alpha = 0.9F;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(container, params);

        return container;
    }

    public static void hide(final Context context) {
        if (container != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(container);
        }
    }

    public CamOverlay(Context context) {
        super(context);
    }

    public CamOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (camera == null) {
            camera = Camera.open();
        }
        if (camera != null) {
            camera.setDisplayOrientation(90);

            Camera.Parameters parameters = camera.getParameters();

            Camera.Size previewSize = parameters.getPreferredPreviewSizeForVideo();

            parameters.setPreviewSize(previewSize.width, previewSize.height);

            List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();

            int[] range = supportedPreviewFpsRange.get(0);
            parameters.setPreviewFpsRange(range[0], range[1]);

            camera.setParameters(parameters);

            try {
                camera.setPreviewTexture(surfaceTexture);
                camera.startPreview();
            } catch (IOException e) {
                camera.release();
                camera = null;
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}





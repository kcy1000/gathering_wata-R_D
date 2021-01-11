package com.geotwo.ImageMap;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.o2mapmobile.CameraAnimator;
import com.geotwo.o2mapmobile.Configuration;
import com.geotwo.o2mapmobile.DebugHelper;
import com.geotwo.o2mapmobile.DrawContext;
import com.geotwo.o2mapmobile.DrawContext$ModelDrawMode;
import com.geotwo.o2mapmobile.DrawListener;
import com.geotwo.o2mapmobile.InputHandler;
import com.geotwo.o2mapmobile.Model;
import com.geotwo.o2mapmobile.O2Map;
import com.geotwo.o2mapmobile.O2MapInstance$ScreenShotInfo;
import com.geotwo.o2mapmobile.ScreenSpaceModel;
import com.geotwo.o2mapmobile.Terrain;
import com.geotwo.o2mapmobile.View;
import com.geotwo.o2mapmobile.drawable.DrawableShaderFactory;
import com.geotwo.o2mapmobile.element.BillboardElement;
import com.geotwo.o2mapmobile.geometry.Line;
import com.geotwo.o2mapmobile.geometry.Matrix;
import com.geotwo.o2mapmobile.gpu.GPUResourceCache;
import com.geotwo.o2mapmobile.inputhandler.FirstPersonInputHandler;
import com.geotwo.o2mapmobile.inputhandler.MultiInputHandler;
import com.geotwo.o2mapmobile.inputhandler.OrbitInputHandler;
import com.geotwo.o2mapmobile.light.Light;
import com.geotwo.o2mapmobile.model.CompositeModel;
import com.geotwo.o2mapmobile.pick.PickInfo;
import com.geotwo.o2mapmobile.pick.PickedObject;
import com.geotwo.o2mapmobile.screenspacemodel.SandboxScreenSpaceModel;
import com.geotwo.o2mapmobile.test.TestView;
import com.geotwo.o2mapmobile.texture.NativeTextureService;
import com.geotwo.o2mapmobile.texture.Texture;
import com.geotwo.o2mapmobile.texture.TextureLoadWork;
import com.geotwo.o2mapmobile.texture.TextureStatus;
import com.geotwo.o2mapmobile.util.Color;
import com.geotwo.o2mapmobile.util.extent.AxisAlignedBox;
import com.geotwo.o2mapmobile.view.ViewUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.geotwo.o2mapmobile.DrawContext$ModelDrawMode.*;

public class WMapInstance implements O2Map {
    private Color wColor;
    private View wView;
    private Terrain wTerrain;
    private Model compositeModel;
    private ScreenSpaceModel screenSpaceModel;
    private List listLight;
    private DrawContext drawContext;
    private GPUResourceCache gpuRcsCache;
    private boolean i;
    private GLSurfaceView glSurfaceView;
    private ExecutorService exeService;
    private boolean l;
    private boolean m;
    private Configuration configuration;
    private int o;
    private int numThreadPool;
    private Lock transLock;
    private boolean r;
    private boolean s;
    private O2MapInstance$ScreenShotInfo screenShotInfo;
    private boolean isOnSurfaceCreated;
    private InputHandler inputHandler;
    private CameraAnimator camAnimator;
    private boolean x;
    private List listDrawListener;
    private DebugHelper debugHelper;

    public WMapInstance(GLSurfaceView glSurface) {
        this((GPUResourceCache) null, glSurface);
    }

    public WMapInstance(GPUResourceCache glCache, GLSurfaceView glSurface) {
        this.transLock = new ReentrantLock();
        this.x = false;
        this.debugHelper = null;
        this.wView = new TestView();
        this.compositeModel = new CompositeModel();
        this.screenSpaceModel = new SandboxScreenSpaceModel();
        this.gpuRcsCache = glCache;
        this.i = false;
        if (glCache == null) {
            this.gpuRcsCache = new GPUResourceCache();
            this.i = true;
        }

        this.drawContext = new DrawContext(this.gpuRcsCache);
        this.glSurfaceView = glSurface;
        this.wColor = Color.GRAY;
        this.screenShotInfo = null;
        this.s = false;
        this.listLight = new ArrayList();
        this.l = true;
        this.o = 1;
        this.numThreadPool = 2;
        this.exeService = Executors.newFixedThreadPool(this.numThreadPool);
        this.inputHandler = new MultiInputHandler();
        OrbitInputHandler var3 = new OrbitInputHandler();
        ((OrbitInputHandler) var3).setVerticalRotation(true);
        ((MultiInputHandler) this.inputHandler).add(var3);
        ((MultiInputHandler) this.inputHandler).setCurrentInputHandler(var3);
        FirstPersonInputHandler var4 = new FirstPersonInputHandler();
        ((FirstPersonInputHandler) var4).setSpeed(100.0D);
        ((MultiInputHandler) this.inputHandler).add(var4);
        this.configuration = new Configuration();
        this.r = true;
        this.isOnSurfaceCreated = false;
        this.m = false;
        this.listDrawListener = new ArrayList();
        NativeTextureService.initService();
    }

    private boolean draw() {
        if (this.wView == null) {
            return false;
        } else {
            this.drawContext.init();
            this.drawContext.setCurrentView(this.wView);
            this.drawContext.setCurrentTerrain(this.wTerrain);
            this.drawContext.setCurrentModel(this.compositeModel);
            this.drawContext.setCurrentLights(this.listLight);
            this.drawContext.setUseLighting(this.l);
            this.drawContext.setUseAlphaSort(this.m);
            BillboardElement.billCount = 0;
            this.drawContext.getDrawStatistics().beginDraw();
            boolean var1 = false;
            if (this.inputHandler != null) {
                this.inputHandler.onUpdate(this.drawContext);
                var1 = var1 ? true : this.inputHandler.isRedraw();
            }

            if (this.camAnimator != null) {
                this.camAnimator.update(this.drawContext);
                var1 = var1 ? true : this.camAnimator.isRedraw();
            }

            this.wView.applyTransform(this.drawContext);
            this.glClear();
            if (this.wTerrain != null) {
                this.wTerrain.draw(this.drawContext);
            }

            if (this.compositeModel != null) {
                try {
                    this.drawContext.setModelDrawMode(DrawContext$ModelDrawMode.COLLECT_TARGET);
                    this.compositeModel.draw(this.drawContext);
                    this.drawContext.setModelDrawMode(DRAW_TARGET);
                    List var2 = this.drawContext.getUnodredDrawModels();
                    Queue _queue = this.drawContext.getOrderedDrawModels();
                    Model _model;
                    Iterator _iter;
                    if (this.isUseAlphaSort()) {
                        Queue var9 = this.drawContext.getSortedDrawables();
                    } else {
                        _iter = var2.iterator();


                        while (_iter.hasNext()) {
                            _model = (Model) _iter.next();
                            this.drawContext.setCurrentModelMatrix(Matrix.IDENTITY);
                            _model.draw(this.drawContext);
                            // kcy1000 - ConcurrentModificationException 발생
                            _iter.remove();
                        }
                    }

                    _iter = _queue.iterator();

                    while (_iter.hasNext()) {
                        _model = (Model) _iter.next();
                        this.drawContext.setCurrentModelMatrix(Matrix.IDENTITY);
                        _model.draw(this.drawContext);
                    }
                } catch (Exception e) {
                    WataLog.e("Exception=" + e.toString());
                }
            }

            if (this.inputHandler != null) {
                this.drawContext.setModelDrawMode(DRAW_TARGET);
                this.inputHandler.draw(this.drawContext);
            }

            if (this.debugHelper != null) {
                this.drawContext.setModelDrawMode(DRAW_TARGET);
                this.debugHelper.draw(this.drawContext);
            }

            if (this.screenSpaceModel != null) {
                this.drawContext.setModelDrawMode(DRAW_TARGET);
                this.screenSpaceModel.draw(this.drawContext);
            }

            boolean var6 = this.drawTexture();
            var1 = var1 ? true : var6;
            this.drawContext.clear();

            Log.d("geotwo", "Redraw");

            this.drawContext.getDrawStatistics().endDraw();
            this.drawContext.getDrawStatistics().printStatistics();
            if (this.s && this.screenShotInfo == null) {
                int var7 = this.drawContext.getCurrentView().getViewportWidth();
                int var10 = this.drawContext.getCurrentView().getViewportHeight();
                ByteBuffer var12 = ByteBuffer.allocateDirect(var7 * var10 * 4);
                var12.order(ByteOrder.nativeOrder());
                GLES20.glReadPixels(0, 0, var7, var10, 6408, 5121, var12);
                this.screenShotInfo = new O2MapInstance$ScreenShotInfo(var12, var7, var10);
            }

            DrawListener var8;
            Iterator var11;
            if (!this.x && !this.listDrawListener.isEmpty()) {
                var11 = this.listDrawListener.iterator();

                while (var11.hasNext()) {
                    var8 = (DrawListener) var11.next();
                    var8.onFirstDraw();
                }

                this.x = true;
            }

            if (this.x && !this.listDrawListener.isEmpty()) {
                var11 = this.listDrawListener.iterator();

                while (var11.hasNext()) {
                    var8 = (DrawListener) var11.next();
                    if (var8.getFrameDrawFlag()) {
                        var8.onFrameDraw();
                    }
                }
            }

            return var1;
        }
    }

    private boolean glEnable() {
        GLES20.glEnable(2929);
        return true;
    }

    private boolean c() {
        return true;
    }

    public View getView() {
        return this.wView;
    }

    public void setView(View var1) {
        this.wView = var1;
    }

    public Terrain getTerrain() {
        return this.wTerrain;
    }

    public void setTerrain(Terrain var1) {
        this.wTerrain = var1;
    }

    public Model getModel() {
        return this.compositeModel;
    }

    public void setModel(Model var1) {
        this.compositeModel = var1;
    }

    public ScreenSpaceModel getScreenSpaceModel() {
        return this.screenSpaceModel;
    }

    public void setScreenSpaceModel(ScreenSpaceModel var1) {
        this.screenSpaceModel = var1;
    }

    public Color getBackgroundColor() {
        return this.wColor;
    }

    public void setBackgroundColor(Color var1) {
        this.wColor = var1;
    }

    public void onDrawFrame(GL10 var1) {
        long curTime = System.currentTimeMillis();
        this.renderLock();
        this.glEnable();
        boolean updateDraw = this.draw();
        this.c();
        if (updateDraw) {
            this.glSurfaceView.requestRender();
        }

        this.renderUnlock();
        Log.d(Configuration.TAG, "Draw Time :" + (System.currentTimeMillis() - curTime));
    }

    public void onSurfaceChanged(GL10 var1, int width, int height) {
        this.renderLock();
        GLES20.glViewport(0, 0, width, height);
        if (this.wView != null) {
            this.wView.setViewport(width, height);
        }

        this.renderUnlock();
    }

    public void onSurfaceCreated(GL10 var1, EGLConfig config) {
        if (this.isOnSurfaceCreated) {
            this.drawOnSurfaceCreated();
        } else {
            DrawableShaderFactory.resetContext();
        }

        GLES20.glClearColor(0.0F, 1.0F, 0.0F, 1.0F);
        this.isOnSurfaceCreated = true;
    }

    public void dispose() {
        this.exeService.shutdownNow();

        try {
            this.exeService.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (Exception var2) {
        }

        if (this.compositeModel != null) {
            this.compositeModel.dispose();
        }

        if (this.gpuRcsCache != null && this.i) {
            this.gpuRcsCache.dispose();
        }

        NativeTextureService.destroyService();
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    private void drawOnSurfaceCreated() {
        this.exeService.shutdownNow();

        try {
            this.exeService.awaitTermination(10000L, TimeUnit.SECONDS);
        } catch (Exception var2) {
        }

        this.exeService = Executors.newFixedThreadPool(this.numThreadPool);
        if (this.gpuRcsCache != null && this.i) {
            this.gpuRcsCache.dispose();
            this.gpuRcsCache = null;
        }

        this.gpuRcsCache = new GPUResourceCache();
        this.drawContext.resetContext();
        this.drawContext.setCurrentGPUResourceCache(this.gpuRcsCache);
        DrawableShaderFactory.resetContext();
        if (this.compositeModel != null) {
            this.drawContext.setModelDrawMode(UPDATE_CONTEXT);
            this.compositeModel.draw(this.drawContext);
        }

        if (this.screenSpaceModel != null) {
            this.drawContext.setModelDrawMode(UPDATE_CONTEXT);
            this.screenSpaceModel.draw(this.drawContext);
        }

        if (this.inputHandler != null) {
            this.drawContext.setModelDrawMode(UPDATE_CONTEXT);
            this.inputHandler.draw(this.drawContext);
        }

        if (this.debugHelper != null) {
            this.drawContext.setModelDrawMode(UPDATE_CONTEXT);
            this.debugHelper.draw(this.drawContext);
        }

    }

    public void addLight(Light var1) {
        this.listLight.add(var1);
    }

    public Light getLight(int var1) {
        return var1 < this.listLight.size() ? (Light) this.listLight.get(var1) : null;
    }

    public int getLightSize() {
        return this.listLight.size();
    }

    public void setUseLighting(boolean var1) {
        this.l = var1;
    }

    public boolean isUseLighting() {
        return this.l;
    }

    public void setUseAlphaSort(boolean var1) {
        this.m = var1;
    }

    public boolean isUseAlphaSort() {
        return this.m;
    }

    public boolean isUseOnetimeTextureLoad() {
        return this.r;
    }

    public void setUseOnetimeTextureLoad(boolean var1) {
        this.r = var1;
    }

    private void glClear() {
        GLES20.glClearColor(this.wColor.r, this.wColor.g, this.wColor.b, this.wColor.a);
        GLES20.glClear(16640);
    }

    // Ethan
    private boolean drawTexture() {
        boolean var1 = false;
        if (this.gpuRcsCache != null) {
            this.gpuRcsCache.freeDeletedTexture();
            this.gpuRcsCache.freeDeletedBuffer();
        }

        Queue _queue;
        Texture _texture;
        Queue var7;
        int var9;
        if (this.r && this.gpuRcsCache.getTextureMapSize() == 0) {
            _queue = this.drawContext.getLoadTextureList();
            if (_queue.size() > 0) {
                var7 = null;

                while (true) {
                    Texture __texture;
                    while ((__texture = (Texture) _queue.poll()) != null) {
                        if (__texture.getKey() != null && this.gpuRcsCache.isExistTexture(__texture.getKey())) {
                            var9 = this.gpuRcsCache.getTexture(__texture.getKey());
                            __texture.setTexture(var9, this.gpuRcsCache);
                        } else {
                            __texture.setStatus(TextureStatus.TEXTURE_DOWNLOADING);
                            TextureLoadWork var10 = new TextureLoadWork(__texture, this);
                            var10.run();
                            __texture._registerTexture(this.drawContext);
                        }
                    }

                    var1 = true;
                    break;
                }
            }

            var7 = this.drawContext.getRegisterTextureList();
            if (var7.size() > 0) {
                _texture = null;

                while ((_texture = (Texture) var7.poll()) != null) {
                    _texture._registerTexture(this.drawContext);
                }

                var1 = true;
            }
        } else {
            _queue = this.drawContext.getRegisterTextureList();
            if (_queue.size() > 0) {
                for (int var3 = 0; var3 < this.o && _queue.size() > 0; ++var3) {
                    _texture = (Texture) _queue.poll();
                    _texture._registerTexture(this.drawContext);
                }

                var1 = true;
            }

            var7 = this.drawContext.getLoadTextureList();
            if (var7.size() > 0 && _queue.size() <= this.numThreadPool) {
                for (var9 = 0; var9 < this.numThreadPool && var7.size() > 0; ++var9) {
                    Texture var5 = (Texture) var7.poll();
                    if (var5.getKey() != null && this.gpuRcsCache.isExistTexture(var5.getKey())) {
                        int var6 = this.gpuRcsCache.getTexture(var5.getKey());
                        var5.setTexture(var6, this.gpuRcsCache);
                    } else {
                        var5.setStatus(TextureStatus.TEXTURE_DOWNLOADING);
                        this.exeService.execute(new TextureLoadWork(var5, this));
                    }
                }
            }
        }

        return var1;
    }

    public void requestRedraw() {
        this.glSurfaceView.requestRender();
    }

    public InputHandler getInputHandler() {
        return this.inputHandler;
    }

    public void setInputHandler(InputHandler var1) {
        this.inputHandler = var1;
    }

    public Line computeRayFromScreenPoint(int var1, int var2) {
        return ViewUtil.computeRayFromScreenPoint(this.wView, var1, var2);
    }

    public PickedObject getPickObject(PickInfo var1) {
        if (var1 == null) {
            return null;
        } else {
            PickedObject var2 = null;
            Line var3 = this.wView.getRayFromScreenCoord(var1.x, var1.y);
            var1.setPickingRay(var3);
            if (this.compositeModel != null) {
                var2 = this.compositeModel.pickObject(var1);
            }

            return var2;
        }
    }

    public void renderLock() {
        this.transLock.lock();
    }

    public void renderUnlock() {
        this.transLock.unlock();
    }

    public O2MapInstance$ScreenShotInfo getScreenShot() {
        if (this.s) {
            if (this.screenShotInfo != null) {
                O2MapInstance$ScreenShotInfo var1 = this.screenShotInfo;
                this.screenShotInfo = null;
                this.s = false;
                return var1;
            } else {
                return null;
            }
        } else {
            this.s = true;
            return null;
        }
    }

    public Bitmap getScreenShotOrtho(AxisAlignedBox var1) {
        TestView var2 = (TestView) this.getView();
        ViewUtil.FitTopView(var2, var1);
        var2.setPerspective(false);
        int var3 = var2.getViewportWidth();
        int var4 = var2.getViewportHeight();
        int var5 = (int) (var1.maxX - var1.minX);
        int var6 = (int) (var1.maxY - var1.minY);
        float var7 = (float) var5 / (float) var3;
        int var8 = var5;
        int var9 = (int) ((float) var4 * var7);
        if (var9 < var6) {
            float var10 = (float) var6 / (float) var4;
            var8 = (int) ((float) var3 * var10);
            var9 = var6;
        }

        var2.setViewportOrthoWidth(var8);
        var2.setViewportOrthoHeight(var9);
        this.getScreenShot();
        this.requestRedraw();

        O2MapInstance$ScreenShotInfo var18;
        for (var18 = null; var18 == null; var18 = this.getScreenShot()) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException var17) {
                var17.printStackTrace();
                break;
            }
        }

        int var11 = (int) ((float) var5 / (float) var8 * (float) var3);
        int var12 = (int) ((float) var6 / (float) var9 * (float) var4);
        int var13 = (int) ((double) ((float) (var3 - var11)) * 0.5D);
        int var14 = (int) ((double) ((float) (var4 - var12)) * 0.5D);
        Bitmap var15 = var18.getBitmap();
        Bitmap var16 = Bitmap.createBitmap(var15, var13, var14, var11, var12);
        var15 = null;
        var2.setPerspective(true);
        this.requestRedraw();
        return var16;
    }

    public void addDrawListener(DrawListener var1) {
        this.listDrawListener.add(var1);
    }

    public void removeDrawListener(DrawListener var1) {
        this.listDrawListener.remove(var1);
    }

    public void clearFirstDraw() {
        this.x = false;
    }

    public void setCameraAnimator(CameraAnimator var1) {
        this.camAnimator = var1;
    }

    public CameraAnimator getCAmeraAnimator() {
        return this.camAnimator;
    }

    public void setDebugHelper(DebugHelper var1) {
        this.debugHelper = var1;
    }
}


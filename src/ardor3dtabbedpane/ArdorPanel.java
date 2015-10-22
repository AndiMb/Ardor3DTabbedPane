/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ardor3dtabbedpane;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.awt.JoglAwtCanvas;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.DummyControllerWrapper;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.RendererCallable;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Teapot;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author hauffe
 */
public class ArdorPanel extends JPanel implements Scene, Updater, Runnable {

    private final Node root;
    //private final JoglSwingCanvas canvas;
    private final JoglAwtCanvas canvas;
    private final Timer timer = new Timer();
    private final FrameHandler frameWork = new FrameHandler(timer);
    private final LogicalLayer logicalLayer = new LogicalLayer();
    private volatile boolean exit = false;
    private final AwtMouseManager mouseManager;
    private final PhysicalLayer pl;
    private PointLight light;

    private LightState lightState;

    private final Mesh targetMesh = new Teapot("target");

    private MouseControl control;
    
    private Color background = Color.WHITE;

    public ArdorPanel() {
        System.setProperty("ardor3d.useMultipleContexts", "true");
        setLayout(new BorderLayout());
        this.root = new Node("root");

        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);

        final DisplaySettings settings = new DisplaySettings(400, 300, 24, 0, 0, 16, 0, 0, false, false);
        canvas = new JoglAwtCanvas(settings, canvasRenderer);
        //canvas = new JoglSwingCanvas(settings, canvasRenderer);
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resizeCanvas(canvas);
            }

        });
        canvas.setBackground(background);
        add(canvas);

        mouseManager = new AwtMouseManager(canvas);
        pl = new PhysicalLayer(new AwtKeyboardWrapper(canvas),
                new AwtMouseWrapper(canvas, mouseManager),
                DummyControllerWrapper.INSTANCE,
                new AwtFocusWrapper(canvas));

        logicalLayer.registerInput(canvas, pl);

        frameWork.addUpdater(this);
        frameWork.addCanvas(canvas);
    }

    @Override
    public boolean renderUnto(Renderer renderer) {
        // Execute renderQueue item
        GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER)
                .execute(renderer);

        // set background color
        GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).render(new RendererCallable<Void>() {
            @Override
            public Void call() throws Exception {
                getRenderer().setBackgroundColor(new ColorRGBA(background.getRed(), background.getGreen(), background.getBlue(), background.getAlpha()));
                return null;
            }
        });
        ContextGarbageCollector.doRuntimeCleanup(renderer);

        renderer.draw(root);
        return true;
    }

    @Override
    public void init() {

        /**
         * Create a ZBuffer to display pixels closest to the camera above
         * farther ones.
         */
        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(buf);

        // ---- LIGHTS
        /**
         * Set up a basic, default light.
         */
        light = new PointLight();

        final Random random = new Random();

        final float r = random.nextFloat();
        final float g = random.nextFloat();
        final float b = random.nextFloat();
        final float a = random.nextFloat();

        light.setDiffuse(new ColorRGBA(r, g, b, a));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3(100, 100, 100));
        light.setEnabled(true);

        /**
         * Attach the light to a lightState and the lightState to rootNode.
         */
        lightState = new LightState();
        lightState.setEnabled(true);
        lightState.attach(light);
        root.setRenderState(lightState);

        root.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);

        control = new MouseControl(targetMesh);
        control.setupMouseTriggers(logicalLayer);

        // setup some basics on the teapot.
        targetMesh.setModelBound(new BoundingBox());
        Vector3 transCent = targetMesh.getModelBound().getCenter().clone();
        transCent.multiplyLocal(-1);
        targetMesh.getMeshData().translatePoints(transCent);
        root.attachChild(targetMesh);

        root.updateGeometricState(0);
    }

    @Override
    public void update(ReadOnlyTimer rot) {
        logicalLayer.checkTriggers(rot.getTimePerFrame());
        GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.UPDATE)
                .execute();
        root.updateGeometricState(rot.getTimePerFrame(), true);
    }

    public void panelOpened() {
        System.out.println("Opened");
        exit = false;
        new Thread(this).start();
    }

    public void panelClosed() {
        System.out.println("Closed");
        exit = true;
    }

    protected void panelHidden() {
        System.out.println("Hidden");
    }

    protected void panelShowing() {
        System.out.println("Showing");
    }

    private static void resizeCanvas(JoglAwtCanvas canvas) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        double r = (double) w / (double) h;

        Camera cam = canvas.getCanvasRenderer().getCamera();
        if (null != cam) {
            cam.resize(w, h);

            cam.setFrustumPerspective(cam.getFovY(), r, cam.getFrustumNear(),
                    cam.getFrustumFar());
        }
    }

    @Override
    public void run() {
        frameWork.init();
        while (!exit) {
            frameWork.updateFrame();
            Thread.yield();
        }
        System.gc();
    }

    @Override
    public PickResults doPick(Ray3 ray3) {
        return null;
    }
}

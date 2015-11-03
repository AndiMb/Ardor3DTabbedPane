/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ardor3dtabbedpane;

import com.ardor3d.image.Texture;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

/**
 *
 * @author hauffe
 */
public class GradientBackground extends Node{

    private final Quad backgroundQuad;
    
    private final Component canvas;
    private Color colorTop = Color.WHITE;
    private Color colorButtom = Color.BLUE;

    public GradientBackground(Component canvas) {
        super("Background");
        
        this.canvas = canvas;
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                update();
            }

        });
        
        int width = Math.max(canvas.getWidth(), 10);
        int height = Math.max(canvas.getHeight(), 10);
        
        backgroundQuad = new Quad("BackgroundQuad", width, height);
        backgroundQuad.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        backgroundQuad.getSceneHints().setOrthoOrder(1);

        update();
        attachChild(backgroundQuad);

        final ZBufferState zstate = new ZBufferState();
        zstate.setWritable(false);
        zstate.setEnabled(false);
        setRenderState(zstate);

        getSceneHints().setRenderBucketType(RenderBucketType.Skip);
    }
    
    public GradientBackground(Component canvas, Color colorTop, Color colorButtom) {
        this(canvas);
        this.colorTop = colorTop;
        this.colorButtom = colorButtom;
    }

    public Color getColorTop() {
        return colorTop;
    }

    public void setColorTop(Color colorTop) {
        this.colorTop = colorTop;
    }

    public Color getColorButtom() {
        return colorButtom;
    }

    public void setColorButtom(Color colorButtom) {
        this.colorButtom = colorButtom;
    }
    
    public boolean renderUnto(Renderer renderer) {
        renderer.setOrtho();
        renderer.draw(this);
        renderer.unsetOrtho();
        return true;
    }
    
    private void update(){
        
        int width = Math.max(canvas.getWidth(), 10);
        int height = Math.max(canvas.getHeight(), 10);
        backgroundQuad.resize(width, height);
        backgroundQuad.setTranslation(width/2.0, height/2.0, 0.0);

        BufferedImage gradientImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) gradientImage.getGraphics();

        GradientPaint gp = new GradientPaint(0, 0, colorButtom, 0, height, colorTop);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        g2d.dispose();

        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.loadFromImage(AWTImageLoader.makeArdor3dImage(gradientImage, false), Texture.MinificationFilter.Trilinear));

        backgroundQuad.setRenderState(ts);
    }
    
}

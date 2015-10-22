/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ardor3dtabbedpane;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 *
 * @author hauffe
 */
public class MouseControl {

    protected Spatial _lookAtSpatial = null;

    protected double _xSpeed = 0.01;
    protected double _ySpeed = 0.01;

    protected double _zoomSpeedWheel = 0.1;
    protected double _zoomSpeedDrag = 0.001;

    public MouseControl(final Spatial target) {
        _lookAtSpatial = target;
    }

    public void setupMouseTriggers(final LogicalLayer layer) {
        
        if (layer == null){
            return;
        }

        final Predicate<TwoInputStates> scrollWheelMoved = new MouseWheelMovedCondition();
        final TriggerAction wheelZoomAction = new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                final MouseState mouse = inputStates.getCurrent().getMouseState();
                if (mouse.getDwheel() != 0) {
                    zoom(_zoomSpeedWheel * mouse.getDwheel());
                }
            }
        };
        layer.registerTrigger(new InputTrigger(scrollWheelMoved, wheelZoomAction));

        final Predicate<TwoInputStates> leftDownMouseMoved = Predicates.and(TriggerConditions.leftButtonDown(),
                TriggerConditions.mouseMoved());
        final TriggerAction rotationAction = new TriggerAction() {

            // Test boolean to allow us to ignore first mouse event. First event can wildly vary based on platform.
            private boolean firstPing = true;

            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                final MouseState mouse = inputStates.getCurrent().getMouseState();
                if (mouse.getDx() != 0 || mouse.getDy() != 0) {
                    if (!firstPing) {
                        rotate(_xSpeed * mouse.getDx(), _ySpeed * mouse.getDy());
                    } else {
                        firstPing = false;
                    }
                }
            }
        };
        layer.registerTrigger(new InputTrigger(leftDownMouseMoved, rotationAction));

        final Predicate<TwoInputStates> middleDownMouseMoved = Predicates.and(TriggerConditions.middleButtonDown(),
                TriggerConditions.mouseMoved());
        final TriggerAction mouseZoomAction = new TriggerAction() {

            // Test boolean to allow us to ignore first mouse event. First event can wildly vary based on platform.
            private boolean firstPing = true;

            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                final MouseState mouse = inputStates.getCurrent().getMouseState();
                if (mouse.getDy() != 0) {
                    if (!firstPing) {
                        zoom(_zoomSpeedDrag * mouse.getDy());
                    } else {
                        firstPing = false;
                    }
                }
            }
        };
        layer.registerTrigger(new InputTrigger(middleDownMouseMoved, mouseZoomAction));

        final Predicate<TwoInputStates> rightDownMouseMoved = Predicates.and(TriggerConditions.rightButtonDown(),
                TriggerConditions.mouseMoved());
        final TriggerAction mouseTranslateAction = new TriggerAction() {

            // Test boolean to allow us to ignore first mouse event. First event can wildly vary based on platform.
            private boolean firstPing = true;

            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                final MouseState mouse = inputStates.getCurrent().getMouseState();
                if (mouse.getDx() != 0 || mouse.getDy() != 0) {
                    if (!firstPing) {
                        translate(_xSpeed * mouse.getDx(), _ySpeed * mouse.getDy());
                    } else {
                        firstPing = false;
                    }
                }
            }
        };
        layer.registerTrigger(new InputTrigger(rightDownMouseMoved, mouseTranslateAction));
    }

    public void zoom(final double percent) {
        if (_lookAtSpatial == null) {
            return;
        }
        _lookAtSpatial.setScale(Math.max(_lookAtSpatial.getScale().getX() + percent, 0.000001));
    }

    public void rotate(final double xDif, final double yDif) {
        if (_lookAtSpatial == null) {
            return;
        }
        Matrix3 rotationMatrix = new Matrix3();
        rotationMatrix.fromAngles(-30 * yDif * MathUtils.DEG_TO_RAD, 30 * xDif * MathUtils.DEG_TO_RAD, 0.0);
        rotationMatrix.multiplyLocal(_lookAtSpatial.getRotation());
        _lookAtSpatial.setRotation(rotationMatrix);
    }

    public void translate(final double xDif, final double yDif) {
        if (_lookAtSpatial == null) {
            return;
        }
        Vector3 trans = _lookAtSpatial.getTranslation().clone();
        trans.addLocal(xDif, yDif, 0);

        _lookAtSpatial.setTranslation(trans);
    }
}

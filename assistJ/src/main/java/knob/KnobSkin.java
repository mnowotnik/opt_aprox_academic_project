package knob;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * A simple knob skin for slider
 * 
 * @author Jasper Potts
 */
public class KnobSkin extends BehaviorSkinBase<Slider, KnobBehavior> {

    private double knobRadius;
    private double minAngle = -140;
    private double maxAngle = 140;
    private double dragOffset;
    private double dragOffset2;
    
    private StackPane knob;
    private StackPane knobOverlay;
    private StackPane knobDot;

    public KnobSkin(Slider slider) {
        super(slider, new KnobBehavior(slider));
        initialize();
        getSkinnable().requestLayout();
        registerChangeListener(slider.minProperty(), "MIN");
        registerChangeListener(slider.maxProperty(), "MAX");
        registerChangeListener(slider.valueProperty(), "VALUE");
    }

    private void initialize() {
        knob = new StackPane() {
            @Override protected void layoutChildren() {
                knobDot.autosize();
                knobDot.setLayoutX((knob.getWidth()-knobDot.getWidth())/2);
                knobDot.setLayoutY(5+(knobDot.getHeight()/2));
            }
            
        };
        knob.getStyleClass().setAll("knob");
        knobOverlay = new StackPane();
        knobOverlay.getStyleClass().setAll("knobOverlay");
        knobDot = new StackPane();
        knobDot.getStyleClass().setAll("knobDot");

        getChildren().setAll(knob, knobOverlay);
        knob.getChildren().add(knobDot);
        
        getSkinnable().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                double dragStart = mouseToValue(me.getX(), me.getY());
                double zeroOneValue = (getSkinnable().getValue() - getSkinnable().getMin()) / (getSkinnable().getMax() - getSkinnable().getMin());
                //dragOffset = /*zeroOneValue -*/ dragStart;
                dragOffset = me.getX();
                dragOffset2 = me.getY();
                getBehavior().knobPressed(me,dragStart);
            }
        });
        getSkinnable().setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                getBehavior().knobRelease(me,mouseToValue(me.getX(), me.getY()));
            }
        });
        getSkinnable().setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                getBehavior().knobDragged(me, mouseToValue(dragOffset,dragOffset2)-mouseToValue(me.getX(), me.getY()));
            }
        });
    }
    
    private double mouseToValue(double mouseX, double mouseY) {
        return (-mouseX+mouseY)/500;
    }

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        super.getSkinnable().requestLayout();
    }

    void rotateKnob() {
        Slider s = getSkinnable();
        double zeroOneValue = (s.getValue()-s.getMin()) / (s.getMax() - s.getMin());
        double angle = minAngle + ((maxAngle-minAngle) * zeroOneValue);
        knob.setRotate(angle);
    }
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight)
    {
    	 // calculate the available space
        double x = getSkinnable().getInsets().getLeft();
        double y = getSkinnable().getInsets().getTop();
        double w = getSkinnable().getWidth() - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight());
        double h = getSkinnable().getHeight() - (getSkinnable().getInsets().getTop() + getSkinnable().getInsets().getBottom());
        double cx = x+(w/2);
        double cy = y+(h/2);

        // resize thumb to preferred size
        double knobWidth = knob.prefWidth(-1);
        double knobHeight = knob.prefHeight(-1);
        knobRadius = Math.max(knobWidth, knobHeight)/2;
        knob.resize(knobWidth, knobHeight);
        knob.setLayoutX(cx-knobRadius);
        knob.setLayoutY(cy-knobRadius);
        knobOverlay.resize(knobWidth, knobHeight);
        knobOverlay.setLayoutX(cx-knobRadius);
        knobOverlay.setLayoutY(cy-knobRadius);
        rotateKnob();
    }
    protected void layoutChildren() {
        // calculate the available space
        double x = getSkinnable().getInsets().getLeft();
        double y = getSkinnable().getInsets().getTop();
        double w = getSkinnable().getWidth() - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight());
        double h = getSkinnable().getHeight() - (getSkinnable().getInsets().getTop() + getSkinnable().getInsets().getBottom());
        double cx = x+(w/2);
        double cy = y+(h/2);

        // resize thumb to preferred size
        double knobWidth = knob.prefWidth(-1);
        double knobHeight = knob.prefHeight(-1);
        knobRadius = Math.max(knobWidth, knobHeight)/2;
        knob.resize(knobWidth, knobHeight);
        knob.setLayoutX(cx-knobRadius);
        knob.setLayoutY(cy-knobRadius);
        knobOverlay.resize(knobWidth, knobHeight);
        knobOverlay.setLayoutX(cx-knobRadius);
        knobOverlay.setLayoutY(cy-knobRadius);
        rotateKnob();
    }
    
    protected double computeMinWidth(double height) {
        return (getSkinnable().getInsets().getLeft() + knob.minWidth(-1) + getSkinnable().getInsets().getRight());
    }

    protected double computeMinHeight(double width) {
        return(getSkinnable().getInsets().getTop() + knob.minHeight(-1) + getSkinnable().getInsets().getBottom());
    }

    protected double computePrefWidth(double height) {
        return (getSkinnable().getInsets().getLeft() + knob.prefWidth(-1) + getSkinnable().getInsets().getRight());
    }

    protected double computePrefHeight(double width) {
        return(getSkinnable().getInsets().getTop() + knob.prefHeight(-1) + getSkinnable().getInsets().getBottom());
    }

    protected double computeMaxWidth(double height) {
        return Double.MAX_VALUE;
    }

    protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }
}


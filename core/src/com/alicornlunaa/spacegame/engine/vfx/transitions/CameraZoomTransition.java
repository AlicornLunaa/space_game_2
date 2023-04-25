package com.alicornlunaa.spacegame.engine.vfx.transitions;

import com.alicornlunaa.spacegame.engine.vfx.IVfx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class CameraZoomTransition implements IVfx {

    // Variables
    private Stage stage;
    private OrthographicCamera startingCam;
    private OrthographicCamera endingCam;
    private OrthographicCamera transitionCam;
    private float transitionTime = 0.f;
    private float currentTime = 0.f;
    private Interpolation interp = new Interpolation.Exp(2, 10);

    // Constructor
    public CameraZoomTransition(Stage stage, OrthographicCamera startingCam, OrthographicCamera endingCam, float time){
        this.stage = stage;
        this.startingCam = startingCam;
        this.endingCam = endingCam;
        transitionTime = time;

        transitionCam = new OrthographicCamera();
        transitionCam.setToOrtho(false, startingCam.viewportWidth, startingCam.viewportHeight);
        transitionCam.zoom = startingCam.zoom;
        transitionCam.update();

        stage.getViewport().setCamera(transitionCam);
    }

    // Functions
    public OrthographicCamera getTransitionCamera(){ return transitionCam; }

    @Override
    public boolean update(float delta){
        currentTime += delta;

        float scalar = (currentTime / transitionTime);
        float i = interp.apply(scalar);
        transitionCam.zoom = ((1 - i) * startingCam.zoom) + (i * endingCam.zoom);
        transitionCam.update();

        if(scalar >= 1){
            stage.getViewport().setCamera(endingCam);
            return true;
        }

        return false;
    }

}

package com.alicornlunaa.selene_engine.vfx.transitions;

import com.alicornlunaa.selene_engine.vfx.IVfx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;

public class CameraZoomTransition implements IVfx {

    // Variables
    private OrthographicCamera targetCam;
    private float startingZoom;
    private float endingZoom;
    private float transitionTime = 0.f;
    private float currentTime = 0.f;
    private Interpolation interp = new Interpolation.Exp(2, 10);

    // Constructor
    public CameraZoomTransition(OrthographicCamera targetCam, float startingZoom, float endingZoom, float time){
        this.targetCam = targetCam;
        this.startingZoom = startingZoom;
        this.endingZoom = endingZoom;
        transitionTime = time;

        targetCam.zoom = startingZoom;
        targetCam.update();
    }

    // Functions
    @Override
    public boolean update(float delta){
        // Advance the transition
        currentTime += delta;

        // Get distance between both zoom points
        float scalar = (currentTime / transitionTime);
        float i = interp.apply(scalar);
        targetCam.zoom = ((1 - i) * startingZoom) + (i * endingZoom);
        targetCam.update();

        return (scalar >= 1);
    }

}

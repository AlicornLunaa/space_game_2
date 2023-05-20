package com.alicornlunaa.selene_engine.vfx.transitions;

import com.alicornlunaa.selene_engine.vfx.IVfx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;

public class CameraUpTransition implements IVfx {

    // Variables
    private Vector3 startingUp;
    private Vector3 endingUp;
    private OrthographicCamera cam;
    private float transitionTime = 0.f;
    private float currentTime = 0.f;
    private Interpolation interp = new Interpolation.Exp(2, 10);

    // Constructor
    public CameraUpTransition(OrthographicCamera cam, Vector3 endingUp, float time){
        this.endingUp = endingUp;
        transitionTime = time;

        this.cam = cam;
        startingUp = cam.up.cpy();
    }

    // Functions
    @Override
    public boolean update(float delta){
        currentTime += delta;

        float scalar = (currentTime / transitionTime);
        float i = interp.apply(scalar);
        cam.up.set(startingUp.cpy().scl(1 - i).add(endingUp.cpy().scl(i)));
        cam.update();

        if(scalar >= 1){
            return true;
        }

        return false;
    }

}

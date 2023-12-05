package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.ashley.core.Component;

public class CameraComponent implements Component {
    // Variables
    public OrthographicCamera camera;
    public boolean active = true;

    // Constructor
    public CameraComponent(int width, int height){
        camera = new OrthographicCamera(width / Constants.PPM, height / Constants.PPM);
    }
}

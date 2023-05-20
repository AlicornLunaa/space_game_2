package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class CameraComponent implements IComponent {

    public OrthographicCamera camera;
    public boolean active = true;

    public CameraComponent(int width, int height){
        camera = new OrthographicCamera(width, height);
    }
    
}

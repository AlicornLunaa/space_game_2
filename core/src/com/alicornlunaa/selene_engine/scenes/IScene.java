package com.alicornlunaa.selene_engine.scenes;

import com.badlogic.gdx.Screen;

public interface IScene extends Screen {
    
    void render(float delta);
    void show();
    void hide();
    void dispose();

}

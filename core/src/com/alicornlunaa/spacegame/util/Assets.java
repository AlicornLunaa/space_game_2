package com.alicornlunaa.spacegame.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/*
 * Load all the files for the game here
 */
public class Assets extends AssetManager {
    public Assets(){
        super();

        // Texture loading
        load("textures/editor_ui_mockup.png", Texture.class);
        load("textures/fuselage.png", Texture.class);
        load("textures/rocket.png", Texture.class);
        load("textures/tip1.png", Texture.class);
    }
}

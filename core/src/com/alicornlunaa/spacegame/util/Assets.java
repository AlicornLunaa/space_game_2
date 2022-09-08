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
        load("textures/aero_atlas.png", Texture.class);
        load("textures/structural_atlas.png", Texture.class);
        load("textures/thruster_atlas.png", Texture.class);
    }
}

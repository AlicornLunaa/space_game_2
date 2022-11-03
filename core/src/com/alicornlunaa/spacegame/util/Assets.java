package com.alicornlunaa.spacegame.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/*
 * Load all the files for the game here
 */
public class Assets extends AssetManager {
    public Assets(){
        super();

        // Load the texture atlas
        load("textures_packed/textures.atlas", TextureAtlas.class);
        load("particles_packed/particles.atlas", TextureAtlas.class);
    }
}

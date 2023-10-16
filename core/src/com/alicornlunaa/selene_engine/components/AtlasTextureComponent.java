package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AtlasTextureComponent extends TextureComponent {
    // Constructors
    public AtlasTextureComponent(String name){
        super(name, new TextureRegion());
        texture.setRegion(App.instance.atlas.findRegion(name));
    }

    // Functions
    @Override
    public void reload(Assets manager) {
        texture.setRegion(App.instance.atlas.findRegion(textureName));
    }
}

package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets.Reloadable;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureComponent implements IComponent, Reloadable {
    // Variables
    public String textureName;
    public TextureRegion texture;

    // Constructors
    public TextureComponent(Assets manager, String name){
        this.textureName = name;
        this.texture = new TextureRegion();
        texture.setRegion(manager.get(name, Texture.class));
    }

    public TextureComponent(String name){
        this.textureName = name;
        this.texture = new TextureRegion();
        texture.setRegion(App.instance.manager.get(name, Texture.class));
    }

    protected TextureComponent(String name, TextureRegion region){
        this.textureName = name;
        this.texture = region;
    }

    // Functions
    @Override
    public void reload(Assets manager) {
        texture.setRegion(manager.get(textureName, Texture.class));
    }
}

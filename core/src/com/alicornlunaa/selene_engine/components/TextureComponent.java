package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureComponent implements IComponent, Reloadable {

    public String textureName;
    public TextureRegion texture;

    public TextureComponent(Assets manager, String name){
        this.textureName = name;
        this.texture = new TextureRegion();
        texture.setRegion(manager.get(name, Texture.class));
    }

    @Override
    public void reload(Assets manager) {
        texture.setRegion(manager.get(textureName, Texture.class));
    }

}

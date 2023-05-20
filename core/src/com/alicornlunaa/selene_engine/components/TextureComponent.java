package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class TextureComponent implements IComponent, Disposable, Reloadable {

    public String textureName;
    public Texture texture;

    public TextureComponent(Assets manager, String name){
        this.textureName = name;
        this.texture = manager.get(name, Texture.class);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    @Override
    public void reload(Assets manager) {
        texture.dispose();
        texture = manager.get(textureName, Texture.class);
    }

}

package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class TextureComponent implements IComponent, Disposable {

    public Texture texture;

    public TextureComponent(Texture texture){
        this.texture = texture;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

}

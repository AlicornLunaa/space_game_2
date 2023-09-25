package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class ShapeDrawableComponent extends BaseComponent {
    public ShapeDrawableComponent(IEntity entity) {
        super(entity);
    }

    public abstract void draw(ShapeRenderer renderer);
}

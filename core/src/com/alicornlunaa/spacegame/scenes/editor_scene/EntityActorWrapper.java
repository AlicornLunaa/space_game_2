package com.alicornlunaa.spacegame.scenes.editor_scene;

import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class EntityActorWrapper extends Actor {

    private BaseEntity ent;

    public EntityActorWrapper(BaseEntity ent){
        this.ent = ent;
    }

    @Override
    public void draw(Batch batch, float a){
        ent.render(batch);
    }
    
}

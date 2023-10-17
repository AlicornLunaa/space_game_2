package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActorComponent extends Actor implements IComponent {
    // Variables
    private IEntity entity;

    // Constructor
    public ActorComponent(IEntity entity){
        this.entity = entity;
    }

    // Functions
    public IEntity getEntity(){
        return entity;
    }
}

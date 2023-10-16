package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActorComponent implements IComponent {
    // Variables
    public Actor actor;

    // Constructors
    public ActorComponent(){
        actor = new Actor();
    }

    public ActorComponent(Actor customActor){
        actor = customActor;
    }
}

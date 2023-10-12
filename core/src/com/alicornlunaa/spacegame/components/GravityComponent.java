package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.spacegame.systems.GravitySystem;
import com.alicornlunaa.spacegame.util.Constants;

public class GravityComponent implements IComponent {
    // Variables
    private TransformComponent transform;
    private BodyComponent bodyComponent;

    // Constructor
    public GravityComponent(IEntity entity) {
        transform = entity.getComponent(TransformComponent.class);
        bodyComponent = entity.getComponent(BodyComponent.class);
    }

    // Functions
    public float getSphereOfInfluence(){
        return (float)Math.sqrt((Constants.GRAVITY_CONSTANT * bodyComponent.body.getMass()) / GravitySystem.MIN_FORCE);
    }

    public TransformComponent getTransform(){ return transform; }
    public BodyComponent getBodyComponent(){ return bodyComponent; }
}

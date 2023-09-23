package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.components.BaseComponent;
import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.badlogic.gdx.utils.Null;

public class CelestialComponent extends BaseComponent {
    // Variables
    public @Null EllipticalConic conic;
    public float elapsedTime = 0.0f;
    public float radius = 100.f;
    
    private BodyComponent bodyComponent;
    
    // Constructor
    public CelestialComponent(IEntity entity, float radius) {
        super(entity);
        this.radius = radius;

        bodyComponent = entity.getComponent(BodyComponent.class);
    }

    // Functions
    public void update(){
        conic = new EllipticalConic(conic.getParent(), conic.getChild());
    }

    public float getRadius(){
        return radius;
    }

    public float getSphereOfInfluence(){
        if(conic != null){
            return (float)((conic.getSemiMajorAxis() * bodyComponent.world.getPhysScale()) * Math.pow(bodyComponent.body.getMass() / conic.getParent().getComponent(BodyComponent.class).body.getMass(), 2.f/5.f));
        }

        return radius * 4000;
    }
}

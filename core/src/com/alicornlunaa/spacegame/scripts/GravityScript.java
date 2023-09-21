package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.simulation.Celestial2;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class GravityScript implements IScriptComponent {
    // Variables
    private Universe universe;
    private IEntity entity;

    // Private functions
    private void applyGravity(Body a, Body b){
        float m1 = a.getMass();
        float m2 = b.getMass();
        float r = b.getPosition().dst(a.getPosition());
        Vector2 direction = b.getPosition().cpy().sub(a.getPosition()).cpy().nor();
        a.applyForceToCenter(direction.scl(Constants.GRAVITY_CONSTANT * (m1 * m2) / (r * r)), true);
    }

    // Constructor
    public GravityScript(Universe universe, IEntity entity){
        this.universe = universe;
        this.entity = entity;
    }

    // Functions
    @Override
    public void start(){}

    @Override
    public void update() {
        BodyComponent bc = entity.getComponent(BodyComponent.class);
        Celestial2 parent = universe.getParentCelestial(entity);

        if(bc == null) return;
        if(!(bc.world instanceof CelestialPhysWorld)) return;
        
        if(parent != null){
            BodyComponent parentBc = parent.getComponent(BodyComponent.class);
            applyGravity(bc.body, parentBc.body);
        }
    }

    @Override
    public void render() {}
}

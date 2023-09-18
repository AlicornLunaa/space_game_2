package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class GravityScript implements IScriptComponent {
    // Variables
    private App game;
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
    public GravityScript(App game, IEntity entity){
        this.game = game;
        this.entity = entity;
    }

    // Functions
    @Override
    public void start(){
        TransformComponent entityTransform = entity.getComponent(TransformComponent.class);
        
        for(Celestial other : game.gameScene.universe.getCelestials()){
            if(other == entity) continue;

            TransformComponent celestialTransform = other.getComponent(TransformComponent.class);
            BodyComponent celestialBodyComponent = other.getComponent(BodyComponent.class);
    
            if(celestialTransform == null || entityTransform == null || celestialBodyComponent == null) return; // Cant create an orbit for something that doesnt have a position
    
            Vector2 tangentDirection = entityTransform.position.cpy().sub(celestialTransform.position).nor().rotateDeg(90);
            float orbitalRadius = entityTransform.position.dst(celestialTransform.position) / celestialBodyComponent.world.getPhysScale();
            float speed = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * celestialBodyComponent.body.getMass()) / orbitalRadius);
    
            entityTransform.velocity.add(tangentDirection.scl(speed));
        }
    }

    @Override
    public void update() {
        BodyComponent bc = entity.getComponent(BodyComponent.class);

        if(bc == null) return;
        if(!(bc.world instanceof CelestialPhysWorld)) return;

        for(Celestial other : game.gameScene.universe.getCelestials()){
            if(other == entity) continue;
            applyGravity(bc.body, other.bodyComponent.body);
        }
    }

    @Override
    public void render() {}
}

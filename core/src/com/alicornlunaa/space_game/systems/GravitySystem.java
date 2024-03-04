package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.components.celestial.GravityComponent;
import com.alicornlunaa.space_game.components.celestial.PlanetComponent;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Null;

public class GravitySystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<GravityComponent> gm = ComponentMapper.getFor(GravityComponent.class);
    
    public static final float MIN_FORCE = 0.05f;

    // Constructor
    public GravitySystem(){
        super(1);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, GravityComponent.class).get());
    }

    @Override
    public void update(final float deltaTime){
        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            final Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            BodyComponent bodyComp = bm.get(entity);
            GravityComponent gravityComp = gm.get(entity);
            
            // Only run if its allowed
            if(!gravityComp.affectsOthers)
                continue;

            // Update gravity for entity
            bodyComp.body.applyForceToCenter(calculateGravity(transform, bodyComp, gravityComp), false);
        }
    }

    public static float getSphereOfInfluence(BodyComponent bodyComponent){
        return (float)Math.sqrt((Constants.GRAVITY_CONSTANT * bodyComponent.body.getMass()) / GravitySystem.MIN_FORCE);
    }

    private Vector2 calculateGravity(TransformComponent transform, BodyComponent bodyComponent, GravityComponent gravityComponent){
        @Null PlanetComponent planetComponent = null;

        Vector2 spacePosition = transform.position.cpy();
        Vector2 acceleration = new Vector2();
        boolean normalizedDirection = false;

        // Convert to space-coordinates if the entity is on the world
        // if(bodyComponent.world instanceof PlanetaryPhysWorld){
        //     planetComponent = ((PlanetaryPhysWorld)bodyComponent.world).getPlanet().getComponent(PlanetComponent.class);
        //     spacePosition.set(planetComponent.convertToGlobalTransform(entity).position);
        //     normalizedDirection = true;
        //     return acceleration;
        // }

        // Calculate gravity for everything
        for(int i = 0; i < entities.size(); i++){
            // Calculate gravity for every n-body
            Entity otherEntity = entities.get(i);
            TransformComponent otherTransform = otherEntity.getComponent(TransformComponent.class);
            BodyComponent otherBodyComponent = otherEntity.getComponent(BodyComponent.class);
            GravityComponent otherGravity = otherEntity.getComponent(GravityComponent.class);

            if(otherGravity == gravityComponent) continue; // Prevent infinite forces
            // if(otherBodyComponent.world instanceof PlanetaryPhysWorld) continue; // Ignore gravity for others if its on the same world

            // Only apply if they also have a gravity component
            if(otherGravity != null){
                // More error guarding
                if(!otherGravity.affectsOthers) continue;

                // Get variables
                float radiusSqr = spacePosition.dst2(otherTransform.position);
                float soi = getSphereOfInfluence(otherBodyComponent);
                
                // Prevent insignificant forces
                if(radiusSqr > soi * soi)
                    continue;

                // Calculate gravitational force
                Vector2 direction = otherTransform.position.cpy().sub(spacePosition).nor();
                Vector2 force = direction.scl(Constants.GRAVITY_CONSTANT * otherBodyComponent.body.getMass() * bodyComponent.body.getMass() / radiusSqr);
                acceleration.add(force);
            }
        }

        return acceleration;
    }
}

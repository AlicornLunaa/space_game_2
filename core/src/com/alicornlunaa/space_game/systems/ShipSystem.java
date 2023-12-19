package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;

public class ShipSystem extends EntitySystem {
    // Static classes
    private static class ShipPhysicsListener implements EntityListener {
        // Variables
        private ComponentMapper<ShipComponent> tm = ComponentMapper.getFor(ShipComponent.class);
        private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);

        // Constructor
        public ShipPhysicsListener(){}

        // Functions
        @Override
        public void entityAdded(Entity entity) {
            ShipComponent shipComp = tm.get(entity);
            BodyComponent bodyComp = bm.get(entity);

            if(shipComp.rootPart != null){
                shipComp.rootPart.setParent(shipComp, bodyComp, new Matrix4());
            }
        }

        @Override
        public void entityRemoved(Entity entity) {
            ShipComponent shipComp = tm.get(entity);

            if(shipComp.rootPart != null){
                shipComp.rootPart.setParent(null, null, new Matrix4());
            }
        }
    }

    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<ShipComponent> sm = ComponentMapper.getFor(ShipComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);

    // Constructor
    public ShipSystem(){}

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(ShipComponent.class).get());
        engine.addEntityListener(Family.all(ShipComponent.class, BodyComponent.class).get(), new ShipPhysicsListener());
    }

    @Override
    public void update(float deltaTime){
        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            ShipComponent shipComp = sm.get(entity);
            BodyComponent bodyComp = bm.get(entity);

            // Compute SAS angle for a ship
            if(bodyComp != null){
                // Reduce angular velocity with controls
                float angVel = (Math.abs(bodyComp.body.getAngularVelocity()) <= 0.005f) ? 0 : bodyComp.body.getAngularVelocity();
                angVel = Math.min(Math.max(angVel * 2, -1), 1); // Clamp value
                shipComp.artifRoll = angVel;
            }
        }
    }
}

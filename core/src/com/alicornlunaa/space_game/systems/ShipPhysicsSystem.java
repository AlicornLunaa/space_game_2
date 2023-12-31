package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class ShipPhysicsSystem extends EntitySystem {
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
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<ShipComponent> sm = ComponentMapper.getFor(ShipComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    // Constructor
    public ShipPhysicsSystem(){
        super(1);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, ShipComponent.class).get());
        engine.addEntityListener(Family.all(ShipComponent.class, BodyComponent.class).get(), new ShipPhysicsListener());
    }

    @Override
    public void update(float deltaTime){
        // Start render
        Matrix4 renderMatrix = new Matrix4();

		batch.setProjectionMatrix(App.instance.camera.combined);
		batch.setTransformMatrix(renderMatrix);
		batch.begin();

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            BodyComponent bodyComp = bm.get(entity);
            ShipComponent shipComp = sm.get(entity);

            // Compute SAS angle for a ship
            float angVel = (Math.abs(bodyComp.body.getAngularVelocity()) <= 0.005f) ? 0 : bodyComp.body.getAngularVelocity();
            angVel = Math.min(Math.max(angVel * 2, -1), 1); // Clamp value
            shipComp.artifRoll = angVel;

            // Update everything
            shipComp.rootPart.tick(deltaTime, shipComp, bodyComp);
        }

        // Finish render
        batch.end();
    }
}

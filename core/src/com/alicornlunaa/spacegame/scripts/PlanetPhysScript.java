package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.simulation.Planet;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;

public class PlanetPhysScript extends ScriptComponent {
    // Variables
    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent entityBodyComponent = getEntity().getComponent(BodyComponent.class);

    // Constructor
    public PlanetPhysScript(IEntity entity){
        super(entity);
    }

    // Functions
    @Override
    public void start(){}
    
    @Override
    public void update() {
        if(entityBodyComponent == null || transform == null) return;
        if(entityBodyComponent.world instanceof PlanetaryPhysWorld){
            Planet planet = ((PlanetaryPhysWorld)entityBodyComponent.world).getPlanet();
            PlanetComponent planetComponent = planet.getComponent(PlanetComponent.class);
            float worldWidthUnits = planetComponent.chunkWidth * Constants.CHUNK_SIZE * Constants.TILE_SIZE;

            IEntity e = getEntity();
            if(transform.position.x > worldWidthUnits){
                transform.position.x -= worldWidthUnits;
            } else if(transform.position.x < 0){
                transform.position.x += worldWidthUnits;
            }

            planetComponent.checkLeavePlanet(e);

            // Simple gravity
            float heightNormalized = Math.max(entityBodyComponent.body.getPosition().y / planetComponent.chunkHeight / Constants.CHUNK_SIZE / Constants.TILE_SIZE, 1);
            float heightScaled = (float)Math.pow(heightNormalized * planetComponent.terrainRadius, 2);
            float force = Constants.GRAVITY_CONSTANT * ((planet.getComponent(BodyComponent.class).body.getMass() * entityBodyComponent.body.getMass()) / heightScaled);
            entityBodyComponent.body.applyForceToCenter(0.0f, -force, false);
        } else {
            // TODO: Add checking for entering planet
        }
    }

    @Override
    public void render() {}
}

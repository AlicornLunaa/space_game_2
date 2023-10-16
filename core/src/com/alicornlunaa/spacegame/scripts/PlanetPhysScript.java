package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.simulation.Planet;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;

public class PlanetPhysScript extends ScriptComponent {

    private TransformComponent transform;
    private BodyComponent entityBody;

    public PlanetPhysScript(IEntity entity){
        super((BaseEntity)entity);
        transform = entity.getComponent(TransformComponent.class);
        entityBody = entity.getComponent(BodyComponent.class);
    }

    @Override
    public void start(){}
    
    @Override
    public void update() {
        if(entityBody == null || transform == null) return;
        if(!(entityBody.world instanceof PlanetaryPhysWorld)) return;

        Planet planet = ((PlanetaryPhysWorld)entityBody.world).getPlanet();
        PlanetComponent planetComponent = planet.getComponent(PlanetComponent.class);
        float worldWidthPixels = planetComponent.chunkWidth * Constants.CHUNK_SIZE * Constants.TILE_SIZE;

        BaseEntity e = getEntity();
        if(transform.position.x > worldWidthPixels){
            transform.position.x -= worldWidthPixels;
        } else if(transform.position.x < 0){
            transform.position.x += worldWidthPixels;
        }

        planetComponent.checkLeavePlanet(e);
        
        // Taken from Celestial.java to correctly apply the right force
        float heightNormalized = Math.max(entityBody.body.getPosition().y / planetComponent.chunkHeight / Constants.CHUNK_SIZE / Constants.TILE_SIZE, 1);
        float heightScaled = (float)Math.pow(heightNormalized * planetComponent.terrainRadius, 2);
        float force = Constants.GRAVITY_CONSTANT * ((planet.getComponent(BodyComponent.class).body.getMass() * entityBody.body.getMass()) / heightScaled);
        entityBody.body.applyForceToCenter(0.0f, -force, true);
    }

    @Override
    public void render() {}
    
}

package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
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
        CelestialComponent celestialComponent = planet.getComponent(CelestialComponent.class);
        PlanetComponent planetComponent = planet.getComponent(PlanetComponent.class);
        float worldWidthPixels = planetComponent.chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE;

        BaseEntity e = getEntity();
        if(transform.position.x > worldWidthPixels){
            transform.position.x -= worldWidthPixels;
        } else if(transform.position.x < 0){
            transform.position.x += worldWidthPixels;
        }

        planetComponent.checkLeavePlanet(e);
        
        // Taken from Celestial.java to correctly apply the right force
        // Vector2 dragForce = planet.applyDrag(entityBody);
        // entityBody.body.applyForceToCenter(dragForce.x, (-force * 0.5f * (128.f / entityBody.world.getPhysScale() * 1.f)) + dragForce.y, true);
        float height = Math.max(entityBody.body.getPosition().y, celestialComponent.radius / planet.getComponent(BodyComponent.class).world.getPhysScale());
        float force = Constants.GRAVITY_CONSTANT * ((planet.getComponent(BodyComponent.class).body.getMass() * entityBody.body.getMass()) / (height * height));
        entityBody.body.applyForceToCenter(0.0f, -force * 0.1f, true);
    }

    @Override
    public void render() {}
    
}

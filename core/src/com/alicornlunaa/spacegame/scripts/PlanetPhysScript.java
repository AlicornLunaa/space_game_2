package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

public class PlanetPhysScript implements IScriptComponent {

    private IEntity entity;
    private TransformComponent transform;
    private BodyComponent entityBody;

    public PlanetPhysScript(IEntity entity){
        this.entity = entity;

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
        float worldWidthPixels = planet.getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE;

        BaseEntity e = (BaseEntity)entity;
        if(transform.position.x > worldWidthPixels){
            transform.position.x -= worldWidthPixels;
        } else if(transform.position.x < 0){
            transform.position.x += worldWidthPixels;
        }

        planet.checkLeavePlanet(e);
        
        // Taken from Celestial.java to correctly apply the right force
        Vector2 dragForce = planet.applyDrag(entityBody);
        float height = Math.max(entityBody.body.getPosition().y, planet.getRadius() / planet.bodyComponent.world.getPhysScale());
        float force = Constants.GRAVITY_CONSTANT * ((planet.getComponent(BodyComponent.class).body.getMass() * entityBody.body.getMass()) / (height * height));
        entityBody.body.applyForceToCenter(dragForce.x, (-force * 0.5f * (128.f / entityBody.world.getPhysScale() * 1.f)) + dragForce.y, true);
    }

    @Override
    public void render() {}
    
}

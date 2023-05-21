package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

public class PlanetPhysScript implements IScriptComponent {

    private IEntity entity;

    public PlanetPhysScript(IEntity entity){
        this.entity = entity;
    }

    @Override
    public void update() {
        if(!entity.hasComponent(BodyComponent.class)) return;
        if(!(entity.getComponent(BodyComponent.class).world instanceof PlanetaryPhysWorld)) return;

        Planet planet = ((PlanetaryPhysWorld)entity.getComponent(BodyComponent.class).world).getPlanet();
        float worldWidthPixels = planet.getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE;

        BaseEntity e = (BaseEntity)entity;
        if(e.getX() > worldWidthPixels){
            e.setX(e.getX() - worldWidthPixels);
        } else if(e.getX() < 0){
            e.setX(e.getX() + worldWidthPixels);
        }

        planet.checkLeavePlanet(e);
        
        // Taken from Celestial.java to correctly apply the right force
        Vector2 dragForce = planet.applyDrag(e.getBody());
        float height = Math.max(e.getBody().getPosition().y, planet.getRadius() / planet.getPhysScale());
        float force = Constants.GRAVITY_CONSTANT * ((planet.getBody().getMass() * e.getBody().getMass()) / (height * height));
        e.getBody().applyForceToCenter(dragForce.x, (-force * 0.5f * (128.f / e.getPhysScale() * 1.f)) + dragForce.y, true);
    }

    @Override
    public void render() {}
    
}

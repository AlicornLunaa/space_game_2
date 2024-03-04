package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.components.celestial.CelestialComponent;
import com.badlogic.ashley.core.Entity;

public class CelestialFactory {
    public static Entity createCelestial(float x, float y, float rotation, float radius){
        Entity entity = new Entity();
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(new BodyComponent());
        entity.add(new CelestialComponent(radius));
        return entity;
    }

    public static Entity createPlanet(float x, float y, float rotation, float radius){
        Entity entity = createCelestial(x, y, rotation, radius);
        return entity;
    }
}

package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.ActorComponent;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.components.ship.InteriorComponent;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.ashley.core.Entity;

public class ShipFactory {
    public static Entity createShip(float x, float y, float rotation){
        TransformComponent transformComponent = new TransformComponent();
        transformComponent.position.set(x, y);
        transformComponent.rotation = rotation;

        Entity entity = new Entity();
        entity.add(transformComponent);
        entity.add(new BodyComponent());
        entity.add(new ActorComponent());
        entity.add(new InteriorComponent());
        entity.add(new ShipComponent("./saves/ships/test.ship"));
        return entity;
    }
}

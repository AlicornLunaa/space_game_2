package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.components.ship.GridComponent;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.ashley.core.Entity;

public class GridFactory {
    public static Entity createGrid(float x, float y, float rotation){
        Entity entity = new Entity();
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(new BodyComponent());
        entity.add(new GridComponent("./saves/grids/unnamed_grid.grid"));
        entity.add(new ShipComponent());
        entity.add(new CameraComponent(1280, 720, false));
        return entity;
    }
}

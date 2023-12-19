package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Entity;

public class CharacterFactory {
    public static Entity createCharacter(float x, float y, float rotation){
        // Create default character
        BodyComponent bodyComp = new BodyComponent();
        bodyComp.addCollider(Collider.box(0, 0, Constants.PLAYER_WIDTH / 2.f - Constants.HITBOX_LINEUP_FACTOR, Constants.PLAYER_HEIGHT / 2.f - Constants.HITBOX_LINEUP_FACTOR, 0));

        Entity entity = new Entity();
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(bodyComp);
        return null;
    }

    public static Entity createPlayer(float x, float y, float rotation){
        Entity entity = createCharacter(x, y, rotation);

        return entity;
    }

    public static Entity createNPC(float x, float y, float rotation){
        Entity entity = createCharacter(x, y, rotation);
        return entity;
    }
}

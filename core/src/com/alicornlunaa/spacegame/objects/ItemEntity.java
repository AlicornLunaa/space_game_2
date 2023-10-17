package com.alicornlunaa.spacegame.objects;

import com.alicornlunaa.selene_engine.components.AtlasTextureComponent;
import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.BoxColliderComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent.AnchorPoint;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.ItemComponent;
import com.alicornlunaa.spacegame.scripts.PlanetPhysScript;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;

public class ItemEntity extends BaseEntity {
    // Variables
    private TransformComponent transform = getComponent(TransformComponent.class);
    private ItemComponent itemComponent;

    // Constructor
    public ItemEntity(PhysWorld world, float x, float y, String itemID, int count, int maxStack){
        itemComponent = addComponent(new ItemComponent(itemID, count, maxStack));
        transform.position.set(x, y);
        addComponent(new BodyComponent(world));
        addComponent(new BoxColliderComponent(this.getComponent(BodyComponent.class), 0.09f / 2, 0.09f / 2, 0.1f));
        addComponent(new AtlasTextureComponent("items/" + itemID));
        addComponent(new SpriteComponent(0.09f, 0.09f, AnchorPoint.CENTER));
        addComponent(new PlanetPhysScript(this));

        // Pickup script, defined anonymously because nothing else will use it
        addComponent(new ScriptComponent(this) {
            // Variables
            private TransformComponent playerTransform = App.instance.gameScene.player.getComponent(TransformComponent.class);

            // Functions
            @Override
            public void start() {}

            @Override
            public void render() {}

            @Override
            public void update() {
                // Check player position for pickup
                float dist = playerTransform.position.dst2(transform.position);

                if(dist < Constants.ITEM_PICKUP_RANGE * Constants.ITEM_PICKUP_RANGE){
                    Gdx.app.log("Item Manager", "Picked up " + itemComponent.itemID);
                    transform.position.set(0, 0); //! TODO: Fix this, should be deleting the entity
                }
            }
        });
    }
}

package com.alicornlunaa.spacegame.objects.blocks;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.ActorComponent;
import com.alicornlunaa.selene_engine.components.AtlasTextureComponent;
import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.BoxColliderComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.components.tiles.DynamicTileComponent;
import com.alicornlunaa.spacegame.components.tiles.StaticTileComponent;
import com.alicornlunaa.spacegame.components.tiles.TileComponent;
import com.alicornlunaa.spacegame.scripts.PlanetPhysScript;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;

public class BaseTile extends BaseEntity {
    // Statics
    public static HashMap<String, TextureRegion> tileTextures = new HashMap<>();
    
    public static TextureRegion getTexture(TextureAtlas atlas, String id){
        if(tileTextures.containsKey(id)) return tileTextures.get(id);

        String texture = "tiles/" + id;
        TextureRegion region = atlas.findRegion(texture);
        tileTextures.put(id, region);
        return region;
    }
    
    // Variables
    protected TransformComponent transform = getComponent(TransformComponent.class);
    protected TextureComponent textureComponent;
    protected SpriteComponent spriteComponent;
    protected TileComponent tileComponent;
    protected ActorComponent actorComponent;

    // Constructor
    public BaseTile(String tileID, int x, int y){
        // Static Tile Constructor
        textureComponent = addComponent(new AtlasTextureComponent("tiles/" + tileID));
        spriteComponent = addComponent(new SpriteComponent(Constants.TILE_SIZE, Constants.TILE_SIZE));
        tileComponent = addComponent(new StaticTileComponent(textureComponent, tileID, x, y));
        actorComponent = addComponent(new ActorComponent(){
            @Override
            public void draw(Batch batch, float alpha){
                batch.draw(
                    textureComponent.texture,
                    ((StaticTileComponent)tileComponent).x * Constants.TILE_SIZE, ((StaticTileComponent)tileComponent).y * Constants.TILE_SIZE,
                    0, 0,
                    Constants.TILE_SIZE, Constants.TILE_SIZE,
                    1, 1,
                    0
                );
            }
        });

        actorComponent.setBounds(
            x * Constants.TILE_SIZE,
            y * Constants.TILE_SIZE,
            Constants.TILE_SIZE,
            Constants.TILE_SIZE
        );
    }

    public BaseTile(PhysWorld world, String tileID, float x, float y, float rotation){
        // Dynamic Tile Constructor
        transform.position.set(x, y);
        transform.rotation = rotation;

        BodyComponent bodyComponent = addComponent(new BodyComponent(world));
        textureComponent = addComponent(new AtlasTextureComponent("tiles/" + tileID));
        spriteComponent = addComponent(new SpriteComponent(Constants.TILE_SIZE, Constants.TILE_SIZE));
        actorComponent = addComponent(new ActorComponent());
        tileComponent = addComponent(new DynamicTileComponent(textureComponent, bodyComponent, tileID));

        addComponent(new BoxColliderComponent(bodyComponent, Constants.TILE_SIZE / 2, Constants.TILE_SIZE / 2, 1.f));
        addComponent(new PlanetPhysScript(this));
        addComponent(new ScriptComponent(this) {
            // Variables
            private TransformComponent transform = getEntity().getComponent(TransformComponent.class);

            // Functions
            @Override
            public void start() {}

            @Override
            public void render() {}

            @Override
            public void update() {
                // Update the actor to fit
                actorComponent.setPosition(transform.position.x, transform.position.y, Align.center);
                actorComponent.setRotation((float)Math.toDegrees(transform.rotation));
            }
        });

        actorComponent.setBounds(
            x * Constants.TILE_SIZE,
            y * Constants.TILE_SIZE,
            Constants.TILE_SIZE,
            Constants.TILE_SIZE
        );
    }
}

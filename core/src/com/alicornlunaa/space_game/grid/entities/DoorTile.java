package com.alicornlunaa.space_game.grid.entities;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class DoorTile extends TileEntity {
    // Variables
    private Animation<AtlasRegion> textures;
    private float animationTimer = 0.f;
    private boolean open = false;

    // Constructor
    public DoorTile(int rotation) {
        super("door", 1, 2, rotation);
        textures = new Animation<>(1.f / 12.f, App.instance.atlas.findRegions("parts/door"));
        textures.setPlayMode(PlayMode.REVERSED);
    }
    
    // Functions
    private void openDoor(){
        open = true;
        animationTimer = 0.f;
        textures.setPlayMode(PlayMode.NORMAL);
    }

    private void closeDoor(){
        open = false;
        animationTimer = 0.f;
        textures.setPlayMode(PlayMode.REVERSED);
    }

    @Override
    public void render(Batch batch, float deltaTime){
        batch.draw(
            textures.getKeyFrame(animationTimer),
            x * Constants.TILE_SIZE,
            y * Constants.TILE_SIZE,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE * width,
            Constants.TILE_SIZE * height,
            1,
            1,
            rotation * -90
        );

        animationTimer += deltaTime;
    }

    @Override
    public void update(Entity entity, float deltaTime) {
    }

    @Override
    public boolean click(Entity entity, Entity interactor, int button){
        // Handle interaction
        switch(button){
            case Buttons.LEFT:
                if(open) closeDoor();
                else openDoor();
                return true;
        }

        return false;
    }
}

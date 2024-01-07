package com.alicornlunaa.space_game.grid.entities;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ControlSeatTile extends TileEntity {
    private TextureRegion texture;

    public ControlSeatTile(int rotation) {
        super("controlseat", 1, 2, rotation);
        texture = App.instance.atlas.findRegion("parts/control_seat");
    }
    
    @Override
    public void render(Batch batch, float deltaTime){
        batch.draw(
            texture,
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
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public boolean click(int button){
        switch(button){
            case Buttons.LEFT:
                System.out.println("Clicked!");
                return true;
        }

        return false;
    }
}

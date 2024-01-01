package com.alicornlunaa.space_game.grid.entities;

import com.alicornlunaa.space_game.grid.tiles.TileEntity;

public class CustomTile extends TileEntity {
    public CustomTile(int x, int y, int rotation) {
        super("custom", x, y, rotation, 4, 2);
    }

    @Override
    public void update(float deltaTime) {
    }
}

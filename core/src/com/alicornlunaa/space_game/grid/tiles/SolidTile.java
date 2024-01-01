package com.alicornlunaa.space_game.grid.tiles;

public class SolidTile extends TileElement {
    // Variables
    public boolean movable = false;

    // Constructor
    public SolidTile(Element element, int x, int y) {
        super(element, State.SOLID, x, y);
    }
}

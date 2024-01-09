package com.alicornlunaa.space_game.grid.tiles;

public class SolidTile extends TileElement {
    // Variables
    public boolean movable = false;

    // Constructor
    public SolidTile(Element element, Shape shape) {
        super(element, State.SOLID, shape);
    }
}

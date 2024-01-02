package com.alicornlunaa.space_game.grid.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class LiquidTile extends TileElement {
    // Variables
    public int spreadFactor = 1;
    public float viscosity = 0.975f;
    public boolean renderFullBlock = false;

    // Constructor
    public LiquidTile(Element element, TextureRegion texture) {
        super(element, State.LIQUID, texture);
        mass = element.density;
    }
}

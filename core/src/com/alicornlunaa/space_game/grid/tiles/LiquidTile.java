package com.alicornlunaa.space_game.grid.tiles;

public class LiquidTile extends TileElement {
    // Variables
    public int spreadFactor = 1;
    public float viscosity = 0.975f;
    public boolean renderFullBlock = false;

    // Constructor
    public LiquidTile(Element element) {
        super(element, State.LIQUID);
        mass = element.density;
    }
}

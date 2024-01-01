package com.alicornlunaa.space_game.grid.tiles;

import com.badlogic.gdx.math.Vector2;

public class TileElement extends AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final Element element;
    public final State state;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms
    public Vector2 floatingPosition = new Vector2(0.5f, 0.5f); // Keeps the decimals of the current position to allow small movements
    public Vector2 velocity = new Vector2();
    public boolean isUpdated = false; // If this tile was updated this frame or not

    // Constructor
    public TileElement(Element element, State state, int x, int y){
        super("element_" + element, x, y, 0, 1, 1);
        this.element = element;
        this.state = state;
    }
}

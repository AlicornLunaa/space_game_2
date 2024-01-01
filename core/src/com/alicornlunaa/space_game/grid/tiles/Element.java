package com.alicornlunaa.space_game.grid.tiles;

import com.badlogic.gdx.graphics.Color;

public enum Element {
    // Enumerations
    SAND(1602, 1773.15f, 2503.15f, 1500, true, Color.YELLOW),
    STONE(2800, 1000.f, 2503.15f, 15000, false, Color.GRAY),
    STEEL(7850, 1370.f, 2900.15f, 15000, false, Color.DARK_GRAY),
    WATER(1000, 273.15f, 373.15f, 1273.15f, false, Color.BLUE),
    ETHANOL(789, 159.15f, 351.15f, 1500.f, false, Color.WHITE),
    OXYGEN(1, 55, 90, 15000, false, Color.CYAN),
    CARBON_DIOXIDE(1.1f, 217.15f, 195.15f, 15000, false, Color.LIGHT_GRAY);

    // Variables
    public final float density; // Density of the element in kg/M^3
    public final float meltingPoint; // Temperature the element transitions from solid to liquid in K
    public final float boilingPoint; // Temperature the element transitions from liquid to gas in K
    public final float ionizationPoint; // Temperature the element transitions from gas to plasma in K
    public final boolean falling; // Whether or not this tile can move every turn like sand
    public final Color color;

    // Constructor
    private Element(float density, float meltingPoint, float boilingPoint, float ionizationPoint, boolean falling, Color color){
        this.density = density;
        this.meltingPoint = meltingPoint;
        this.boilingPoint = boilingPoint;
        this.ionizationPoint = ionizationPoint;
        this.falling = falling;
        this.color = color;
    }
}

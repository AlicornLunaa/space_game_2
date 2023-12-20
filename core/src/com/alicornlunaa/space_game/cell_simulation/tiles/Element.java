package com.alicornlunaa.space_game.cell_simulation.tiles;

public enum Element {
    // Enumerations
    SAND(1602, 1773.15f, 2503.15f, 1500),
    STONE(2800, 1000.f, 2503.15f, 15000),
    WATER(1000, 273.15f, 373.15f, 1273.15f),
    ETHANOL(789, 159.15f, 351.15f, 1500.f),
    OXYGEN(1, 55, 90, 15000),
    CARBON_DIOXIDE(1.1f, 217.15f, 195.15f, 15000);

    // Variables
    public final float density; // Density of the element in kg/M^3
    public final float meltingPoint; // Temperature the element transitions from solid to liquid in K
    public final float boilingPoint; // Temperature the element transitions from liquid to gas in K
    public final float ionizationPoint; // Temperature the element transitions from gas to plasma in K

    // Constructor
    private Element(float density, float meltingPoint, float boilingPoint, float ionizationPoint){
        this.density = density;
        this.meltingPoint = meltingPoint;
        this.boilingPoint = boilingPoint;
        this.ionizationPoint = ionizationPoint;
    }
}

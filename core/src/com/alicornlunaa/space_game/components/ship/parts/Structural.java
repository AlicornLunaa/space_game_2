package com.alicornlunaa.space_game.components.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.objects.ship.Ship;

public class Structural extends Part {
    // Variables
    private float fuelCapacity;
    private float batteryCapacity;

    // Constructor
    public Structural(final App game, final Ship ship, JSONObject obj){
        super(game, ship, obj);

        JSONObject metadata = obj.optJSONObject("metadata", new JSONObject());
        fuelCapacity = metadata.optFloat("fuelCapacity", 1000);
        batteryCapacity = metadata.optFloat("batteryCapacity", 1000);
    }

    // Getters
    public float getFuelCapacity(){ return fuelCapacity; }
    public float getBatteryCapacity(){ return batteryCapacity; }
}

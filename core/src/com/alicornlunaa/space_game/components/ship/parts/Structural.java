package com.alicornlunaa.space_game.components.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.space_game.components.ship.ShipComponent;

public class Structural extends Part {
    // Variables
    public float fuelCapacity;
    public float batteryCapacity;

    // Constructor
    public Structural(ShipComponent shipComponent, JSONObject obj){
        super(shipComponent, obj);

        JSONObject metadata = obj.optJSONObject("metadata", new JSONObject());
        fuelCapacity = metadata.optFloat("fuelCapacity", 1000);
        batteryCapacity = metadata.optFloat("batteryCapacity", 1000);
    }
}

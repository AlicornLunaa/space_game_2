package com.alicornlunaa.spacegame.parts;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;

public class Structural extends Part {
    // Variables
    private float fuelCapacity;
    private float batteryCapacity;

    // Constructor
    public Structural(final App game, final Ship ship, JSONObject obj){
        super(game, ship, obj);

        JSONObject metadata = obj.getJSONObject("metadata");
        fuelCapacity = metadata.getFloat("fuelCapacity");
        batteryCapacity = metadata.getFloat("batteryCapacity");
    }

}

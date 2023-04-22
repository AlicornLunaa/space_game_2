package com.alicornlunaa.spacegame.objects.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;

public class Aero extends Part {
    // Variables
    private float drag;
    private float lift;

    // Constructor
    public Aero(final App game, final Ship ship, JSONObject obj){
        super(game, ship, obj);

        JSONObject metadata = obj.getJSONObject("metadata");
        drag = metadata.getFloat("drag");
        lift = metadata.getFloat("lift");
    }

    // Getters
    public float getDrag(){ return drag; }
    public float getLift(){ return lift; }

}

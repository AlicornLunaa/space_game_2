package com.alicornlunaa.space_game.components.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.space_game.components.ship.ShipComponent;

public class Aero extends Part {
    // Variables
    public float drag;
    public float lift;

    // Constructor
    public Aero(ShipComponent ship, JSONObject obj){
        super(ship, obj);

        JSONObject metadata = obj.getJSONObject("metadata");
        drag = metadata.getFloat("drag");
        lift = metadata.getFloat("lift");
    }
}

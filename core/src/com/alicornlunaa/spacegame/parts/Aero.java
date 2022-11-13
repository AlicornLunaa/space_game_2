package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class Aero extends ShipPart {
    // Variables
    private String name;
    private String description;
    private float drag;
    private float lift;

    // Constructor
    public Aero(Body parent, Array<PhysShapeInternal> interiorShapes, ShipState stateRef, TextureRegion region, float scale, Vector2 posOffset, float rotOffset, ArrayList<Vector2> attachmentPoints, String name, String description, float density, float drag, float lift){
        super(parent, interiorShapes, stateRef, region, scale, posOffset, rotOffset, attachmentPoints);

        this.name = name;
        this.description = description;
        this.drag = drag;
        this.lift = lift;
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nDrag: " + String.valueOf(drag);
        str += "\nLift: " + String.valueOf(lift);
        str += "\n";
        return str;
    }
}

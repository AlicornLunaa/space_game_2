package com.alicornlunaa.spacegame.parts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Thruster extends ShipPart {
    // Variables
    private String name;
    private String description;
    private float power;
    private float coneAngle;
    private float coneSpeed;
    private float rotationOffset;

    private float currentAngle;

    // Constructor
    public Thruster(Body parent, TextureRegion region, Vector2 size, Vector2 posOffset, float rotOffset, String name, String description, float density, float power, float coneAngle){
        super(parent, region, size, posOffset, rotOffset);

        this.name = name;
        this.description = description;
        this.power = power;
        this.coneAngle = coneAngle;
        this.coneSpeed = 1.5f;
        this.rotationOffset = rotOffset;

        currentAngle = 0.f;
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);

        if(Gdx.input.isKeyPressed(Keys.A)){
            currentAngle += Math.min(Math.max(-coneAngle - currentAngle, -coneSpeed), coneSpeed);
        } else if(Gdx.input.isKeyPressed(Keys.D)){
            currentAngle += Math.min(Math.max(coneAngle - currentAngle, -coneSpeed), coneSpeed);
        } else {
            currentAngle += Math.min(Math.max(0 - currentAngle, -coneSpeed), coneSpeed);
        }

        if(Gdx.input.isKeyPressed(Keys.SPACE)){
            Vector2 dir = new Vector2(
                (float)Math.cos((currentAngle - 90) * (Math.PI / 180.f) + parent.getAngle()),
                (float)Math.sin((currentAngle - 90) * (Math.PI / 180.f) + parent.getAngle())
            );
            parent.applyForce(dir.scl(power * -2.f).add(parent.getPosition()), parent.getWorldPoint(new Vector2(getX(), getY())), true);
        }
 
        setRotation(rotationOffset + currentAngle);
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nPower: " + String.valueOf(power);
        str += "\nConeAngle: " + String.valueOf(coneAngle);
        str += "\n";
        return str;
    }
}

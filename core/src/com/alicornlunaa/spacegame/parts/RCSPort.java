package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/*
 * Mono propellant/RCS ports are decided to be fired depending on the button pressed
 * 
 * Roll can be calculated by getting the angle of the port to the ship's center of
 * mass and checking if its tangent value is positive or negative, just like on a
 * unit circle.
 */

public class RCSPort extends ShipPart {

    // Variables
    private String name;
    private String description;
    private float power;
    private float fuelUsage;

    // Constructors
    public RCSPort(Body parent, ShipState stateRef, TextureRegion region, Vector2 size, Vector2 pos, float rot, ArrayList<Vector2> attachmentPoints, String name, String description, float denstiy, float power, float fuelUsage){
        super(parent, stateRef, region, size, pos, rot, attachmentPoints);

        this.name = name;
        this.description = description;
        this.power = power;
        this.fuelUsage = fuelUsage;
    }

    // Functions
    private int getSignAtPos(){
        Vector2 center = parent.getLocalCenter();
        Vector2 posOfCenter = new Vector2(getX() - center.x, getY() - center.y);
        float tanVal = (float)Math.atan(posOfCenter.y / posOfCenter.x);
        return Math.round(Math.abs(tanVal) / tanVal);
    }

    private void verticalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentVert = new Vector2(0, direction).rotateRad(parent.getAngle());
        float adj = portDir.dot(parentVert);
        if(adj <= 0) return;

        parent.applyForceToCenter(new Vector2(portDir).scl(power * 2.f * adj).add(parent.getPosition()), true);
    }

    private void horizontalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentHori = new Vector2(direction, 0).rotateRad(parent.getAngle());
        float adj = portDir.dot(parentHori);
        if(adj <= 0) return;

        parent.applyForceToCenter(new Vector2(portDir).scl(power * 2.f * adj).add(parent.getPosition()), true);
    }

    private void rollThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        if(direction <= 0) return; // Cant thrust negative

        Vector2 parentRight = new Vector2(1, 0).rotateRad(parent.getAngle());
        float adj = Math.abs(portDir.dot(parentRight));

        parent.applyForce(new Vector2(portDir).scl(power * 2.f * adj).add(portPos), portPos, true);
    }

    @Override
    public void draw(Batch batch, float p){
        if(!stateRef.debug) return;

        batch.end();
        // s.begin(ShapeRenderer.ShapeType.Filled);
        // s.setProjectionMatrix(batch.getProjectionMatrix());
        // s.setTransformMatrix(batch.getTransformMatrix());

        // Vector2 portPos = new Vector2(getX(), getY()).rotateDeg((float)Math.toDegrees(parent.getAngle())).add(parent.getPosition());
        // Vector2 portDir = new Vector2(1, 0).rotateDeg(((float)Math.toDegrees(parent.getAngle()) + getRotation()) * getScaleX()).scl(-getScaleX(), -getScaleY());
        // Vector2 parentUp = new Vector2(stateRef.horizontal, stateRef.vertical).rotateRad(parent.getAngle());
        // float adj = portDir.dot(parentUp);

        // s.setColor(adj <= 0 ? Color.MAGENTA : Color.GREEN);
        // s.line(portPos, new Vector2(portDir).scl(50).add(portPos));
        // s.circle(portPos.x, portPos.y, 3);

        // s.setColor(Color.CORAL);
        // s.line(parent.getWorldCenter(), new Vector2(parentUp).scl(150).add(parent.getWorldCenter()));

        // s.end();
        batch.begin();
    }

    @Override
    public void act(float delta){
        super.act(delta);

        if(stateRef.rcs){
            Vector2 portPos = new Vector2(getX(), getY()).rotateDeg((float)Math.toDegrees(parent.getAngle())).add(parent.getPosition());
            Vector2 portDir = new Vector2(1, 0).rotateDeg(((float)Math.toDegrees(parent.getAngle()) + getRotation()) * getScaleX()).scl(-getScaleX(), -getScaleY());

            float compRoll = Math.min(Math.max(stateRef.roll + stateRef.artifRoll, -1), 1);

            rollThrust(delta, getSignAtPos() * compRoll, portPos, portDir);
            verticalThrust(delta, stateRef.vertical, portPos, portDir);
            horizontalThrust(delta, stateRef.horizontal, portPos, portDir);
        }
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nPower: " + String.valueOf(power);
        str += "\nFuel usage: " + String.valueOf(fuelUsage);
        str += "\n";
        return str;
    }
    
}

package com.alicornlunaa.spacegame.objects;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class Entity extends Actor implements Disposable {

    // Variables
    protected @Null Body body = null;

    // Functions
    public Body setBody(Body b){
        if(b != null){
            setPosition(b.getPosition());
            setRotation((float)Math.toDegrees(b.getAngle()));
        }

        body = b;
        return b;
    }

    public Body getBody(){
        return body;
    }
    
    /**
     * @return Transformation matrix to the center of the actor
     */
    public Matrix3 getTransform(){
        if(body != null){
            // Get up-to-date body information
            Vector2 p = body.getPosition();
            setPosition(p.x, p.y);
            setRotation((float)Math.toDegrees(body.getAngle()));
        }

        // Get matrix
        Matrix3 trans = new Matrix3();
        trans.idt();
        trans.translate(getX(), getY());
        trans.rotate(getRotation());
        trans.scale(getScaleX(), getScaleY());
        return trans;
    }

    /**
     * @return A new vector for this position
     */
    public Vector2 getPosition(){ return new Vector2(getX(), getY()); }
    public void setPosition(Vector2 v){ setX(v.x); setY(v.y); }

    public Vector2 localToWorld(Vector2 v){ return v.mul(getTransform()); }
    public Vector2 worldToLocal(Vector2 v){ return v.mul(getTransform().inv()); }

    // Overrides
    @Override
    public float getX(){
        if(body == null) return super.getX();

        super.setX(body.getPosition().x);
        return body.getPosition().x;
    }

    @Override
    public float getY(){
        if(body == null) return super.getY();

        super.setX(body.getPosition().y);
        return body.getPosition().y;
    }

    @Override
    public float getRotation(){
        if(body == null) return super.getRotation();

        float degrees = (float)Math.toDegrees(body.getAngle());
        super.setRotation(degrees);
        return degrees;
    }
    
    @Override
    public void setX(float x){
        if(body != null) body.getPosition().x = x;
        super.setX(x);
    }
    
    @Override
    public void setY(float y){
        if(body != null) body.getPosition().y = y;
        super.setY(y);
    }

    @Override
    public void setRotation(float degrees){
        if(body != null) body.setTransform(body.getPosition(), (float)Math.toRadians(degrees));
        super.setRotation(degrees);
    }

    @Override
    public void setPosition(float x, float y){
        if(body != null) body.setTransform(x, y, body.getAngle());
        super.setPosition(x, y);
    }

    @Override
    public void dispose() {}

}

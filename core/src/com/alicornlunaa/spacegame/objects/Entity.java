package com.alicornlunaa.spacegame.objects;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Entity extends Actor {
    public Matrix3 getTransform(){
        Matrix3 trans = new Matrix3();
        trans.translate(getX() + getOriginX(), getY() + getOriginY());
        trans.rotate(getRotation());
        trans.scale(getScaleX(), getScaleY());
        return trans;
    }

    public Vector2 localToWorld(Vector2 v){ return v.mul(getTransform()); }
    public Vector2 worldToLocal(Vector2 v){ return v.mul(getTransform().inv()); }
}

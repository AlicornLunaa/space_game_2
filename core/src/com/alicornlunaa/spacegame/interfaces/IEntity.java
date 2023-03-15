package com.alicornlunaa.spacegame.interfaces;

import com.badlogic.gdx.physics.box2d.World;

public interface IEntity {

    public boolean onLeftClick();
    public boolean onRightClick();

    public void update(float delta);
    public void fixedUpdate(float delta);
    public void loadBodyToWorld(World world, float newPhysScale);

}

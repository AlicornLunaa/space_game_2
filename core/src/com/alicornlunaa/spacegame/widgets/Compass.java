package com.alicornlunaa.spacegame.widgets;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Null;

/**
 * The compass will give the entity's orientation relative to the nearest sphere of influence
 */
public class Compass extends Widget {
    
    // Variables
    private @Null Entity gravityParentEnt = null;
    private @Null Entity targetEnt = null;

    private TextureRegion ballTexture;
    private TextureRegion arrowTexture;
    private TextureRegion markerTexture;

    // Constructor
    public Compass(App game){
        super();

        ballTexture = game.atlas.findRegion("ui/compass");
        arrowTexture = game.atlas.findRegion("ui/compass_arrow");
        markerTexture = game.atlas.findRegion("ui/compass_markers");

        setBounds(0, 0, ballTexture.getRegionWidth(), ballTexture.getRegionHeight());
        setOrigin(ballTexture.getRegionWidth() / 2, ballTexture.getRegionHeight() / 2);
    }

    // Functions
    public void setGravityParent(Entity e){
        gravityParentEnt = e;
    }

    public void setTarget(Entity e){
        targetEnt = e;
    }

    // Overrides
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);

        // Draw the ball with the angle relative to the target and parent
        float theta = 0;
        if(targetEnt != null && gravityParentEnt != null){
            Vector2 targetToParent = targetEnt.getPosition().sub(gravityParentEnt.getPosition()).nor();
            theta = targetToParent.angleDeg() - 90;
        }
        batch.draw(ballTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), theta);

        // Draw the ball with the angle relative to the target and parent
        theta = 0;
        if(targetEnt != null){
            theta = targetEnt.getRotation();
        }
        batch.draw(
            arrowTexture,
            getX() + getOriginX() - arrowTexture.getRegionWidth() / 2,
            getY() + getOriginY() - arrowTexture.getRegionHeight() / 2,
            arrowTexture.getRegionWidth() / 2,
            arrowTexture.getRegionHeight() / 2,
            arrowTexture.getRegionWidth(),
            arrowTexture.getRegionHeight(),
            getScaleX(),
            getScaleY(),
            theta
        );

        // Draw the ball with the angle relative to the target velocity
        theta = 0;
        if(targetEnt != null){
            if(targetEnt.getDriving() != null){
                theta = targetEnt.getDriving().getBody().getLinearVelocity().angleDeg() - 90;
            } else {
                theta = targetEnt.getBody().getLinearVelocity().angleDeg() - 90;
            }
        }
        batch.draw(
            markerTexture,
            getX() + getOriginX() - markerTexture.getRegionWidth() / 2,
            getY() + getOriginY() - markerTexture.getRegionHeight() / 2,
            markerTexture.getRegionWidth() / 2,
            markerTexture.getRegionHeight() / 2,
            markerTexture.getRegionWidth(),
            markerTexture.getRegionHeight(),
            getScaleX(),
            getScaleY(),
            theta
        );
    }

    @Override
    public float getPrefWidth(){
        return 60;
    }

    @Override
    public float getPrefHeight(){
        return 60;
    }

}

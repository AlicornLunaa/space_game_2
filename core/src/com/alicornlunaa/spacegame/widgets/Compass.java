package com.alicornlunaa.spacegame.widgets;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.badlogic.gdx.graphics.Color;
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
    private Universe universe;
    private @Null BaseEntity targetEnt = null;

    private TextureRegion ballTexture;
    private TextureRegion arrowTexture;
    private TextureRegion markerTexture;
    private TextureRegion backgroundTexture;

    // Constructor
    public Compass(App game){
        super();

        ballTexture = game.atlas.findRegion("ui/compass");
        arrowTexture = game.atlas.findRegion("ui/compass_arrow");
        markerTexture = game.atlas.findRegion("ui/compass_markers");
        backgroundTexture = game.atlas.findRegion("ui/compass_background");

        setBounds(0, 0, ballTexture.getRegionWidth(), ballTexture.getRegionHeight());
        setOrigin(ballTexture.getRegionWidth() / 2, ballTexture.getRegionHeight() / 2);
    }

    // Functions
    public void setUniverse(Universe universe){
        this.universe = universe;
    }

    public void setTarget(BaseEntity e){
        targetEnt = e;
    }

    // Overrides
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);

        // Background
        batch.setColor(Color.WHITE);
        batch.draw(
            backgroundTexture,
            getX() + getOriginX() - backgroundTexture.getRegionWidth() / 2,
            getY() + getOriginY() - backgroundTexture.getRegionHeight() / 2,
            backgroundTexture.getRegionWidth() / 2,
            backgroundTexture.getRegionHeight() / 2,
            backgroundTexture.getRegionWidth(),
            backgroundTexture.getRegionHeight(),
            getScaleX(),
            getScaleY(),
            0
        );

        // Draw the ball with the angle relative to the target and parent
        float theta = 0;
        if(universe != null){
            BaseEntity gravityParentEnt = universe.getParentCelestial(targetEnt);

            if(targetEnt != null && gravityParentEnt != null){
                Vector2 targetToParent = targetEnt.getComponent(TransformComponent.class).position.cpy().sub(gravityParentEnt.getComponent(TransformComponent.class).position).nor();
                theta = targetToParent.angleDeg() - 90;
            }
        }
        batch.draw(ballTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), theta);

        // Draw the ball with the angle relative to the target and parent
        theta = 0;
        if(targetEnt != null){
            theta = (float)Math.toDegrees(targetEnt.getRotation());
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
            // if(targetEnt.getDriving() != null){
            //     theta = targetEnt.getDriving().getBody().getLinearVelocity().angleDeg() - 90;
            // } else {
                theta = targetEnt.getComponent(BodyComponent.class).body.getLinearVelocity().angleDeg() - 90;
            // }
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

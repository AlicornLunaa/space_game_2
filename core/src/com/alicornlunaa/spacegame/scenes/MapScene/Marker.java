package com.alicornlunaa.spacegame.scenes.MapScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class Marker extends Actor {

    private TextureRegion region;
    private Label hoverLabel;

    public Marker(final App game, Celestial parent, Vector2 position, TextureRegion texture, float size, String text){
        super();

        Matrix3 universalSpaceTransform = parent.getUniverseSpaceTransform();
        Vector2 universalSpacePosition = position.cpy().scl(Constants.PPM).mul(universalSpaceTransform);

        setSize(size, size);
        setOrigin(getWidth() / 2, 0);
        setPosition(universalSpacePosition.x - getWidth() / 2, universalSpacePosition.y);

        region = texture;

        hoverLabel = new Label(text, game.skin);
        hoverLabel.setVisible(false);
        hoverLabel.setPosition(getX(), getY() + 2);
        hoverLabel.setFontScale(size / 20);
        hoverLabel.setAlignment(Align.topLeft);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                hoverLabel.setVisible(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor){
                hoverLabel.setVisible(false);
            }
        });
    }

    @Override
    public void draw(Batch b, float a){
        b.draw(
            region,
            getX(),
            getY(),
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            getRotation()
        );

        if(hoverLabel.isVisible())
            hoverLabel.draw(b, a);
    }

}

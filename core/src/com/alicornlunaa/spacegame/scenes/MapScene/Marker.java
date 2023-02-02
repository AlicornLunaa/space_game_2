package com.alicornlunaa.spacegame.scenes.MapScene;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Marker extends Actor {

    private Label hoverLabel;

    public Marker(final App game, TextureRegion texture, Runnable displayTextFunc){
        super();

        hoverLabel = new Label("NONE", game.skin);
        hoverLabel.setVisible(false);
        hoverLabel.setPosition(getX(), getY());

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

}

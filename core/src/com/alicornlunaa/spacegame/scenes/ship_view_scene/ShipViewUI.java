package com.alicornlunaa.spacegame.scenes.ship_view_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.vfx.transitions.FadeTransitionScene;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.stripe.scenecomposer.SceneComposerStageBuilder;

public class ShipViewUI extends Stage {

    // Variables
    private final Screen previousScreen;

    // Constructor
    public ShipViewUI(final App game){
        super(new ScreenViewport());
        previousScreen = game.getScreen();

        // Initialize UI
        SceneComposerStageBuilder builder = new SceneComposerStageBuilder();
        builder.build(this, game.skin, Gdx.files.internal("layouts/ship_hud.json"));

        getRoot().findActor("backbutton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor a){
                game.vfxManager.add(new FadeTransitionScene(game, game.getScreen(), previousScreen, 0.15f));
            }
        });
    }
    
}

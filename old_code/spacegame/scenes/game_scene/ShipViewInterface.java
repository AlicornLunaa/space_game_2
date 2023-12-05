package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.space_game.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.stripe.scenecomposer.SceneComposerStageBuilder;

public class ShipViewInterface extends Stage {

    // Constructor
    public ShipViewInterface(final App game){
        super(new ScreenViewport());

        // Initialize UI
        SceneComposerStageBuilder builder = new SceneComposerStageBuilder();
        builder.build(this, game.skin, Gdx.files.internal("layouts/ship_hud.json"));

        getRoot().findActor("backbutton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor a){
                // game.vfxManager.add(new FadeTransitionScene(game, game.getScreen(), previousScreen, 0.15f));
                game.gameScene.closeShipView();
            }
        });
    }
    
}

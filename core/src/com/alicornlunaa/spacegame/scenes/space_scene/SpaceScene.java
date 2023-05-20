package com.alicornlunaa.spacegame.scenes.space_scene;

import com.alicornlunaa.selene_engine.scenes.GameScene;
import com.alicornlunaa.spacegame.App;

public class SpaceScene extends GameScene<SpacePanel, SpaceUIPanel> {

    // Constructor
    public SpaceScene(final App game){
        super(game, new SpacePanel(game), new SpaceUIPanel(game));

        // Initialize UI
        getUI().shipCompass.setTarget(getContent().ship);
        getUI().shipCompass.setUniverse(game.universe);
    }

}

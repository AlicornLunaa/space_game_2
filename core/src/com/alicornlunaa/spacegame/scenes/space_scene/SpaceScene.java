package com.alicornlunaa.spacegame.scenes.space_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.scenes.GameScene;

public class SpaceScene extends GameScene<SpacePanel, SpaceUIPanel> {

    // Constructor
    public SpaceScene(final App game){
        super(game, new SpacePanel(game), new SpaceUIPanel(game));

        // Initialize UI
        getUI().shipCompass.setTarget(getContent().ship);
        getUI().shipCompass.setUniverse(game.universe);
    }

}

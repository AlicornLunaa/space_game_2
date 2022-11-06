package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.PlanetScene;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PlanetUIPanel extends Stage {
    
    // Variables
    final App game;
    
    private Label fpsCounter;
    private Label posLabel;

    // Constructor
    public PlanetUIPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        fpsCounter = new Label("FPS: N/A", game.skin);
        fpsCounter.setPosition(20, getHeight() - 60);
        this.addActor(fpsCounter);

        posLabel = new Label("Pos: N/A", game.skin);
        posLabel.setPosition(20, getHeight() - 90);
        this.addActor(posLabel);
    }

    // Functions
    @Override
    public void draw(){
        PlanetPanel planetPanel = ((PlanetScene)game.getScreen()).planetPanel;

        fpsCounter.setText((int)(1 / Gdx.graphics.getDeltaTime()));
        posLabel.setText(planetPanel.player.getPosition().toString());

        super.draw();
    }

}

package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.ConsoleScene;
import com.alicornlunaa.spacegame.scenes.PauseScene;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PlanetUIPanel extends Stage {
    
    // Variables
    final App game;

    private Label fpsCounter;
    private Label posLabel;

    // Constructor
    public PlanetUIPanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        fpsCounter = new Label("FPS: N/A", game.skin);
        fpsCounter.setPosition(20, getHeight() - 60);
        this.addActor(fpsCounter);

        posLabel = new Label("Pos: N/A", game.skin);
        posLabel.setPosition(20, getHeight() - 90);
        this.addActor(posLabel);

        // Controls
        this.addListener(new InputListener(){
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game, game.planetScene, (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(new ConsoleScene(game, game.planetScene, (int)getWidth(), (int)getHeight()));
                    return true;
                }

                return false;
            }
        });
    }

    // Functions
    @Override
    public void draw(){
        PlanetPanel planetPanel = game.planetScene.planetPanel;

        fpsCounter.setText((int)(1 / Gdx.graphics.getDeltaTime()));
        posLabel.setText(planetPanel.player.getPosition().toString());

        super.draw();
    }

}

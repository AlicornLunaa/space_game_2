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

    // Constructor
    public PlanetUIPanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        fpsCounter = new Label("FPS: N/A", game.skin);
        fpsCounter.setPosition(20, getHeight() - 120);
        this.addActor(fpsCounter);

        // Controls
        this.addListener(new InputListener(){
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game,  (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(new ConsoleScene(game, (int)getWidth(), (int)getHeight()));
                    return true;
                }

                return false;
            }
        });
    }

    // Functions
    @Override
    public void draw(){
        fpsCounter.setText((int)(1 / Gdx.graphics.getDeltaTime()));
        super.draw();
    }

}

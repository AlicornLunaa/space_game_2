package com.alicornlunaa.spacegame.scenes.planet_scene;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.scenes.transitions.PauseScene;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.widgets.ConsoleWidget;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.kotcrab.vis.ui.widget.VisWindow;

public class PlanetUIPanel extends Stage {
    
    // Variables
    final App game;

    // Constructor
    public PlanetUIPanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        // Controls
        this.addListener(new InputListener(){
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game,  (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    VisWindow console = new ConsoleWidget(game).fadeIn(0.15f);
                    addActor(console);
                    setKeyboardFocus(console);
                    return true;
                }

                return false;
            }
        });
    }

    // Functions
    @Override
    public void draw(){
        super.draw();
    }

}

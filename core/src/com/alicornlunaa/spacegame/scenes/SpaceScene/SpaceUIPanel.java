package com.alicornlunaa.spacegame.scenes.SpaceScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.scenes.MapScene.MapScene;
import com.alicornlunaa.spacegame.scenes.ShipViewScene.ShipViewScene;
import com.alicornlunaa.spacegame.scenes.Transitions.FadeTransitionScene;
import com.alicornlunaa.spacegame.scenes.Transitions.PauseScene;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.widgets.Compass;
import com.alicornlunaa.spacegame.widgets.ConsoleWidget;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.ray3k.stripe.scenecomposer.SceneComposerStageBuilder;

public class SpaceUIPanel extends Stage {
    
    // Variables
    private final App game;
    
    private TextButton sasBtn;
    private TextButton rcsBtn;
    private TextButton shipViewButton;
    private ProgressBar throttleBar;
    private Slider warpSlider;
    public Compass shipCompass;

    private Label positionLabel;
    private Label velocityLabel;
    private Label fpsCounter;

    // Constructor
    @SuppressWarnings("unchecked")
    public SpaceUIPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        // Load UI
        SceneComposerStageBuilder builder = new SceneComposerStageBuilder();
        builder.build(this, game.skin, Gdx.files.internal("layouts/space_hud.json"));

        // Create UI logic
        sasBtn = getRoot().findActor("sasbutton");
        rcsBtn = getRoot().findActor("rcsbutton");
        shipViewButton = getRoot().findActor("shipbutton");
        throttleBar = getRoot().findActor("throttlebar");
        warpSlider = getRoot().findActor("warpslider");

        warpSlider.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.universe.setTimewarp(warpSlider.getValue());
            }
        });

        sasBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.spaceScene.spacePanel.ship.state.sas = !game.spaceScene.spacePanel.ship.state.sas;
            }
        });

        rcsBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.spaceScene.spacePanel.ship.state.rcs = !game.spaceScene.spacePanel.ship.state.rcs;
            }
        });

        shipViewButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(!(game.player.getDriving() instanceof Ship)) return;
                game.setScreen(new FadeTransitionScene(game, game.getScreen(), new ShipViewScene(game, (Ship)game.player.getDriving()), 0.15f));
            }
        });

        // Navigation compass
        shipCompass = new Compass(game);
        shipCompass.setColor(Color.WHITE);
        ((Container<Compass>)getRoot().findActor("Compass")).setActor(shipCompass);

        // Labels
        positionLabel = new Label("Pos: N/a", game.skin);
        positionLabel.setPosition(20, getHeight() - 65);
        this.addActor(positionLabel);
        velocityLabel = new Label("Vel: N/a", game.skin);
        velocityLabel.setPosition(20, getHeight() - 90);
        this.addActor(velocityLabel);
        fpsCounter = new Label("FPS: N/A", game.skin);
        fpsCounter.setPosition(20, getHeight() - 120);
        this.addActor(fpsCounter);

        // Controls
        this.addListener(new InputListener(){
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game, (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    VisWindow console = new ConsoleWidget(game).fadeIn(0.15f);
                    addActor(console);
                    setKeyboardFocus(console);
                    return true;
                } else if(keycode == ControlSchema.DEBUG_TOGGLE){
                    game.spaceScene.spacePanel.ship.state.debug = !game.spaceScene.spacePanel.ship.state.debug;
                    return true;
                } else if(keycode == ControlSchema.SHIP_TOGGLE_RCS){
                    game.spaceScene.spacePanel.ship.state.rcs = !game.spaceScene.spacePanel.ship.state.rcs;
                    return true;
                } else if(keycode == ControlSchema.SHIP_TOGGLE_SAS){
                    game.spaceScene.spacePanel.ship.state.sas = !game.spaceScene.spacePanel.ship.state.sas;
                    return true;
                } else if(keycode == ControlSchema.SHIP_FULL_THROTTLE){
                    game.spaceScene.spacePanel.ship.state.throttle = 1;
                    return true;
                } else if(keycode == ControlSchema.SHIP_NO_THROTTLE){
                    game.spaceScene.spacePanel.ship.state.throttle = 0;
                    return true;
                } else if(keycode == ControlSchema.OPEN_ORBITAL_MAP){
                    game.setScreen(new MapScene(game, game.getScreen(), game.player));
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

        SpacePanel spacePanel = ((SpaceScene)game.spaceScene).spacePanel;
        sasBtn.setColor(spacePanel.ship.state.sas ? Color.GREEN : Color.RED);
        rcsBtn.setColor(spacePanel.ship.state.rcs ? Color.GREEN : Color.RED);
        throttleBar.setValue(spacePanel.ship.state.throttle);

        positionLabel.setText(spacePanel.ship.getBody().getWorldCenter().toString());
        velocityLabel.setText(spacePanel.ship.getBody().getLinearVelocity().toString());
        fpsCounter.setText((int)(1 / Gdx.graphics.getDeltaTime()));
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

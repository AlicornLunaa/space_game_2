package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.transitions.PauseScene;
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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.ray3k.stripe.scenecomposer.SceneComposerStageBuilder;

public class SpaceInterface extends Stage {
    
    // Variables
    private final App game;
    
    private TextButton sasBtn;
    private TextButton rcsBtn;
    private TextButton shipViewButton;
    private ProgressBar throttleBar;
    private Slider warpSlider;
    public Compass shipCompass;
    public TextButton pilotButton;

    private Label positionLabel;
    private Label velocityLabel;
    private Label fpsCounter;

    // Constructor
    @SuppressWarnings("unchecked")
    public SpaceInterface(final App game){
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
                game.gameScene.universe.setTimewarp(warpSlider.getValue());
            }
        });

        sasBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.gameScene.spacePanel.ship.getState().sas = !game.gameScene.spacePanel.ship.getState().sas;
            }
        });

        rcsBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.gameScene.spacePanel.ship.getState().rcs = !game.gameScene.spacePanel.ship.getState().rcs;
            }
        });

        shipViewButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(!game.gameScene.player.isDriving()) return;
                // game.vfxManager.add(new FadeTransitionScene(game, game.getScreen(), new ShipViewScene(game, game.gameScene.spacePanel.ship), 0.15f));
                game.gameScene.openShipView(game.gameScene.spacePanel.ship);
            }
        });

        // Pilot button
        pilotButton = new TextButton("PILOT", game.skin);
        pilotButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(game.gameScene.player.isDriving()){
                    DriveableEntity ship = game.gameScene.spacePanel.ship;
                    ship.stopDriving();

                    if(ship.hasComponent(BodyComponent.class)){
                        TransformComponent shipTransform = ship.getComponent(TransformComponent.class);
                        BodyComponent shipBodyComponent = ship.getComponent(BodyComponent.class);
                        game.gameScene.player.bodyComponent.setWorld(shipBodyComponent.world);
                        game.gameScene.player.transform.velocity.set(shipTransform.velocity);
                    }
                } else {
                    DriveableEntity ship = game.gameScene.spacePanel.ship;
                    ship.drive(game.gameScene.player);

                    if(ship.hasComponent(BodyComponent.class)){
                        BodyComponent shipBodyComponent = ship.getComponent(BodyComponent.class);
                        game.gameScene.player.bodyComponent.setWorld(shipBodyComponent.world);
                    }
                }
            }
        });
        this.addActor(pilotButton);

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
                    game.gameScene.spacePanel.ship.getState().debug = !game.gameScene.spacePanel.ship.getState().debug;
                    return true;
                } else if(keycode == ControlSchema.OPEN_ORBITAL_MAP){
                    game.gameScene.openMap();
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

        SpacePanel spacePanel = game.gameScene.spacePanel;
        sasBtn.setColor(spacePanel.ship.getState().sas ? Color.GREEN : Color.RED);
        rcsBtn.setColor(spacePanel.ship.getState().rcs ? Color.GREEN : Color.RED);
        throttleBar.setValue(spacePanel.ship.getState().throttle);

        positionLabel.setText(game.gameScene.player.getPosition().toString());
        velocityLabel.setText(game.gameScene.player.getVelocity().toString());
        fpsCounter.setText((int)(1 / Gdx.graphics.getDeltaTime()));
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

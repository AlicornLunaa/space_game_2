package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.ConsoleScene;
import com.alicornlunaa.spacegame.scenes.FadeTransitionScene;
import com.alicornlunaa.spacegame.scenes.MapScene;
import com.alicornlunaa.spacegame.scenes.SpaceScene;
import com.alicornlunaa.spacegame.scenes.PauseScene;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.widgets.Compass;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SpaceUIPanel extends Stage {
    
    // Variables
    private final App game;
    
    private TextButton sasBtn;
    private TextButton rcsBtn;
    private ProgressBar throttleBar;

    public Compass shipCompass;

    private Label positionLabel;
    private Label velocityLabel;

    // Constructor
    public SpaceUIPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        // Initialize UI elements
        Table tbl = new Table(game.skin);
        tbl.setFillParent(true);
        this.addActor(tbl);

        // Navigation compass
        shipCompass = new Compass(game);
        shipCompass.setPosition(getWidth() / 2.0f - shipCompass.getOriginX(), 0);
        this.addActor(shipCompass);

        // Labels
        positionLabel = new Label("Pos: N/a", game.skin);
        positionLabel.setPosition(20, getHeight() - 60);
        this.addActor(positionLabel);
        velocityLabel = new Label("Vel: N/a", game.skin);
        velocityLabel.setPosition(20, getHeight() - 90);
        this.addActor(velocityLabel);

        // Icon elements
        tbl.row().expandX().fillX().pad(10).right();
        tbl.add(new Table()).width(300).colspan(6);

        sasBtn = new TextButton("SAS", game.skin);
        sasBtn.setPosition(getWidth() / 2.0f - 128 - 5, 5);
        sasBtn.setSize(64, 32);
        sasBtn.setColor(Color.RED);
        this.addActor(sasBtn);

        rcsBtn = new TextButton("RCS", game.skin);
        rcsBtn.setPosition(getWidth() / 2.0f + shipCompass.getOriginX() + 5, 5);
        rcsBtn.setSize(64, 32);
        rcsBtn.setColor(Color.RED);
        this.addActor(rcsBtn);

        TextButton editBtn = new TextButton("Editor", game.skin);
        editBtn.setPosition(640 - 192, 480 - 32);
        editBtn.setSize(128, 32);
        editBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.setScreen(new FadeTransitionScene(game, game.spaceScene, game.editorScene, 0.15f));
            }
        });
        tbl.add(editBtn).right().maxWidth(84);
        tbl.row().expand().fill().left();

        Table hud = new Table(game.skin);
        hud.setFillParent(true);
        hud.row().maxWidth(32);
        throttleBar = new ProgressBar(0, 1, 0.01f, true, game.skin);
        hud.add(throttleBar).expand().right().bottom().pad(20).minHeight(256);

        tbl.add(hud).colspan(9);

        // Controls
        this.addListener(new InputListener(){
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game, game.spaceScene, (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(new ConsoleScene(game, game.spaceScene, (int)getWidth(), (int)getHeight()));
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
                } else if(keycode == ControlSchema.SHIP_NO_THROTTLE){
                    game.spaceScene.spacePanel.ship.state.throttle = 0;
                } else if(keycode == ControlSchema.OPEN_ORBITAL_MAP){
                    game.setScreen(new MapScene(game, game.getScreen(), game.spaceScene.spacePanel.player));
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
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

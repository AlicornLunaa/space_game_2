package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.ConsoleScene;
import com.alicornlunaa.spacegame.scenes.SpaceScene;
import com.alicornlunaa.spacegame.scenes.PauseScene;
import com.alicornlunaa.spacegame.util.ControlSchema;
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

public class GameUIPanel extends Stage {
    
    // Variables
    private final App game;
    
    private TextButton sasBtn;
    private TextButton rcsBtn;
    private ProgressBar throttleBar;

    private Label positionLabel;
    private Label velocityLabel;

    // Constructor
    public GameUIPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        // Initialize UI elements
        Table tbl = new Table(game.skin);
        tbl.setFillParent(true);
        this.addActor(tbl);

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
        sasBtn.setPosition(640 - 64, 480 - 32);
        sasBtn.setSize(64, 32);
        sasBtn.setColor(Color.RED);
        tbl.add(sasBtn).right().maxWidth(64);

        rcsBtn = new TextButton("RCS", game.skin);
        rcsBtn.setPosition(640 - 128, 480 - 32);
        rcsBtn.setSize(64, 32);
        rcsBtn.setColor(Color.RED);
        tbl.add(rcsBtn).right().maxWidth(64);

        TextButton editBtn = new TextButton("Editor", game.skin);
        editBtn.setPosition(640 - 192, 480 - 32);
        editBtn.setSize(64, 32);
        editBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                game.setScreen(game.editorScene);
            }
        });
        tbl.add(editBtn).right().maxWidth(64);
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
                    game.setScreen(new PauseScene(game, game.gameScene, (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(new ConsoleScene(game, game.gameScene, (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.DEBUG_TOGGLE){
                    game.gameScene.gamePanel.ship.state.debug = !game.gameScene.gamePanel.ship.state.debug;
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

        GamePanel gamePanel = ((SpaceScene)game.gameScene).gamePanel;
        sasBtn.setColor(gamePanel.ship.state.sas ? Color.GREEN : Color.RED);
        rcsBtn.setColor(gamePanel.ship.state.rcs ? Color.GREEN : Color.RED);
        throttleBar.setValue(gamePanel.ship.state.throttle);

        positionLabel.setText(gamePanel.ship.getBody().getWorldCenter().toString());
        velocityLabel.setText(gamePanel.ship.getBody().getLinearVelocity().toString());
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

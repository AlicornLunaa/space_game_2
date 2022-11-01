package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.GameScene;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameUIPanel extends Stage {
    
    // Variables
    private final App game;
    
    private TextButton sasBtn;
    private TextButton rcsBtn;

    // Constructor
    public GameUIPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        // Initialize UI elements
        Table tbl = new Table(game.skin);
        tbl.setFillParent(true);
        this.addActor(tbl);

        // Icon elements
        sasBtn = new TextButton("SAS", game.skin);
        sasBtn.setPosition(640 - 64, 480 - 32);
        sasBtn.setSize(64, 32);
        sasBtn.setColor(Color.RED);
        this.addActor(sasBtn);

        rcsBtn = new TextButton("RCS", game.skin);
        rcsBtn.setPosition(640 - 128, 480 - 32);
        rcsBtn.setSize(64, 32);
        rcsBtn.setColor(Color.RED);
        this.addActor(rcsBtn);

        TextButton editBtn = new TextButton("Editor", game.skin);
        editBtn.setPosition(640 - 192, 480 - 32);
        editBtn.setSize(64, 32);
        editBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){

            }
        });
        this.addActor(editBtn);
    }

    // Functions
    @Override
    public void draw(){
        super.draw();

        GamePanel gamePanel = ((GameScene)game.getScreen()).gamePanel;
        sasBtn.setColor(gamePanel.sas ? Color.GREEN : Color.RED);
        rcsBtn.setColor(gamePanel.rcs ? Color.GREEN : Color.RED);
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

package com.alicornlunaa.spacegame.widgets;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.scenes.dev_kit.DevKit;
import com.alicornlunaa.spacegame.util.state_management.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

public class ConsoleWidget extends VisWindow {

    // Variables
    private final App game;
    private final InputProcessor oldInput;

    private VisScrollPane historyScroll;
    private VerticalGroup history;
    private VisTextField commandBar;
    private VisTextButton sendBtn;
    
    // Private functions
    private void handleCmd(String cmd){
        String[] args = cmd.split("\\s+");
        
        if(args[0].equals("loadship")){
            game.gameScene.spacePanel.ship.load(args[1]);
        } else if(args[0].equals("settimewarp")){
            game.gameScene.universe.setTimewarp(Float.parseFloat(args[1]));
        } else if(args[0].equals("devkit")){
            game.setScreen(new DevKit(game));
        } else if(args[0].equals("save")){
            SaveManager.save(game, "dev_world");
        }

        commandBar.setText("");
        history.addActor(new VisLabel(cmd));
        historyScroll.setScrollPercentY(100);
    }

    // Constructor
    public ConsoleWidget(final App game){
        super("Console", true);
        this.game = game;
        this.oldInput = Gdx.input.getInputProcessor();
        setSize(640, 360);
        closeOnEscape();
        addCloseButton();
        setModal(true);
        center();
        fadeIn(0.15f);

        history = new VerticalGroup().left().columnLeft();
        historyScroll = new VisScrollPane(history);
        commandBar = new VisTextField("Command");
        sendBtn = new VisTextButton("Execute");

        this.add(historyScroll).expand().fill().colspan(2).left().row();
        this.add(commandBar).expandX().fillX();
        this.add(sendBtn).right().padLeft(2);

        sendBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                handleCmd(commandBar.getText());
            }
        });

        commandBar.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.ENTER) handleCmd(commandBar.getText());
                return true;
            }
        });
    }

    // Functions
    @Override
    public boolean remove(){
        Gdx.input.setInputProcessor(oldInput);
        return super.remove();
    }
    
}

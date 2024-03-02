package com.alicornlunaa.space_game.widgets;

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
    private VisScrollPane historyScroll;
    private VerticalGroup history;
    private VisTextField commandBar;
    private VisTextButton sendBtn;
    
    // Private functions
    private void handleCmd(String cmd){
        // String[] args = cmd.split("\\s+");
        
        // if(args[0].equals("loadship")){
        //     game.gameScene.ship.load(args[1]);
        // } else if(args[0].equals("settimewarp")){
        //     game.gameScene.universe.setTimewarp(Float.parseFloat(args[1]));
        // } else if(args[0].equals("devkit")){
        //     game.setScreen(new DevKit(game));
        // } else if(args[0].equals("save")){
        //     SaveManager.save(game, "dev_world");
        // }

        commandBar.setText("");
        history.addActor(new VisLabel(cmd));
        historyScroll.setScrollPercentY(100);
    }

    // Constructor
    public ConsoleWidget(){
        super("Console", true);
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
}

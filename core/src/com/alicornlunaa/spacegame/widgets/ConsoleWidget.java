package com.alicornlunaa.spacegame.widgets;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.alicornlunaa.spacegame.scenes.DevEditors.PartEditor;
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

    private VisScrollPane historyScroll;
    private VerticalGroup history;
    private VisTextField commandBar;
    private VisTextButton sendBtn;
    
    // Private functions
    private void handleCmd(String cmd){
        String[] args = cmd.split("\\s+");
        
        if(args[0].equals("load_ship")){
            game.spaceScene.spacePanel.ship.load(args[1]);
        } else if(args[0].equals("set_pos")){
            game.spaceScene.spacePanel.ship.setPosition(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else if(args[0].equals("set_rot")){
            game.spaceScene.spacePanel.ship.setRotation(Integer.parseInt(args[1]));
        } else if(args[0].equals("orbit")){
            game.spaceScene.spacePanel.ship.setPosition(Integer.parseInt(args[1]), 0);
            game.spaceScene.spacePanel.universe.createEntityOrbit(game.spaceScene.spacePanel.ship);
        } else if(args[0].equals("set_timewarp")){
            game.spaceScene.spacePanel.universe.setTimewarp(Float.parseFloat(args[1]));
        } else if(args[0].equals("reload_shaders")){
            ((Star)game.spaceScene.spacePanel.universe.getCelestial(0)).reloadShaders();
        } else if(args[0].equals("part_editor")){
            game.setScreen(new PartEditor(game));
        }

        commandBar.setText("");
        history.addActor(new VisLabel(cmd));
        historyScroll.setScrollPercentY(100);
    }

    // Constructor
    public ConsoleWidget(final App game){
        super("Console", true);
        this.game = game;
        setSize(640, 360);
        closeOnEscape();
        addCloseButton();

        addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                return true;
            }
        });

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

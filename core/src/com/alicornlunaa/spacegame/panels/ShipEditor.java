package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/*
 * The ShipEditor class is a stage used to render a window used to create
 * and edit 2d ships.
 */
public class ShipEditor extends Stage {
    // Variables
    private InputProcessor oldProcessor;

    private String selectedCategory;

    private Table ui;
    private EditorPane editor;

    // Constructor
    public ShipEditor(final Assets manager, final ArrayList<Stage> stages, final Skin skin, final PartManager partManager){
        super(new ScreenViewport());
        float scale = ControlSchema.GUI_SCALE;

        oldProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(this);

        selectedCategory = "AERO";

        ui = new Table(skin);
        ui.setFillParent(true);
        this.addActor(ui);

        // Interface members
        TextField nameBar = new TextField("NAME", skin);
        TextButton saveButton = new TextButton("Save", skin);
        TextButton closeButton = new TextButton("Close", skin);

        Table editorTable = new Table(skin);
        VerticalGroup categoryGroup = new VerticalGroup();
        ScrollPane categoryScroll = new ScrollPane(categoryGroup);
        VerticalGroup partsVertical = new VerticalGroup();
        ScrollPane partsScroll = new ScrollPane(partsVertical);
        editor = new EditorPane(manager, skin);
        
        // Interface layout
        ui.row().expandX().fillX().pad(20);
        ui.add(nameBar).width(300 * scale).left();
        ui.add(saveButton).right().maxWidth(64 * scale);
        ui.add(closeButton).right().maxWidth(64 * scale);

        ui.row().expand().fill().colspan(3);
        ui.add(editorTable);
        editorTable.row().expandY().fillY().left();
        editorTable.add(categoryScroll).prefWidth(64 * scale);
        for(final String entry : partManager.getPartsList().keySet()) {
            // Each entry is a category
            TextButton btn = new TextButton(entry, skin);

            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor){
                    ((ShipEditor)event.getStage()).selectedCategory = entry;
                }
            });
            
            categoryGroup.addActor(btn);
        }
        categoryGroup.expand().fill();

        int count = 0;
        for(final String objKey : partManager.getPartsList().get(selectedCategory).keySet()) {
            if(count % 3 == 0){
                partsVertical.addActor(new HorizontalGroup());
                ((HorizontalGroup)partsVertical.getChild(count / 3)).expand().fill();
            }

            // Each entry is a category
            TextButton btn = new TextButton(partManager.get(selectedCategory, objKey).getString("name"), skin);

            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor){
                    //! SPAWN PART HERE
                }
            });

            ((HorizontalGroup)partsVertical.getChild(count / 3)).addActor(btn);
            count++;
        }
        partsVertical.expand().fill();
        
        editorTable.add(partsVertical).prefWidth(128 * scale);
        editorTable.add(editor).expandX().fillX().center();

        // Interface functions
        saveButton.setColor(Color.CYAN);
        saveButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                //! Create ship saver
            }
        });

        closeButton.setColor(Color.RED);
        closeButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                stages.get(stages.size() - 1).dispose();
                stages.remove(stages.size() - 1);
            }
        });

        this.setDebugAll(true);
    }

    // Functions
    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

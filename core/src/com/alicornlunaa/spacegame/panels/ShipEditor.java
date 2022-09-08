package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

    private final Skin skin;
    private Table ui;

    // Constructor
    public ShipEditor(final ArrayList<Stage> stages, final Skin skin){
        super(new ScreenViewport());
        float scale = ControlSchema.GUI_SCALE;

        oldProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(this);

        selectedCategory = Constants.PART_CATEGORIES.get(0);

        this.skin = skin;
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
        TextButton testBtn1 = new TextButton("1", skin);
        TextButton testBtn2 = new TextButton("2", skin);
        TextButton testBtn3 = new TextButton("3", skin);
        TextButton testBtn4 = new TextButton("4", skin);
        Table partsTable = new Table(skin);
        EditorPane editorView = new EditorPane();
        
        // Interface layout
        ui.row().expandX().fillX().pad(20);
        ui.add(nameBar).width(300 * scale).left();
        ui.add(saveButton).right().maxWidth(64 * scale);
        ui.add(closeButton).right().maxWidth(64 * scale);

        ui.row().expand().fill().colspan(3);
        ui.add(editorTable);
        editorTable.row().expandY().fillY().left();
        editorTable.add(categoryScroll).prefWidth(64 * scale);
        categoryGroup.addActor(testBtn1);
        categoryGroup.addActor(testBtn2);
        categoryGroup.addActor(testBtn3);
        categoryGroup.addActor(testBtn4);
        categoryGroup.expand().fill();

        partsTable.row().expand().fill().top().maxHeight(64 * scale);
        partsTable.add(new Label("A", skin));
        partsTable.add(new Label("A", skin));
        partsTable.add(new Label("A", skin));
        
        editorTable.add(partsTable).prefWidth(128 * scale);
        editorTable.add(editorView).expandX().fillX();

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

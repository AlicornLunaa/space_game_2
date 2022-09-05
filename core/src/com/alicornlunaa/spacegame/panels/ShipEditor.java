package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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

    private final Skin skin;
    private Table ui;
    private TextButton closeButton;
    private TextButton saveButton;
    private TextField nameBar;
    private ScrollPane categoriesScroll;
    private VerticalGroup categoriesList;
    private ScrollPane partsScroll;
    private ArrayList<VerticalGroup> partsLists;

    // Constructor
    public ShipEditor(final ArrayList<Stage> stages, final Skin skin){
        super(new ScreenViewport());
        float scale = ControlSchema.GUI_SCALE;

        oldProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(this);

        selectedCategory = Constants.PART_CATEGORIES.get(0);

        this.skin = skin;
        ui = new Table();
        ui.setFillParent(true);
        ui.setDebug(true);
        ui.top();
        this.addActor(ui);

        nameBar = new TextField("NAME", skin);
        ui.add(nameBar).expandX().fillX().pad(10);

        saveButton = new TextButton("Save", this.skin);
        saveButton.setColor(Color.CYAN);
        saveButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                
            }
        });
        ui.add(saveButton).width(64 * scale).right();

        closeButton = new TextButton("Close", this.skin);
        closeButton.setColor(Color.RED);
        closeButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                stages.get(stages.size() - 1).dispose();
                stages.remove(stages.size() - 1);
            }
        });
        ui.add(closeButton).width(64 * scale).right();

        ui.row();
        categoriesList = new VerticalGroup();
        for(final String c : Constants.PART_CATEGORIES){
            Button btn = new Button(skin);
            btn.setSize(64, 64);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor){
                    ((ShipEditor)event.getStage()).selectedCategory = c;
                }
            });
            categoriesList.addActor(btn);
        }
        categoriesScroll = new ScrollPane(categoriesList);
        ui.add(categoriesScroll).expandY().fillY().left().width(64);
    }

    // Functions
    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

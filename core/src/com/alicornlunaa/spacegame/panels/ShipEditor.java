package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
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
    private Table categoriesList;
    private Stack partsStack;
    private Map<String, Table> partsLists;

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
        ui.add(nameBar).expandX().width(200 * scale).height(32 * scale).pad(10).left();

        saveButton = new TextButton("Save", this.skin);
        saveButton.setColor(Color.CYAN);
        saveButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                //! Create ship saver
            }
        });
        ui.add(saveButton).width(64 * scale).height(32 * scale).right();

        closeButton = new TextButton("Close", this.skin);
        closeButton.setColor(Color.RED);
        closeButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                stages.get(stages.size() - 1).dispose();
                stages.remove(stages.size() - 1);
            }
        });
        ui.add(closeButton).width(64 * scale).height(32 * scale).right();

        ui.row();
        categoriesList = new Table().pad(8);
        categoriesList.setFillParent(true);
        for(final String c : Constants.PART_CATEGORIES){
            TextButton btn = new TextButton(c, skin);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor){
                    partsLists.get(((ShipEditor)event.getStage()).selectedCategory).setVisible(false);
                    partsLists.get(c).setVisible(true);

                    ((ShipEditor)event.getStage()).selectedCategory = c;
                }
            });
            categoriesList.add(btn).expand().fill().uniform();
            categoriesList.row();
        }
        categoriesScroll = new ScrollPane(categoriesList);
        categoriesScroll.setScrollingDisabled(false, true);
        ui.add(categoriesScroll).left().expand().fill();

        partsStack = new Stack();
        partsLists = new HashMap<String, Table>();
        for(final String c : Constants.PART_CATEGORIES){
            Table t = new Table();

            for(int x = 0; x < 8; x++) {
                for(int y = 0; y < 3; y++){
                    TextButton btn = new TextButton(String.valueOf(y) + "," + String.valueOf(x), skin);
                    btn.addListener(new ChangeListener(){
                        @Override
                        public void changed(ChangeEvent event, Actor actor){
                            System.out.println("A");
                        }
                    });

                    t.add(btn).expand().fill();
                }

                t.row();
            }

            t.setVisible(c.equals(selectedCategory));
            partsStack.add(t);
            partsLists.put(c, t);
        }
        ui.add(partsStack).expandX().width(128 * scale).fill();
    }

    // Functions
    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

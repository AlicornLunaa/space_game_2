package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/*
 * The ShipEditor class is a stage used to render a window used to create
 * and edit 2d ships.
 */
public class ShipEditor extends Stage {
    // Variables
    private InputProcessor oldProcessor;

    private String selectedCategory;
    private Map<String, Integer> cateogryIndices;

    private Table ui;
    private Stack partsStack;
    private EditorPane editor;

    private SpriteDrawable ghostedPart;
    private String selectedPart;

    // Constructor
    public ShipEditor(final Assets manager, final ArrayList<Stage> stages, final Skin skin, final PartManager partManager){
        super(new ScreenViewport());
        float scale = ControlSchema.GUI_SCALE;

        oldProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(this);

        selectedCategory = "AERO";
        cateogryIndices = new HashMap<String, Integer>();

        ghostedPart = new SpriteDrawable(new Sprite());
        selectedPart = "";

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
        partsStack = new Stack();
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

        int categoryCount = 0;
        for(final String entry : partManager.getPartsList().keySet()) {
            // Each entry is a category
            TextButton btn = new TextButton(entry, skin);

            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor){
                    ((ShipEditor)event.getStage()).partsStack.getChild(cateogryIndices.get(((ShipEditor)event.getStage()).selectedCategory)).setVisible(false);
                    ((ShipEditor)event.getStage()).selectedCategory = entry;
                    ((ShipEditor)event.getStage()).partsStack.getChild(cateogryIndices.get(entry)).setVisible(true);
                }
            });
            
            categoryGroup.addActor(btn);

            cateogryIndices.put(entry, categoryCount);
            categoryCount++;
        }
        categoryGroup.expand().fill();

        // Add each part to a different stack for each category
        for(final String entry : partManager.getPartsList().keySet()) {
            // Each entry is a category
            VerticalGroup partsVertical = new VerticalGroup();
            int count = 0;

            for(final String objKey : partManager.getPartsList().get(entry).keySet()) {
                if(count % 3 == 0){
                    partsVertical.addActor(new HorizontalGroup().wrap().expand().fill().center());
                    ((HorizontalGroup)partsVertical.getChild(count / 3)).expand().fill();
                }

                // Each entry is a category
                JSONObject btnData = partManager.get(entry, objKey);
                final TextureRegion region = new TextureRegion(
                    manager.get(partManager.get(entry, objKey).getString("texture"), Texture.class),
                    btnData.getJSONObject("uv").getInt("x"),
                    btnData.getJSONObject("uv").getInt("y"),
                    btnData.getJSONObject("uv").getInt("width"),
                    btnData.getJSONObject("uv").getInt("height")
                );
                TextureRegionDrawable texture = new TextureRegionDrawable(region);
                ImageButton btn = new ImageButton(texture);

                btn.addListener(new ChangeListener(){
                    @Override
                    public void changed(ChangeEvent event, Actor actor){
                        // TODO: SPAWN PART HERE
                        ((ShipEditor)event.getStage()).selectedPart = objKey;
                        ((ShipEditor)event.getStage()).ghostedPart.getSprite().setRegion(region);
                        ((ShipEditor)event.getStage()).ghostedPart.getSprite().setSize(region.getRegionWidth(), region.getRegionHeight());
                    }
                });

                ((HorizontalGroup)partsVertical.getChild(count / 3)).addActor(btn);
                count++;
            }

            partsVertical.setVisible(entry == selectedCategory);
            partsVertical.expand().fill();
            partsStack.add(partsVertical);
        }

        editorTable.add(partsStack).prefWidth(128 * scale);
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
    public void act(float delta){
        super.act(delta);

        // Set the cursor object to the mouse if an object was selected
        if(!selectedPart.equals("")){
            ghostedPart.getSprite().setPosition(Gdx.input.getX(), Gdx.input.getY());
        }
    }

    @Override
    public void draw(){
        super.draw();

        // Set the cursor object to the mouse if an object was selected
        if(!selectedPart.equals("")){
            Sprite s = ghostedPart.getSprite();

            this.getBatch().setProjectionMatrix(this.getCamera().combined);
            this.getBatch().begin();
            ghostedPart.draw(this.getBatch(), s.getX(), this.getHeight() - s.getY(), s.getWidth(), s.getHeight());
            this.getBatch().end();
        }
    }

    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

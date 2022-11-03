package com.alicornlunaa.spacegame.panels;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.parts.*;
import com.alicornlunaa.spacegame.scenes.ConsoleScene;
import com.alicornlunaa.spacegame.scenes.EditorScene;
import com.alicornlunaa.spacegame.scenes.PauseScene;
import com.alicornlunaa.spacegame.util.*;

/*
 * The ShipEditor class is a stage used to render a window used to create
 * and edit 2d ships.
 */
public class ShipEditorUIPanel extends Stage {

    // Variables
    final App game;

    public String selectedCategory;
    public String selectedPart;

    private Map<String, Integer> cateogryIndices;

    private Table ui;
    private Stack partsStack;
    private TextField nameBar;
    private Table editorPlaceholder;

    // Constructor
    public ShipEditorUIPanel(final App game){
        super(new ScreenViewport());
        this.game = game;
        
        float scale = ControlSchema.GUI_SCALE;

        selectedCategory = "AERO";
        selectedPart = "";

        cateogryIndices = new HashMap<String, Integer>();

        ui = new Table(game.skin);
        ui.setFillParent(true);
        this.addActor(ui);

        // UI members
        nameBar = new TextField("NAME", game.skin);
        TextButton saveButton = new TextButton("Save", game.skin);
        TextButton loadButton = new TextButton("Load", game.skin);
        TextButton closeButton = new TextButton("Close", game.skin);

        Table editorTable = new Table(game.skin);
        VerticalGroup categoryGroup = new VerticalGroup();
        ScrollPane categoryScroll = new ScrollPane(categoryGroup);
        partsStack = new Stack();
        editorPlaceholder = new Table();
        
        // Interface layout
        ui.row().expandX().fillX().pad(20);
        ui.add(nameBar).width(300 * scale).left();
        ui.add(saveButton).right().maxWidth(64 * scale);
        ui.add(loadButton).right().maxWidth(64 * scale);
        ui.add(closeButton).right().maxWidth(64 * scale);

        ui.row().expand().fill().colspan(4);
        ui.add(editorTable);
        editorTable.row().expandY().fillY().left();
        editorTable.add(categoryScroll).minWidth(64 * scale);

        int categoryCount = 0;
        for(final String entry : game.partManager.getPartsList().keySet()) {
            // Each entry is a category
            TextButton btn = new TextButton(entry, game.skin);

            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor){
                    ShipEditorUIPanel ui = (ShipEditorUIPanel)event.getStage();

                    ui.partsStack.getChild(cateogryIndices.get(ui.selectedCategory)).setVisible(false);
                    ui.selectedCategory = entry;
                    ui.partsStack.getChild(cateogryIndices.get(entry)).setVisible(true);
                }
            });
            
            categoryGroup.addActor(btn);

            cateogryIndices.put(entry, categoryCount);
            categoryCount++;
        }
        categoryGroup.expand().fill();

        // Add each part to a different stack for each category
        for(final String entry : game.partManager.getPartsList().keySet()) {
            // Each entry is a category
            VerticalGroup partsVertical = new VerticalGroup();
            int count = 0;

            for(final String objKey : game.partManager.getPartsList().get(entry).keySet()) {
                if(count % 3 == 0){
                    partsVertical.addActor(new HorizontalGroup().wrap().expand().fill().center());
                    ((HorizontalGroup)partsVertical.getChild(count / 3)).expand().fill();
                }

                // Each entry is a category
                JSONObject btnData = game.partManager.get(entry, objKey);
                final TextureRegion region = new TextureRegion(
                    game.manager.get(game.partManager.get(entry, objKey).getString("texture"), Texture.class),
                    btnData.getJSONObject("uv").getInt("x"),
                    btnData.getJSONObject("uv").getInt("y"),
                    btnData.getJSONObject("uv").getInt("width"),
                    btnData.getJSONObject("uv").getInt("height")
                );
                TextureRegionDrawable texture = new TextureRegionDrawable(region);
                texture.setMinSize(32 * ((float)region.getRegionWidth() / (float)region.getRegionHeight()), 32);
                ImageButton btn = new ImageButton(texture);

                btn.addListener(new ChangeListener(){
                    @Override
                    public void changed(ChangeEvent event, Actor actor){
                        ShipEditorPanel editor = ((EditorScene)game.getScreen()).editorPanel;
                        ShipEditorUIPanel ui = (ShipEditorUIPanel)event.getStage();

                        ui.selectedPart = objKey;
                        editor.ghostPart = ShipPart.spawn(
                            game.manager,
                            game.partManager,
                            ui.selectedCategory,
                            ui.selectedPart,
                            editor.ghostBody,
                            editor.ghostState,
                            new Vector2(),
                            0
                        );
                        editor.ghostPart.drawPoints(true);
                    }
                });
                btn.pad(10);
                ((HorizontalGroup)partsVertical.getChild(count / 3)).addActor(btn);
                count++;
            }

            partsVertical.setVisible(entry == selectedCategory);
            partsVertical.expand().fill();
            partsStack.add(partsVertical);
        }

        editorTable.add(partsStack).prefWidth(128 * scale);
        editorTable.add(editorPlaceholder).expandX().fillX().center();

        // Interface functions
        saveButton.setColor(Color.CYAN);
        saveButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Save file to ./saves/ships/name.ship
                ShipEditorPanel editor = ((EditorScene)game.getScreen()).editorPanel;
                ShipEditorUIPanel ui = (ShipEditorUIPanel)event.getStage();

                String shipName = ui.nameBar.getText();

                if(shipName.length() > 0){
                    editor.rootShip.save("./saves/ships/" + shipName + ".ship");
                }
            }
        });

        loadButton.setColor(Color.CYAN);
        loadButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Load file from ./saves/ships/name.ship
                ShipEditorPanel editor = ((EditorScene)game.getScreen()).editorPanel;
                ShipEditorUIPanel ui = (ShipEditorUIPanel)event.getStage();

                String shipName = ui.nameBar.getText();

                if(shipName.length() > 0){
                    editor.rootShip.load("./saves/ships/" + shipName + ".ship");
                    editor.rootShip.drawPoints(true);
                }
            }
        });

        closeButton.setColor(Color.RED);
        closeButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                game.setScreen(game.gameScene);
            }
        });

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                if(button == Buttons.LEFT && nameBar.hasKeyboardFocus() && event.getTarget() != nameBar){
                    game.editorScene.uiPanel.setKeyboardFocus(null);
                    return true;
                }

                return false;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game, game.editorScene, (int)getWidth(), (int)getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(new ConsoleScene(game, game.editorScene, (int)getWidth(), (int)getHeight()));
                    return true;
                }

                return false;
            }
        });
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);
        
        // Resize editor into panel
        ShipEditorPanel editor = ((EditorScene)game.getScreen()).editorPanel;
        editor.getViewport().setWorldSize(editorPlaceholder.getWidth(), editorPlaceholder.getHeight());
        editor.getViewport().setScreenBounds(
            (int)editorPlaceholder.getX(),
            (int)editorPlaceholder.getY(),
            (int)editorPlaceholder.getWidth(),
            (int)editorPlaceholder.getHeight()
        );
        editor.getViewport().apply(true);
        editor.getCamera().position.set(
            editor.camOffset.x + editorPlaceholder.getWidth() / 2,
            editor.camOffset.y + editorPlaceholder.getHeight() / 2,
            0
        );
    }

    @Override
    public void draw(){
        this.getViewport().apply();
        this.getCamera().update();
        super.draw();
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

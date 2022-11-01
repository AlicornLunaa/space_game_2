package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.parts.*;
import com.alicornlunaa.spacegame.parts.ShipPart.Attachment;
import com.alicornlunaa.spacegame.util.*;

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
    private TextField nameBar;

    private Attachment selectedAttachment;
    private int targetAttachmentId;
    private ShipPart ghostedPart;
    private String selectedPart;
    private World world;
    private Body ghostBody;
    private Ship rootShip; // Original center part, set to the first one placed

    private static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Constructor
    public ShipEditor(final Assets manager, final ArrayList<Stage> stages, final Skin skin, final PartManager partManager){
        super(new ScreenViewport());
        float scale = ControlSchema.GUI_SCALE;

        oldProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(this);

        selectedCategory = "AERO";
        cateogryIndices = new HashMap<String, Integer>();

        selectedPart = "";
        world = new World(new Vector2(0, 0), true);
        rootShip = new Ship(manager, partManager, world, 300, 300, 0); // The ship to build
        
        BodyDef def = new BodyDef();
		def.type = BodyType.KinematicBody;
		ghostBody = world.createBody(def);

        ui = new Table(skin);
        ui.setFillParent(true);
        this.addActor(ui);

        // Interface members
        nameBar = new TextField("NAME", skin);
        TextButton saveButton = new TextButton("Save", skin);
        TextButton loadButton = new TextButton("Load", skin);
        TextButton closeButton = new TextButton("Close", skin);

        Table editorTable = new Table(skin);
        VerticalGroup categoryGroup = new VerticalGroup();
        ScrollPane categoryScroll = new ScrollPane(categoryGroup);
        partsStack = new Stack();
        editor = new EditorPane(manager, skin, rootShip);
        
        // Interface layout
        ui.row().expandX().fillX().pad(20);
        ui.add(nameBar).width(300 * scale).left();
        ui.add(saveButton).right().maxWidth(64 * scale);
        ui.add(loadButton).right().maxWidth(64 * scale);
        ui.add(closeButton).right().maxWidth(64 * scale);

        ui.row().expand().fill().colspan(4);
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
                        ((ShipEditor)event.getStage()).selectedPart = objKey;
                        ((ShipEditor)event.getStage()).ghostedPart = ShipPart.spawn(
                            manager,
                            partManager,
                            ((ShipEditor)event.getStage()).selectedCategory,
                            ((ShipEditor)event.getStage()).selectedPart,
                            ghostBody,
                            new Vector2(),
                            0
                        );
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
                // Save file to ./saves/ships/name.ship
                String shipName = ((ShipEditor)actor.getStage()).nameBar.getMessageText();
                rootShip.save("./saves/ships/" + shipName + ".ship");
            }
        });

        loadButton.setColor(Color.CYAN);
        loadButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Load file from ./saves/ships/name.ship
                String shipName = ((ShipEditor)actor.getStage()).nameBar.getMessageText();
                rootShip.load("./saves/ships/" + shipName + ".ship");
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
        
        editor.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                if(!selectedPart.equals("")){
                    // Part is ghosted, spawn one and reset it
                    ghostedPart.setPosition(0, 0);
                    selectedAttachment.getParent().attachPart(ghostedPart, targetAttachmentId, selectedAttachment.getThisId());
                    ghostedPart = null;
                    selectedPart = "";
                }
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
            float radius = 16;
            Vector2 pos = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY())).sub(editor.getX(), editor.getY());
            Attachment closest = rootShip.getClosestAttachment(new Vector2(pos), radius);

            if(closest != null && ghostedPart != null){
                Vector2 attachmentPos = closest.getGlobalPos().add(rootShip.getX(), rootShip.getY()).add(editor.getX(), editor.getY());

                Attachment closest2 = ghostedPart.getClosestAttachment(attachmentPos);
                Vector2 attachmentPos2 = closest2.getPos();

                pos.set(attachmentPos.x + attachmentPos2.x - editor.getX(), attachmentPos.y + attachmentPos2.y);

                targetAttachmentId = closest2.getThisId();
                selectedAttachment = closest;
            }

            ghostedPart.setPosition(pos.x + editor.getX(), pos.y + editor.getY());
        }
    }

    @Override
    public void draw(){
        super.draw();

        if(!selectedPart.equals("")){
            getBatch().begin();
            ghostedPart.draw(getBatch(), 255);
            getBatch().end();

            float radius = 16;
            Vector2 pos = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY())).sub(editor.getX(), editor.getY());
            Attachment closest = rootShip.getClosestAttachment(new Vector2(pos), radius);

            if(closest != null && ghostedPart != null){
                Vector2 attachmentPos = closest.getGlobalPos().add(rootShip.getX(), rootShip.getY()).add(editor.getX(), editor.getY());

                Attachment closest2 = ghostedPart.getClosestAttachment(attachmentPos);
                Vector2 attachmentPos2 = closest2.getGlobalPos();

                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.circle(attachmentPos.x, attachmentPos.y, 4);
                shapeRenderer.setColor(Color.MAGENTA);
                shapeRenderer.circle(attachmentPos2.x, attachmentPos2.y, 4);
                shapeRenderer.end();
            }
        }
    }

    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

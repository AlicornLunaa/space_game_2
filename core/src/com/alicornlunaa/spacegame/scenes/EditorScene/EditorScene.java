package com.alicornlunaa.spacegame.scenes.EditorScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.parts.Part;
import com.alicornlunaa.spacegame.scenes.Misc.ConsoleScene;
import com.alicornlunaa.spacegame.scenes.Transitions.FadeTransitionScene;
import com.alicornlunaa.spacegame.scenes.Transitions.PauseScene;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.ray3k.stripe.scenecomposer.SceneComposerStageBuilder;

public class EditorScene implements Screen {

    // Variables
    private final App game;
    private InputMultiplexer inputs = new InputMultiplexer();

    private Screen previouScreen;
    private Stage ui;
    private Stage editor;
    private OrthographicCamera cam;

    // UI variables
    private FileChooser fileChooser;

    // Editor variables
    private Vector2 cursor = new Vector2();
    private Vector2 camOffset = new Vector2();
    private Part ghostPart;
    private World editorWorld;
    private Ship editorShip;
    private String shipName = "";
    private String selectedCategory = "AERO";
    private boolean snapped = false;

    // Private functions
    private void select(String category){
        ui.getRoot().findActor(selectedCategory).setVisible(false);
        ui.getRoot().findActor(category).setVisible(true);
        selectedCategory = category;
    }

    private void spawnPart(String partID){
        // Spawn at origin if its the first part
        if(editorShip.getParts().size == 0){
            editorShip.addPart(Part.spawn(game, editorShip, selectedCategory, partID));
            return;
        }

        // Put a ghosted part on the cursor
        ghostPart = Part.spawn(game, editorShip, selectedCategory, partID);
        editor.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                // Place part when clicked
                if(button != Buttons.LEFT) return false;

                if(snapped){
                    editorShip.addPart(ghostPart);
                }

                ghostPart = null;
                editor.removeListener(this);
                return true;
            }
        });
    }

    private VerticalGroup addPartList(final String category){
        ScrollPane pane = new ScrollPane(editorShip, game.skin);
        VerticalGroup group = new VerticalGroup();
        group.expand().fill();
        group.pad(10, 0, 10, 0);
        group.space(10);
        group.top();
        pane.setName(category);
        pane.setActor(group);
        pane.setVisible(false);
        ((Stack)ui.getRoot().findActor("partstack")).add(pane);

        TextButton btn = new TextButton(category, game.skin);
        btn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                select(category);
            }
        });
        ((VerticalGroup)ui.getRoot().findActor("categories")).addActor(btn);

        return group;
    }

    private void initUI(){
        SceneComposerStageBuilder builder = new SceneComposerStageBuilder();
        builder.build(ui, game.skin, Gdx.files.internal("layouts/editor_hud.json"));
        
        fileChooser = new FileChooser(Gdx.files.internal("./saves/ships"), Mode.OPEN);
        fileChooser.setSelectionMode(SelectionMode.FILES);

        ui.getRoot().findActor("shipnamebar").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                shipName = ((TextField)a).getText();
            }
        });

        for(String entry : game.partManager.getPartsList().keySet()){
            VerticalGroup category = addPartList(entry);

            for(final String partID : game.partManager.getPartsList().get(entry).keySet()){
                TextureRegionDrawable texture = new TextureRegionDrawable(game.atlas.findRegion("parts/" + partID.toLowerCase()));
                ImageButton btn = new ImageButton(texture);
                texture.setMinSize(32 * ((float)texture.getRegion().getRegionWidth() / (float)texture.getRegion().getRegionHeight()), 32);

                btn.addListener(new ChangeListener(){
                    @Override
                    public void changed(ChangeEvent e, Actor a){
                        spawnPart(partID);
                    }
                });

                category.addActor(btn);
            }
        }
        select("AERO");

        ui.getRoot().findActor("savebutton").addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Save file to ./saves/ships/name.ship
                if(shipName.length() > 0){
                    editorShip.save("./saves/ships/" + shipName + ".ship");
                }
            }
        });

        ui.getRoot().findActor("loadbutton").addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle bodyHandle = files.first();
                        editorShip.load(bodyHandle.path());
                    }
                });
                
                ui.addActor(fileChooser);
            }
        });

        ui.getRoot().findActor("exitbutton").addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                game.setScreen(new FadeTransitionScene(game, game.getScreen(), previouScreen, 0.15f));
            }
        });

        ui.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                TextField namebar = ui.getRoot().findActor("shipnamebar");

                if(button == Buttons.LEFT && namebar.hasKeyboardFocus() && event.getTarget() != namebar){
                    ui.setKeyboardFocus(null);
                    return true;
                }

                return false;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(new PauseScene(game, (int)ui.getWidth(), (int)ui.getHeight()));
                    return true;
                } else if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(new ConsoleScene(game, (int)ui.getWidth(), (int)ui.getHeight()));
                    return true;
                }

                return false;
            }
        });
    }

    private void initEditor(){
        cam = (OrthographicCamera)editor.getCamera();
        cam.zoom = 0.2f;
        cam.position.set(0, 0, 0);
        cam.update();

        editorWorld = new World(new Vector2(), true);
        editorShip = new Ship(game, editorWorld, 0, 0, 0);
        editor.addActor(editorShip);

        editor.addListener(new InputListener(){
            Vector2 prevDrag = new Vector2();

            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(ghostPart != null){
                    if(keycode == ControlSchema.EDITOR_ROTATE){
                        ghostPart.setRotation(ghostPart.getRotation() + 45);
                        return true;
                    }

                    if(keycode == ControlSchema.EDITOR_FLIP_X){
                        ghostPart.setFlipX();
                        return true;
                    }

                    if(keycode == ControlSchema.EDITOR_FLIP_Y){
                        ghostPart.setFlipY();
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                cam.zoom = Math.min(Math.max(cam.zoom + (amountY / 50), 0.05f), 1.5f);
                cam.update();

                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                if(button == Buttons.LEFT){
                    if(ghostPart == null){
                        // TODO: Create functionality to find which part was clicked, and to pick it up
                        for(Part p : editorShip.getParts()){
                            if(p.hit(cursor)){
                                System.out.println(p);
                            }
                        }
                        return true;
                    }
                } else if(button == Buttons.RIGHT){
                    prevDrag.set(x, y);
                    return true;
                }

                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                Vector2 vel = new Vector2(x, y).sub(prevDrag).scl(-0.4f);
                camOffset.add(vel);
            }
        });
    }

    private void drawPoints(Part p){
        Matrix3 trans = new Matrix3().translate(p.getX(), p.getY()).rotate(p.getRotation());

        for(Vector2 a : p.getAttachmentPoints()){
            Vector2 v = a.cpy().mul(trans);
            game.shapeRenderer.circle(v.x, v.y, 1);
        }
    }

    private Vector2 getPartPos(){
        // Get the closest snapping attachment point, otherwise just the cursor
        // Linear search the two nearest points
        int nearestGhostAttachment = 0;
        Vector2 nearestPartPoint = new Vector2();
        float minDist = Float.MAX_VALUE;
        float minDistGhost = Float.MAX_VALUE;
        snapped = false;

        // Loop every part on the ship
        for(Vector2 partPoint : editorShip.getAttachments().getMap().keySet()){
            float dist = partPoint.dst(cursor);

            if(dist < minDist){
                nearestPartPoint = partPoint;
                minDist = dist;
            }
        }

        // Find mating point
        Matrix3 ghostTrans = new Matrix3().translate(ghostPart.getX(), ghostPart.getY()).rotate(ghostPart.getRotation());
        for(int i = 0; i < ghostPart.getAttachmentPoints().size; i++){
            Vector2 ghostAttachmentLocal = ghostPart.getAttachmentPoints().get(i);
            Vector2 ghostPoint = ghostAttachmentLocal.cpy().mul(ghostTrans);
            float dist = ghostPoint.dst(nearestPartPoint);

            if(dist < minDistGhost){
                nearestGhostAttachment = i;
                minDistGhost = dist;
            }
        }

        if(minDist < 16 && !editorShip.getAttachments().getActive(nearestPartPoint)){
            ghostTrans = new Matrix3().rotate(ghostPart.getRotation()).scale(ghostPart.getFlipX() ? -1 : 1, ghostPart.getFlipY() ? -1 : 1);
            Vector2 p1 = nearestPartPoint.cpy();
            Vector2 p2 = ghostPart.getAttachmentPoints().get(nearestGhostAttachment).cpy().mul(ghostTrans);

            snapped = true;
            return p1.sub(p2);
        }

        return cursor;
    }

    private void drawEditor(float delta){
        cam.position.set(camOffset, 0);
        cam.update();

        Batch batch = editor.getBatch();
        batch.begin();
        batch.setProjectionMatrix(cam.combined);
        
        if(ghostPart != null){
            Vector2 pos = getPartPos();
            ghostPart.setX(pos.x);
            ghostPart.setY(pos.y);
            ghostPart.draw(batch, delta);
        }
        
        editorShip.draw(batch, 1);
        batch.end();
        
        game.shapeRenderer.begin(ShapeType.Filled);
        game.shapeRenderer.setProjectionMatrix(cam.combined);
        game.shapeRenderer.setColor(Color.GREEN);
        for(Vector2 pos : editorShip.getAttachments().getMap().keySet()){
            game.shapeRenderer.setColor(editorShip.getAttachments().getActive(pos) ? Color.RED : Color.GREEN);
            game.shapeRenderer.circle(pos.x, pos.y, 1.0f);
        }
        if(ghostPart != null){ drawPoints(ghostPart); }
        game.shapeRenderer.end();
    }

    // Constructor
    public EditorScene(final App game){
        this.game = game;
        previouScreen = game.getScreen();
        
        ui = new Stage(new ScreenViewport());
        editor = new Stage(new ScreenViewport());

        initUI();
        initEditor();

        inputs.addProcessor(ui);
        inputs.addProcessor(editor);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
        cursor = editor.screenToStageCoordinates(cursor.set(Gdx.input.getX(), Gdx.input.getY()));
        
        editor.act(delta);
        ui.act(delta);

        drawEditor(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        editor.getViewport().update(width, height, true);
        ui.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        editor.dispose();
        ui.dispose();
    }
    
}

package com.alicornlunaa.spacegame.scenes.Dev.PhysicsEditor;

import org.json.JSONArray;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

/**
 * Physics editor to create collider JSONs
 */
public class PhysicsEditor implements Screen {

    // Variables
    private final App game;
    private Stage ui;
    private InputMultiplexer inputs = new InputMultiplexer();

    // UI variables
    private VisTable root;
    private FileChooser fileChooser;
    private VisSplitPane splitPane;
    private VisTable partSettings;
    // private VerticalGroup shapeGroup;

    // Editor variables
    public Vector2 cursorOnPart = new Vector2();
    public Vector2 cursor = new Vector2();
    public Vector2 center = new Vector2();
    public float partSize = 256;
    public Texture reference;
    public Collider shape = new Collider();
    public float friction = 1.0f;
    public float restitution = 0.0f;
    public float density = 0.0f;

    // Private functions
    private void saveCollider(FileHandle handle){
        // JSONArray shapeData = new JSONArray();
        // for(PhysShape s : shapes){
        //     shapeData.put(s.serialize());
        // }
        handle.writeString(shape.serialize().toString(), false);
    }

    private void loadCollider(FileHandle handle){
        // shapes.clear();

        // JSONArray shapeData = new JSONArray(handle.readString());
        // for(int i = 0; i < shapeData.length(); i++){
        //     JSONArray shape = shapeData.getJSONArray(i);
        //     shapes.add(PhysShape.unserialize(game.shapeRenderer, shape));
        // }
        shape = Collider.unserialize(new JSONArray(handle.readString()));
    }

    // private void newShape(){
    //     shapes.add(new PhysShape(game.shapeRenderer));
    //     shapeBeingEdited = (shapes.size - 1);

    //     VisTextButton btn = new VisTextButton("Select " + shapeBeingEdited);
    //     btn.addListener(new ChangeListener(){
    //         final int i = shapes.size - 1;

    //         @Override
    //         public void changed(ChangeEvent e, Actor a){
    //             shapeBeingEdited = i;
    //         }
    //     });
    //     btn.pad(5);
    //     shapeGroup.addActor(btn);
    // }

    // private void delShape(){
    //     if(shapeBeingEdited == -1) return;
    //     if(shapeBeingEdited > shapes.size - 1) return;
    //     shapes.removeIndex(shapeBeingEdited);
    //     shapeGroup.removeActorAt(shapeBeingEdited, false);

    //     for(int i = shapeBeingEdited; i < shapeGroup.getChildren().size; i++){
    //         final int index = i;
            
    //         VisTextButton btn = ((VisTextButton)shapeGroup.getChild(i));
    //         btn.setText("Select " + i);
    //         btn.removeListener(btn.getListeners().get(2));
    //         btn.addListener(new ChangeListener(){
    //             @Override
    //             public void changed(ChangeEvent e, Actor a){
    //                 shapeBeingEdited = index;
    //             }
    //         });
    //     }

    //     shapeBeingEdited = -1;
    // }

    private void initUI(){
        // Initialize UI
        ui = new Stage(new ScreenViewport());
        root = new VisTable();
        root.setFillParent(true);
        ui.addActor(root);
        reference = new Texture(1, 1, Format.RGBA8888);
        shape.triangulate();

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        menuBar.addMenu(fileMenu);
        root.add(menuBar.getTable()).fillX().expandX().top().row();

        // Properties menu
        VisTextField frictionField = new VisValidatableTextField(Validators.FLOATS);
        VisTextField restitutionField = new VisValidatableTextField(Validators.FLOATS);
        VisTextField densityField = new VisValidatableTextField(Validators.FLOATS);
        // VisTextButton newShapeBtn = new VisTextButton("New Shape");
        // VisTextButton delShapeBtn = new VisTextButton("Delete Shape");
        // shapeGroup = new VerticalGroup();
        
        partSettings = new VisTable(); partSettings.top();
        partSettings.add(new VisLabel("Friction")).pad(10).right();
        partSettings.add(frictionField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Restitution")).pad(10).right();
        partSettings.add(restitutionField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Density")).pad(10).right();
        partSettings.add(densityField).expandX().fillX().padRight(10).row();

        // partSettings.add(new VisLabel("Shapes")).pad(10, 10, 0, 10).center().colspan(2).row();
        // partSettings.add(newShapeBtn).expandX().fillX().pad(10);
        // partSettings.add(delShapeBtn).expandX().fillX().pad(10).row();
        // partSettings.add(shapeGroup).expandX().fillX().pad(10).colspan(2).row();

        frictionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                // if(shapeBeingEdited == -1) return;
                // shapes.get(shapeBeingEdited).friction = Float.parseFloat(((VisTextField)a).getText());
                friction = Float.parseFloat(((VisTextField)a).getText());
            }
        });

        restitutionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                // if(shapeBeingEdited == -1) return;
                // shapes.get(shapeBeingEdited).restitution = Float.parseFloat(((VisTextField)a).getText());
                restitution = Float.parseFloat(((VisTextField)a).getText());
            }
        });

        densityField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                // if(shapeBeingEdited == -1) return;
                // shapes.get(shapeBeingEdited).density = Float.parseFloat(((VisTextField)a).getText());
                density = Float.parseFloat(((VisTextField)a).getText());
            }
        });

        // newShapeBtn.addListener(new ChangeListener(){
        //     @Override
        //     public void changed(ChangeEvent e, Actor a){
        //         newShape();
        //     }
        // });

        // delShapeBtn.addListener(new ChangeListener(){
        //     @Override
        //     public void changed(ChangeEvent e, Actor a){
        //         delShape();
        //     }
        // });

        VisTable placeholder = new VisTable();
        placeholder.setFillParent(true);
        placeholder.add().expand().fill();

        splitPane = new VisSplitPane(partSettings, null, false);
        splitPane.setSplitAmount(0.25f);
        root.add(splitPane).fill().expand().row();

        fileChooser = new FileChooser(Gdx.files.internal("./assets/parts/"), Mode.OPEN);
        fileChooser.setSelectionMode(SelectionMode.FILES);

        fileMenu.addItem(new MenuItem("Load image", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load image into reference
                fileChooser.setMode(Mode.OPEN);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle handle = files.first();
                        reference.dispose();
                        reference = new Texture(handle);
                    }
                });
                
                ui.addActor(fileChooser);
            }
        }));

        fileMenu.addItem(new MenuItem("Import", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.OPEN);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle handle = files.first();
                        loadCollider(handle);
                    }
                });
                
                ui.addActor(fileChooser);
            }
        }));

        fileMenu.addItem(new MenuItem("Export", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.SAVE);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle handle = files.first();
                        saveCollider(handle);
                    }
                });
                
                ui.addActor(fileChooser);
            }
        }));

        fileMenu.addItem(new MenuItem("Clear", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Clear shape
                shape.vertices.clear();
                shape.triangles.clear();
            }
        }));

        // Controls
        inputs.addProcessor(0, ui);
        inputs.addProcessor(1, new InputAdapter(){
            @Override
            public boolean scrolled(float amountX, float amountY){
                partSize = Math.min(Math.max(partSize - amountY * 20, 1), 500);
                return true;
            }
        });
    }

    private void initEditor(){
        inputs.addProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button){
                // if(shapes.size == 0) return false;
                // if(shapeBeingEdited == -1) return false;
                
                float snapX = (int)(cursorOnPart.x * 2) / 2.0f;
                float snapY = (int)(cursorOnPart.y * 2) / 2.0f;

                if(button == Buttons.LEFT){
                    // shapes.get(shapeBeingEdited).vertices.add(new Vector2(snapX - center.x, snapY - center.y));
                    shape.vertices.add(new Vector2(snapX - center.x, snapY - center.y));
                } else if(button == Buttons.RIGHT){
                    // shapes.get(shapeBeingEdited).vertices.removeValue(new Vector2(snapX - center.x, snapY - center.y), false);
                    shape.vertices.removeValue(new Vector2(snapX - center.x, snapY - center.y), false);
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode){
                return false;
            }
        });
    }

    // Constructor
    public PhysicsEditor(final App game){
        // Initialize components
        this.game = game;
        initUI();
        initEditor();
    }

    // Functions
    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        cursor = ui.screenToStageCoordinates(cursor.set(Gdx.input.getX(), Gdx.input.getY()));
        cursor.set(Math.round(cursor.x), Math.round(cursor.y)); // Snap points

        // Draw the reference image
        Rectangle rect = splitPane.getSecondWidgetBounds();
        Vector2 corner = new Vector2(rect.x + rect.width / 2 - partSize / 2, rect.y + rect.height / 2 - partSize / 2);
        float ratio = ((float)reference.getHeight() / reference.getWidth());
        center.set(reference.getWidth() / 2.0f, reference.getHeight() / 2.0f);
        cursorOnPart.set(cursor.cpy().sub(corner).scl(1 / partSize, 1 / (partSize * ratio)).scl(reference.getWidth(), reference.getWidth() * ratio));

        Batch batch = ui.getBatch();
        batch.begin();
        batch.draw(reference, corner.x, corner.y, partSize, partSize * ratio);
        batch.end();

        // Drawing the colliders
        Vector2 screenScale = new Vector2(1.0f / reference.getWidth(), 1.0f / (reference.getWidth() * ratio)).scl(partSize, partSize * ratio);
        float snapX = (int)(cursorOnPart.x * 2) / 2.0f;
        float snapY = (int)(cursorOnPart.y * 2) / 2.0f;
        
        game.shapeRenderer.begin(ShapeType.Filled);
        game.shapeRenderer.setColor(Color.CYAN);
        game.shapeRenderer.circle(corner.x + snapX * screenScale.x, corner.y + snapY * screenScale.y, 4.0f);
        
        game.shapeRenderer.setColor(Color.GREEN);
        Array<Vector2> arr = shape.triangulate();
        for(int i = 0; i < arr.size; i += 3){
            Vector2 v1 = arr.get(i);
            Vector2 v2 = arr.get((i + 1) % arr.size);
            Vector2 v3 = arr.get((i + 2) % arr.size);
            
            game.shapeRenderer.rectLine(
                corner.x + (v1.x + center.x) * screenScale.x,
                corner.y + (v1.y + center.y) * screenScale.y,
                corner.x + (v2.x + center.x) * screenScale.x,
                corner.y + (v2.y + center.y) * screenScale.y,
                2.0f
            );
            
            game.shapeRenderer.rectLine(
                corner.x + (v2.x + center.x) * screenScale.x,
                corner.y + (v2.y + center.y) * screenScale.y,
                corner.x + (v3.x + center.x) * screenScale.x,
                corner.y + (v3.y + center.y) * screenScale.y,
                2.0f
            );
            
            game.shapeRenderer.rectLine(
                corner.x + (v3.x + center.x) * screenScale.x,
                corner.y + (v3.y + center.y) * screenScale.y,
                corner.x + (v1.x + center.x) * screenScale.x,
                corner.y + (v1.y + center.y) * screenScale.y,
                2.0f
            );
        }

        game.shapeRenderer.setColor(Color.RED);
        for(Vector2 v : shape.vertices){
            game.shapeRenderer.circle(
                corner.x + (v.x + center.x) * screenScale.x,
                corner.y + (v.y + center.y) * screenScale.y,
                4.0f
            );
        }

        // for(PhysShape shape : shapes){
        //     game.shapeRenderer.setColor((shapeBeingEdited != -1 && shape == shapes.get(shapeBeingEdited)) ? Color.GREEN : Color.RED);
        //     Array<Vector2> arr = shape.calculateHull();

        //     for(int i = 0; i < arr.size; i++){
        //         Vector2 v1 = arr.get(i);
        //         Vector2 v2 = arr.get((i + 1) % arr.size);
        //         game.shapeRenderer.rectLine(
        //             corner.x + (v1.x + center.x) * screenScale.x,
        //             corner.y + (v1.y + center.y) * screenScale.y,
        //             corner.x + (v2.x + center.x) * screenScale.x,
        //             corner.y + (v2.y + center.y) * screenScale.y,
        //             2.0f
        //         );
        //     }

        //     for(Vector2 v : shape.vertices){
        //         game.shapeRenderer.circle(
        //             corner.x + (v.x + center.x) * screenScale.x,
        //             corner.y + (v.y + center.y) * screenScale.y,
        //             4.0f
        //         );
        //     }
        // }

        game.shapeRenderer.end();

        // Render UI
        ui.getViewport().apply();
        ui.act(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        ui.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        ui.dispose();
    }
    
}

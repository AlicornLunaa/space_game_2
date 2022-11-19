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
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
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
    private VerticalGroup shapeGroup;

    // Editor variables
    public Vector2 cursorOnPart = new Vector2();
    public Vector2 cursor = new Vector2();
    public Vector2 center = new Vector2();
    public float partSize = 256;
    public float snapDistance = 2;
    public Texture reference;
    public Collider collider = new Collider();

    private int shapeBeingEdited = -1;

    // Private functions
    private void saveCollider(FileHandle handle){ handle.writeString(collider.serialize().toString(2), false); }

    private void loadCollider(FileHandle handle){
        shapeBeingEdited = -1;
        collider.clear();
        collider = Collider.unserialize(new JSONArray(handle.readString()));

        for(int i = 0; i < collider.getShapeCount(); i++){
            final int index = i;

            VisTextButton btn = new VisTextButton("Select " + index);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectShape(index);
                }
            });
            btn.pad(5);
            shapeGroup.addActor(btn);
        }
    }

    private void newShape(){
        shapeBeingEdited = collider.addShape();

        VisTextButton btn = new VisTextButton("Select " + shapeBeingEdited);
        btn.addListener(new ChangeListener(){
            final int i = shapeBeingEdited;

            @Override
            public void changed(ChangeEvent e, Actor a){
                selectShape(i);
            }
        });
        btn.pad(5);
        shapeGroup.addActor(btn);
    }

    private void delShape(){
        if(shapeBeingEdited == -1) return;

        collider.removeShape(shapeBeingEdited);
        shapeGroup.removeActorAt(shapeBeingEdited, false);

        for(int i = shapeBeingEdited; i < shapeGroup.getChildren().size; i++){
            final int index = i;
            
            VisTextButton btn = ((VisTextButton)shapeGroup.getChild(i));
            btn.setText("Select " + i);
            btn.removeListener(btn.getListeners().get(2));
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectShape(index);
                }
            });
        }

        shapeBeingEdited = -1;
    }

    private void selectShape(int shape){
        shapeBeingEdited = shape;

        ((VisTextField)ui.getRoot().findActor("frictionField")).setText(String.valueOf(collider.getFriction(shape)));
        ((VisTextField)ui.getRoot().findActor("restitutionField")).setText(String.valueOf(collider.getRestitution(shape)));
        ((VisTextField)ui.getRoot().findActor("densityField")).setText(String.valueOf(collider.getDensity(shape)));
        ((VisCheckBox)ui.getRoot().findActor("sensorCheck")).setChecked(collider.getSensor(shape));
    }

    private void initUI(){
        // Initialize UI
        ui = new Stage(new ScreenViewport());
        root = new VisTable();
        root.setFillParent(true);
        ui.addActor(root);
        reference = new Texture(1, 1, Format.RGBA8888);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        menuBar.addMenu(fileMenu);
        root.add(menuBar.getTable()).fillX().expandX().top().row();

        // Properties menu
        VisTextField frictionField = new VisValidatableTextField(Validators.FLOATS); frictionField.setName("frictionField");
        VisTextField restitutionField = new VisValidatableTextField(Validators.FLOATS); restitutionField.setName("restitutionField");
        VisTextField densityField = new VisValidatableTextField(Validators.FLOATS); densityField.setName("densityField");
        VisCheckBox sensorCheck = new VisCheckBox("Sensor"); sensorCheck.setName("sensorCheck");
        VisTextField snapField = new VisValidatableTextField(Validators.FLOATS); snapField.setName("snapField"); snapField.setText("2");
        VisTextButton newShapeBtn = new VisTextButton("New Shape");
        VisTextButton delShapeBtn = new VisTextButton("Delete Shape");
        shapeGroup = new VerticalGroup();
        
        partSettings = new VisTable(); partSettings.top();
        partSettings.add(new VisLabel("Friction")).pad(10).right();
        partSettings.add(frictionField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Restitution")).pad(10).right();
        partSettings.add(restitutionField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Density")).pad(10).right();
        partSettings.add(densityField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Snap distnace")).pad(10).right();
        partSettings.add(snapField).expandX().fillX().padRight(10).row();
        partSettings.add(sensorCheck).expandX().fillX().padRight(10).colspan(2).row();

        partSettings.add(new VisLabel("Shapes")).pad(10, 10, 0, 10).center().colspan(2).row();
        partSettings.add(newShapeBtn).expandX().fillX().pad(10);
        partSettings.add(delShapeBtn).expandX().fillX().pad(10).row();
        partSettings.add(shapeGroup).expandX().fillX().pad(10).colspan(2).row();

        frictionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shapeBeingEdited == -1) return;
                try {
                    collider.setFriction(shapeBeingEdited, Float.parseFloat(((VisTextField)a).getText()));
                } catch(NumberFormatException exception){}
            }
        });

        restitutionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shapeBeingEdited == -1) return;
                try {
                    collider.setRestitution(shapeBeingEdited, Float.parseFloat(((VisTextField)a).getText()));
                } catch(NumberFormatException exception){}
            }
        });

        densityField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shapeBeingEdited == -1) return;
                try {
                    collider.setDensity(shapeBeingEdited, Float.parseFloat(((VisTextField)a).getText()));
                } catch(NumberFormatException exception){}
            }
        });

        snapField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                try {
                    snapDistance = Float.parseFloat(((VisTextField)a).getText());
                } catch(NumberFormatException exception){}
            }
        });

        sensorCheck.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shapeBeingEdited == -1) return;
                collider.setSensor(shapeBeingEdited, !collider.getSensor(shapeBeingEdited));
            }
        });

        newShapeBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                newShape();
            }
        });

        delShapeBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                delShape();
            }
        });

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
				fileChooser.setPrefsName("com.alicornlunaa.spacegame2.physeditor.img");
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
				fileChooser.setPrefsName("com.alicornlunaa.spacegame2.physeditor.import");
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
				fileChooser.setPrefsName("com.alicornlunaa.spacegame2.physeditor.export");
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
                shapeBeingEdited = -1;
                collider.clear();
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
                if(shapeBeingEdited == -1) return false;
                
                float snapX = (int)(cursorOnPart.x * snapDistance) / snapDistance;
                float snapY = (int)(cursorOnPart.y * snapDistance) / snapDistance;

                if(button == Buttons.LEFT){
                    collider.addVertex(shapeBeingEdited, new Vector2(snapX - center.x, snapY - center.y));
                } else if(button == Buttons.RIGHT){
                    collider.removeVertex(shapeBeingEdited, new Vector2(snapX - center.x, snapY - center.y));
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
        float snapX = (int)(cursorOnPart.x * snapDistance) / snapDistance;
        float snapY = (int)(cursorOnPart.y * snapDistance) / snapDistance;
        
        collider.calculateShapes();

        game.shapeRenderer.begin(ShapeType.Filled);
        
        for(int i = 0; i < collider.getShapeCount(); i++){
            game.shapeRenderer.setColor((shapeBeingEdited == i) ? Color.GREEN : Color.RED);

            for(int j = 0; j < collider.getIndexCount(i); j++){
                Vector2 v1 = collider.getVertex(i, collider.getIndex(i, j));
                Vector2 v2 = collider.getVertex(i, collider.getIndex(i, (j + 1) % collider.getIndexCount(i)));

                game.shapeRenderer.rectLine(
                    corner.x + (v1.x + center.x) * screenScale.x,
                    corner.y + (v1.y + center.y) * screenScale.y,
                    corner.x + (v2.x + center.x) * screenScale.x,
                    corner.y + (v2.y + center.y) * screenScale.y,
                    2.0f
                );
            }
        }

        for(int i = 0; i < collider.getShapeCount(); i++){
            game.shapeRenderer.setColor((shapeBeingEdited == i) ? Color.GREEN : Color.RED);

            for(int j = 0; j < collider.getVertexCount(i); j++){
                Vector2 v = collider.getVertex(i, j);

                game.shapeRenderer.circle(
                    corner.x + (v.x + center.x) * screenScale.x,
                    corner.y + (v.y + center.y) * screenScale.y,
                    6.0f
                );
            }
        }
        
        game.shapeRenderer.setColor(Color.CYAN);
        game.shapeRenderer.circle(corner.x + snapX * screenScale.x, corner.y + snapY * screenScale.y, 4.0f);

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

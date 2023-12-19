package com.alicornlunaa.space_game.scenes.dev_kit_scene;

import org.json.JSONArray;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.selene_engine.phys.Collider.CircleShape;
import com.alicornlunaa.selene_engine.phys.Collider.PolygonShape;
import com.alicornlunaa.selene_engine.phys.Collider.Shape;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;
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

public class PhysicsEditor extends VisTable {
    // Static variables
    private static ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Inner classes
    private static class Cursor extends Actor {
        // Functions
        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(getColor());
            shapeRenderer.circle(getX(), getY(), 0.5f, 16);
            shapeRenderer.end();
            batch.begin();
        }
    };

    private static class ShapeOutline extends Actor {
        // Variables
        private Shape shape;

        // Constructor
        private ShapeOutline(Shape shape){
            this.shape = shape;
        }

        // Functions
        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(getColor());

            if(shape instanceof PolygonShape){
                PolygonShape polyShape = (PolygonShape)shape;

                for(int j = 0; j < polyShape.getIndexCount(); j++){
                    if(polyShape.getIndex(j) >= polyShape.getVertexCount() || polyShape.getIndex((j + 1) % polyShape.getIndexCount()) >= polyShape.getVertexCount()) continue;
                    Vector2 v1 = polyShape.getVertex(polyShape.getIndex(j));
                    Vector2 v2 = polyShape.getVertex(polyShape.getIndex((j + 1) % polyShape.getIndexCount()));
                    shapeRenderer.rectLine(v1.x, v1.y, v2.x, v2.y, 0.4f);
                }
        
                for(int j = 0; j < polyShape.getVertexCount(); j++){
                    Vector2 v = polyShape.getVertex(j);
                    shapeRenderer.circle(v.x, v.y, 0.6f, 16);
                }
            } else if(shape instanceof CircleShape){
                CircleShape circShape = (CircleShape)shape;
                shapeRenderer.circle(circShape.getPosition().x, circShape.getPosition().y, circShape.getRadius());
            }

            shapeRenderer.end();
            batch.begin();
        }
    }

    // Variables
    private FileChooser fileChooser;

    private Collider collider = new Collider();
    private @Null Shape shape = null;
    private float snapDistance = 2;
    private Texture referenceTexture = new Texture(1, 1, Format.RGBA8888);
    private Image referenceImage;

    private Stage editorStage;
    private OrthographicCamera editorCamera;
    private @Null Vector2 panningVector = null;
    private VisSplitPane splitPane;
    
    private Cursor cursor = new Cursor();
    private VisTextField snapField;
    private VisTextField frictionField;
    private VisTextField restitutionField;
    private VisTextField densityField;
    private VisCheckBox convexCheck;
    private VisCheckBox sensorCheck;
    private VerticalGroup shapesList = new VerticalGroup();
    private Group shapeOutlines = new Group();

    // Private functions
    private void loadShapeOutlines(){
        shapeOutlines.clear();

        for(int i = 0; i < collider.getShapeCount(); i++){
            Shape s = collider.getShape(i);
            ShapeOutline outline = new ShapeOutline(s);
            outline.setColor((s == this.shape) ? Color.GREEN : Color.RED);
            shapeOutlines.addActor(outline);
        }
    }

    private void refreshShapes(){
        shapesList.clear();

        for(int i = 0; i < collider.getShapeCount(); i++){
            final int index = i;

            VisTextButton btn = new VisTextButton("Select " + index);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectShape(collider.getShape(index));
                }
            });
            btn.pad(5);
            shapesList.addActor(btn);
        }

        loadShapeOutlines();
    }

    private void selectShape(Shape shape){
        this.shape = shape;
        loadShapeOutlines();

        if(shape == null){
            frictionField.setText("0.0");
            restitutionField.setText("0.0");
            densityField.setText("0.0");
            convexCheck.setChecked(true);
            sensorCheck.setChecked(false);
        } else {
            frictionField.setText(String.valueOf(shape.friction));
            restitutionField.setText(String.valueOf(shape.restitution));
            densityField.setText(String.valueOf(shape.density));
            convexCheck.setChecked(shape.convex);
            sensorCheck.setChecked(shape.sensor);
        }
    }

    private void save(FileHandle handle){
        try {
            handle.writeString(collider.serialize().toString(), false);
        } catch(GdxRuntimeException e){
            Gdx.app.log("Physics Editor", "Cannot save here");
        }
    }

    private void load(FileHandle handle){
        collider.clear();

        try {
            collider = new Collider(new JSONArray(handle.readString()));
        } catch(GdxRuntimeException e){
            Gdx.app.log("Physics Editor", "Cannot load file");
        }

        refreshShapes();
    }

    private void initMenu(){
        // Start UI
        MenuBar menu = new MenuBar();
        this.add(menu.getTable()).fillX().expandX().top().row();
        
        Menu fileMenu = new Menu("File");
        menu.addMenu(fileMenu);
        
        fileChooser = new FileChooser(Gdx.files.internal("./assets/textures/"), Mode.OPEN);
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
                        referenceTexture.dispose();
                        referenceTexture = new Texture(handle);
                        referenceImage.setDrawable(new TextureRegionDrawable(new TextureRegion(referenceTexture)));
                        referenceImage.setOrigin(Align.center);
                        referenceImage.setSize(referenceTexture.getWidth(), referenceTexture.getHeight());
                        referenceImage.setPosition(referenceImage.getWidth() / -2, referenceImage.getHeight() / -2);
                    }
                });
                PhysicsEditor.this.addActor(fileChooser);
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
                        PhysicsEditor.this.load(handle);
                    }
                });
                PhysicsEditor.this.addActor(fileChooser);
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
                        PhysicsEditor.this.save(handle);
                    }
                });
                PhysicsEditor.this.addActor(fileChooser);
            }
        }));

        fileMenu.addItem(new MenuItem("Clear", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Clear shape
                collider.clear();
                selectShape(null);
                refreshShapes();
            }
        }));

        fileMenu.addItem(new MenuItem("Exit", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Remove editor from table
                exit();
            }
        }));
    }

    private void initProperties(){
        // Properties menu
        VisTable settings = new VisTable();
        settings.top();

        // Editor properties
        snapField = new VisValidatableTextField(true, Validators.FLOATS);
        snapField.setText(String.valueOf(snapDistance));
        snapField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                try {
                    snapDistance = Float.parseFloat(((VisTextField)a).getText());
                } catch(NumberFormatException exception){}
            }
        });
        settings.add(new VisLabel("Snap Distance")).pad(10).right();
        settings.add(snapField).expandX().fillX().padRight(10).row();

        // Shape properties
        frictionField = new VisValidatableTextField(true, Validators.FLOATS);
        frictionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                try {
                    shape.friction = Float.parseFloat(((VisTextField)a).getText());
                } catch(NumberFormatException exception){}
            }
        });
        settings.add(new VisLabel("Friction")).pad(10).right();
        settings.add(frictionField).expandX().fillX().padRight(10).row();
        
        restitutionField = new VisValidatableTextField(true, Validators.FLOATS);
        restitutionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                try {
                    shape.restitution = Float.parseFloat(((VisTextField)a).getText());
                } catch(NumberFormatException exception){}
            }
        });
        settings.add(new VisLabel("Restitution")).pad(10).right();
        settings.add(restitutionField).expandX().fillX().padRight(10).row();

        densityField = new VisValidatableTextField(true, Validators.FLOATS);
        densityField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                try {
                    shape.density = Float.parseFloat(((VisTextField)a).getText());
                } catch(NumberFormatException exception){}
            }
        });
        settings.add(new VisLabel("Density")).pad(10).right();
        settings.add(densityField).expandX().fillX().padRight(10).row();
        
        convexCheck = new VisCheckBox("Convex");
        convexCheck.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                shape.convex = convexCheck.isChecked();
                loadShapeOutlines();
            }
        });
        settings.add(convexCheck).expandX().fillX().padRight(10);

        sensorCheck = new VisCheckBox("Sensor");
        sensorCheck.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                shape.sensor = sensorCheck.isChecked();
            }
        });
        settings.add(sensorCheck).expandX().fillX().padRight(10).row();

        // Shape UI
        shapesList = new VerticalGroup();
        settings.add(new VisLabel("Shapes")).pad(10, 10, 0, 10).center().colspan(2).row();

        VisTextButton newShapeBtn = new VisTextButton("New Shape");
        newShapeBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                shape = collider.addPolygon();
                selectShape(shape);
                refreshShapes();
            }
        });
        settings.add(new VisLabel("New Shape")).pad(10).right();
        settings.add(newShapeBtn).expandX().fillX().padRight(10).row();

        VisTextButton delShapeBtn = new VisTextButton("Delete Shape");
        delShapeBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                for(int i = 0; i < collider.getShapeCount(); i++){
                    if(collider.getShape(i) == shape){
                        shapesList.removeActorAt(i, true);
                        collider.removeShape(i);
                        break;
                    }
                }
                
                refreshShapes();
                selectShape(null);
            }
        });
        settings.add(new VisLabel("Delete Shape")).pad(10).right();
        settings.add(delShapeBtn).expandX().fillX().padRight(10).row();
        settings.add(shapesList).expandX().fillX().pad(10).colspan(2).row();

        splitPane = new VisSplitPane(settings, new VisTable(), false);
        splitPane.setSplitAmount(0.3f);
        this.add(splitPane).fill().expand().row();
    }

    private void initEditor(){
        // Create a new stage for the editor to function in
        editorStage = new Stage();

        editorCamera = (OrthographicCamera)editorStage.getCamera();
        editorCamera.position.set(0, 0, 0);
        editorCamera.zoom = 0.1f;
        editorCamera.update();
        
        referenceTexture = new Texture("./assets/textures/dev_texture.png");
        referenceImage = new Image(referenceTexture);
        referenceImage.setPosition(referenceImage.getWidth() / -2, referenceImage.getHeight() / -2);
        editorStage.addActor(referenceImage);

        editorStage.addActor(shapeOutlines);

        cursor = new Cursor();
        editorStage.addActor(cursor);
    }

    private void initControls(InputMultiplexer inputs){
        inputs.addProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(button == Buttons.MIDDLE){
                    panningVector = new Vector2(screenX, screenY);
                    return true;
                }

                if(shape == null) return false;
                if(shape instanceof PolygonShape) return false;
                PolygonShape polyShape = (PolygonShape)shape;

                if(button == Buttons.LEFT){
                    polyShape.addVertex(new Vector2(cursor.getX(), cursor.getY()));
                    polyShape.simplify();
                    loadShapeOutlines();
                } else if(button == Buttons.RIGHT){
                    polyShape.removeVertex(new Vector2(cursor.getX(), cursor.getY()));
                    polyShape.simplify();
                    loadShapeOutlines();
                }

                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(panningVector != null){
                    panningVector = null;
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(panningVector != null){
                    panningVector.sub(screenX, screenY).scl(editorCamera.zoom);
                    editorCamera.position.add(panningVector.x, -panningVector.y, 0);
                    editorCamera.update();
                    panningVector.set(screenX, screenY);
                }

                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                Vector2 pos = editorStage.screenToStageCoordinates(new Vector2(screenX, screenY));
                cursor.setPosition((int)(pos.x / snapDistance) * snapDistance, (int)(pos.y / snapDistance) * snapDistance);
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                editorCamera.zoom = Math.min(Math.max(editorCamera.zoom + amountY * 0.01f, 0.01f), 50);
                return true;
            }
        });
    }

    // Constructor
    public PhysicsEditor(InputMultiplexer inputs){
        super();
        this.setFillParent(true);
        initMenu();
        initProperties();
        initEditor();
        initControls(inputs);
    }

    // Functions
    @Override
    public void draw(Batch batch, float parentAlpha){
        float ratio = getHeight() / getWidth();
        Rectangle bounds = splitPane.getSecondWidgetBounds();
        editorStage.getViewport().setScreenBounds((int)bounds.getX(), (int)bounds.getY(), (int)bounds.getWidth(), (int)bounds.getWidth());
        editorStage.getViewport().setWorldSize(1280, 720 / ratio);
        editorStage.getViewport().apply(false);
        editorStage.draw();

        this.getStage().getCamera().update();
        this.getStage().getViewport().apply();
        batch.setProjectionMatrix(this.getStage().getCamera().combined);
        super.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta){
        super.act(delta);
        editorStage.act(delta);
    }

    public void exit(){}
}

package com.alicornlunaa.spacegame.scenes.PartEditor;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;

/**
 * The part editor has four modes so far. The exterior controls the shape given to the universe
 * world. It is a dynamic shape. The interior editor controls the shape while inside the ship,
 * the static body. The Attachment editor changes the attachment point positions and the
 * animator sets different states for different icons to be shown.
 */
public class PartEditor implements Screen {

    // Variables
    private final App game;
    private Stage ui;
    private Stage editor;
    private InputMultiplexer inputs = new InputMultiplexer();

    // UI variables
    private VisTable root;
    private FileChooser fileChooser;
    private TabbedPane tabs;
    private VisSplitPane splitPane;
    private VisTable partSettings;
    private Stack editorPanes;

    // Editor variables
    private enum EditType { SHAPE_EDITOR_WORLD, SHAPE_EDITOR_INTERIOR, ATTACHMENT_EDITOR, ANIMATOR };
    private EditType mode = EditType.SHAPE_EDITOR_WORLD;
    private InputListener currentControl;
    private Texture referenceImage;
    private Array<PhysShape> worldShapes = new Array<>();
    private Array<PhysShape> interiorShapes = new Array<>();
    private Array<Vector2> attachmentPoints = new Array<>();
    private Vector2 cursor = new Vector2();
    private OrthographicCamera editorCam;

    private String type = "PART_TYPE";
    private String id = "PART_ID";
    private String name = "PART_NAME";
    private String desc = "PART_DESCRIPTION";
    private float density = 0.1f;
    private float scale = 1.0f;

    // Shape editor variables
    private PopupMenu shapePopup;
    private int editingVertexShapeID = -1;
    private int editingVertexID = -1;
    private boolean lineStarted = false;
    private Vector2 start = new Vector2();
    private Vector2 point1 = new Vector2();
    private PhysShape currentShape;
    private InputListener worldEditorControls;
    private InputListener interiorEditorControls;

    // Attachment editor variables
    private EditType previousType;
    private int selectedAttachment = -1;
    private boolean selectingAttachment = false;
    private InputListener attachmentEditorControls;

    // Private functions
    private void savePart(FileHandle handle){
        JSONObject o = new JSONObject();

        // Save exterior polygons
        JSONArray exterior = new JSONArray();
        for(PhysShape s : worldShapes){
            exterior.put(s.serialize());
        }
        o.put("externalShape", exterior);

        // Save interior polygons
        JSONArray interior = new JSONArray();
        for(PhysShape s : interiorShapes){
            interior.put(s.serialize());
        }
        o.put("internalShape", interior);

        // Save attachment points
        JSONArray attaches = new JSONArray();
        for(Vector2 v : attachmentPoints){
            JSONObject p = new JSONObject();
            p.put("x", v.x);
            p.put("y", v.y);
            attaches.put(p);
        }
        o.put("attachmentPoints", attaches);
        
        // Basic data
        o.put("type", type);
        o.put("id", id);
        o.put("name", name);
        o.put("desc", desc);
        o.put("scale", scale);
        o.put("density", density);
        o.put("metadata", new JSONObject());
        
        handle.writeString(o.toString(4), false);
    }

    private void loadPart(FileHandle handle){
        addCategory("Test");
    }

    private void changeMode(EditType type){
        mode = type;
        editor.removeListener(currentControl);

        if(type == EditType.SHAPE_EDITOR_INTERIOR){
            currentControl = interiorEditorControls;
        } else if(type == EditType.SHAPE_EDITOR_WORLD){
            currentControl = worldEditorControls;
        } else if(type == EditType.ATTACHMENT_EDITOR){
            currentControl = attachmentEditorControls;
        }
        
        editor.addListener(currentControl);
    }

    private void addCategory(String name){
        /** Adds category to the editors tabs */

    }

    private void initUI(){
        // Initialize UI
        ui = new Stage(new ScreenViewport());
        root = new VisTable();
        root.setFillParent(true);
        ui.addActor(root);
        ui.setDebugAll(true);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu modeMenu = new Menu("Mode");
        Menu settingsMenu = new Menu("Settings");
        menuBar.addMenu(fileMenu);
        menuBar.addMenu(modeMenu);
        menuBar.addMenu(settingsMenu);
        root.add(menuBar.getTable()).fillX().expandX().top().row();

        tabs = new TabbedPane();
        root.add(tabs.getTable()).fillX().expandX().top().row();

        partSettings = new VisTable();
        partSettings.add(new VisTextButton("TEST")).expandX().fillX().pad(10);

        editorPanes = new Stack();
        editorPanes.add(new AttachmentEditor(game));

        splitPane = new VisSplitPane(partSettings, editorPanes, false);
        splitPane.setSplitAmount(0.25f);
        root.add(splitPane).fill().expand().row();

        fileChooser = new FileChooser("Part Editor", Mode.OPEN);
        fileChooser.setSelectionMode(SelectionMode.FILES);

        fileMenu.addItem(new MenuItem("New", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.SAVE);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle bodyHandle = files.first();
                        savePart(bodyHandle);
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
                        FileHandle bodyHandle = files.first();
                        savePart(bodyHandle);
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
                        FileHandle bodyHandle = files.first();
                        loadPart(bodyHandle);
                    }
                });
                
                ui.addActor(fileChooser);
            }
        }));

        modeMenu.addItem(new MenuItem("Exterior Shape", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.SHAPE_EDITOR_WORLD);
            }
        }));

        modeMenu.addItem(new MenuItem("Interior Shape", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.SHAPE_EDITOR_INTERIOR);
            }
        }));

        modeMenu.addItem(new MenuItem("Attachment Points", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.ATTACHMENT_EDITOR);
            }
        }));

        modeMenu.addItem(new MenuItem("Animation", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.ANIMATOR);
            }
        }));
        
        settingsMenu.addItem(new MenuItem("Type", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                Dialogs.showInputDialog(ui, "Settings", "Enter a part type", new InputDialogListener(){
                    @Override
                    public void finished(String input){
                        type = input;
                    }

                    @Override
                    public void canceled() {}
                });
            }
        }));
        
        settingsMenu.addItem(new MenuItem("ID", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                Dialogs.showInputDialog(ui, "Settings", "Enter a part ID", new InputDialogListener(){
                    @Override
                    public void finished(String input){
                        id = input;
                    }

                    @Override
                    public void canceled() {}
                });
            }
        }));
        
        settingsMenu.addItem(new MenuItem("Name", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                Dialogs.showInputDialog(ui, "Settings", "Enter a part name", new InputDialogListener(){
                    @Override
                    public void finished(String input){
                        name = input;
                    }

                    @Override
                    public void canceled() {}
                });
            }
        }));
        
        settingsMenu.addItem(new MenuItem("Description", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                Dialogs.showInputDialog(ui, "Settings", "Enter a part description", new InputDialogListener(){
                    @Override
                    public void finished(String input){
                        desc = input;
                    }

                    @Override
                    public void canceled() {}
                });
            }
        }));
        
        settingsMenu.addItem(new MenuItem("Density", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                Dialogs.showInputDialog(ui, "Settings", "Enter a part density", new InputDialogListener(){
                    @Override
                    public void finished(String input){
                        density = Float.parseFloat(input);
                    }

                    @Override
                    public void canceled() {}
                });
            }
        }));
        
        settingsMenu.addItem(new MenuItem("Scale", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                Dialogs.showInputDialog(ui, "Settings", "Enter a part scale", new InputDialogListener(){
                    @Override
                    public void finished(String input){
                        scale = Float.parseFloat(input);
                    }

                    @Override
                    public void canceled() {}
                });
            }
        }));

        // Initialize controls
        inputs.addProcessor(ui);
    }

    private void renderUI(float delta){
        ui.getViewport().apply();
        ui.act(delta);
        ui.draw();
    }

    private void initEditor(){
        // Create editor variables
        editor = new Stage(new FillViewport(640, 360));
        editorCam = (OrthographicCamera)editor.getCamera();
        currentShape = new PhysShape(game.shapeRenderer);

        shapePopup = new PopupMenu();
        shapePopup.addItem(new MenuItem("Delete", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                interiorShapes.removeIndex(editingVertexShapeID);
                editingVertexShapeID = -1;
            }
        }));
        shapePopup.addItem(new MenuItem("Set disabled when", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                selectingAttachment = true;
                previousType = EditType.SHAPE_EDITOR_INTERIOR;
                changeMode(EditType.ATTACHMENT_EDITOR);
            }
        }));
        
        // Controls
        worldEditorControls = new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                editorCam.zoom = Math.min(Math.max(editorCam.zoom + (amountY / 30), 0.05f), 3.0f);
                editorCam.update();
                return true;
            }

            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button){
                // Remove a shape
                if(button == Buttons.RIGHT){
                    if(editingVertexID == -1){
                        for(int i = 0; i < worldShapes.size; i++){
                            for(int j = 0; j < worldShapes.get(i).vertices.size; j++){
                                if(!cursor.equals(worldShapes.get(i).vertices.get(j))) continue;
                                worldShapes.removeIndex(i);
                                i--;
                                break;
                            }
                        }
                    }
                }

                // Line editor
                if(button == Buttons.LEFT){
                    if(!lineStarted){
                        // Check if you clicked on a vertex to edit its position
                        boolean thisChanged = false;
                        if(editingVertexID == -1){
                            for(int i = 0; i < worldShapes.size; i++){
                                for(int j = 0; j < worldShapes.get(i).vertices.size; j++){
                                    if(cursor.equals(worldShapes.get(i).vertices.get(j))){
                                        editingVertexShapeID = i;
                                        editingVertexID = j;
                                        thisChanged = true;
                                    }
                                }
                            }
                        }

                        if(editingVertexShapeID == -1){
                            // Create new line
                            point1.set(cursor);
                            start.set(point1);

                            currentShape.vertices.clear();
                            currentShape.vertices.add(point1.cpy());

                            lineStarted = true;
                        } else if(!thisChanged){
                            editingVertexShapeID = -1;
                            editingVertexID = -1;
                        }
                    } else {
                        // End line
                        currentShape.vertices.add(cursor.cpy());
                        point1.set(cursor);

                        if(point1.equals(start)){
                            // End the shape
                            lineStarted = false;
                            worldShapes.add(new PhysShape(game.shapeRenderer, currentShape));
                            currentShape.vertices.clear();
                        }
                    }

                    return true;
                }

                return false;
            }
        };
        editor.addListener(worldEditorControls);
        currentControl = worldEditorControls;

        interiorEditorControls = new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                editorCam.zoom = Math.min(Math.max(editorCam.zoom + (amountY / 30), 0.05f), 3.0f);
                editorCam.update();
                return true;
            }

            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button){
                // Remove a shape
                if(button == Buttons.RIGHT){
                    if(editingVertexID == -1){
                        for(int i = 0; i < interiorShapes.size; i++){
                            for(int j = 0; j < interiorShapes.get(i).vertices.size; j++){
                                if(!cursor.equals(interiorShapes.get(i).vertices.get(j))) continue;
                                editingVertexShapeID = i;
                                shapePopup.showMenu(ui, x, y);
                            }
                        }
                    }
                }

                // Line editor
                if(button == Buttons.LEFT){
                    if(!lineStarted){
                        // Check if you clicked on a vertex to edit its position
                        boolean thisChanged = false;
                        if(editingVertexID == -1){
                            for(int i = 0; i < interiorShapes.size; i++){
                                for(int j = 0; j < interiorShapes.get(i).vertices.size; j++){
                                    if(cursor.equals(interiorShapes.get(i).vertices.get(j))){
                                        editingVertexShapeID = i;
                                        editingVertexID = j;
                                        thisChanged = true;
                                    }
                                }
                            }
                        }

                        if(editingVertexID == -1){
                            // Create new line
                            point1.set(cursor);
                            start.set(point1);

                            currentShape.vertices.clear();
                            currentShape.vertices.add(point1.cpy());

                            lineStarted = true;
                        } else if(!thisChanged){
                            editingVertexShapeID = -1;
                            editingVertexID = -1;
                        }
                    } else {
                        // End line
                        currentShape.vertices.add(cursor.cpy());
                        point1.set(cursor);

                        if(point1.equals(start)){
                            // End the shape
                            lineStarted = false;
                            interiorShapes.add(new PhysShape(game.shapeRenderer, currentShape));
                            currentShape.vertices.clear();
                        }
                    }

                    return true;
                }

                return false;
            }
        };

        attachmentEditorControls = new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                editorCam.zoom = Math.min(Math.max(editorCam.zoom + (amountY / 30), 0.05f), 3.0f);
                editorCam.update();
                return true;
            }

            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button){
                // Remove a point
                if(button == Buttons.RIGHT){
                    if(editingVertexID == -1){
                        for(int i = 0; i < attachmentPoints.size; i++){
                            if(!cursor.equals(attachmentPoints.get(i))) continue;
                            attachmentPoints.removeIndex(i);
                            i--;
                            break;
                        }
                    }

                    return true;
                }

                // Line editor
                if(button == Buttons.LEFT){
                    if(selectingAttachment){
                        for(int i = 0; i < attachmentPoints.size; i++){
                            if(!cursor.equals(attachmentPoints.get(i))) continue;
                            selectedAttachment = i;
                            interiorShapes.get(i).disableWhen = selectedAttachment;
                            editingVertexShapeID = -1;
                            changeMode(previousType);
                            break;
                        }
                    } else {
                        attachmentPoints.add(cursor.cpy());
                    }

                    return true;
                }

                return false;
            }
        };

        inputs.addProcessor(editor);
    }

    private void renderShapeEditorWorld(float delta){
        Batch batch = editor.getBatch();
        batch.setProjectionMatrix(editor.getCamera().combined);
        batch.setTransformMatrix(new Matrix4().translate(editor.getWidth() / 2, editor.getHeight() / 2, 0));
        batch.begin();
        batch.draw(referenceImage, referenceImage.getWidth() / -2, referenceImage.getHeight() / -2);
        batch.end();

        ShapeRenderer s = game.shapeRenderer;
        s.setProjectionMatrix(editor.getCamera().combined);
        s.setTransformMatrix(batch.getTransformMatrix());
        s.begin(ShapeType.Filled);

        if(editingVertexShapeID != -1){
            worldShapes.get(editingVertexShapeID).vertices.get(editingVertexID).set(cursor);
        }
        
        if(lineStarted){
            s.setColor(Color.RED);
            s.circle(point1.x, point1.y, 0.5f);
            s.rectLine(point1, cursor, 0.4f);
        } else {
            s.setColor(Color.CYAN);
            s.circle(cursor.x, cursor.y, 0.5f);
        }

        for(int i = 0; i < currentShape.vertices.size; i++){
            if((i + 1) % currentShape.vertices.size == 0) continue;
            Vector2 p1 = currentShape.vertices.get(i);
            Vector2 p2 = currentShape.vertices.get((i + 1) % currentShape.vertices.size);

            s.setColor(Color.YELLOW);
            s.circle(p1.x, p1.y, 0.5f);
            s.rectLine(p1, p2, 0.4f);
        }

        for(PhysShape shape : worldShapes){
            for(int i = 0; i < shape.vertices.size; i++){
                Vector2 p1 = shape.vertices.get(i);
                Vector2 p2 = shape.vertices.get((i + 1) % shape.vertices.size);

                s.setColor(Color.GREEN);
                s.circle(p1.x, p1.y, 0.5f);
                s.rectLine(p1, p2, 0.4f);
            }
        }

        s.end();
    }

    private void renderShapeEditorInterior(float delta){
        Batch batch = editor.getBatch();
        batch.setProjectionMatrix(editor.getCamera().combined);
        batch.setTransformMatrix(new Matrix4().translate(editor.getWidth() / 2, editor.getHeight() / 2, 0));
        batch.begin();
        batch.draw(referenceImage, referenceImage.getWidth() / -2, referenceImage.getHeight() / -2);
        batch.end();

        ShapeRenderer s = game.shapeRenderer;
        s.setProjectionMatrix(editor.getCamera().combined);
        s.setTransformMatrix(batch.getTransformMatrix());
        s.begin(ShapeType.Filled);

        if(editingVertexID != -1){
            interiorShapes.get(editingVertexShapeID).vertices.get(editingVertexID).set(cursor);
        }
        
        if(lineStarted){
            s.setColor(Color.RED);
            s.circle(point1.x, point1.y, 0.5f);
            s.rectLine(point1, cursor, 0.4f);
        } else {
            s.setColor(Color.CYAN);
            s.circle(cursor.x, cursor.y, 0.5f);
        }

        for(int i = 0; i < currentShape.vertices.size; i++){
            if((i + 1) % currentShape.vertices.size == 0) continue;
            Vector2 p1 = currentShape.vertices.get(i);
            Vector2 p2 = currentShape.vertices.get((i + 1) % currentShape.vertices.size);

            s.setColor(Color.YELLOW);
            s.circle(p1.x, p1.y, 0.5f);
            s.rectLine(p1, p2, 0.4f);
        }

        for(PhysShape shape : interiorShapes){
            for(int i = 0; i < shape.vertices.size; i++){
                Vector2 p1 = shape.vertices.get(i);
                Vector2 p2 = shape.vertices.get((i + 1) % shape.vertices.size);

                s.setColor(Color.GREEN);
                s.circle(p1.x, p1.y, 0.5f);
                s.rectLine(p1, p2, 0.4f);
            }
        }

        s.end();

        editor.act();
        editor.draw();
    }
    
    private void renderAttachmentEditor(float delta){
        Batch batch = editor.getBatch();
        batch.setProjectionMatrix(editor.getCamera().combined);
        batch.setTransformMatrix(new Matrix4().translate(editor.getWidth() / 2, editor.getHeight() / 2, 0));
        batch.begin();
        batch.draw(referenceImage, referenceImage.getWidth() / -2, referenceImage.getHeight() / -2);
        batch.end();

        ShapeRenderer s = game.shapeRenderer;
        s.setProjectionMatrix(editor.getCamera().combined);
        s.setTransformMatrix(batch.getTransformMatrix());
        s.begin(ShapeType.Filled);

        s.setColor(Color.ORANGE);
        for(Vector2 a : attachmentPoints){
            s.circle(a.x, a.y, 0.5f);
        }

        s.setColor(Color.CYAN);
        s.circle(cursor.x, cursor.y, 0.5f);

        s.end();

        editor.act(delta);
        editor.draw();
    }

    private void renderAnimator(float delta){

    }
    
    // Constructor
    public PartEditor(final App game){
        // Initialize components
        this.game = game;
        referenceImage = new Texture(Gdx.files.internal("textures/parts/med_cmd_pod.png"));

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
        editor.getViewport().apply();
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        cursor = editor.screenToStageCoordinates(cursor.set(Gdx.input.getX(), Gdx.input.getY())).sub(editor.getWidth() / 2, editor.getHeight() / 2);
        cursor.set(Math.round(cursor.x), Math.round(cursor.y)); // Snap points

        if(mode == EditType.SHAPE_EDITOR_WORLD){
            renderShapeEditorWorld(delta);
        } else if(mode == EditType.SHAPE_EDITOR_INTERIOR){
            renderShapeEditorInterior(delta);
        } else if(mode == EditType.ATTACHMENT_EDITOR){
            renderAttachmentEditor(delta);
        } else if(mode == EditType.ANIMATOR){
            renderAnimator(delta);
        }

        ui.getViewport().apply();
        renderUI(delta);
    }

    @Override
    public void resize(int width, int height) {
        ui.getViewport().update(width, height, true);
        editor.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        editor.dispose();
        ui.dispose();
    }

    // Static
    
}

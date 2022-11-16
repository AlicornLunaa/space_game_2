package com.alicornlunaa.spacegame.scenes.PartEditor;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
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

    // Editor variables
    private enum EditType { SHAPE_EDITOR_WORLD, SHAPE_EDITOR_INTERIOR, ATTACHMENT_EDITOR, ANIMATOR };
    private EditType mode = EditType.SHAPE_EDITOR_WORLD;
    private Vector2 cursor = new Vector2();
    private HashMap<EditType, EditorPanel> panels = new HashMap<>();

    private JSONArray partList;
    private JSONObject partData;

    private String type = "PART_TYPE";
    private String id = "PART_ID";
    private String name = "PART_NAME";
    private String desc = "PART_DESCRIPTION";
    private float density = 0.1f;
    private float scale = 1.0f;
    private Texture exteriorTexture;
    private Texture interiorTexture;

    // Private functions
    private void saveParts(FileHandle handle){
        handle.writeString(partList.toString(4), false);
    }

    private void loadParts(FileHandle handle){
        // TODO: Load JSON data for parts
    }

    private void changeMode(EditType type){ mode = type; }

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

        VisTable placeholder = new VisTable();
        placeholder.setFillParent(true);
        placeholder.add().expand().fill();
        panels.put(EditType.ATTACHMENT_EDITOR, new AttachmentEditor(game));

        splitPane = new VisSplitPane(partSettings, placeholder, false);
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
                        // TODO: Implement logic to add a new part
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
                        saveParts(bodyHandle);
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
                        loadParts(bodyHandle);
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
    }

    // Constructor
    public PartEditor(final App game){
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

        cursor = editor.screenToStageCoordinates(cursor.set(Gdx.input.getX(), Gdx.input.getY())).sub(editor.getWidth() / 2, editor.getHeight() / 2);
        cursor.set(Math.round(cursor.x), Math.round(cursor.y)); // Snap points

        panels.get(mode).render(splitPane.getSecondWidgetBounds());
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
    
}

package com.alicornlunaa.spacegame.scenes.dev_kit;

import org.json.JSONArray;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.selene_engine.phys.Collider.Shape;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
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
    // Variables
    private FileChooser fileChooser;

    private Collider collider = new Collider();
    private @Null Shape shape = null;
    private float snapDistance = 2;
    private float partSize = 1;
    private Texture referenceTexture = new Texture(1, 1, Format.RGBA8888);
    private Image referenceImage;

    // Private functions
    private void save(FileHandle handle){
        handle.writeString(collider.serialize().toString(), false);
    }

    private void load(FileHandle handle){
        collider.clear();
        collider = new Collider(new JSONArray(handle.readString()));
    }

    private void initMenu(){
        // Start UI
        MenuBar menu = new MenuBar();
        this.add(menu.getTable()).fillX().expandX().top().row();
        
        Menu fileMenu = new Menu("File");
        menu.addMenu(fileMenu);
        
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
                        referenceTexture.dispose();
                        referenceTexture = new Texture(handle);
                        referenceImage.setDrawable(new TextureRegionDrawable(new TextureRegion(referenceTexture)));
                        referenceImage.setOrigin(Align.center);
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
                // shapeBeingEdited = -1;
                collider.clear();
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
        final VisTextField snapField = new VisValidatableTextField(true, Validators.FLOATS);
        snapField.setText(String.valueOf(snapDistance));
        snapField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                snapDistance = Float.parseFloat(((VisTextField)a).getText());
            }
        });
        settings.add(new VisLabel("Snap Distance")).pad(10).right();
        settings.add(snapField).expandX().fillX().padRight(10).row();

        // Shape properties
        final VisTextField frictionField = new VisValidatableTextField(true, Validators.FLOATS);
        frictionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
            }
        });
        settings.add(new VisLabel("Friction")).pad(10).right();
        settings.add(frictionField).expandX().fillX().padRight(10).row();
        
        final VisTextField restitutionField = new VisValidatableTextField(true, Validators.FLOATS);
        restitutionField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                shape.setRestitution(Float.parseFloat(((VisTextField)a).getText()));
            }
        });
        settings.add(new VisLabel("Restitution")).pad(10).right();
        settings.add(restitutionField).expandX().fillX().padRight(10).row();

        final VisTextField densityField = new VisValidatableTextField(Validators.FLOATS);
        densityField.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                shape.setDensity(Float.parseFloat(((VisTextField)a).getText()));
            }
        });
        settings.add(new VisLabel("Density")).pad(10).right();
        settings.add(densityField).expandX().fillX().padRight(10).row();
        
        final VisCheckBox convexCheck = new VisCheckBox("Convex");
        convexCheck.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                shape.setConvex(!shape.getConvex());
            }
        });
        settings.add(convexCheck).expandX().fillX().padRight(10);

        final VisCheckBox sensorCheck = new VisCheckBox("Sensor");
        sensorCheck.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(shape == null) return;
                shape.setSensor(!shape.getSensor());
            }
        });
        settings.add(sensorCheck).expandX().fillX().padRight(10).row();

        // Shape UI
        final VerticalGroup shapesList = new VerticalGroup();
        settings.add(new VisLabel("Shapes")).pad(10, 10, 0, 10).center().colspan(2).row();

        VisTextButton newShapeBtn = new VisTextButton("New Shape");
        newShapeBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                shape = collider.addShape();

                frictionField.setText(String.valueOf(shape.getFriction()));
                restitutionField.setText(String.valueOf(shape.getRestitution()));
                densityField.setText(String.valueOf(shape.getDensity()));
                convexCheck.setChecked(shape.getConvex());
                sensorCheck.setChecked(shape.getSensor());

                VisTextButton btn = new VisTextButton("Select " + collider.getShapeCount());
                btn.addListener(new ChangeListener(){
                    final Shape thisShape = shape;

                    @Override
                    public void changed(ChangeEvent e, Actor a){
                        PhysicsEditor.this.shape = thisShape;
                        frictionField.setText(String.valueOf(thisShape.getFriction()));
                        restitutionField.setText(String.valueOf(thisShape.getRestitution()));
                        densityField.setText(String.valueOf(thisShape.getDensity()));
                        convexCheck.setChecked(thisShape.getConvex());
                        sensorCheck.setChecked(thisShape.getSensor());
                    }
                });
                btn.pad(5);
                shapesList.addActor(btn);
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
                
                for(int i = 0; i < shapesList.getChildren().size; i++){
                    VisTextButton btn = ((VisTextButton)shapesList.getChild(i));
                    btn.setText("Select " + (i + 1));
                }

                shape = null;
            }
        });
        settings.add(new VisLabel("Delete Shape")).pad(10).right();
        settings.add(delShapeBtn).expandX().fillX().padRight(10).row();
        settings.add(shapesList).expandX().fillX().pad(10).colspan(2).row();

        VisTable placeholder = new VisTable();
        referenceImage = new Image(referenceTexture);
        placeholder.add(referenceImage).expand();

        VisSplitPane splitPane = new VisSplitPane(settings, placeholder, false);
        splitPane.setSplitAmount(0.3f);
        this.add(splitPane).fill().expand().row();
    }

    private void initControls(InputMultiplexer inputs){
        inputs.addProcessor(0, new InputAdapter(){
            @Override
            public boolean scrolled(float amountX, float amountY) {
                partSize = Math.min(Math.max(partSize - amountY * 0.25f, 0.1f), 500);
                referenceImage.setScale(partSize);
                referenceImage.setOrigin(Align.center);
                return true;
            }
        });
    }

    // Constructor
    public PhysicsEditor(InputMultiplexer inputs){
        super();
        this.setFillParent(true);
        this.debug();
        initMenu();
        initProperties();
        initControls(inputs);
    }

    // Functions
    public void exit(){}
}

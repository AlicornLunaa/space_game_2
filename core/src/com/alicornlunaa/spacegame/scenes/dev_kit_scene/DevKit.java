package com.alicornlunaa.spacegame.scenes.dev_kit_scene;

import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class DevKit extends BaseScene {
    // Variables
    private Stage root;
    private VisTable mainMenu;
    private Texture logoTexture;

    // Private functions
    private void initializeRoot(){
        root = new Stage(new ScreenViewport());
        root.setDebugAll(true);
        inputs.addProcessor(root);

        mainMenu = new VisTable();
        mainMenu.setFillParent(true);
        root.addActor(mainMenu);

        Image logo = new Image(logoTexture);
        logo.scaleBy(2);
        mainMenu.row().expandX().top().left().pad(20);
        mainMenu.add(logo);

        VisTextButton btn = new VisTextButton("Part Editor");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.clear();
                root.addActor(new PartEditor());
            }
        });
        mainMenu.row().left().pad(0, 40, 10, 40);
        mainMenu.add(btn);

        btn = new VisTextButton("Physics Editor");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.clear();
                root.addActor(new PhysicsEditor(inputs){
                    @Override
                    public void exit(){
                        inputs.setProcessors(root);
                        root.clear();
                        root.addActor(mainMenu);
                    }
                });
            }
        });
        mainMenu.row().left().pad(0, 40, 10, 40);
        mainMenu.add(btn);

        btn = new VisTextButton("Animation Editor");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.clear();
                root.addActor(new AnimationEditor());
            }
        });
        mainMenu.row().left().pad(0, 40, 10, 40);
        mainMenu.add(btn);

        btn = new VisTextButton("Shader Editor");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.clear();
                root.addActor(new ShaderEditor());
            }
        });
        mainMenu.row().left().pad(0, 40, 10, 40);
        mainMenu.add(btn);

        btn = new VisTextButton("Quit");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        mainMenu.row().left().pad(0, 40, 10, 40);
        mainMenu.add(btn);
    }

    // Constructor
    public DevKit(App game) {
        super(game);
        
        if(!VisUI.isLoaded())
            VisUI.load();

        logoTexture = new Texture("textures/dev_kit_logo.png");
        
        initializeRoot();
    }
    
    // Functions
    @Override
    public void render(float delta) {
        super.render(delta);

        root.getViewport().apply();
        root.act(delta);
        root.draw();
    }

    @Override
    public void resize(int width, int height) {
        root.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        logoTexture.dispose();
    }
}

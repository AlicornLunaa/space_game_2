package com.alicornlunaa.space_game.scenes;

import java.util.ArrayList;

import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.space_game.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class OptionsPanel extends Stage {
    private InputProcessor oldProcessor;
    private Table ui;
    private TextButton applyButton;
    private TextButton saveButton;
    private TextButton cancelButton;

    public OptionsPanel(final Assets manager, final ArrayList<Stage> stages, final Skin skin){
        super(new ScreenViewport());
        oldProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(this);

        float scale = 1;
        ui = new Table();
        ui.setFillParent(true);
        ui.setDebug(true);

        saveButton = new TextButton("Save", skin);
        saveButton.setSize(64 * scale, 32 * scale);
        saveButton.setPosition(getWidth() - saveButton.getWidth(), 5);
        saveButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                Gdx.files.local("./saves/settings/spacegame_controls.json").writeString(App.instance.inputs.serialize().toString(1), false);
                stages.get(stages.size() - 1).dispose();
                stages.remove(stages.size() - 1);
            }
        });
        this.addActor(saveButton);

        applyButton = new TextButton("Apply", skin);
        applyButton.setSize(64 * scale, 32 * scale);
        applyButton.setPosition(getWidth() - applyButton.getWidth() - saveButton.getWidth() - 5, 5);
        applyButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                Gdx.files.local("./saves/settings/spacegame_controls.json").writeString(App.instance.inputs.serialize().toString(1), false);
            }
        });
        this.addActor(applyButton);

        cancelButton = new TextButton("Cancel", skin);
        cancelButton.setSize(64 * scale, 32 * scale);
        cancelButton.setPosition(5, 5);
        cancelButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                stages.get(stages.size() - 1).dispose();
                stages.remove(stages.size() - 1);
            }
        });
        this.addActor(cancelButton);

        this.addActor(ui);
    }

    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

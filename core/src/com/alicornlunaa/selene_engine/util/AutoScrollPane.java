package com.alicornlunaa.selene_engine.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Null;

public class AutoScrollPane extends ScrollPane {
    public AutoScrollPane(@Null Actor widget){
        super(widget);

        addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(getStage() == null) return;
                getStage().setScrollFocus(AutoScrollPane.this);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(getStage() == null) return;
                getStage().setScrollFocus(null);
            }
        });
    }
}

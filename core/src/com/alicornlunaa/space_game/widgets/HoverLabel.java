package com.alicornlunaa.space_game.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

public class HoverLabel extends Label {
    // Variables
    public float hoverTimeToAppear = 1.f;

    private Actor parent;
    private float timeHovered = 0.f;
    private boolean hovered = false;
    private Vector2 pos = new Vector2();

    private InputListener listener = new InputListener() {
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            HoverLabel.this.timeHovered = 0.f;
            hovered = true;
            setVisible(true);
        }

        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            hovered = false;
            setVisible(false);
        }
    };

    // Constructor
    public HoverLabel(Actor parent, CharSequence text, Skin skin, float time) {
        super(text, skin);
        this.parent = parent;
        setVisible(false);
        setTouchable(Touchable.disabled);
        setAlignment(Align.topLeft);
        parent.addListener(listener);
        hoverTimeToAppear = time;
    }

    // Functions
    public void detach(){
        parent.removeListener(listener);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(hovered){
            timeHovered += Gdx.graphics.getDeltaTime();

            if(timeHovered > hoverTimeToAppear){
                pos.set(getStage().screenToStageCoordinates(pos.set(Gdx.input.getX(), Gdx.input.getY())));
                super.draw(batch, parentAlpha);
                super.setPosition(pos.x + 16, pos.y - getHeight());
            }
        }
	}
}

package com.alicornlunaa.spacegame.scenes.dev.part_editor;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.widget.VisTable;

public class EditorPanel extends VisTable {

    // Variables
    protected final PartEditor editor;
    protected final ShapeRenderer render;
    protected final VisTable content;
    protected InputProcessor controls;
    protected Vector2 cursor = new Vector2();
    private Rectangle bounds = new Rectangle();

    // Constructor
    public EditorPanel(final App game, final PartEditor editor, final VisTable content){
        super();
        this.editor = editor;
        this.render = game.shapeRenderer;
        this.content = content;
        setFillParent(true);
        add().expand().fill().row();
    }

    // Functions
    public InputProcessor getInputListener(){ return controls; }

    public void render(final Rectangle bounds, JSONObject part, Vector2 corner, Vector2 cursor){
        this.bounds = bounds;
        this.cursor = cursor;
    }

    public void updateContent(){
        content.clear();
    }

    @Override
    public Actor hit(float x, float y, boolean touchable){
        return (bounds.x < x && x < bounds.x + bounds.width && bounds.y < y && y < bounds.y + bounds.height) ? this : null;
    }
    
}

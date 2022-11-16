package com.alicornlunaa.spacegame.scenes.PartEditor;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.kotcrab.vis.ui.widget.VisTable;

public class AttachmentEditor extends VisTable {

    // Variables
    private final App game;

    // Constructor
    public AttachmentEditor(final App game){
        super();
        this.game = game;
        setFillParent(true);
    }

    // Functions
    public void render(float delta){
        game.shapeRenderer.begin(ShapeType.Filled);
        game.shapeRenderer.circle(getWidth() / -4, 0, 2.0f);
        game.shapeRenderer.end();
    }
    
}

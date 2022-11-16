package com.alicornlunaa.spacegame.scenes.PartEditor;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.kotcrab.vis.ui.widget.VisTable;

public class EditorPanel extends VisTable {

    // Variables
    protected final ShapeRenderer render;

    // Constructor
    public EditorPanel(final App game){
        super();
        this.render = game.shapeRenderer;
        setFillParent(true);
        add().expand().fill().row();
    }

    // Functions
    public void render(final Rectangle bounds){}
    
}

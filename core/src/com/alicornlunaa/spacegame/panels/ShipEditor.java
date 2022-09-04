package com.alicornlunaa.spacegame.panels;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/*
 * The ShipEditor class is a stage used to render a window used to create
 * and edit 2d ships.
 */
public class ShipEditor extends Stage {
    // Variables
    private InputMultiplexer inputs;

    // Constructor
    public ShipEditor(InputMultiplexer inputs){
        super(new ScreenViewport());

        this.inputs = inputs;

        inputs.addProcessor(this);
    }

    // Functions
    @Override
    public void dispose(){
        super.dispose();
        inputs.removeProcessor(this);
    }
}

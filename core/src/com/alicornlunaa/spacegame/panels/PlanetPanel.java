package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final Planet planetRef;

    // Constructor
    public PlanetPanel(final App game, final Planet planetRef){
        super(new ScreenViewport());
        this.planetRef = planetRef;
    }

    // Functions
    @Override
    public void draw(){
        super.draw();

        // Draw every map tile
        Batch batch = getBatch();
        batch.begin();
        batch.setProjectionMatrix(getCamera().combined);
        batch.setTransformMatrix(new Matrix4().translate(getWidth() / 2, getHeight() / 2, 0));

        for(Planet.Tile tile : planetRef.getMap().values()){
            tile.draw(batch);
        }

        batch.end();
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

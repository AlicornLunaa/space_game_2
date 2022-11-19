package com.alicornlunaa.spacegame.objects.Ship.interior;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

/** Drawable class to render the inside of a ship
 * Will also store all user-created objects
 */
public class Interior {

    // Variables
    private final App game;

    private Array<InteriorCell> cells = new Array<>();
    private World internalWorld;
    private float physAccumulator = 0.0f;
    private Body internalBody;

    // Constructor
    public Interior(final App game, final Ship ship){
        // Construct interior cells based on the ship
        this.game = game;
        internalWorld = new World(new Vector2(), true);

        BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		internalBody = internalWorld.createBody(def);


    }
    
    // Functions
    public void update(float delta){
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            internalWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }
    }

    public void draw(Batch batch){
        for(InteriorCell c : cells){
            c.draw(batch);
        }
        
        game.debug.render(internalWorld, batch.getProjectionMatrix().cpy().scl(Constants.SHIP_PPM));
    }
    
}

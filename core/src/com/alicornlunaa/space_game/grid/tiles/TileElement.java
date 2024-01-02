package com.alicornlunaa.space_game.grid.tiles;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Null;

public class TileElement extends AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };
    public static enum Shape {
        // Enumerations
        SQUARE(Collider.box(0, 0, Constants.TILE_SIZE / 2.f, Constants.TILE_SIZE / 2.f, 0)),
        SLOPE(Collider.triangle(0, 0, Constants.TILE_SIZE / 2.f, Constants.TILE_SIZE / 2.f, 0));

        // Variables
        public final Collider collider;

        // Constructor
        private Shape(Collider collider){
            this.collider = new Collider(collider);
        }
    };
    
    // Static variables
    public static @Null TextureRegion squareTexture;
    public static @Null TextureRegion slopeTexture;

    // Variables
    public final Element element;
    public final State state;
    public Shape shape = Shape.SQUARE;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms
    public Vector2 floatingPosition = new Vector2(0.5f, 0.5f); // Keeps the decimals of the current position to allow small movements
    public Vector2 velocity = new Vector2();

    // Constructor
    public TileElement(Element element, State state, TextureRegion texture){
        super("element_" + element, 1, 1, 0);
        this.element = element;
        this.state = state;
    }

    public TileElement(TileElement other){
        super(other);
        this.element = other.element;
        this.state = other.state;
        this.temperature = other.temperature;
        this.mass = other.mass;
        this.floatingPosition = other.floatingPosition.cpy();
        this.velocity = other.velocity.cpy();
    }

    // Functions
    private TextureRegion getTexture(){
        switch(shape){
            default:
                if(squareTexture == null)
                    squareTexture = App.instance.atlas.findRegion("tiles/steel");
        
                return squareTexture;

                
            case SLOPE:
                if(slopeTexture == null)
                    slopeTexture = App.instance.atlas.findRegion("tiles/steel_slope");
        
                return slopeTexture;
        }
        
    }

    public void render(Batch batch, float deltaTime){
        batch.setColor(element.color);
        batch.draw(
            getTexture(),
            x * Constants.TILE_SIZE,
            y * Constants.TILE_SIZE,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE,
            Constants.TILE_SIZE,
            1,
            1,
            rotation * -90
        );
    }

    public void update(float deltaTime){
    }
}

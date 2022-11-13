package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet.Tile;
import com.alicornlunaa.spacegame.parts.ShipPart;
import com.alicornlunaa.spacegame.parts.ShipPart.Attachment;
import com.alicornlunaa.spacegame.states.ShipState;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * The ship class will hold the physical ship in space as well as
 * a tilemap and physics world for the player inside the ship
 */
public class Ship extends Entity {
    
    // Variables
    private final App game;
    private ShipPart rootPart = null;
    public ShipState state = new ShipState(); // Ship controls and stuff
    
    private World shipWorld;
    private float physAccumulator;
    private Body shipWorldBody;
    private HashMap<Vector2, Tile> shipMap = new HashMap<>();

    // Private functions
    private void generateInterior(){
        // Creates interior world and generates the interior squares
        shipWorld = new World(new Vector2(), true);

        BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		shipWorldBody = shipWorld.createBody(def);
        
        shipMap.put(new Vector2(0, 0), new Tile(game, shipWorldBody, 0, 0, 0, 0, Tile.TileType.STONE));
    }

    // Constructor
    public Ship(final App game, World world, float x, float y, float rotation){
        this.game = game;

        BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.position.set(x, y);
        def.angle = (float)Math.toRadians(rotation);
		setBody(world.createBody(def));

        // rootPart = ShipPart.spawn(game, "AERO", "MED_CMD_POD", body, state, new Vector2(), 0.0f);

        generateInterior();
    }

    // Interior functions
    public void drawWorld(Batch batch, float parentAlpha){
        for(Tile tile : shipMap.values()){
            tile.draw(batch);
        }

        game.player.draw(batch, parentAlpha);
        game.debug.render(shipWorld, batch.getProjectionMatrix().cpy().scl(Constants.SHIP_PPM));
    }

    public void updateWorld(float delta){
        // Step the physics world inside the ship
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            shipWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }

        game.player.act(delta);
    }

    public World getInteriorWorld(){ return shipWorld; }

    // Space functions
    private void assemble(ShipPart head){
        head.buildExterior(body);
        head.buildInterior(shipWorldBody);

        for(ShipPart.Attachment a : head.getAttachments()){
            if(a.getChild() != null){
                assemble(a.getChild());
            }
        }
    }

    public void assemble(){
        // Puts all the parts together with their respective physics bodies
        // Recursive wrapper
        for(Fixture f : body.getFixtureList()){
            body.destroyFixture(f);
        }

        if(rootPart == null) return;

        assemble(rootPart);
    }

    public ShipPart getRoot(){ return rootPart; }

    private void getPositions(ShipPart head, ArrayList<Vector2> posList, ArrayList<Attachment> attachList){
        if(head == null) return;

        for(ShipPart.Attachment a : head.getAttachments()){
            Vector2 pos = head.localToWorld(a.getPos().cpy().add(head.getOriginX(), head.getOriginY()));

            posList.add(pos);
            attachList.add(a);

            if(a.getChild() != null){
                getPositions(a.getChild(), posList, attachList);
            }
        }
    }
    
    public Attachment getClosestAttachment(Vector2 point, float radius){
        // Wrapper for recursive function
        if(rootPart == null) return null;

        ArrayList<Vector2> positions = new ArrayList<Vector2>();
        ArrayList<Attachment> lAttachments = new ArrayList<Attachment>();
        getPositions(rootPart, positions, lAttachments);

        Vector2 localPointer = this.worldToLocal(point);
        Vector2 closestPoint = positions.get(0);
        float minDistance = localPointer.dst2(closestPoint);
        Attachment closestAttachment = lAttachments.get(0);

        for(int i = 1; i < positions.size(); i++){
            Vector2 curPoint = positions.get(i);
            float curDist = localPointer.dst2(curPoint);
            Attachment curAttachment = lAttachments.get(i);

            if(curDist < minDistance){
                closestPoint = curPoint;
                closestAttachment = curAttachment;
                minDistance = curDist;
            }
        }

        if(minDistance < (radius * radius)){
            return closestAttachment;
        }

        return null;
    }

    private void drawPoints(ShipPart head, boolean draw){
        head.drawPoints(draw);

        for(ShipPart.Attachment a : head.getAttachments()){
            if(a.getChild() != null){
                drawPoints(a.getChild(), draw);
            }
        }
    }

    public void drawPoints(boolean draw){
        // Recursive wrapper
        if(rootPart == null) return;
        drawPoints(rootPart, draw);
    }

    public ShipPart getPartClicked(Vector2 pos){
        return rootPart.hit(pos, getTransform());
    }

    private ShipPart findParent(ShipPart head, ShipPart child){
        // Loop through the head's attachments
        for(Attachment a : head.getAttachments()){
            if(a.getChild() == null) continue; // Nothing attached, skip
            if(a.getChild() != child){
                // This is not the parent, check the child part
                ShipPart t = findParent(a.getChild(), child);

                if(t != null){
                    // Parent of child found, return it
                    return t;
                }
            } else {
                // This is the parent
                return head;
            }
        }

        // Everything looped through, none is the child
        return null;
    }
    
    public ShipPart findParent(ShipPart child){
        // Recursive wrapper
        if(rootPart == child) return null; // The root will never have a parent
        return findParent(rootPart, child);
    }

    public boolean save(String path){
        try {
            JSONObject data = new JSONObject();
            data.put("x", getX());
            data.put("y", getY());
            data.put("rotation", getRotation());
            data.put("rootPart", rootPart.serialize());

            FileHandle file = Gdx.files.local(path);
            file.writeString(data.toString(4), false);

            return true;
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error saving ship");
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean load(String path){
        try {
            // Read filedata
            FileHandle file = Gdx.files.local(path);
            JSONObject data = new JSONObject(file.readString());

            // Extract data
            // float x = data.getFloat("x");
            // float y = data.getFloat("y");
            // float rotation = data.getFloat("rotation");

            // Reset body
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);

            // setPosition(x, y);
            // setRotation(rotation);

            // Load body data
            JSONObject root = data.getJSONObject("rootPart");
            rootPart = ShipPart.unserialize(game, body, state, root);
            assemble();

            System.out.printf("Ship %s loaded\n", path);

            return true;
        } catch (GdxRuntimeException|JSONException e){
            System.out.println("Error reading ship");
            e.printStackTrace();
        }

        return false;
    }

    private void computeSAS(){
        // Reduce angular velocity with controls
        float angVel = body.getAngularVelocity();
        angVel = Math.min(Math.max(angVel * 2, -1), 1); // Clamp value

        if(Math.abs(angVel) <= 0.005f) angVel = 0;

        state.artifRoll = angVel;
    }

    @Override
    public void act(float delta){
        if(state.sas){
            computeSAS();
        } else {
            state.artifRoll = 0;
        }

        if(driver == null) return;

        if(rootPart != null){
            rootPart.act(delta);
        }

        // Ship controls
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_INCREASE_THROTTLE)){
            state.throttle = Math.min(state.throttle + 0.01f, 1);
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_DECREASE_THROTTLE)){
            state.throttle = Math.max(state.throttle - 0.01f, 0);
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_LEFT)){
            state.roll = -1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_RIGHT)){
            state.roll = 1;
        } else {
            state.roll = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_UP)){
            state.vertical = 1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_DOWN)){
            state.vertical = -1;
        } else {
            state.vertical = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_LEFT)){
            state.horizontal = -1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_RIGHT)){
            state.horizontal = 1;
        } else {
            state.horizontal = 0;
        }
        
        super.act(delta);
    }

    @Override
    protected void afterWorldChange(){
        rootPart.setParent(body, getPhysScale());
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Matrix3 transform = getTransform();
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform)));

        if(rootPart != null){
            rootPart.draw(batch, getColor().a * parentAlpha);
        }
        
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform.inv())));
    }

    @Override
    public boolean remove(){
        if(rootPart != null){
            rootPart.remove();
        }

        return super.remove();
    }

}

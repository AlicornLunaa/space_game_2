package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.parts.ShipPart;
import com.alicornlunaa.spacegame.parts.ShipPart.Attachment;
import com.alicornlunaa.spacegame.states.ShipState;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Ship extends Entity {
    
    // Variables
    private Body body;
    private ShipPart rootPart = null;
    public ShipState state = new ShipState(); // Ship controls and stuff

    private Assets manager;
    private PartManager partManager;

    // Constructor
    public Ship(Assets manager, PartManager partManager, World world, float x, float y, float rotation){
        this.manager = manager;
        this.partManager = partManager;

        BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.position.set(x, y);
        def.angle = rotation;
		body = world.createBody(def);

        setPosition(x, y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));

        rootPart = ShipPart.spawn(manager, partManager, "AERO", "MED_CMD_POD", body, state, new Vector2(0, 0), 0.f);
        ShipPart fuselage = rootPart.attachPart(ShipPart.spawn(manager, partManager, "STRUCTURAL", "BSC_FUSELAGE", body, state, new Vector2(0, 0), 0.f), 1, 0);
        fuselage.attachPart(ShipPart.spawn(manager, partManager, "THRUSTER", "BSC_THRUSTER", body, state, new Vector2(0, 0), 0.f), 0, 0);
        assemble();
    }

    // Functions
    private void assemble(ShipPart head){
        FixtureDef def = new FixtureDef();
        def.shape = head.getShape();
        def.density = 1;
        body.createFixture(def);

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

        assemble(rootPart);
    }

    public ShipPart getRoot(){ return rootPart; }

    private void getPositions(ShipPart head, ArrayList<Vector2> posList, ArrayList<Attachment> attachList){
        for(ShipPart.Attachment a : head.getAttachments()){
            Vector2 pos = head.localToWorld(new Vector2(a.getPos()));

            posList.add(pos);
            attachList.add(a);

            if(a.getChild() != null){
                getPositions(a.getChild(), posList, attachList);
            }
        }
    }
    
    public Attachment getClosestAttachment(Vector2 point, float radius){
        // Wrapper for recursive function
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

    public void drawPoints(boolean draw){ drawPoints(rootPart, draw); } // Recursive wrapper

    public ShipPart getPartClicked(Vector2 pos){
        return rootPart.hit(pos, getTransform());
    }

    private ShipPart findParent(ShipPart head, ShipPart child){
        // Loop through the head's attachments
        for(Attachment a : head.getAttachments()){
            if(a.getChild() == null) continue; // Nothing attached, skip

            if(a.getChild() != child){
                // This is not the parent, check it
                ShipPart t = findParent(a.getChild(), child);

                if(t != null){
                    // Parent of child found, return it
                    return a.getChild();
                }
            }

            // This is the parent
            return head;
        }

        // Nothing
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
            float x = data.getFloat("x");
            float y = data.getFloat("y");
            float rotation = data.getFloat("rotation");

            // Reset body
            body.setTransform(new Vector2(x, y), (float)(rotation * (Math.PI / 180)));
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);

            setPosition(x, y);
            setRotation(rotation);

            // Load body data
            JSONObject root = data.getJSONObject("rootPart");
            rootPart = ShipPart.unserialize(manager, partManager, body, state, root);
            assemble();

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

        state.artifRoll = angVel;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));

        if(state.sas){
            computeSAS();
        } else {
            state.artifRoll = 0;
        }

        if(rootPart != null){
            rootPart.act(delta);
        }
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
    public void setPosition(float x, float y){
        super.setPosition(x, y);
        body.setTransform(x, y, body.getAngle());
    }

    @Override
    public boolean remove(){
        if(rootPart != null){
            rootPart.remove();
        }

        return super.remove();
    }
}

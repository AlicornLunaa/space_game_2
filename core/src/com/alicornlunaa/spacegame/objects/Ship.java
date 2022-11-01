package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.parts.ShipPart;
import com.alicornlunaa.spacegame.parts.ShipPart.Attachment;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Ship extends Entity {
    // Classes
    protected static class ShipState {
        /** Contains a list of all the ship statistics and variables
         * such as fuel level, electricity level, thrust level, RCS,
         * SAS, and other instance based variables
         */
    }

    // Variables
    private Assets manager;
    private PartManager partManager;
    private Body body;
    private ShipPart rootPart;

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

        rootPart = ShipPart.fromJSON(manager, partManager.get("AERO", "MED_CMD_POD"), body, new Vector2(0, 0), 0.f);
        ShipPart fuselage = rootPart.attachPart(ShipPart.fromJSON(manager, partManager.get("STRUCTURAL", "BSC_FUSELAGE"), body, new Vector2(0, 0), 0.f), 1, 0);
        ShipPart thruster = fuselage.attachPart(ShipPart.fromJSON(manager, partManager.get("THRUSTER", "BSC_THRUSTER"), body, new Vector2(0, 0), 0.f), 0, 0);
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
        assemble(rootPart);
    }

    public ShipPart getRoot(){
        return rootPart;
    }

    private void getPositions(ShipPart head, ArrayList<Vector2> posList, ArrayList<Attachment> attachList){
        for(ShipPart.Attachment a : head.getAttachments()){
            Vector2 pos = new Vector2(a.getParent().getX() + a.getPos().x, a.getParent().getY() + a.getPos().y);
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

        Vector2 localPointer = (new Vector2(point)).sub(getX(), getY());
        Vector2 closestPoint = positions.get(0);
        Attachment closestAttachment = lAttachments.get(0);
        float minDistance = localPointer.dst2(closestPoint);

        for(int i = 1; i < positions.size(); i++){
            Vector2 curPoint = positions.get(i);
            Attachment curAttachment = lAttachments.get(i);
            float curDist = localPointer.dst2(curPoint);

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

    public boolean save(String path){
        // TODO: Finish ship save method
        return false;
    }

    public boolean load(String path){
        // TODO: Finish ship load method
        return false;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));

        rootPart.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Color c = getColor();
        rootPart.draw(batch, c.a * parentAlpha);
    }

    @Override
    public boolean remove(){
        rootPart.remove();
        return super.remove();
    }
}

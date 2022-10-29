package com.alicornlunaa.spacegame.objects;

import com.alicornlunaa.spacegame.parts.ShipPart;
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
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ship extends Actor {
    // Classes
    protected static class ShipState {

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

        setPosition(body.getPosition().x, body.getPosition().y);
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

        System.out.println(head);

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

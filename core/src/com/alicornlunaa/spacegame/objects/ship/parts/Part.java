package com.alicornlunaa.spacegame.objects.ship.parts;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class Part implements Disposable,Comparable<Part> {
    // Inner classes
    public static class Node {
        public @Null Node previous = null;
        public @Null Node next = null;

        public Vector2 point;
        public Part part;

        private Node(Part part, Vector2 point){
            this.part = part;
            this.point = point.cpy();
        }

        private Node(Part part, float x, float y){
            this.part = part;
            this.point = new Vector2(x, y);
        }
    };

    // Private functions
    private void deleteAllColliders(){
        collider.detachCollider();

        for(Node node : attachments){
            if(node.next != null){
                node.next.part.deleteAllColliders();
            }
        }
    }

    // Variables
    protected Ship parent;
    private TextureRegion texture;
    private Collider collider;
    private Array<Node> attachments = new Array<>();

    private String type;
    private String id;
    private String name;
    private String description;
    private float partScale = 1.f / 32.f;
    private int interiorSize;
    private boolean freeform = false;
    private boolean flipX = false;
    private boolean flipY = false;
    private Vector2 pos = new Vector2();
    private float rotation = 0;

    // Constructor
    public Part(final App game, Ship parent, JSONObject data){
        this.parent = parent;

        type = data.optString("type", "STRUCTURAL");
        id = data.optString("id", "BSC_FUSELAGE");
        name = data.optString("name", "Basic Fuselage");
        description = data.optString("desc", "Ol' reliable with its 1000 fuel and battery!");
        interiorSize = data.optInt("interiorSize", 3);
        freeform = data.optBoolean("freeform", false);

        texture = game.atlas.findRegion("parts/" + id.toLowerCase());
        collider = new Collider(new JSONArray(Gdx.files.internal("colliders/parts/" + id.toLowerCase() + ".json").readString()));
        
        for(int i = 0; i < data.getJSONArray("attachmentPoints").length(); i++){
            JSONObject vec = data.getJSONArray("attachmentPoints").getJSONObject(i);
            attachments.add(new Node(this, vec.getFloat("x"), vec.getFloat("y")));
        }
    }

    // Functions
    public Part get(int index){
        Node thisNode = attachments.get(index);
        
        if(thisNode.next != null){
            return thisNode.next.part;
        } else if(thisNode.previous != null){
            return thisNode.previous.part;
        }

        return null;
    }

    public Part attach(Node fromNode, Node toNode){
        if(fromNode.next != null) return null;
        if(toNode.previous != null) return null;

        fromNode.next = toNode;
        toNode.previous = fromNode;

        return toNode.part;
    }

    public Part attach(int from, int to, Part part){
        Node fromNode = attachments.get(from);
        Node toNode = part.attachments.get(to);

        if(fromNode.next != null) return null;
        if(toNode.previous != null) return null;

        fromNode.next = toNode;
        toNode.previous = fromNode;

        return part;
    }

    public boolean deattach(Node thisNode){
        deleteAllColliders();

        if(thisNode.next != null){
            // This node is the parent, orphan the child
            Node toNode = thisNode.next;
            toNode.previous = null;
            thisNode.next = null;
            return true;
        } else if(thisNode.previous != null){
            // This node is the child, run away from home
            Node fromNode = thisNode.previous;
            fromNode.next = null;
            thisNode.previous = null;
            return true;
        }

        return false;
    }

    public boolean deattach(int index){
        Node thisNode = attachments.get(index);
        return deattach(thisNode);
    }
    
    public void setParent(Ship ship, Matrix4 trans){
        Vector3 temp = new Vector3();
        trans.getTranslation(temp);
        pos.set(temp.x, temp.y);

        BodyComponent bc = ship.getBody();
        parent = ship;
        collider.setScale((flipX ? -1 : 1) * partScale, (flipY ? -1 : 1) * partScale);
        collider.setPosition(pos.cpy());
        collider.setRotation(rotation * ((getFlipX() ^ getFlipY()) ? -1 : 1));
        collider.attachCollider(bc.body);

        for(Node node : attachments){
            if(node.next != null){
                trans.scale(partScale, partScale, 1);
                trans.translate(node.point.x, node.point.y, 0);
                trans.rotate(0, 0, 1, node.next.part.getRotation());
                trans.scale(node.next.part.flipX ? -1 : 1, node.next.part.flipY ? -1 : 1, 1);
                trans.rotate(0, 0, 1, -node.part.getRotation());
                trans.translate(-node.next.point.x, -node.next.point.y, 0);
                trans.scale(1.f / partScale, 1.f / partScale, 1);
                node.next.part.setParent(ship, trans);
                trans.scale(partScale, partScale, 1);
                trans.translate(node.next.point.x, node.next.point.y, 0);
                trans.rotate(0, 0, 1, node.part.getRotation());
                trans.scale(node.next.part.flipX ? -1 : 1, node.next.part.flipY ? -1 : 1, 1);
                trans.rotate(0, 0, 1, -node.next.part.getRotation());
                trans.translate(-node.point.x, -node.point.y, 0);
                trans.scale(1.f / partScale, 1.f / partScale, 1);
            }
        }
    }

    public Node getParentNode(){
        // Returns a node that has a previous one, indicating a parent
        for(Node node : attachments){
            if(node.previous != null){
                return node;
            }
        }

        return null;
    }

    public Vector2 getNodePosition(Node node){
        return this.getPosition().cpy().add(node.point.cpy().rotateDeg(getRotation()).scl((node.part.getFlipX() ? -1 : 1) * partScale, (node.part.getFlipY() ? -1 : 1) * partScale));
    }

    public boolean contains(Vector2 position){
        Matrix3 trans = new Matrix3();
        trans.translate(pos.x, pos.y);
        trans.rotate(getRotation());
        trans.scale((flipX ? -1 : 1) * partScale, (flipY ? -1 : 1) * partScale);

        if(this.collider.contains(position.cpy().mul(trans.inv()))){
            return true;
        }

        for(Node node : attachments){
            if(node.next != null){
                if(node.next.part.contains(position)){
                    return true;
                }
            }
        }

        return false;
    }

    public Part hit(Vector2 position){
        // Returns the part hit in the tree
        for(Node node : attachments){
            if(node.next != null){
                Part res = node.next.part.hit(position);

                if(res != null){
                    return res;
                }
            }
        }

        Matrix3 trans = new Matrix3();
        trans.translate(pos.x, pos.y);
        trans.rotate(getRotation());
        if(this.collider.contains(position.cpy().mul(trans.inv()))){
            return this;
        }

        return null;
    }

    public void tick(float delta){
        for(Node node : attachments){
            if(node.next != null){
                node.next.part.tick(delta);
            }
        }
    }
    
    @Override
    public void dispose(){}

    // Rendering functions
    protected void drawEffectsAbove(Batch batch){}
    protected void drawEffectsBelow(Batch batch){}

    public void draw(Batch batch, Matrix4 trans){
        batch.setTransformMatrix(trans);
        
        drawEffectsBelow(batch);
        batch.draw(
            texture,
            texture.getRegionWidth() / -2, texture.getRegionHeight() / -2,
            texture.getRegionWidth() / 2, texture.getRegionHeight() / 2,
            texture.getRegionWidth(), texture.getRegionHeight(),
            partScale, partScale,
            0
        );
        drawEffectsAbove(batch);

        for(Node node : attachments){
            if(node.next != null){
                trans.scale(partScale, partScale, 1);
                trans.translate(node.point.x, node.point.y, 0);
                trans.rotate(0, 0, 1, node.next.part.getRotation());
                trans.scale(node.next.part.flipX ? -1 : 1, node.next.part.flipY ? -1 : 1, 1);
                trans.rotate(0, 0, 1, -node.part.getRotation());
                trans.translate(-node.next.point.x, -node.next.point.y, 0);
                trans.scale(1.f / partScale, 1.f / partScale, 1);
                node.next.part.draw(batch, trans);
                trans.scale(partScale, partScale, 1);
                trans.translate(node.next.point.x, node.next.point.y, 0);
                trans.rotate(0, 0, 1, node.part.getRotation());
                trans.scale(node.next.part.flipX ? -1 : 1, node.next.part.flipY ? -1 : 1, 1);
                trans.rotate(0, 0, 1, -node.next.part.getRotation());
                trans.translate(-node.point.x, -node.point.y, 0);
                trans.scale(1.f / partScale, 1.f / partScale, 1);
            }
        }
    }

    public void drawAttachmentPoints(ShapeRenderer renderer, Matrix4 trans){
        for(Node node : attachments){
            if(node.previous != null || node.next != null){
                renderer.setColor(1, 0, 0, 1);
            } else {
                renderer.setColor(0, 1, 0, 1);
            }

            renderer.setTransformMatrix(trans);
            renderer.circle(node.point.x * partScale, node.point.y * partScale, 1);

            if(node.next != null){
                trans.scale(partScale, partScale, 1);
                trans.translate(node.point.x, node.point.y, 0);
                trans.rotate(0, 0, 1, node.next.part.getRotation());
                trans.scale(node.next.part.flipX ? -1 : 1, node.next.part.flipY ? -1 : 1, 1);
                trans.rotate(0, 0, 1, -node.part.getRotation());
                trans.translate(-node.next.point.x, -node.next.point.y, 0);
                trans.scale(1.f / partScale, 1.f / partScale, 1);
                node.next.part.drawAttachmentPoints(renderer, trans);
                trans.scale(partScale, partScale, 1);
                trans.translate(node.next.point.x, node.next.point.y, 0);
                trans.rotate(0, 0, 1, node.part.getRotation());
                trans.scale(node.next.part.flipX ? -1 : 1, node.next.part.flipY ? -1 : 1, 1);
                trans.rotate(0, 0, 1, -node.next.part.getRotation());
                trans.translate(-node.point.x, -node.point.y, 0);
                trans.scale(1.f / partScale, 1.f / partScale, 1);
            }
        }
    }

    public void drawDebug(ShapeRenderer renderer){
        renderer.setColor(0.2f, 0.2f, 1, 1);
        renderer.circle(pos.x, pos.y, 1);

        for(Node node : attachments){
            if(node.next != null){
                node.next.part.drawDebug(renderer);
            }
        }
    }

    // Getters & setters
    public boolean getFreeform(){ return freeform; }
    public int getInteriorSize(){ return interiorSize; }
    public float getPartScale(){ return partScale; }
    public float getWidth(){ return texture.getRegionWidth() * partScale; }
    public float getHeight(){ return texture.getRegionHeight() * partScale; }
    public void setFlipX(){ flipX = !flipX; }
    public void setFlipY(){ flipY = !flipY; }
    public boolean getFlipX(){ return flipX; }
    public boolean getFlipY(){ return flipY; }
    public void setRotation(float rot){ rotation = rot; }
    public Vector2 getPosition(){ return pos; }
    public void setPosition(float x, float y){ pos.set(x, y); }
    public float getRotation(){ return rotation; }
    public TextureRegion getTexture(){ return texture; }
    public String getType(){ return type; }
    public String getID(){ return id; }
    public String getName(){ return name; }
    public String getDescription(){ return description; }
    public Array<Node> getAttachments(){ return attachments; }

    public void addParts(Array<Part> parts){
        // Adds all the parts recursively to the array
        parts.add(this);

        for(Node node : attachments){
            if(node.next != null){
                node.next.part.addParts(parts);
            }
        }
    }

    @Override
    public int compareTo(Part o) {
        if(o.pos.x == pos.x && o.pos.y == pos.y) return 0;
        if(o.pos.x + o.pos.y > pos.x + pos.y) return 1;
        if(o.pos.x + o.pos.y < pos.x + pos.y) return -1;
        return 0;
    }

    // Serialization functions
    public JSONObject serialize(){
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("id", id);
        obj.put("rotation", rotation);
        obj.put("flipX", flipX);
        obj.put("flipY", flipY);

        JSONArray children = new JSONArray();
        for(Node node : attachments){
            if(node.next != null){
                int connectingIndex = 0;
                for(Node nextNodes : node.next.part.attachments){
                    if(nextNodes.previous != node){
                        connectingIndex++;
                    } else {
                        break;
                    }
                }

                JSONObject attachment = new JSONObject();
                attachment.put("nodeData", node.next.part.serialize());
                attachment.put("connectingIndex", connectingIndex);
                children.put(attachment);
            } else {
                children.put(JSONObject.NULL);
            }
        }
        obj.put("attachments", children);

        return obj;
    }

    public static Part spawn(final App game, final Ship ship, String type, String id){
        // Load part information from the json object
        switch(type){
            case "AERO":
                return new Aero(game, ship, game.partManager.get(type, id));
                
            case "STRUCTURAL":
                return new Structural(game, ship, game.partManager.get(type, id));
                
            case "THRUSTER":
                return new Thruster(game, ship, game.partManager.get(type, id));
                
            case "RCSPORT":
                return new RCSPort(game, ship, game.partManager.get(type, id));

            default:
                return null;
        }
    }

    public static Part unserialize(final App game, final Ship ship, JSONObject obj){
        String type = obj.getString("type");
        String id = obj.getString("id");
        float rotation = obj.getFloat("rotation");
        boolean flipX = obj.getBoolean("flipX");
        boolean flipY = obj.getBoolean("flipY");

        Part newPart = Part.spawn(game, ship, type, id);
        newPart.rotation = rotation;
        newPart.flipX = flipX;
        newPart.flipY = flipY;

        // Spawn every child
        JSONArray arr = obj.getJSONArray("attachments");
        for(int i = 0; i < arr.length(); i++){
            Object rawData = arr.get(i);

            if(!rawData.equals(null)){
                // Create part for the children
                JSONObject data = (JSONObject)rawData;
                JSONObject nodeData = data.getJSONObject("nodeData");
                int connectingIndex = data.getInt("connectingIndex");
                newPart.attach(i, connectingIndex, unserialize(game, ship, nodeData));
            }
        }

        return newPart;
    }
}

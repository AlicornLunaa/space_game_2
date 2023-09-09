package com.alicornlunaa.spacegame.objects.ship2.parts;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class Part {
    // Inner classes
    private static class Node {
        private @Null Node previous = null;
        private @Null Node next = null;

        private Vector2 point;
        private Part part;

        private Node(Part part, Vector2 point){
            this.part = part;
            this.point = point.cpy();
        }
    };

    // Variables
    private Array<Node> attachments = new Array<>();

    // Constructor
    public Part(final App game){
        attachments.add(new Node(this, new Vector2( -4, 0)));
        attachments.add(new Node(this, new Vector2(4, 0)));
        attachments.add(new Node(this, new Vector2(0,  -4)));
        attachments.add(new Node(this, new Vector2(0, 4)));
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

    public Part attach(int from, int to, Part part){
        Node fromNode = attachments.get(from);
        Node toNode = part.attachments.get(to);

        if(fromNode.next != null) return null;
        if(toNode.previous != null) return null;

        fromNode.next = toNode;
        toNode.previous = fromNode;

        return part;
    }

    public boolean deattach(int index){
        Node thisNode = attachments.get(index);

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

    public void draw(ShapeRenderer renderer, Matrix4 trans){
        for(int i = 0; i < attachments.size; i++){
            Node node = attachments.get(i);

            if(node.previous != null || node.next != null){
                renderer.setColor(1, 0, 0, 1);
            } else {
                renderer.setColor(0, 1, 0, 1);
            }

            renderer.setTransformMatrix(trans);
            renderer.circle(node.point.x, node.point.y, 1);

            if(node.next != null){
                Vector2 position = node.point.cpy().sub(node.next.point.x, node.next.point.y);

                trans.translate(position.x, position.y, 0);
                node.next.part.draw(renderer, trans);
                trans.translate(-position.x, -position.y, 0);
            }
        }
    }
}

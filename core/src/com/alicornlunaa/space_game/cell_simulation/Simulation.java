package com.alicornlunaa.space_game.cell_simulation;

import java.util.Stack;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.cell_simulation.actions.AbstractAction;
import com.alicornlunaa.space_game.cell_simulation.tiles.AbstractTile;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class Simulation {
    // Static classes

    // Variables
    private ShapeRenderer batch = App.instance.shapeRenderer;

    public final int width, height;
    public Stack<AbstractAction> actionStack = new Stack<>();
    public AbstractTile tiles[];
    public float tileSize = 0.1f;

    // Constructor
    public Simulation(int pixelWidth, int pixelHeight){
        width = pixelWidth;
        height = pixelHeight;
        tiles = new AbstractTile[width * height];
    }

    // Functions
    private Array<Vector2i> getLine(int startX, int startY, int endX, int endY){
        Array<Vector2i> arr = new Array<>();

        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        int hStep = (startX < endX) ? 1 : -1;
        int vStep = (startY < endY) ? 1 : -1;
        int diff = dx - dy;

        while(true){
            int doubleDiff = diff * 2;

            if(doubleDiff > -dy){
                diff -= dy;
                startX += hStep;
            }

            if(doubleDiff < dx){
                diff += dx;
                startY += vStep;
            }

            if(startX == endX && startY == endY)
                break;

            arr.add(new Vector2i(startX, startY));
        }

        arr.add(new Vector2i(endX, endY));
        return arr;
    }

    public int getX(int index){
        return index % width;
    }

    public int getY(int index){
        return index / height;
    }

    public int getIndex(int x, int y){
        return y * width + x;
    }

    public @Null AbstractTile getTile(int x, int y){
        if(x >= 0 && x < width && y >= 0 && y < height){
            return tiles[getIndex(x, y)];
        }

        return null;
    }

    public void update(float deltaTime){
        // Update every cell
        for(int i = 0; i < tiles.length; i++){
            tiles[i].update(this);
        }

        // Commit every action
        while(!actionStack.empty()){
            AbstractAction action = actionStack.pop();
            
        }

        // Draw everything
        batch.setProjectionMatrix(App.instance.camera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.setAutoShapeType(true);
        batch.begin();

        for(int i = 0; i < tiles.length; i++){
            batch.set(ShapeType.Line);
            batch.rect(getX(i) * tileSize, getY(i) * tileSize, tileSize, tileSize);
        }

        batch.end();
    }
}

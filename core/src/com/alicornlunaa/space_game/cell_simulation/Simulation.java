package com.alicornlunaa.space_game.cell_simulation;

import java.util.Stack;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.cell_simulation.actions.AbstractAction;
import com.alicornlunaa.space_game.cell_simulation.actions.CreateAction;
import com.alicornlunaa.space_game.cell_simulation.tiles.AbstractTile;
import com.alicornlunaa.space_game.cell_simulation.tiles.Element;
import com.alicornlunaa.space_game.cell_simulation.tiles.LiquidTile;
import com.alicornlunaa.space_game.cell_simulation.tiles.SolidTile;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
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
    public static Array<Vector2i> getLine(int startX, int startY, int endX, int endY){
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

    public boolean inBounds(int x, int y){
        return (x >= 0 && x < width && y >= 0 && y < height);
    }

    public void update(float deltaTime){
        // Update every cell
        for(int i = 0; i < tiles.length; i++){
            if(tiles[i] == null) continue;
            tiles[i].update(this, getX(i), getY(i));
        }

        // Commit every action
        while(!actionStack.empty()){
            AbstractAction action = actionStack.pop();
            action.commit(this);
        }

        // Draw everything
        batch.setProjectionMatrix(App.instance.camera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.setAutoShapeType(true);
        batch.begin();

        batch.set(ShapeType.Line);
        for(int i = 0; i < tiles.length; i++){
            batch.setColor(tiles[i] instanceof SolidTile ? Color.RED : (tiles[i] instanceof LiquidTile ? Color.GREEN : Color.WHITE));
            batch.rect(getX(i) * tileSize, getY(i) * tileSize, tileSize, tileSize);
        }

        // Cursor
        Vector3 v = App.instance.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        v.set((int)(v.x / tileSize) * tileSize, (int)(v.y / tileSize) * tileSize, 0);

        if(inBounds((int)(v.x / tileSize), (int)(v.y / tileSize))){
            batch.setColor(Color.CYAN);
            batch.rect(v.x, v.y, tileSize, tileSize);

            if(Gdx.input.isButtonPressed(Buttons.LEFT)){
                actionStack.add(new CreateAction(new LiquidTile(Element.SAND), (int)(v.x / tileSize), (int)(v.y / tileSize)));
            } else if(Gdx.input.isButtonPressed(Buttons.RIGHT)){
                actionStack.add(new CreateAction(new SolidTile(Element.SAND), (int)(v.x / tileSize), (int)(v.y / tileSize)));
            }
        }

        batch.end();
    }
}

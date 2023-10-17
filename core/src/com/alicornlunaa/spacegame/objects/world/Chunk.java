package com.alicornlunaa.spacegame.objects.world;

import com.alicornlunaa.selene_engine.components.ActorComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.components.tiles.StaticTileComponent;
import com.alicornlunaa.spacegame.objects.ItemEntity;
import com.alicornlunaa.spacegame.objects.blocks.BaseTile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;

public class Chunk extends Group {
    // Variables
    private BaseTile[][] tiles = new BaseTile[Constants.CHUNK_SIZE][Constants.CHUNK_SIZE];
    private boolean active = false;
    private int chunkX;
    private int chunkY;

    public boolean chunkUpdate = false;

    // Private functions
    private void generateTiles(TerrainGenerator generator){
        for(int y = 0; y < Constants.CHUNK_SIZE; y++){
            for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                final @Null BaseTile tile = generator.getTile(chunkX, chunkY, x, y);

                if(tile != null){
                    final StaticTileComponent staticTileComponent = tile.getComponent(StaticTileComponent.class);
                    final ActorComponent actorComponent = tile.getComponent(ActorComponent.class);
                    actorComponent.setBounds(
                        x * Constants.TILE_SIZE + chunkX * Constants.CHUNK_SIZE * Constants.TILE_SIZE,
                        y * Constants.TILE_SIZE + chunkY * Constants.CHUNK_SIZE * Constants.TILE_SIZE,
                        Constants.TILE_SIZE,
                        Constants.TILE_SIZE
                    );
                    tiles[x][y] = tile;
                    addActor(actorComponent);

                    actorComponent.addListener(new ClickListener(){
                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                            if(Gdx.input.isButtonPressed(Buttons.LEFT)){
                                removeActor(actorComponent);
                                tiles[staticTileComponent.x][staticTileComponent.y] = null;
                                chunkUpdate = true;

                                PlanetComponent pc = App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class);
                                App.instance.gameScene.registry.removeEntity(tile);
                                pc.delToPlanetEntities(tile);

                                ItemEntity newItem = new ItemEntity(
                                    pc.physWorld,
                                    staticTileComponent.x * Constants.TILE_SIZE + getChunkX() * Constants.CHUNK_SIZE * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.f,
                                    staticTileComponent.y * Constants.TILE_SIZE + getChunkY() * Constants.CHUNK_SIZE * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.f,
                                    "stone_square",
                                    1,
                                    64
                                );
                                pc.addToPlanetEntities(newItem);
                                App.instance.gameScene.registry.addEntity(newItem);
                            } else if(Gdx.input.isButtonPressed(Buttons.MIDDLE)){
                                BaseTile.convertToDynamic(App.instance.gameScene.registry, Chunk.this, App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class), tile); //! TODO: Garbage code, fix it later
                                chunkUpdate = true;
                            }
                        }
                    });
                }
            }
        }
    }

    // Constructor
    public Chunk(TerrainGenerator generator, PhysWorld world, int chunkX, int chunkY){
        // Slight performance save
        this.setTransform(false);
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        generateTiles(generator);
    }

    // Functions
    public boolean isLoaded(){
        return active;
    }

    public void setLoaded(boolean b){
        active = b;
    }

    public int getChunkX(){
        return chunkX;
    }

    public int getChunkY(){
        return chunkY;
    }

    public BaseTile getTile(int x, int y){
        if(x < 0 || x >= tiles.length) return null;
        if(y < 0 || y >= tiles[x].length) return null;
        return tiles[x][y];
    }

    public boolean setTile(@Null BaseTile tile, int x, int y){
        if(x < 0 || x >= tiles.length) return false;
        if(y < 0 || y >= tiles[x].length) return false;
        tiles[x][y] = tile;
        return true;
    }
    
    @Override
    public void draw(Batch batch, float a){
        Matrix4 trans = batch.getTransformMatrix().cpy();
        batch.setTransformMatrix(trans.cpy().translate(chunkX * Constants.CHUNK_SIZE * Constants.TILE_SIZE, chunkY * Constants.CHUNK_SIZE * Constants.TILE_SIZE, 0));
        super.draw(batch, a);
        batch.setTransformMatrix(trans);
    }
}

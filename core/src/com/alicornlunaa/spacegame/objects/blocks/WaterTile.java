package com.alicornlunaa.spacegame.objects.blocks;

import com.alicornlunaa.spacegame.components.tiles.StaticTileComponent;
import com.alicornlunaa.spacegame.objects.world.ChunkManager;

public class WaterTile extends BaseTile {
    // Variables
    private StaticTileComponent staticTileComponent = getComponent(StaticTileComponent.class);

    // Constructor
    public WaterTile(String tileID, int x, int y) {
        super(tileID, x, y);
    }

    // Functions
    @Override
    public void step(ChunkManager chunkManager){
        // Error clause
        if(staticTileComponent == null) return;

        // Cellular gravity
        if(chunkManager.getTile(staticTileComponent.x, staticTileComponent.y - 1) == null){
            chunkManager.setTile(null, staticTileComponent.x, staticTileComponent.y);
            chunkManager.setTile(this, staticTileComponent.x, staticTileComponent.y - 1);
        }
    }
}

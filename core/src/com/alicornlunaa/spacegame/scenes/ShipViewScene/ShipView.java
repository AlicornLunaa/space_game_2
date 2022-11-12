package com.alicornlunaa.spacegame.scenes.ShipViewScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Ship;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ShipView extends Stage {

    // Variables
    private final App game;
    private final Player player;
    private final Ship ship;

    // Constructor
    public ShipView(final App game, final Player player, final Ship ship){
        super();
        this.game = game;
        this.player = player;
        this.ship = ship;
    }

    // Functions
    
    
}

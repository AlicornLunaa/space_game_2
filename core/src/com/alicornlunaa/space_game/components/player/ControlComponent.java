package com.alicornlunaa.space_game.components.player;

import com.badlogic.ashley.core.Component;

public class ControlComponent implements Component {
    // Variables
    public PlayerComponent playerComponent;

    // Constructor
    public ControlComponent(PlayerComponent playerComponent){
        this.playerComponent = playerComponent;
    }
}

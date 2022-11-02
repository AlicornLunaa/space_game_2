package com.alicornlunaa.spacegame.util;

import com.badlogic.gdx.graphics.Color;

public class HexColor extends Color {
    public HexColor(String hex){
        super();

        // Parse data
        String color = hex;

        if(color.startsWith("#")){
            color = color.substring(1);
        }

        int r = Integer.parseInt(color.substring(0, 2), 16);
        int g = Integer.parseInt(color.substring(2, 4), 16);
        int b = Integer.parseInt(color.substring(4, 6), 16);

        this.set(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
    }
}

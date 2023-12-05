package com.alicornlunaa.space_game.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class ColorWidget extends Widget {
    // Variables
    private Widget widget;
    private Pixmap pixmap;
    private Texture texture;
    private Color color = new Color();
    private Color prevColor = new Color();

    // Constructor
    public ColorWidget(Widget widget, Color color){
        this.widget = widget;
        this.color = color;

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    // Functions
    @Override
    public void draw(Batch batch, float parentAlpha){
        widget.setBounds(getX(), getY(), getWidth(), getHeight());

        prevColor.set(batch.getColor());
        batch.setColor(color);
        batch.draw(texture, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
        batch.setColor(prevColor);
        super.draw(batch, parentAlpha);

        widget.draw(batch, parentAlpha);
    }

    @Override
	public float getMinWidth () {
		return widget.getPrefWidth();
	}

    @Override
	public float getMinHeight () {
		return widget.getPrefHeight();
	}

    @Override
	public float getPrefWidth () {
		return widget.getPrefWidth();
	}

    @Override
	public float getPrefHeight () {
		return widget.getPrefHeight();
	}

    @Override
	public float getMaxWidth () {
		return widget.getMaxWidth();
	}

    @Override
	public float getMaxHeight () {
		return widget.getMaxHeight();
	}

    public void dispose(){
        texture.dispose();
    }
}

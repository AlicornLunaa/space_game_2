package com.alicornlunaa.spacegame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		// Development shit here
		Settings settings = new Settings();
		settings.maxWidth = 1024;
		settings.maxHeight = 1024;
		TexturePacker.process(settings, "./assets/textures/", "./assets/textures_packed", "textures");
		TexturePacker.process(settings, "./assets/particles/", "./assets/particles_packed", "particles");

		// Load game
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("SpaceGame2");
		config.setWindowedMode(1280, 720);
		new Lwjgl3Application(new App(), config);
	}
}

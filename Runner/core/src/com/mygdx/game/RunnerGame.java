package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class RunnerGame extends Game {
	public final int WORLD_WIDTH = 512;
	public final int WORLD_HEIGHT = 256;

	SpriteBatch batch;
	BitmapFont bitmapFont;
	OrthographicCamera camera;
	FitViewport viewport;

	@Override
	public void create () {
		StartMenuScreen startMenuScreen = new StartMenuScreen(this);

		batch = new SpriteBatch();
		bitmapFont = new BitmapFont();
		camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
		Gdx.graphics.setWindowedMode(WORLD_WIDTH, WORLD_HEIGHT);

		this.setScreen(startMenuScreen);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
		bitmapFont.dispose();
	}
}

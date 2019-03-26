package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class GameOverScreen implements Screen {
    RunnerGame game;
    GameScreen gameScreen;
    Texture gameOverTexture;
    float stateTime;

    GameOverScreen(RunnerGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        gameOverTexture = new Texture(Gdx.files.internal("gameover.png"));
        stateTime = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        Gdx.gl20.glClearColor(0.625f, 0.84375f, 0.933594f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(gameOverTexture, 0, 0);
        game.batch.end();

        if (stateTime < 1) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.exit();
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            gameOverTexture.dispose();
            gameScreen.init();
            game.setScreen(gameScreen);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        gameOverTexture.dispose();
    }
}

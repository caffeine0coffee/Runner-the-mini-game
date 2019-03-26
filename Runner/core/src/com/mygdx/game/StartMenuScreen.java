package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class StartMenuScreen implements Screen {
    RunnerGame game;
    Texture title_main, title_prompt;
    boolean keyPressed;
    boolean acceptStart;
    float stateTime, waitTime;
    Sound gameStartSE, beepSound;
    Music bgm;
    int command;

    StartMenuScreen(RunnerGame game) {
        this.game = game;
        title_main = new Texture(Gdx.files.internal("singleImage/RUNNER_logo_runner.png"));
        title_prompt = new Texture(Gdx.files.internal("singleImage/RUNNER_logo_keyPrompt.png"));
        keyPressed = false;
        acceptStart = true;
        waitTime = 2;
        stateTime = 0;
        command = 0;
        game.bitmapFont.setColor(0, 0, 0, 1);
        gameStartSE = Gdx.audio.newSound(Gdx.files.internal("soundEffect/select.wav"));
        beepSound = Gdx.audio.newSound(Gdx.files.internal("soundEffect/beep.wav"));
        bgm = Gdx.audio.newMusic(Gdx.files.internal("music/titleScreen.mp3"));
        bgm.setLooping(true);
        bgm.setVolume(0.5f);
        bgm.play();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (stateTime == 0 && Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            game.enableHellMode();
        }

        stateTime += delta;
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        Gdx.gl20.glClearColor(0.9f, 0.9f, 0.9f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(title_main, 0, 0);
        if (keyPressed) {
            if ((int)(waitTime*8) % 2 == 0) {
                game.batch.draw(title_prompt, 0, 0);
            }
        }
        else {
            if ((int)(stateTime*2) % 2 == 0) {
                game.batch.draw(title_prompt, 0, 0);
            }
        }

        if (command == 7) {
            game.bitmapFont.draw(game.batch, "High score reseted!", 10, game.WORLD_HEIGHT-10);
        }
        if (game.isHellMode) {
            game.bitmapFont.draw(game.batch, "Welcome to HELL!!!", 10, game.WORLD_HEIGHT-30);
        }
        game.batch.end();

        //reset command
        acceptStart = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            command++;
            beepSound.play();
            acceptStart = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && command == 1) {
            command++;
            beepSound.play();
            acceptStart = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && command == 2) {
            command++;
            beepSound.play();
            acceptStart = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && command == 3) {
            command++;
            beepSound.play();
            acceptStart = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && command == 4) {
            command++;
            beepSound.play();
            acceptStart = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T) && command == 5) {
            command++;
            beepSound.play();
            acceptStart = false;
        }
        if (command == 6) {
            gameStartSE.play();
            Preferences pref = Gdx.app.getPreferences("RunnerPreferences");
            pref.putLong("highScore", 0);
            command++;
            acceptStart = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) &&
                !keyPressed &&
                acceptStart) {
            keyPressed = true;
            gameStartSE.play();
        }

        if (keyPressed) {
            waitTime -= delta;
            if (waitTime <= 0) {
                bgm.stop();
                game.bitmapFont.setColor(1, 1, 1, 1);
                game.setScreen(new GameScreen(game));
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
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
        title_main.dispose();
        title_prompt.dispose();
        gameStartSE.dispose();
        bgm.dispose();
    }
}

package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class GameOverScreen implements Screen {
    RunnerGame game;
    GameScreen gameScreen;
    Texture gameOverTexture_main, gameOverTexture_prompt;
    float stateTime;
    Music bgm;
    Sound retrySE;
    float waitTime;
    boolean isKeyPressed;
    boolean scoreUpdated;

    GameOverScreen(RunnerGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        gameOverTexture_main = new Texture(Gdx.files.internal("singleImage/gameover_gameover.png"));
        gameOverTexture_prompt = new Texture(Gdx.files.internal("singleImage/gameover_keyPrompt.png"));
        bgm = Gdx.audio.newMusic(Gdx.files.internal("music/gameOver.mp3"));
        retrySE = Gdx.audio.newSound(Gdx.files.internal("soundEffect/select.wav"));
        bgm.setVolume(0.8f);
        bgm.setLooping(true);
        bgm.play();
        stateTime = 0;
        waitTime = 2;
        isKeyPressed = false;
        scoreUpdated = false;

        if (gameScreen.score > gameScreen.highScore) {
            gameScreen.highScore = gameScreen.score;
            game.bitmapFont.setColor(1, 0.8f, 0, 1);
            scoreUpdated = true;
        }
        else {
            game.bitmapFont.setColor(0, 0, 0, 1);
        }

        Preferences pref = Gdx.app.getPreferences("RunnerPreferences");
        pref.putLong("highScore", gameScreen.score);
        pref.flush();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        stateTime += delta;
        Gdx.gl20.glClearColor(0.9f, 0.9f, 0.9f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(gameOverTexture_main, 0, 0);
        if (stateTime >= 1) {
            if (isKeyPressed) {
                if ((int) (waitTime * 8) % 2 == 0) {
                    game.batch.draw(gameOverTexture_prompt, 0, 0);
                }
            } else {
                if ((int) (stateTime * 2) % 2 == 0) {
                    game.batch.draw(gameOverTexture_prompt, 0, 0);
                }
            }
        }

        game.bitmapFont.draw(game.batch, "Your score: " + gameScreen.score, game.WORLD_WIDTH/2-50, game.WORLD_HEIGHT/2+10);
        game.bitmapFont.draw(game.batch, "High score: " + gameScreen.highScore, game.WORLD_WIDTH/2-50, game.WORLD_HEIGHT/2 - game.bitmapFont.getCapHeight());
        if (scoreUpdated) {
            game.bitmapFont.draw(game.batch, "New record!!", game.WORLD_WIDTH/2-50, game.WORLD_HEIGHT/2 - game.bitmapFont.getCapHeight()*2 - 10);
        }
        game.batch.end();

        if (stateTime < 1) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.exit();
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) && !isKeyPressed) {
            isKeyPressed = true;
            retrySE.play();
        }
        if (isKeyPressed) {
            waitTime -= delta;
            if (waitTime <= 0) {
                bgm.stop();
                free();
                game.bitmapFont.setColor(1, 1, 1, 1);
                game.setScreen(new GameScreen(game));
            }
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
        free();
    }

    private void free() {
        gameOverTexture_main.dispose();
        gameOverTexture_prompt.dispose();
        bgm.dispose();
    }

}

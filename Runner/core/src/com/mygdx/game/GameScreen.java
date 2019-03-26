package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

import java.util.Iterator;
import java.util.Stack;

public class GameScreen implements Screen {
    //VELOCITY's unit is [pixel/sec]
    //ENEMY_SPAWN_DURATION is how many seconds to spawn an enemy next
    private static final int RUNNER_WIDTH = 32;
    private static final int RUNNER_HEIGHT = 32;
    private static final float RUNNER_VELOCITY = 200;
    private static final float RUNNER_JUMP_INIT_SPEED = 10;
    private static final int RUNNER_INIT_HP = 5;
    private static final float GRAVITY_ACCEL = 0.5f;
    private static final int ENEMY_WIDTH = 32;
    private static final int ENEMY_HEIGHT = 32;
    private static final float ENEMY_MIN_VELOCITY = 100;
    private static final float ENEMY_MAX_VELOCITY = 400;
    private static final float ENEMY_INIT_MIN_SPAWN_DURATION = 0.2f;
    private static final float ENEMY_INIT_MAX_SPAWN_DURATION = 1;
    private static final int NUM_OF_RUNNER_FRAMES = 4;
    private static final int NUM_OF_ENEMY_FRAMES = 4;
    private static final int TILE_SIZE = 32;
    private static final int GROUND_Y = TILE_SIZE;
    private static final int NUM_OF_LEVEL = 3;
    private static final float GROUND_VELOCITY = 100;

    private class Runner {
        private Animation<TextureRegion> animation;
        private TextureRegion currentFrame;
        int x, y;
        int initX, initY;
        float yVelocity;
        boolean isInSky;
        int remainJumps;
        int hp;
        Rectangle rect;
        float remainSafeTime;
        boolean visible;
        boolean isSafe;

        public void draw(SpriteBatch batch) {
            if (visible) batch.draw(currentFrame, x, y);
        }

        public void update(float stateTime) {
            visible = true;
            isSafe = false;
            if (remainSafeTime > 0) {
                remainSafeTime -= Gdx.graphics.getDeltaTime();
                isSafe = true;
                if (MathUtils.round(remainSafeTime*40)%2 == 0) {
                    visible = false;
                }
            }

            currentFrame = animation.getKeyFrame(stateTime, true);
            rect.setPosition(x, y);
        }

        public void dispose() {
            for (TextureRegion t : animation.getKeyFrames()) {
                t.getTexture().dispose();
            }
        }

        Runner(int x, int y) {
            animation = generateAnimation("runner.png", RUNNER_WIDTH, RUNNER_HEIGHT, NUM_OF_RUNNER_FRAMES, 0.125f);
            initX = x;
            initY = y;
        }

        public void init() {
            this.x = initX;
            this.y = initY;
            yVelocity = 0;
            isInSky = true;
            remainJumps = 0;
            rect = new Rectangle();
            rect.setSize(RUNNER_WIDTH, RUNNER_HEIGHT);
            remainSafeTime = 0;
            visible = true;
            isSafe = false;
            hp = RUNNER_INIT_HP;
        }

        public void runnerControl() {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                x -= RUNNER_VELOCITY * Gdx.graphics.getDeltaTime();
            }
            else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                x += RUNNER_VELOCITY * Gdx.graphics.getDeltaTime();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && remainJumps != 0) {
                yVelocity = RUNNER_JUMP_INIT_SPEED;
                isInSky = true;
                remainJumps -= 1;
            }
            if (isInSky) {
                yVelocity -= GRAVITY_ACCEL;
                y += yVelocity;
            }

            if (y <= GROUND_Y) {
                isInSky = false;
                y = GROUND_Y;
                remainJumps = 2;
            }
            if (x <= 0) {
                x = 0;
            }
            if (x >= game.WORLD_WIDTH - RUNNER_WIDTH) {
                x = game.WORLD_WIDTH - RUNNER_WIDTH;
            }
        }

        public void addHP(int hp) {
            this.hp += hp;
        }

        public void setSafeTime(float during) {
            remainSafeTime = during;
        }
    }

    private class Ground {
        class GroundElement {
            float x;
            TextureRegion texture;
            GroundElement(float x, TextureRegion texture) {
                this.x = x;
                this.texture = texture;
            }
        }
        Array<TextureRegion> tileTextures;
        Queue<GroundElement> groundElements;

        Ground() {
            tileTextures = new Array<TextureRegion>();
            groundElements = new Queue<GroundElement>();
            Texture sheet = new Texture(Gdx.files.internal("tiles.png"));
            TextureRegion[][] tmp = TextureRegion.split(sheet, TILE_SIZE, TILE_SIZE*2);
            for (int i = 0; i < NUM_OF_LEVEL; i++) {
                tileTextures.add(tmp[0][i]);
            }

            sheetStack.add(sheet);
        }

        public void init() {
            groundElements.clear();

            for (int x = -1; x < game.WORLD_WIDTH/TILE_SIZE + 2; x++) {
                groundElements.addLast(new GroundElement(x*TILE_SIZE, tileTextures.get(level)));
            }
        }

        public void update(float deltaTime) {
            int count = 0;
            boolean update = false;
            float lastX = 0;

            for (GroundElement g : groundElements) {
                g.x -= GROUND_VELOCITY * deltaTime;
                if (++count == groundElements.size) {
                    if (g.x < game.WORLD_WIDTH) {
                        update = true;
                        lastX = g.x;
                    }
                }
            }

            if (update) {
                groundElements.removeFirst();
                groundElements.addLast(new GroundElement(lastX + TILE_SIZE, tileTextures.get(level)));
            }
        }

        public void drawGround() {
            for (GroundElement g : groundElements) {
                game.batch.draw(g.texture, g.x, 0);
            }
        }

        public void dispose() {
            for (TextureRegion t : tileTextures) {
                t.getTexture().dispose();
            }
        }
    }

    private class Enemy {
        Array<EnemyElement> elements;
        Animation<TextureRegion> animation;
        TextureRegion currentFrame;
        float nextSpawnDuration;
        float maxSpawnDuration;

        private class EnemyElement {
            float x, y;
            float velocity;
            Rectangle rect;
            boolean isAlive;

            EnemyElement(float velocity) {
                this.velocity = velocity;
                isAlive = true;
                rect = new Rectangle();
                rect.setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
                x = game.WORLD_WIDTH;
                y = GROUND_Y + ((game.WORLD_HEIGHT - GROUND_Y - ENEMY_HEIGHT) * MathUtils.random(0, 1f));
            }

            public void update(float delta) {
                x -= velocity * delta;
                rect.setPosition(x, y);
                if (x <= -ENEMY_WIDTH) isAlive = false;
            }
        }

        Enemy() {
            elements = new Array<EnemyElement>();
            animation = generateAnimation("enemy.png", ENEMY_WIDTH, ENEMY_HEIGHT, NUM_OF_ENEMY_FRAMES, 0.25f);
        }

        public void init() {
            elements.clear();
            nextSpawnDuration = 0;
        }

        public void update(float stateTime) {
            for (EnemyElement e : elements) {
                e.update(Gdx.graphics.getDeltaTime());
            }
            Iterator<EnemyElement> iter = elements.iterator();
            EnemyElement tmp;
            while (iter.hasNext()) {
                tmp = iter.next();
                if (!tmp.isAlive) {
                    iter.remove();
                }
            }

            nextSpawnDuration -= Gdx.graphics.getDeltaTime();
            if (nextSpawnDuration <= 0) {
                elements.add(generateEnemyElement());
                maxSpawnDuration = ENEMY_INIT_MIN_SPAWN_DURATION;
                maxSpawnDuration += (ENEMY_INIT_MAX_SPAWN_DURATION - ENEMY_INIT_MIN_SPAWN_DURATION) * ((float)1 / (float)(level + 1));
                nextSpawnDuration = MathUtils.random(ENEMY_INIT_MIN_SPAWN_DURATION, maxSpawnDuration);
            }

            currentFrame = animation.getKeyFrame(stateTime, true);
        }

        public void draw(SpriteBatch batch) {
            for (EnemyElement e : elements) {
                batch.draw(currentFrame, e.x, e.y);
            }
        }

        public EnemyElement generateEnemyElement() {
            return new EnemyElement(Math.abs(MathUtils.random(ENEMY_MIN_VELOCITY, ENEMY_MAX_VELOCITY)));
        }

        public void dispose() {
            for (TextureRegion t : animation.getKeyFrames()) {
                t.getTexture().dispose();
            }
        }
    }

    RunnerGame game;
    Runner runner;
    Ground ground;
    Enemy enemy;
    Texture levelUpPopUpTexture;
    Stack<Texture> sheetStack;
    float stateTime;
    long score;
    //start level: 0
    int level, prevLevel;
    String stateText, debugText;
    float popUpRemainTime;

    private void addDebugText(String text) {
        debugText += text + "\n";
    }

    private Animation generateAnimation(
            String filePath, int spWidth, int spHeight, int numOfFrames, float frameDuration)
    {
        Texture sheet = new Texture(Gdx.files.internal(filePath));
        Array<TextureRegion> frames = new Array<TextureRegion>();
        int sheetWidth = sheet.getWidth()/spWidth;
        int sheetHeight = sheet.getHeight()/spHeight;
        TextureRegion[][] tmp = TextureRegion.split(sheet, spWidth, spHeight);
        int count = 0;

        loop_begin:
        for (int y = 0; y < sheetHeight; y++) {
            for (int x = 0; x < sheetWidth; x++) {
                frames.add(tmp[y][x]);
                if (++count >= numOfFrames) break loop_begin;
            }
        }

        sheetStack.add(sheet);
        return new Animation(frameDuration, frames);
    }

    GameScreen(RunnerGame game) {
        this.game = game;

        sheetStack = new Stack<Texture>();
        runner = new Runner(0, TILE_SIZE);
        enemy = new Enemy();
        ground = new Ground();

        init();

        levelUpPopUpTexture = new Texture(Gdx.files.internal("levelup.png"));
        sheetStack.add(levelUpPopUpTexture);
    }

    private void setLevelUpPopUp() {
        popUpRemainTime = 1.5f;
    }

    public void init() {
        stateTime = 0;
        score = 0;
        level = 0;
        prevLevel = level;
        debugText = "";
        stateText = "";
        popUpRemainTime = 0;
        runner.init();
        enemy.init();
        ground.init();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        score = (long)(stateTime/0.5);
        prevLevel = level;
        if (score > 30) {
            level = 1;
        }
        if (score > 60) {
            level = 2;
        }
        if (level != prevLevel) {
            setLevelUpPopUp();
        }

        Gdx.gl20.glClearColor(0.625f, 0.84375f, 0.933594f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        runner.update(stateTime);
        runner.runnerControl();
        enemy.update(stateTime);
        ground.update(delta);

        game.batch.begin();
        runner.draw(game.batch);
        enemy.draw(game.batch);
        ground.drawGround();
        stateText += "LEVEL: " + level + "\n";
        stateText += "SCORE: " + score + "\n";
        stateText += "HP: " + runner.hp + "\n";
        game.bitmapFont.draw(game.batch, stateText, 10, game.WORLD_HEIGHT - 10);
        game.bitmapFont.draw(game.batch, debugText, 10, game.WORLD_HEIGHT - 50);

        //update pop-up
        if (popUpRemainTime > 0) {
            popUpRemainTime -= delta;
            if (MathUtils.round(popUpRemainTime*5)%2 == 0) {
                game.batch.draw(levelUpPopUpTexture, 0, 0);
            }
        }
        game.batch.end();

        //Collision process of runner and enemy
        if (!runner.isSafe) {
            for (Enemy.EnemyElement e : enemy.elements) {
                if (e.rect.overlaps(runner.rect)) {
                    runner.addHP(-1);
                    runner.setSafeTime(3);
                    //TODO: play SE
                }
            }
        }

        if (runner.hp <= 0) {
            game.setScreen(new GameOverScreen(game, this));
        }

        debugText = "";
        stateText = "";
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
        runner.dispose();
        ground.dispose();
        enemy.dispose();

        for (Texture t : sheetStack) {
            t.dispose();
        }
    }
}

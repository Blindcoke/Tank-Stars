package com.mygdx.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.Config;

import java.util.Random;

public class TankChangeScreenComputer extends TankStarsScreen {
    private int playerTankIndex = 0;
    private int enemyTankIndex;

    private Texture TankChangeScreenSprite;

    private TextureRegion TankChangeScreenBackground;
    private TextureRegion TankChangeScreenLogo;
    private TextureRegion TankChangeScreenMusic;
    private TextureRegion TankChangeScreenSound;
    private Texture PlayerTankText;
    private Texture PlayerTank;
    private Texture EnemyTank;
    private TextureRegion TankChangeScreenPlayer1Tank;
    private TextureRegion TankChangeScreenBackButton;
    private TextureRegion TankChangeScreenContinueButton;
    private TextureRegion TankChangeScreenExitButton;

    private SpriteBatch batch;

    private Stage stage;
    private ImageButton musicButton;
    private ImageButton soundButton;
    private ImageButton backButton;
    private ImageButton continueButton;
    private ImageButton exitButton;
    private ImageButton leftArrow;
    private ImageButton rightArrow;

    public TankChangeScreenComputer(Game game) {
        super(game);
    }

    @Override
    public void show() {
        TankChangeScreenSprite = new Texture("TankChange/TankChangeSprite.png");
        Texture PlayerTank = new Texture("TankChange/PlayerTank.png");
        Texture EnemyTank = new Texture("TankChange/EnemyTank.png");

        // Create array of tank TextureRegions
        final TextureRegion[] tankTextureRegions = new TextureRegion[3];
        tankTextureRegions[0] = new TextureRegion(PlayerTank, -45, 30, 324, 217); // SpectreTank
        tankTextureRegions[1] = new TextureRegion(EnemyTank, -45, -1, 324, 247); // AtomicTank
        tankTextureRegions[2] = new TextureRegion(new Texture("TankChange/BuratinoTank.png"), 0, 0, 311, 212);

        // Randomly select enemy tank
        Random random = new Random();
        do {
            enemyTankIndex = random.nextInt(3);
        } while (enemyTankIndex == playerTankIndex);

        TankChangeScreenPlayer1Tank = tankTextureRegions[playerTankIndex];

        TankChangeScreenBackground = new TextureRegion(TankChangeScreenSprite, 0, 450, 960, 540);
        TankChangeScreenLogo = new TextureRegion(TankChangeScreenSprite, 166, 0, 286, 144);
        TankChangeScreenMusic = new TextureRegion(TankChangeScreenSprite, 38, 0, 39, 32);
        TankChangeScreenSound = new TextureRegion(TankChangeScreenSprite, 77, 0, 41, 39);
        TankChangeScreenBackButton = new TextureRegion(TankChangeScreenSprite, 0, 0, 38, 40);
        TankChangeScreenContinueButton = new TextureRegion(TankChangeScreenSprite, 452, 0, 287, 75);
        TankChangeScreenExitButton = new TextureRegion(TankChangeScreenSprite, 118, 0, 48, 47);

        batch = new SpriteBatch();
        PlayerTankText = new Texture("TankChange/PlayerText1.png");
        stage = new Stage();

        musicButton = new ImageButton(new TextureRegionDrawable(TankChangeScreenMusic));
        musicButton.setPosition(831, 467);
        musicButton.setSize(50, 50);

        soundButton = new ImageButton(new TextureRegionDrawable(TankChangeScreenSound));
        soundButton.setPosition(883, 466);
        soundButton.setSize(50, 50);

        backButton = new ImageButton(new TextureRegionDrawable(TankChangeScreenBackButton));
        backButton.setPosition(50, 28);
        backButton.setSize(50, 50);

        continueButton = new ImageButton(new TextureRegionDrawable(TankChangeScreenContinueButton));
        continueButton.setPosition(345, 5);
        continueButton.setSize(300, 75);

        exitButton = new ImageButton(new TextureRegionDrawable(TankChangeScreenExitButton));
        exitButton.setPosition(888, 21);
        exitButton.setSize(50, 50);

        leftArrow = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture("TankChange/leftArrow.png"))));
        leftArrow.setPosition(36, 136);
        leftArrow.setSize(56, 78);

        rightArrow = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture("TankChange/rightArrow.png"))));
        rightArrow.setPosition(36 + 368, 136);
        rightArrow.setSize(56, 78);

        stage.addActor(musicButton);
        stage.addActor(soundButton);
        stage.addActor(backButton);
        stage.addActor(continueButton);
        stage.addActor(exitButton);
        stage.addActor(leftArrow);
        stage.addActor(rightArrow);

        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Music Button Clicked");
            }
        });

        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Sound Button Clicked");
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Back Button Clicked");
                game.setScreen(new StartScreenComputer(game));
            }
        });

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Continue Button Clicked");
                Config.getInstance().setPlayerTank(playerTankIndex);
                Config.getInstance().setEnemyTank(enemyTankIndex);
                game.setScreen(new BattleScreenComputer(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Exit Button Clicked");
                Gdx.app.exit();
            }
        });

        leftArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Left Arrow Clicked");
                playerTankIndex = (playerTankIndex - 1 + 3) % 3;
                TankChangeScreenPlayer1Tank = tankTextureRegions[playerTankIndex];
            }
        });

        rightArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Right Arrow Clicked");
                playerTankIndex = (playerTankIndex + 1) % 3;
                TankChangeScreenPlayer1Tank = tankTextureRegions[playerTankIndex];
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(TankChangeScreenBackground, 0, 0);
        batch.draw(TankChangeScreenLogo, 352, 362);
        batch.draw(PlayerTankText, 150, 300);

        // Draw player tank with positioning adjustment based on index
        if (playerTankIndex == 0) batch.draw(TankChangeScreenPlayer1Tank, 56, 64);
        else if (playerTankIndex == 1) batch.draw(TankChangeScreenPlayer1Tank, 51, 64);
        else if (playerTankIndex == 2) batch.draw(TankChangeScreenPlayer1Tank, 90, 100);

        batch.end();
        stage.draw();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        TankChangeScreenSprite.dispose();
        batch.dispose();
        stage.dispose();
    }
}
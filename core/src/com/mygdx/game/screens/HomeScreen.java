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

public class HomeScreen extends TankStarsScreen {
    private Texture homeScreenSprite;

    private TextureRegion homeScreenBackground;
    private TextureRegion homeScreenMusic;
    private TextureRegion homeScreenSound;
    private TextureRegion homeScreenStarsLogo;

    private TextureRegion homeScreenTank;
    private TextureRegion homeScreenFire;
    private TextureRegion homeScreenGroundLine;

    private TextureRegion homeScreenPlayerButton;
    private TextureRegion homeScreenComputerButton;
    private TextureRegion homeScreenExitButton;

    private SpriteBatch batch;

    private Stage stage;
    private ImageButton.ImageButtonStyle playerButtonStyle;
    private ImageButton.ImageButtonStyle computerButtonStyle;

    public HomeScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        homeScreenSprite = new Texture("HomeScreen/HomeScreenSprite.png");
        homeScreenBackground = new TextureRegion(homeScreenSprite, 0, 330, 960, 540);
        homeScreenComputerButton = new TextureRegion(homeScreenSprite, 128, 3, 276, 75);
        homeScreenExitButton = new TextureRegion(homeScreenSprite, 80, 3, 48, 47);
        homeScreenFire = new TextureRegion(homeScreenSprite, 633, 78, 238, 252);
        homeScreenGroundLine = new TextureRegion(homeScreenSprite, 768, 768, 256, 256);
        homeScreenStarsLogo = new TextureRegion(homeScreenSprite, 0, 78, 309, 160);
        homeScreenMusic = new TextureRegion(homeScreenSprite, 0, 3, 39, 32);
        homeScreenPlayerButton = new TextureRegion(homeScreenSprite, 404, 3, 287, 75);
        homeScreenSound = new TextureRegion(homeScreenSprite, 39, 3, 41, 39);
        homeScreenTank = new TextureRegion(homeScreenSprite, 309, 78, 324, 217);


        batch = new SpriteBatch();

        stage = new Stage();

        playerButtonStyle = new ImageButton.ImageButtonStyle();
        playerButtonStyle.up = new TextureRegionDrawable(homeScreenPlayerButton);
        playerButtonStyle.down = new TextureRegionDrawable(homeScreenPlayerButton);

        computerButtonStyle = new ImageButton.ImageButtonStyle();
        computerButtonStyle.up = new TextureRegionDrawable(homeScreenComputerButton);
        computerButtonStyle.down = new TextureRegionDrawable(homeScreenComputerButton);

        ImageButton playerButton = new ImageButton(playerButtonStyle);
        playerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new StartScreen(game));
            }
        });
        playerButton.setPosition(601, 241);
        stage.addActor(playerButton);

        ImageButton computerButton = new ImageButton(computerButtonStyle);
        computerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new StartScreenComputer(game));
            }
        });
        computerButton.setPosition(601, 147);
        stage.addActor(computerButton);

        ImageButton exitButton = new ImageButton(new TextureRegionDrawable(homeScreenExitButton));
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        exitButton.setPosition(888, 21);
        stage.addActor(exitButton);

        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(homeScreenBackground, 0, 0);
        batch.draw(homeScreenTank, 68, 94);
        batch.draw(homeScreenFire, 242, 115);
        batch.draw(homeScreenStarsLogo, 344, 346);
//        batch.draw(homeScreenGroundLine, 0, 96);
        batch.draw(homeScreenMusic, 831, 467);
        batch.draw(homeScreenSound, 883, 466);
        batch.end();

        stage.draw();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }
}



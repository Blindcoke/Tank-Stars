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

public class StartScreenComputer extends TankStarsScreen {
    private Texture startScreenSprite;

    private TextureRegion startScreenBackground;
    private TextureRegion startScreenGroundLine;
    private TextureRegion startScreenMusic;
    private TextureRegion startScreenSound;
    private TextureRegion tankStarsLogo;
    private TextureRegion startScreenTank;
    private TextureRegion startScreenFire;
    private TextureRegion startScreenStartButton;

    private TextureRegion startScreenResumeButton;
    private TextureRegion startScreenBackButton;
    private TextureRegion startScreenExitButton;


    private SpriteBatch batch;

    private Stage stage;
    private ImageButton.ImageButtonStyle StartScreenStartButtonStyle;
    private ImageButton.ImageButtonStyle StartScreenResumeButtonStyle;
    private ImageButton.ImageButtonStyle StartScreenBackButtonStyle;
    private ImageButton.ImageButtonStyle StartScreenExitButtonStyle;

    public StartScreenComputer(Game game) {
        super(game);
    }

    @Override
    public void show() {

        startScreenSprite = new Texture("StartScreen/StartScreenSprite.png");
        startScreenBackground = new TextureRegion(startScreenSprite, 0, 330, 960, 540);
        tankStarsLogo = new TextureRegion(startScreenSprite, 0, 78, 309, 160);
        startScreenGroundLine = new TextureRegion(startScreenSprite, 0, 327, 960, 3);
        startScreenMusic = new TextureRegion(startScreenSprite, 0, 0, 39, 32);
        startScreenSound = new TextureRegion(startScreenSprite, 77, 0, 41, 39);
        startScreenTank = new TextureRegion(startScreenSprite, 545, 75, 324, 217);
        startScreenFire = new TextureRegion(startScreenSprite, 307, 75, 238, 252);
        startScreenStartButton = new TextureRegion(startScreenSprite, 442, 0,287, 75);
        startScreenResumeButton = new TextureRegion(startScreenSprite, 166, 0, 276, 75);
        startScreenExitButton = new TextureRegion(startScreenSprite, 118, 0, 48, 47);
        startScreenBackButton = new TextureRegion(startScreenSprite, 39, 0, 38, 40);


        batch = new SpriteBatch();

        stage = new Stage();

        StartScreenStartButtonStyle = new ImageButton.ImageButtonStyle();
        StartScreenStartButtonStyle.up = new TextureRegionDrawable(startScreenStartButton);
        StartScreenStartButtonStyle.down = new TextureRegionDrawable(startScreenStartButton);
        StartScreenStartButtonStyle.checked = new TextureRegionDrawable(startScreenStartButton);


        StartScreenResumeButtonStyle = new ImageButton.ImageButtonStyle();
        StartScreenResumeButtonStyle.up = new TextureRegionDrawable(startScreenResumeButton);
        StartScreenResumeButtonStyle.down = new TextureRegionDrawable(startScreenResumeButton);
        StartScreenResumeButtonStyle.checked = new TextureRegionDrawable(startScreenResumeButton);

        StartScreenBackButtonStyle = new ImageButton.ImageButtonStyle();
        StartScreenBackButtonStyle.up = new TextureRegionDrawable(startScreenBackButton);
        StartScreenBackButtonStyle.down = new TextureRegionDrawable(startScreenBackButton);
        StartScreenBackButtonStyle.checked = new TextureRegionDrawable(startScreenBackButton);

        StartScreenExitButtonStyle = new ImageButton.ImageButtonStyle();
        StartScreenExitButtonStyle.up = new TextureRegionDrawable(startScreenExitButton);
        StartScreenExitButtonStyle.down = new TextureRegionDrawable(startScreenExitButton);

        ImageButton startButton = new ImageButton(StartScreenStartButtonStyle);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new TankChangeScreenComputer(game));
            }
        });
        startButton.setPosition(601, 241);
        stage.addActor(startButton);

        ImageButton resumeButton = new ImageButton(StartScreenResumeButtonStyle);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SavedGamesScreen(game));
            }
        });
        resumeButton.setPosition(601, 147);
        stage.addActor(resumeButton);

        ImageButton backButton = new ImageButton(StartScreenBackButtonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new HomeScreen(game));
            }
        });
        backButton.setPosition(50, 28);
        stage.addActor(backButton);

        ImageButton exitButton = new ImageButton(StartScreenExitButtonStyle);
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
        batch.draw(startScreenBackground, 0, 0);
        batch.draw(startScreenMusic, 831, 467);
        batch.draw(startScreenSound, 883, 466);
        batch.draw(startScreenTank, 68, 94);
        batch.draw(startScreenFire, 242, 115);
        batch.draw(startScreenGroundLine, 0, 96);
        batch.draw(startScreenFire, 242, 115);
        batch.draw(tankStarsLogo, 344, 346);
        batch.end();

        stage.draw();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        startScreenSprite.dispose();
        batch.dispose();
        stage.dispose();
    }
}

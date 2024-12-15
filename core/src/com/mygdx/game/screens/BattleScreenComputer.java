package com.mygdx.game.screens;

import java.util.Random;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class BattleScreenComputer extends TankStarsScreen {
    private int aiDecisionTimer = 0;
    private static final int DECISION_INTERVAL = 60; // Make a decision every second (assuming 60 FPS)
    private Random random = new Random();

    int winner;

    private Batch batch;
    private Stage stage;

    // Images
    private Texture battleScreenSprite;

    private Texture GameOverText;
    private Texture Player1WinsText;
    private Texture Player2WinsText;
    private Texture WinText;

    private Texture pauseMenuSprite;
    private TextureRegion pauseMenuBackground;
    private TextureRegion pauseMenuGeneralImage;
    private TextureRegion pauseMenuOuterRectangle;
    private TextureRegion pauseMenuUpperRectangle;

    private TextureRegion battleScreenBackground;
    private TextureRegion battleScreenEarth;
    private TextureRegion battleScreenLogo;
    private TextureRegion battleScreenPlayer1Health;
    private TextureRegion battleScreenPlayer1;
    private TextureRegion battleScreenPlayer2Health;
    private TextureRegion battleScreenPlaye2;
    private TextureRegion battleScreenRedPlanet;
    private TextureRegion battleScreenRock1;
    private TextureRegion battleScreenRock2;
    private TextureRegion battleScreenRock;
    private TextureRegion battleScreenSuperNova;
    private TextureRegion battleScreenWhitePlanet;
    private TextureRegion battleScreenMenu;
    private TextureRegion battleScreenGround;
    private TextureRegion battleScreenPlayerTank;
    private TextureRegion battleScreenEnemyTank;

    ImageButton.ImageButtonStyle style;
    ImageButton menubutton;

    ProgressBar progressBar1;
    ProgressBar progressBar2;

    private Texture playerHealthBar = new Texture("BattleScreen/player 1 health.png");
    private Texture playerTankFuel = new Texture("BattleScreen/player 1 fuel.png");

    private Texture enemyHealthBar = new Texture("BattleScreen/player 2 health.png");
    private Texture enemyTankFuel = new Texture("BattleScreen/enemy 1 fuel.png");

    // World2D
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Vector2 movement = new Vector2();
    private Body ground;
    private Body playerTankBody;
    private Body enemyTankBody;
    private Body bulletBody;
    private ChainShape groundShape;
    //    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    private Collection<Bullet> bullets = new ArrayList<Bullet>();
    //    private ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();
    private Collection<Bullet> enemyBullets = new ArrayList<Bullet>();
    private ArrayList<Vector2> groundCoords = new ArrayList<Vector2>();

    private final Tank playerTank = Config.getInstance().getPlayerTank();
    private final Tank enemyTank = Config.getInstance().getEnemyTank();

    private float shootHoldTimer = 0;

    private InputProcessor playerProcessor;
    private InputProcessor enemyProcessor;

    public BattleScreenComputer(Game game) {
        super(game);
    }

    public void createWorld() {
        if (playerTank instanceof AtomicTank) {
            playerTank.getTextureRegion().flip(true, false);
        }
        if (enemyTank instanceof SpectreTank || enemyTank instanceof BuratinoTank) {
            enemyTank.getTextureRegion().flip(true, false);
        }

        //ground
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.StaticBody;
        groundBodyDef.position.set(new Vector2(0, 0));
        ground = world.createBody(groundBodyDef);
        groundShape = new ChainShape();
        groundShape.createChain(groundCoords.toArray(new Vector2[groundCoords.size()]));
        ground.createFixture(groundShape, 0.0f);

        //player tank
        BodyDef playerTankBodyDef = new BodyDef();
        playerTankBodyDef.type = BodyDef.BodyType.DynamicBody;
//        playerTankBodyDef.position.set(Config.getInstance().getPlayerTankPosition());
        playerTankBodyDef.position.set(new Vector2(100, 220));
        playerTankBody = world.createBody(playerTankBodyDef);
        PolygonShape playerTankShape = new PolygonShape();
        playerTankShape.setAsBox(60, 40);
        playerTankBody.createFixture(playerTankShape, 0.0f);
        playerTank.setBody(playerTankBody);
        playerTankShape.dispose();

        //enemy tank
        BodyDef enemyTankBodyDef = new BodyDef();
        enemyTankBodyDef.type = BodyDef.BodyType.DynamicBody;
//        enemyTankBodyDef.position.set(Config.getInstance().getEnemyTankPosition());
        enemyTankBodyDef.position.set(new Vector2(700, 220));
        enemyTankBody = world.createBody(enemyTankBodyDef);
        PolygonShape enemyTankShape = new PolygonShape();
        enemyTankShape.setAsBox(60, 40);
        enemyTankBody.createFixture(enemyTankShape, 0.0f);
        enemyTank.setBody(enemyTankBody);
        enemyTankShape.dispose();

        BodyDef leftBorderDef = new BodyDef();
        leftBorderDef.type = BodyDef.BodyType.StaticBody;
        leftBorderDef.position.set(new Vector2(0, 0));
        Body leftBorder = world.createBody(leftBorderDef);
        
        PolygonShape leftBorderShape = new PolygonShape();
        leftBorderShape.setAsBox(1, 540 / 2f); // Height of the screen is 540
        FixtureDef leftFixtureDef = new FixtureDef();
        leftFixtureDef.shape = leftBorderShape;
        leftFixtureDef.friction = 0.5f;
        leftFixtureDef.restitution = 0.3f;
        leftBorder.createFixture(leftFixtureDef);
        leftBorderShape.dispose();

        BodyDef rightBorderDef = new BodyDef();
        rightBorderDef.type = BodyDef.BodyType.StaticBody;
        rightBorderDef.position.set(new Vector2(960, 0)); // Width of the screen is 960
        Body rightBorder = world.createBody(rightBorderDef);
        
        PolygonShape rightBorderShape = new PolygonShape();
        rightBorderShape.setAsBox(1, 540 / 2f);
        FixtureDef rightFixtureDef = new FixtureDef();
        rightFixtureDef.shape = rightBorderShape;
        rightFixtureDef.friction = 0.5f;
        rightFixtureDef.restitution = 0.3f;
        rightBorder.createFixture(rightFixtureDef);
        rightBorderShape.dispose();
    }

    public void createBullet(float speedX, float speedY) {
        shootHoldTimer = (shootHoldTimer + 1) * 2; // +1 to set the base as 1 instead of 0, *2 to make it faster to charge up shots.
        if (shootHoldTimer > 2.5f) {
            shootHoldTimer = 2.5f;
        }
        BodyDef bulletBodyDef = new BodyDef();
        bulletBodyDef.type = BodyDef.BodyType.DynamicBody;
        bulletBodyDef.position.set(new Vector2(playerTank.getBody().getPosition().x + 50, playerTank.getBody().getPosition().y + 10));
        bulletBody = world.createBody(bulletBodyDef);

        PolygonShape bulletShape = new PolygonShape();
        bulletShape.setAsBox((float) playerTank.getBulletType().getWidth() / 2, (float) playerTank.getBulletType().getHeight() / 2);
        bulletBody.createFixture(bulletShape, 0.0f);
        bulletShape.dispose();
        if (Objects.equals(playerTank.getTankName(), "Buratino")) {
//            bulletBody.setLinearVelocity(playerTank.getBulletType().getSpeed(), 0);
            bulletBody.applyForceToCenter(speedX * 100f, speedY, true);
            // set bullet gravity to 0
            bulletBody.setGravityScale(0);
            // set playerTank fuel to 5
            playerTank.setFuelCapacity(5);
        } else {
            // apply variable force according to the angle
            bulletBody.applyForceToCenter(speedX, speedY, true);
            // apply gravity to the bullet randomly between 5 to 10
            bulletBody.setGravityScale((float) (Math.random() * 5 + 5));
            // set playerTank fuel to 5 for both tanks
            playerTank.setFuelCapacity(5);
        }

        Bullet bullet = new Bullet(playerTank.getBulletType().getDamage() * shootHoldTimer, playerTank.getBulletType().getSpeed(), playerTank);
        System.out.println("Created bullet with damage value " + bullet.getDamage());
        bullet.setBody(bulletBody);
        bullets.add(bullet);
    }

    public void createEnemyBullet(float speedX, float speedY) {
        shootHoldTimer = (shootHoldTimer + 1) * 2; // +1 to set the base as 1 instead of 0, *2 to make it faster to charge up shots.
        if (shootHoldTimer > 2.5f) {
            shootHoldTimer = 2.5f;
        }
        BodyDef bulletBodyDef = new BodyDef();
        bulletBodyDef.type = BodyDef.BodyType.DynamicBody;
        bulletBodyDef.position.set(new Vector2(enemyTank.getBody().getPosition().x - 50, enemyTank.getBody().getPosition().y + 10));
        bulletBody = world.createBody(bulletBodyDef);
        PolygonShape bulletShape = new PolygonShape();
        bulletShape.setAsBox((float) enemyTank.getBulletType().getWidth() / 2, (float) enemyTank.getBulletType().getHeight() / 2);
        bulletBody.createFixture(bulletShape, 0.0f);
        bulletShape.dispose();
        if (Objects.equals(enemyTank.getTankName(), "Buratino")) {
//            bulletBody.setLinearVelocity(-enemyTank.getBulletType().getSpeed(), 0);
            bulletBody.applyForceToCenter(-speedX * 100f, speedY, true);
            bulletBody.setGravityScale(0);
            enemyTank.setFuelCapacity(5);
        } else {
            bulletBody.applyForceToCenter(-speedX, speedY, true);
            bulletBody.setGravityScale((float) (Math.random() * 5 + 5));
            enemyTank.setFuelCapacity(5);
        }

        Bullet bullet = new Bullet(enemyTank.getBulletType().getDamage() * shootHoldTimer, enemyTank.getBulletType().getSpeed(), enemyTank);
        System.out.println("Created enemy bullet with damage value " + bullet.getDamage());
        bullet.setBody(bulletBody);
        enemyBullets.add(bullet);
    }

    @Override
    public void show() {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Images
        battleScreenSprite = new Texture("BattleScreen/BattleScreenSprite.png");
        battleScreenBackground = new TextureRegion(battleScreenSprite, 0, 454, 960, 540);
        battleScreenEarth = new TextureRegion(battleScreenSprite, 596, 51, 153, 156);
        battleScreenLogo = new TextureRegion(battleScreenSprite, 449, 51, 147, 149);
        battleScreenPlayer1Health = new TextureRegion(battleScreenSprite, 261, 0, 277, 45);
        battleScreenPlayer1 = new TextureRegion(battleScreenSprite, 73, 0, 77, 26);
        battleScreenPlayer2Health = new TextureRegion(battleScreenSprite, 590, 0, 269, 49);
        battleScreenPlaye2 = new TextureRegion(battleScreenSprite, 0, 0, 73, 19);
        battleScreenRedPlanet = new TextureRegion(battleScreenSprite, 174, 51, 81, 67);
        battleScreenRock1 = new TextureRegion(battleScreenSprite, 538, 0, 52, 47);
        battleScreenRock2 = new TextureRegion(battleScreenSprite, 150, 0, 50, 33);
        battleScreenRock = new TextureRegion(battleScreenSprite, 200, 0, 61, 37);
        battleScreenSuperNova = new TextureRegion(battleScreenSprite, 255, 51, 108, 99);
        battleScreenWhitePlanet = new TextureRegion(battleScreenSprite, 363, 51, 86, 126);
        battleScreenMenu = new TextureRegion(battleScreenSprite, 859, 0, 55, 51);
        battleScreenGround = new TextureRegion(battleScreenSprite, 0, 207, 960, 247);
        battleScreenPlayerTank = playerTank.getTextureRegion();
        battleScreenEnemyTank = enemyTank.getTextureRegion();
//        battleScreenPlayerTank = new TextureRegion(battleScreenSprite, 0, 51, 86, 56);
//        battleScreenEnemyTank = new TextureRegion(battleScreenSprite, 86, 51, 88, 62);
        GameOverText = new Texture("PauseMenu/GameOver.png");
        Player1WinsText = new Texture("PauseMenu/Player1.png");
        Player2WinsText = new Texture("PauseMenu/Player2.png");
        WinText = new Texture("PauseMenu/Wins.png");
        WinText.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pauseMenuSprite = new Texture("PauseMenu/PauseMenuSprite.png");
        pauseMenuBackground = new TextureRegion(pauseMenuSprite, 0, 0, 240, 426);
        pauseMenuGeneralImage = new TextureRegion(pauseMenuSprite, 40, 426, 141, 142);
        pauseMenuOuterRectangle = new TextureRegion(pauseMenuSprite, 0, 568, 246, 432);
        pauseMenuUpperRectangle = new TextureRegion(pauseMenuSprite, 0, 1088, 172, 66);

        style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(battleScreenMenu);
        menubutton = new ImageButton(style);
        menubutton.setPosition(27, 463);
        menubutton.setSize(55, 51);

        // if menu button is pressed, set screen to pauseMenu
        menubutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new PauseMenu(game));
            }
        });

        stage.addActor(menubutton);

        // World2D
        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 960, 540);
        camera.update();

        // Ground
        groundCoords.add(new Vector2(0, 189));
        groundCoords.add(new Vector2(70, 186));
        groundCoords.add(new Vector2(157, 192));
        groundCoords.add(new Vector2(390, 190));
        groundCoords.add(new Vector2(484, 193));
        groundCoords.add(new Vector2(553, 191));
        groundCoords.add(new Vector2(736, 190));
        groundCoords.add(new Vector2(870, 200));
        groundCoords.add(new Vector2(960, 234));
        createWorld();

        playerProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.A && playerTank.getFuelCapacity() > 0) {
                    playerTank.getBody().applyLinearImpulse((float) -playerTank.getMoveSpeed(), 0, playerTank.getBody().getPosition().x, playerTank.getBody().getPosition().y, true);
                    playerTank.setFuelCapacity(playerTank.getFuelCapacity() - 1);
                    System.out.println("Player Fuel capacity: " + playerTank.getFuelCapacity());
                }
                if (keycode == Input.Keys.D && playerTank.getFuelCapacity() > 0) {
                    playerTank.getBody().applyLinearImpulse((float) playerTank.getMoveSpeed(), 0, playerTank.getBody().getPosition().x, playerTank.getBody().getPosition().y, true);
                    playerTank.setFuelCapacity(playerTank.getFuelCapacity() - 1);
                    System.out.println("Player Fuel capacity: " + playerTank.getFuelCapacity());
                }
                if (keycode == Input.Keys.G) {
                    shootHoldTimer = 0;
                    return true;
                }
                if (keycode == Input.Keys.P) {
                    // Get position of enemyTank
                    Vector2 enemyTankPosition = enemyTank.getBody().getPosition();
                    // Get position of playerTank
                    Vector2 playerTankPosition = playerTank.getBody().getPosition();
                    Config.getInstance().setPlayerTankPosition(playerTankPosition);
                    Config.getInstance().setEnemyTankPosition(enemyTankPosition);
                    game.setScreen(new PauseMenu(game));
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.A || keycode == Input.Keys.D) {
                    Vector2 vec = playerTank.getBody().getLinearVelocity();
                    vec.x = 0;
                    playerTank.getBody().setLinearVelocity(vec);
                }
                if (keycode == Input.Keys.G) {
                    System.out.println("Shoot hold timer: " + shootHoldTimer);
                    if (Objects.equals(playerTank.getTankName(), "Buratino")) {
                        createBullet(playerTank.getBulletType().getSpeed(), 0);
                    } else if (Objects.equals(playerTank.getTankName(), "Spectre")) {
                        createBullet(playerTank.getBulletType().getSpeed(), playerTank.getBulletType().getSpeed());
                    } else {
                        createBullet(playerTank.getBulletType().getSpeed(), playerTank.getBulletType().getSpeed() * 2.0f);
                        createBullet(playerTank.getBulletType().getSpeed(), playerTank.getBulletType().getSpeed());
                        createBullet(playerTank.getBulletType().getSpeed(), playerTank.getBulletType().getSpeed() * 0.75f);
                    }
                    Config.getInstance().setPlayerTwosTurn();
                }
                return true;
            }
        };
        }
        private void processAITurn(float delta) {
            aiDecisionTimer++;

            // Only make decisions at set intervals
            if (aiDecisionTimer >= DECISION_INTERVAL) {
                aiDecisionTimer = 0;

                // Get tank positions
                Vector2 enemyPosition = enemyTank.getBody().getPosition();
                Vector2 playerPosition = playerTank.getBody().getPosition();

                // Calculate distance between tanks
                float distance = Math.abs(playerPosition.x - enemyPosition.x);

                // Decision making strategy
                if (enemyTank.getFuelCapacity() > 0) {
                    // Movement strategy
                    if (distance > 300) {  // If too far, move closer
                        moveAITankTowardsPlayer(playerPosition, enemyPosition);
                    } else if (distance < 200) {  // If too close, create some distance
                        moveAITankAwayFromPlayer(playerPosition, enemyPosition);
                    }
                }

                // Shooting strategy
                decideAIShootingStrategy(distance);
            }
        }

        private void moveAITankTowardsPlayer(Vector2 playerPos, Vector2 enemyPos) {
            if (playerPos.x < enemyPos.x && enemyTank.getFuelCapacity() > 0) {
                // Move left towards player
                enemyTank.getBody().applyLinearImpulse((float) -enemyTank.getMoveSpeed(), 0, 
                    enemyTank.getBody().getPosition().x, enemyTank.getBody().getPosition().y, true);
                enemyTank.setFuelCapacity(enemyTank.getFuelCapacity() - 1);
                System.out.println("AI Tank moving left. Fuel: " + enemyTank.getFuelCapacity());
            } else if (playerPos.x > enemyPos.x && enemyTank.getFuelCapacity() > 0) {
                // Move right towards player
                enemyTank.getBody().applyLinearImpulse((float) enemyTank.getMoveSpeed(), 0, 
                    enemyTank.getBody().getPosition().x, enemyTank.getBody().getPosition().y, true);
                enemyTank.setFuelCapacity(enemyTank.getFuelCapacity() - 1);
                System.out.println("AI Tank moving right. Fuel: " + enemyTank.getFuelCapacity());
            }
        }

        private void moveAITankAwayFromPlayer(Vector2 playerPos, Vector2 enemyPos) {
            if (playerPos.x < enemyPos.x && enemyTank.getFuelCapacity() > 0) {
                // Move right to create distance
                enemyTank.getBody().applyLinearImpulse((float) enemyTank.getMoveSpeed(), 0, 
                    enemyTank.getBody().getPosition().x, enemyTank.getBody().getPosition().y, true);
                enemyTank.setFuelCapacity(enemyTank.getFuelCapacity() - 1);
                System.out.println("AI Tank creating distance. Fuel: " + enemyTank.getFuelCapacity());
            } else if (playerPos.x > enemyPos.x && enemyTank.getFuelCapacity() > 0) {
                // Move left to create distance
                enemyTank.getBody().applyLinearImpulse((float) -enemyTank.getMoveSpeed(), 0, 
                    enemyTank.getBody().getPosition().x, enemyTank.getBody().getPosition().y, true);
                enemyTank.setFuelCapacity(enemyTank.getFuelCapacity() - 1);
                System.out.println("AI Tank creating distance. Fuel: " + enemyTank.getFuelCapacity());
            }
        }

        private void decideAIShootingStrategy(float distance) {
            // Shooting probability based on distance and tank health
            float shootProbability = calculateAIShootProbability(distance);

            // Random chance to shoot
            if (new Random().nextFloat() < shootProbability) {
                System.out.println("AI deciding to shoot...");
                shootHoldTimer = calculateAIShootHoldTime(distance);
                simulateAIShoot();
            }
        }

        private float calculateAIShootProbability(float distance) {
            // Higher probability when closer, lower when far
            float baseProbability = 1.0f - (distance / 1000f);
            
            // Factor in tank health - more likely to shoot when health is low
            float healthFactor = 1.0f - (enemyTank.getCurrentHealth() / (float)enemyTank.getHealthCapacity());
            
            return Math.min(baseProbability * (1 + healthFactor), 1.0f);
        }

        private float calculateAIShootHoldTime(float distance) {
            // Hold time varies based on distance
            // Closer distance means less hold time, farther means more charge
            return Math.max(0.5f, Math.min(2.0f, distance / 300f));
        }

        private void simulateAIShoot() {
            if (Objects.equals(enemyTank.getTankName(), "Buratino")) {
                createEnemyBullet(enemyTank.getBulletType().getSpeed(), 0);
            } else if (Objects.equals(enemyTank.getTankName(), "Spectre")) {
                createEnemyBullet(enemyTank.getBulletType().getSpeed(), enemyTank.getBulletType().getSpeed());
            } else {
                createEnemyBullet(enemyTank.getBulletType().getSpeed(), enemyTank.getBulletType().getSpeed() * 2.0f);
                createEnemyBullet(enemyTank.getBulletType().getSpeed(), enemyTank.getBulletType().getSpeed());
                createEnemyBullet(enemyTank.getBulletType().getSpeed(), enemyTank.getBulletType().getSpeed() * 0.75f);
            }
            Config.getInstance().setPlayerOnesTurn();
        }

        @Override
        public void render(float delta) {
        stage.act(delta);
            if (Config.getInstance().isPlayerOnesTurn()) {
                Gdx.input.setInputProcessor(playerProcessor);
                // check if the space bar is held
                if (Gdx.input.isKeyPressed(Input.Keys.G)) {
                    shootHoldTimer += Gdx.graphics.getDeltaTime();
                }
            } else {
            // Process AI turn
                processAITurn(delta);
                
                // Simulate continuous shooting mechanics
                shootHoldTimer += Gdx.graphics.getDeltaTime();
            }
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            Batch batch = new SpriteBatch();
            batch.begin();

            batch.draw(battleScreenBackground, 0, 0);
            batch.draw(battleScreenEarth, 750, 288);

            batch.draw(battleScreenPlayer1, Config.getInstance().getPlayerTankPosition().x, Config.getInstance().getPlayerTankPosition().y);
            batch.draw(battleScreenPlaye2, Config.getInstance().getEnemyTankPosition().x, Config.getInstance().getEnemyTankPosition().y);
            batch.draw(battleScreenRedPlanet, 523, 299);
            batch.draw(playerHealthBar, 207, 448, 277 * ((float) playerTank.getCurrentHealth() / playerTank.getHealthCapacity()), 45);
            batch.draw(enemyHealthBar, 484, 448, 277 * ((float) enemyTank.getCurrentHealth() / enemyTank.getHealthCapacity()), 45);
            batch.draw(battleScreenLogo, 404, 381);
            batch.draw(battleScreenRock1, 3, 160);
            batch.draw(battleScreenRock2, 264, 175);
            batch.draw(battleScreenRock, 851, 194);
            batch.draw(battleScreenSuperNova, 304, 299);
            batch.draw(battleScreenWhitePlanet, 75, 303);
            batch.draw(battleScreenGround, 0, 0);
            batch.draw(playerTankFuel, 100, 81, 150 * ((float) playerTank.getFuelCapacity() / 5), 20);
            batch.draw(enemyTankFuel, 700, 81, 150 * ((float) enemyTank.getFuelCapacity() / 5), 20);
            try {
                batch.draw(battleScreenPlayerTank, playerTank.getBody().getPosition().x - 45, playerTank.getBody().getPosition().y - 40);
            } catch (NullPointerException e) {
                System.out.println(battleScreenPlayerTank);
                System.out.println(playerTank.getBody());
                System.out.println(playerTank.getBody().getPosition());
            }
            batch.draw(battleScreenEnemyTank, enemyTank.getBody().getPosition().x - 30, enemyTank.getBody().getPosition().y - 40);
//        batch.draw(BulletImage, bulletBody.getPosition().x - 5, bulletBody.getPosition().y - 5);
            batch.end();
            stage.draw();

            // World2D
            world.step(1 / 60f, 6, 2);

            // Check for bullet-tank collision
            CollisionRect bulletRect;
            CollisionRect enemyTankRect = new CollisionRect((int) enemyTank.getBody().getPosition().x - 40, (int) enemyTank.getBody().getPosition().y - 40, 60 + 80, 40 + 80);
            CollisionRect playerTankRect = new CollisionRect((int) playerTank.getBody().getPosition().x, (int) playerTank.getBody().getPosition().y, 60 + 10, 40 + 10);
            Iterator<Bullet> iterator = bullets.iterator();

            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                bulletRect = new CollisionRect((int) bullet.getBody().getPosition().x, (int) bullet.getBody().getPosition().y, 40, 40);
                if (bulletRect.collidesWith(enemyTankRect)) {
                    enemyTank.getBody().applyLinearImpulse(bullet.getSpeed(), 0, enemyTank.getBody().getPosition().x, enemyTank.getBody().getPosition().y, true);
                    enemyTank.getBody().setLinearDamping(1.0f);
                    enemyTank.reduceHealth((int) bullet.getDamage());
                    bullets.remove(bullet);
                    world.destroyBody(bullet.getBody());

                    // progressBar2.setValue(enemyTank.getCurrentHealth());
                    System.out.println("Enemy tank health: " + enemyTank.getCurrentHealth());
                    if (enemyTank.getCurrentHealth() <= 0) {
                        batch.begin();
                        batch.draw(pauseMenuOuterRectangle, 345, 39);
                        batch.draw(pauseMenuBackground, 348, 42);
                        batch.draw(pauseMenuGeneralImage, 398, 267);
                        batch.draw(pauseMenuUpperRectangle, 384, 434);
                        batch.draw(GameOverText, 419, 460);
                        batch.draw(Player1WinsText, 391, 165);
                        batch.draw(WinText, 395, 96);
                        batch.end();
                        pause();
                        System.out.println("Enemy tank destroyed");
                        world.destroyBody(enemyTank.getBody());
                        Gdx.app.exit();
                    }
                    break;
                }
            }
            iterator = enemyBullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                bulletRect = new CollisionRect((int) bullet.getBody().getPosition().x - 10, (int) bullet.getBody().getPosition().y - 10, 20 + 20, 20 + 20);
                if (playerTankRect.collidesWith(bulletRect)) {
                    playerTank.getBody().applyLinearImpulse(-bullet.getSpeed(), 0, playerTank.getBody().getPosition().x, playerTank.getBody().getPosition().y, true);
                    playerTank.getBody().setLinearDamping(1.0f);
                    playerTank.reduceHealth((int) bullet.getDamage());
                    enemyBullets.remove(bullet);
                    world.destroyBody(bullet.getBody());

                    // progressBar1.setValue(playerTank.getCurrentHealth());
                    System.out.println("Player tank health: " + playerTank.getCurrentHealth());
                    if (playerTank.getCurrentHealth() <= 0) {
                        batch.begin();
                        batch.draw(pauseMenuOuterRectangle, 345, 39);
                        batch.draw(pauseMenuBackground, 348, 42);
                        batch.draw(pauseMenuGeneralImage, 398, 267);
                        batch.draw(pauseMenuUpperRectangle, 384, 434);
                        batch.draw(GameOverText, 419, 460);
                        batch.draw(Player2WinsText, 391, 165);
                        batch.draw(WinText, 395, 96);
                        batch.end();
                        pause();
                        System.out.println("Player tank destroyed");
                        world.destroyBody(playerTank.getBody());
                        Gdx.app.exit();
                    }
                    break;
                }
            }

            iterator = bullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                if (bullet.getBody().getPosition().x > 960 || bullet.getBody().getPosition().x < 0 || bullet.getBody().getPosition().y > 540 || bullet.getBody().getPosition().y < 205) {
                    bullets.remove(bullet);
                    world.destroyBody(bullet.getBody());
                    break;
                }
            }

            iterator = enemyBullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                if (bullet.getBody().getPosition().x > 960 || bullet.getBody().getPosition().x < 0 || bullet.getBody().getPosition().y > 540 || bullet.getBody().getPosition().y < 205) {
                    enemyBullets.remove(bullet);
                    world.destroyBody(bullet.getBody());
                    break;
                }
            }

            debugRenderer.render(world, camera.combined);
            camera.update();
        }

        @Override
        public void hide
            
        
            () {
        dispose();
        }

        @Override
        public void dispose
            
        
            () {
        battleScreenSprite.dispose();
            stage.dispose();
            world.dispose();
            debugRenderer.dispose();
        }

        @Override
        public void pause
            
        
            () {
        try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void resume
        
        
        
        
        () {
    }

    @Override
        public void resize
        (int width, int height


    
        
        


    
        ) {
    }
}

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
import com.badlogic.gdx.math.Rectangle;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class BattleScreenComputer extends TankStarsScreen {

    private int aiDecisionTimer = 0;
    private static final int DECISION_INTERVAL = 60; // Make a decision every second (assuming 60 FPS)
    private Random random = new Random();
    private boolean canPlayerShoot = true;
    int winner;

    private enum Action {
        MOVE_TOWARDS_PLAYER, // Move the AI tank closer to the player
        EVADE, // Evade incoming bullets
        SHOOT, // Shoot at the player
        STOP                  // Stop moving (optional, can be used for idle state)
    }

    private Batch batch;
    private Stage stage;

    // Images
    private Texture battleScreenSprite;

    private Texture GameOverText;
    private Texture Player1WinsText;
    private Texture Player2WinsText;
    private Texture WinText;
    private Texture battleBullet;
    private Texture battleBulletRed;

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

        BodyDef playerTankBodyDef = new BodyDef();
        playerTankBodyDef.type = BodyDef.BodyType.DynamicBody;
        playerTankBodyDef.position.set(new Vector2(100, 220));
        playerTankBody = world.createBody(playerTankBodyDef);
        PolygonShape playerTankShape = new PolygonShape();
        // Reduce the size of the box to make it tighter to the tank image
        playerTankShape.setAsBox(40, 30); // Reduced from 60, 40
        playerTankBody.createFixture(playerTankShape, 0.0f);
        playerTank.setBody(playerTankBody);
        playerTankShape.dispose();

        //enemy tank
        BodyDef enemyTankBodyDef = new BodyDef();
        enemyTankBodyDef.type = BodyDef.BodyType.DynamicBody;
        enemyTankBodyDef.position.set(new Vector2(700, 220));
        enemyTankBody = world.createBody(enemyTankBodyDef);
        PolygonShape enemyTankShape = new PolygonShape();
        // Reduce the size of the box to make it tighter to the tank image
        enemyTankShape.setAsBox(40, 30); // Reduced from 60, 40
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
            bulletBody.applyForceToCenter(speedX, speedY, true);
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
        battleBullet = new Texture("BattleScreen/Bullet.png");
        battleBulletRed = new Texture("BattleScreen/BulletRed.png");
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
                // Only allow movement and shooting if it's the player's turn and the computer has shot
                if (!Config.getInstance().isPlayerOnesTurn()) {
                    return true; // Ignore input if not player's turn
                }

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
                // Only allow shooting if it's the player's turn and they are allowed to shoot
                if (!Config.getInstance().isPlayerOnesTurn()) {
                    return true; // Ignore input if not player's turn
                }

                if (keycode == Input.Keys.A || keycode == Input.Keys.D) {
                    Vector2 vec = playerTank.getBody().getLinearVelocity();
                    vec.x = 0;
                    playerTank.getBody().setLinearVelocity(vec);
                }
                if (keycode == Input.Keys.G) {
                    // Only allow shooting if it's the player's turn and they are allowed to shoot
                    if (canPlayerShoot) {
                        System.out.println("Shoot hold timer: " + shootHoldTimer);
                        if (Objects.equals(playerTank.getTankName(), "Buratino")) {
                            createBullet(playerTank.getBulletType().getSpeed()*100, 0);
                        } else if (Objects.equals(playerTank.getTankName(), "Spectre")) {
                            createBullet(playerTank.getBulletType().getSpeed()*100, playerTank.getBulletType().getSpeed());
                        } else {
                            createBullet(playerTank.getBulletType().getSpeed()*100, playerTank.getBulletType().getSpeed() * 2.0f);
                            createBullet(playerTank.getBulletType().getSpeed()*100, playerTank.getBulletType().getSpeed());
                            createBullet(playerTank.getBulletType().getSpeed()*100, playerTank.getBulletType().getSpeed() * 0.75f);
                        }

                        // Prevent player from shooting again until computer's turn
                        canPlayerShoot = false;
                        Config.getInstance().setPlayerTwosTurn();
                    }
                }
                return true;
            }
        };
    }

    private void processAITurn() {
        aiDecisionTimer++;

        if (aiDecisionTimer >= DECISION_INTERVAL) {
            aiDecisionTimer = 0;

            if (enemyTank.getFuelCapacity() <= 0) {
                handleOutOfFuelState();
                return;
            }

            System.out.println("AI Decision Time...");
            MonteCarloTreeSearch mcts = new MonteCarloTreeSearch();
            Action bestAction = mcts.findBestAction(enemyTank, playerTank, bullets);

            executeAIAction(bestAction);
        }
    }

    private void handleOutOfFuelState() {
        stopAITank();
        System.out.println("AI is out of fuel. Shooting from current position.");
        simulateAIShoot();
    }

    private void executeAIAction(Action action) {
        switch (action) {
            case MOVE_TOWARDS_PLAYER:
                moveAITankTowardsPlayer(playerTank.getBody().getPosition(), enemyTank.getBody().getPosition());
                break;
            case EVADE:
                performEvasiveAction(playerTank.getBody().getPosition(), enemyTank.getBody().getPosition());
                break;
            case SHOOT:
                stopAITank();
                simulateAIShoot();
                break;
            default:
                stopAITank();
        }
    }

    private class MonteCarloTreeSearch {

        public Action findBestAction(Tank enemyTank, Tank playerTank, Collection<Bullet> bullets) {
            int moveTowardsPlayer = 0;
            int evade = 0;
            int shoot = 0;

            for (int i = 0; i < 1000; i++) {
                if (simulateEvasion(enemyTank, bullets)) {
                    evade++;
                }
                else if (simulateMoveTowardsPlayer(enemyTank, playerTank)) {
                    moveTowardsPlayer++;
                }
                else {
                    shoot++;
                }
            }
            System.out.println("Best action probabilities: Move towards player: " + moveTowardsPlayer + ", Evade: " + evade + ", Shoot: " + shoot);
            return chooseBestAction(moveTowardsPlayer, shoot, evade);
        }


        private boolean simulateMoveTowardsPlayer(Tank enemy, Tank player) {
            float distance = player.getBody().getPosition().dst(enemy.getBody().getPosition());
            if (Objects.equals(enemyTank.getTankName(), "Buratino")) {
                return distance > 500; // Buratino хоче залишатись далеко
            } else if (Objects.equals(enemyTank.getTankName(), "Spectre")) {
                return distance > 300; // Spectre не дуже переймається відстанню
            } else {
                return distance > 200; // Інші танки намагаються бути ближче
            }
        }

        private boolean simulateEvasion(Tank enemy, Collection<Bullet> bullets) {
            for (Bullet bullet : bullets) {
                if (isShotReachable(enemy.getBody().getPosition(), bullet.getBody().getPosition())) {
                    return true;
                }
            }
            return false;
        }


        private boolean simulateShoot(Tank enemy, Tank player) {
            return isShotReachable(player.getBody().getPosition(), enemy.getBody().getPosition());
        }

        private Action chooseBestAction(int move, int shoot, int evade) {
            if (evade > move && evade > shoot) {
                return Action.EVADE;
            }
            if (shoot > move) {
                return Action.SHOOT;
            }
            return Action.MOVE_TOWARDS_PLAYER;
        }
    }

    private boolean isShotReachable(Vector2 playerPos, Vector2 enemyPos) {
        float distance = playerPos.dst(enemyPos);
        return distance < 200;
    }

    private void performEvasiveAction(Vector2 playerPos, Vector2 enemyPos) {
        float direction = playerPos.x < enemyPos.x ? 1 : -1;
        System.out.println(".(AI) Evading incoming bullets.");
        applyMovement(direction * enemyTank.getMoveSpeed(), "AI evading incoming bullets.");

    }


    private void moveAITankTowardsPlayer(Vector2 playerPos, Vector2 enemyPos) {
        float optimalDistance = 200f;
        float currentDistance = Math.abs(playerPos.x - enemyPos.x);

        if (Math.abs(currentDistance - optimalDistance) < 50) {
            return;
        }

        float direction = playerPos.x < enemyPos.x ? -1 : 1;
        applyMovement(direction * enemyTank.getMoveSpeed(), "AI moving closer to player.");
    }

    private void applyMovement(float speed, String actionDescription) {
        enemyTank.getBody().applyLinearImpulse(speed, 0, enemyTank.getBody().getPosition().x, enemyTank.getBody().getPosition().y, true);
        enemyTank.setFuelCapacity(enemyTank.getFuelCapacity() - 1);
        System.out.println(actionDescription + " Fuel: " + enemyTank.getFuelCapacity());
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

        canPlayerShoot = true;
        Config.getInstance().setPlayerOnesTurn();
    }

    private void stopAITank() {
        enemyTank.getBody().setLinearVelocity(0, 0);
    }

    public class CollisionHelper {

        public boolean checkPreciseCollision(Vector2 bulletPos, Vector2 tankPos, float bulletWidth, float bulletHeight, float tankWidth, float tankHeight) {
            // Create precise rectangles for bullet and tank
            Rectangle bulletRect = new Rectangle(
                    bulletPos.x - bulletWidth / 2,
                    bulletPos.y - bulletHeight / 2,
                    bulletWidth,
                    bulletHeight
            );

            Rectangle tankRect = new Rectangle(
                    tankPos.x - tankWidth / 2,
                    tankPos.y - tankHeight / 2,
                    tankWidth,
                    tankHeight
            );

            return bulletRect.overlaps(tankRect);
        }

        public Vector2 calculateBulletImpactForce(Vector2 bulletVelocity) {
            // Calculate impact force based on bullet velocity
            float impactMultiplier = 50f; // Adjust this value to change impact strength
            return new Vector2(
                    bulletVelocity.x * impactMultiplier,
                    bulletVelocity.y * impactMultiplier
            );
        }
    }

    @Override
    public void render(float delta) {
        // Update stage logic
        stage.act(delta);

        // Determine turn
        if (Config.getInstance().isPlayerOnesTurn()) {
            handlePlayerTurn(delta);
        } else {
            processAITurn();
        }

        // Clear screen
        clearScreen();

        // Begin drawing elements
        Batch batch = new SpriteBatch();
        batch.begin();

        drawBackground(batch);
        drawTanks(batch);
        drawBullets(batch);
        drawUI(batch);

        batch.end();

        // Draw stage UI
        stage.draw();

        // Update physics world
        updatePhysicsWorld();

        // Handle collisions and bullet cleanup
        handleBulletCollisions();
        cleanupOffScreenBullets();

        // Render debug elements
        debugRenderer.render(world, camera.combined);
        camera.update();
    }

    private void handlePlayerTurn(float delta) {
        Gdx.input.setInputProcessor(playerProcessor);
        if (Gdx.input.isKeyPressed(Input.Keys.G)) {
            shootHoldTimer += delta;
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawBackground(Batch batch) {
        batch.draw(battleScreenBackground, 0, 0);
        batch.draw(battleScreenEarth, 750, 288);
        batch.draw(battleScreenRedPlanet, 523, 299);
        batch.draw(battleScreenSuperNova, 304, 299);
        batch.draw(battleScreenWhitePlanet, 75, 303);
        batch.draw(battleScreenGround, 0, 0);
        batch.draw(battleScreenRock1, 3, 160);
        batch.draw(battleScreenRock2, 264, 175);
        batch.draw(battleScreenRock, 851, 194);
        batch.draw(battleScreenLogo, 404, 381);
    }

    private void drawTanks(Batch batch) {
        batch.draw(battleScreenPlayer1, Config.getInstance().getPlayerTankPosition().x, Config.getInstance().getPlayerTankPosition().y);
        batch.draw(battleScreenPlaye2, Config.getInstance().getEnemyTankPosition().x, Config.getInstance().getEnemyTankPosition().y);

        try {
            batch.draw(battleScreenPlayerTank, playerTank.getBody().getPosition().x - 45, playerTank.getBody().getPosition().y - 40);
        } catch (NullPointerException e) {
            System.out.println("Player Tank Issue: " + e.getMessage());
        }
        batch.draw(battleScreenEnemyTank, enemyTank.getBody().getPosition().x - 30, enemyTank.getBody().getPosition().y - 40);
    }

    private void drawBullets(Batch batch) {
        for (Bullet bullet : bullets) {
            batch.draw(battleBullet, bullet.getBody().getPosition().x - 10, bullet.getBody().getPosition().y - 10, 20, 20);
        }
        for (Bullet bullet : enemyBullets) {
            batch.draw(battleBulletRed, bullet.getBody().getPosition().x + 10, bullet.getBody().getPosition().y - 10, -20, 20);
        }
    }

    private void drawUI(Batch batch) {
        batch.draw(playerHealthBar, 207, 448, 277 * ((float) playerTank.getCurrentHealth() / playerTank.getHealthCapacity()), 45);
        batch.draw(enemyHealthBar, 484, 448, 277 * ((float) enemyTank.getCurrentHealth() / enemyTank.getHealthCapacity()), 45);
        batch.draw(playerTankFuel, 100, 81, 150 * ((float) playerTank.getFuelCapacity() / 5), 20);
        batch.draw(enemyTankFuel, 700, 81, 150 * ((float) enemyTank.getFuelCapacity() / 5), 20);
    }

    private void updatePhysicsWorld() {
        world.step(1 / 60f, 6, 2);
    }

    private void handleBulletToBulletCollisions() {
        Iterator<Bullet> playerBulletIterator = bullets.iterator();
        while (playerBulletIterator.hasNext()) {
            Bullet playerBullet = playerBulletIterator.next();
            CollisionRect playerBulletRect = new CollisionRect(
                    (int) playerBullet.getBody().getPosition().x,
                    (int) playerBullet.getBody().getPosition().y,
                    40, 40
            );

            Iterator<Bullet> enemyBulletIterator = enemyBullets.iterator();
            while (enemyBulletIterator.hasNext()) {
                Bullet enemyBullet = enemyBulletIterator.next();
                CollisionRect enemyBulletRect = new CollisionRect(
                        (int) enemyBullet.getBody().getPosition().x,
                        (int) enemyBullet.getBody().getPosition().y,
                        40, 40
                );

                if (playerBulletRect.collidesWith(enemyBulletRect)) {
                    // Destroy both bullets
                    world.destroyBody(playerBullet.getBody());
                    world.destroyBody(enemyBullet.getBody());

                    // Remove from respective lists
                    playerBulletIterator.remove();
                    enemyBulletIterator.remove();

                    // Break inner loop as playerBullet is already destroyed
                    break;
                }
            }
        }
    }

    private void handleBulletCollisions() {
        // Existing tank collision handling
        CollisionRect enemyTankRect = createTankCollisionRect(enemyTank);
        CollisionRect playerTankRect = createTankCollisionRect(playerTank);

        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            if (handleBulletCollision(bulletIterator.next(), enemyTank, enemyTankRect)) {
                bulletIterator.remove();
            }
        }

        Iterator<Bullet> enemyBulletIterator = enemyBullets.iterator();
        while (enemyBulletIterator.hasNext()) {
            if (handleBulletCollision(enemyBulletIterator.next(), playerTank, playerTankRect)) {
                enemyBulletIterator.remove();
            }
        }

        // New bullet-to-bullet collision handling
        handleBulletToBulletCollisions();
    }

    private CollisionRect createTankCollisionRect(Tank tank) {
        return new CollisionRect(
                (int) tank.getBody().getPosition().x - 40,
                (int) tank.getBody().getPosition().y - 40,
                100, 120
        );
    }

    private boolean handleBulletCollision(Bullet bullet, Tank targetTank, CollisionRect targetTankRect) {
        CollisionRect bulletRect = new CollisionRect(
                (int) bullet.getBody().getPosition().x,
                (int) bullet.getBody().getPosition().y,
                40, 40
        );

        if (bulletRect.collidesWith(targetTankRect)) {
            targetTank.getBody().applyLinearImpulse(bullet.getSpeed(), 0,
                    targetTank.getBody().getPosition().x, targetTank.getBody().getPosition().y, true);
            targetTank.getBody().setLinearDamping(1.0f);
            targetTank.reduceHealth((int) bullet.getDamage());

            System.out.println(targetTank == enemyTank
                    ? "Enemy tank health: " + targetTank.getCurrentHealth()
                    : "Player tank health: " + targetTank.getCurrentHealth());

            if (targetTank.getCurrentHealth() <= 0) {
                handleTankDestruction(targetTank);
            }

            world.destroyBody(bullet.getBody());
            return true;
        }
        return false;
    }

    private void handleTankDestruction(Tank tank) {
        Batch batch = new SpriteBatch();
        batch.begin();
        drawGameOverScreen(batch, tank == playerTank ? Player2WinsText : Player1WinsText);
        batch.end();

        world.destroyBody(tank.getBody());
        Gdx.app.exit();
    }

    private void drawGameOverScreen(Batch batch, Texture winnerText) {
        batch.draw(pauseMenuOuterRectangle, 345, 39);
        batch.draw(pauseMenuBackground, 348, 42);
        batch.draw(pauseMenuGeneralImage, 398, 267);
        batch.draw(pauseMenuUpperRectangle, 384, 434);
        batch.draw(GameOverText, 419, 460);
        batch.draw(winnerText, 391, 165);
        batch.draw(WinText, 395, 96);
    }

    private void cleanupOffScreenBullets() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (isOffScreen(bullet)) {
                bulletIterator.remove();
                world.destroyBody(bullet.getBody());
            }
        }

        Iterator<Bullet> enemyBulletIterator = enemyBullets.iterator();
        while (enemyBulletIterator.hasNext()) {
            Bullet bullet = enemyBulletIterator.next();
            if (isOffScreen(bullet)) {
                enemyBulletIterator.remove();
                world.destroyBody(bullet.getBody());
            }
        }
    }

    private boolean isOffScreen(Bullet bullet) {
        return bullet.getBody().getPosition().x > 960 || bullet.getBody().getPosition().x < 0
                || bullet.getBody().getPosition().y > 540 || bullet.getBody().getPosition().y < 205;
    }

    private void handleGameOver(Batch batch, String winnerMessage) {
        batch.begin();
        batch.draw(pauseMenuOuterRectangle, 345, 39);
        batch.draw(pauseMenuBackground, 348, 42);
        batch.draw(pauseMenuGeneralImage, 398, 267);
        batch.draw(pauseMenuUpperRectangle, 384, 434);
        batch.draw(GameOverText, 419, 460);
        batch.draw(winnerMessage.equals("Player 1 Wins!") ? Player1WinsText : Player2WinsText, 391, 165);
        batch.draw(WinText, 395, 96);
        batch.end();
        pause();
        System.out.println(winnerMessage);
        Gdx.app.exit();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        battleScreenSprite.dispose();
        battleBullet.dispose();
        battleBulletRed.dispose();
        stage.dispose();
        world.dispose();
        debugRenderer.dispose();
    }

    @Override
    public void pause() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height
    ) {
    }
}

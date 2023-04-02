package pl.cohop;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.almasb.fxgl.dsl.FXGL.*;
import static pl.cohop.EntityType.*;

public class CoHop extends GameApplication {
    private Entity bluePlayer;
    private Entity purplePlayer;
    private Entity greenPlayer;

    private LazyValue<LevelEndScene> levelEndScene = new LazyValue<>(() -> new LevelEndScene());

    private int carrots = 0;
    private int rabbits_in_holes = 0;
    private int level = -1;
    private boolean endLevel = false;
    private Label carrotsCount = null;
    HBox carrotsBox = null;

    Entity start = null;
    Entity background = null;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(25 * 64);
        settings.setHeight(15 * 64);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public LoadingScene newLoadingScene() {
                return new MainLoadingScene();
            }
        });
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
        settings.setTitle("CoHoperation");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void onPreInit() {
        getSettings().setGlobalMusicVolume(0.10);
        loopBGM("cohoperation.wav");
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new CoHopFactory());
        setLevelFromMap("start.tmx");

        start = spawn("start");
    }

    @Override
    protected void initInput() {


        getInput().addAction(new UserAction("ENTER") {
            @Override
            protected void onAction() {
                if(level == -1)
                    loadNextLevel();
            }
        }, KeyCode.ENTER);

        // BLUE MOVEMENT
        getInput().addAction(new UserAction("BlueLeft") {
            @Override
            protected void onAction() {
                bluePlayer.getComponent(RabbitComponent.class).left();
            }

            @Override
            protected void onActionEnd() {
                bluePlayer.getComponent(RabbitComponent.class).stop();
            }
        }, KeyCode.A, VirtualButton.LEFT);

        getInput().addAction(new UserAction("BlueRight") {
            @Override
            protected void onAction() {
                bluePlayer.getComponent(RabbitComponent.class).right();
            }

            @Override
            protected void onActionEnd() {
                bluePlayer.getComponent(RabbitComponent.class).stop();
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("BlueJump") {
            @Override
            protected void onActionBegin() {
                bluePlayer.getComponent(RabbitComponent.class).jump();
            }
        }, KeyCode.W, VirtualButton.A);

        // PURPLE MOVEMENT
        getInput().addAction(new UserAction("PurpleLeft") {
            @Override
            protected void onAction() {
                purplePlayer.getComponent(RabbitComponent.class).left();
            }

            @Override
            protected void onActionEnd() {
                purplePlayer.getComponent(RabbitComponent.class).stop();
            }
        }, KeyCode.LEFT, VirtualButton.LEFT);

        getInput().addAction(new UserAction("PurpleRight") {
            @Override
            protected void onAction() {
                purplePlayer.getComponent(RabbitComponent.class).right();
            }

            @Override
            protected void onActionEnd() {
                purplePlayer.getComponent(RabbitComponent.class).stop();
            }
        }, KeyCode.RIGHT, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("PurpleJump") {
            @Override
            protected void onActionBegin() {
                purplePlayer.getComponent(RabbitComponent.class).jump();
            }
        }, KeyCode.UP, VirtualButton.A);

        // GREEN MOVEMENT
        getInput().addAction(new UserAction("GreenLeft") {
            @Override
            protected void onAction() {
                greenPlayer.getComponent(RabbitComponent.class).left();
            }

            @Override
            protected void onActionEnd() {
                greenPlayer.getComponent(RabbitComponent.class).stop();
            }
        }, KeyCode.NUMPAD4, VirtualButton.LEFT);

        getInput().addAction(new UserAction("greenRight") {
            @Override
            protected void onAction() {
                greenPlayer.getComponent(RabbitComponent.class).right();
            }

            @Override
            protected void onActionEnd() {
                greenPlayer.getComponent(RabbitComponent.class).stop();
            }
        }, KeyCode.NUMPAD6, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("GreenJump") {
            @Override
            protected void onActionBegin() {
                greenPlayer.getComponent(RabbitComponent.class).jump();
            }
        }, KeyCode.NUMPAD8, VirtualButton.A);
    }

    @Override
    protected void initPhysics() {

        CollisionHandler spikesCol = new CollisionHandler(PLAYER_BLUE, SPIKES) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity player, Entity spikes) {
                List<Entity> levers = spikes.getWorld().getEntitiesFiltered(entity -> entity.getType() == LEVER);
                List<Entity> trapdoors = spikes.getWorld().getEntitiesFiltered(entity -> entity.getType() == TRAPDOOR);
                for(Entity entity : levers) {
                    entity.setOpacity(0);
                    entity.setProperty("entered", 0);
                }

                for(Entity entity : trapdoors) {
                    entity.removeFromWorld();
                }
                endLevel = true;
                --level;
                inc("level", -1);
                loadNextLevel();
            }
        };

        FXGL.getPhysicsWorld().addCollisionHandler(spikesCol);
        FXGL.getPhysicsWorld().addCollisionHandler(spikesCol.copyFor(PLAYER_PURPLE, SPIKES));
        FXGL.getPhysicsWorld().addCollisionHandler(spikesCol.copyFor(PLAYER_GREEN, SPIKES));

        CollisionHandler holesCol = new CollisionHandler(PLAYER_BLUE, BLUE_HOLE) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity player, Entity hole) {
                ++rabbits_in_holes;
                if(rabbits_in_holes == 3) {
                    set("carrotsGathered", carrots);
//                    System.out.println("all rabbits in holes");
                    levelEndScene.get().onLevelFinish();
                    getGameScene().getViewport().fade(() -> {
                        loadNextLevel();
                    });
                }
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity hole) {
                --rabbits_in_holes;
            }
        };

        FXGL.getPhysicsWorld().addCollisionHandler(holesCol);
        FXGL.getPhysicsWorld().addCollisionHandler(holesCol.copyFor(PLAYER_PURPLE, PURPLE_HOLE));
        FXGL.getPhysicsWorld().addCollisionHandler(holesCol.copyFor(PLAYER_GREEN, GREEN_HOLE));

        BiConsumer<Entity, Entity> playerCarrotColHandler = (player, carrot) -> {
            carrot.removeFromWorld();

            inc("carrotsGathered", 1);
            ++carrots;

            if(carrotsCount != null)
                carrotsCount.setText(Integer.toString(carrots));
        };

        onCollision(PLAYER_BLUE, CARROT, playerCarrotColHandler);
        onCollision(PLAYER_GREEN, CARROT, playerCarrotColHandler);
        onCollision(PLAYER_PURPLE, CARROT, playerCarrotColHandler);

        CollisionHandler leverHandler = new CollisionHandler(PLAYER_GREEN, LEVER) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity player, Entity lever) {
                if(endLevel) {
                    endLevel = false;
                }
                Integer entered = lever.getInt("entered");

                if(entered == 0) {
                    lever.setScaleX(-1);
                    Integer trapdoorGroup = lever.getInt("trapdoor_group");

                    List<Entity> toAnimate = lever.getWorld().getEntitiesFiltered(entity -> {
                        if(entity.getType() != TRAPDOOR)
                            return false;

                        if(entity.getInt("trapdoor_group") != trapdoorGroup)
                            return false;

                        return true;
                    });

                    for(Entity entity : toAnimate) {
//                        entity.setScaleX(0);
                        entity.setVisible(false);
                        entity.setType(HIDDEN_TRAPDOORS);
                        entity.getComponent(PhysicsComponent.class);
                    }
                }
                ++entered;
                lever.setProperty("entered", entered);
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity lever) {
                if(endLevel) {
                    return;
                }
                Integer entered = lever.getInt("entered");

                --entered;
                if(entered == 0) {
                    lever.setScaleX(1);

                    Integer trapdoorGroup = lever.getInt("trapdoor_group");

                    List<Entity> toAnimate = lever.getWorld().getEntitiesFiltered(entity -> {
                        if(entity.getType() != HIDDEN_TRAPDOORS)
                            return false;

                        if(entity.getInt("trapdoor_group") != trapdoorGroup)
                            return false;

                        return true;
                    });

                    for(Entity entity : toAnimate) {
//                        entity.setScaleX(1);
                        entity.setVisible(true);
                        entity.setType(TRAPDOOR);
                        entity.getComponent(PhysicsComponent.class);
                    }
                }

                lever.setProperty("entered", entered);
            }
        };

        getPhysicsWorld().addCollisionHandler(leverHandler);
        getPhysicsWorld().addCollisionHandler(leverHandler.copyFor(PLAYER_BLUE, LEVER));
        getPhysicsWorld().addCollisionHandler(leverHandler.copyFor(PLAYER_PURPLE, LEVER));
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("carrotsGathered", 0);
        vars.put("level", -1);
    }

    void loadNextLevel() {
        if(level != -1){
            bluePlayer.setVisible(false);
            bluePlayer.setType(HIDDEN);
            greenPlayer.setVisible(false);
            greenPlayer.setType(HIDDEN);
            purplePlayer.setVisible(false);
            purplePlayer.setType(HIDDEN);
        }

        carrots = 0;
        set("carrotsGathered", 0);
        ++level;
        inc("level", +1);

        Level lvl;
        try {
            lvl = setLevelFromMap("level" + level + ".tmx");
        }
        catch(Exception e) {
            showMessage("Thanks for playing our game!\nExitting back to main menu...");

            set("carrotsGathered", 0);
            set("level", -1);
            level = -1;

            setLevelFromMap("start.tmx");
            if(background != null) {
                FXGL.getGameWorld().removeEntity(background);
                background = null;
            }
            start = spawn("start");
            FXGL.getSceneService().getOverlayRoot().getChildren().remove(carrotsBox);
            return;
        }

        if(start != null) {
            FXGL.getGameWorld().removeEntity(start);
            start = null;
        }

        background = spawn("background");

        int carrot1 = lvl.getProperties().getInt("carrot1");
        int carrot2 = lvl.getProperties().getInt("carrot2");
        int carrot3 = lvl.getProperties().getInt("carrot3");

        var carrotsGatheredData = new LevelEndScene.levelCarrotData(carrot1, carrot2, carrot3);
        set("carrotsGatheredData", carrotsGatheredData);

        bluePlayer = lvl.getEntities().stream().filter(
                entity -> entity.getType() == PLAYER_BLUE
        ).findAny().orElse(null);

        greenPlayer = lvl.getEntities().stream().filter(
                entity -> entity.getType() == PLAYER_GREEN
        ).findAny().orElse(null);

        purplePlayer = lvl.getEntities().stream().filter(
                entity -> entity.getType() == PLAYER_PURPLE
        ).findAny().orElse(null);

        if(level == 0) {
            Entity e = lvl.getEntities().stream().filter(
                    entity -> entity.getType() == FOCUS_KEY_1
            ).findFirst().orElse(null);

            FXGL.animationBuilder()

                    // interpolator drives the animation rate
                    .interpolator(Interpolators.SMOOTH.EASE_IN_OUT())
                    .duration(Duration.seconds(2))
                    .repeatInfinitely()
                    .scale(e)
                    .origin(new Point2D(35, 35))
                    .from(new Point2D(0.75, 0.75))
                    .to(new Point2D(0.85, 0.85))
                    .buildAndPlay();

            e = lvl.getEntities().stream().filter(
                    entity -> entity.getType() == FOCUS_KEY_2
            ).findFirst().orElse(null);

            FXGL.animationBuilder()

                    // interpolator drives the animation rate
                    .interpolator(Interpolators.SMOOTH.EASE_IN_OUT())
                    .duration(Duration.seconds(2))
                    .repeatInfinitely()
                    .scale(e)
                    .origin(new Point2D(35, 35))
                    .from(new Point2D(0.75, 0.75))
                    .to(new Point2D(0.85, 0.85))
                    .buildAndPlay();

            e = lvl.getEntities().stream().filter(
                    entity -> entity.getType() == FOCUS_KEY_3
            ).findFirst().orElse(null);

            FXGL.animationBuilder()

                    // interpolator drives the animation rate
                    .interpolator(Interpolators.SMOOTH.EASE_IN_OUT())
                    .duration(Duration.seconds(2))
                    .repeatInfinitely()
                    .scale(e)
                    .origin(new Point2D(35, 35))
                    .from(new Point2D(0.75, 0.75))
                    .to(new Point2D(0.85, 0.85))
                    .buildAndPlay();

            Texture carrotIcon = FXGL.getAssetLoader().loadTexture("carrot.png");
            carrotsCount = new Label(Integer.valueOf(FXGL.geti("carrotsGathered")).toString());

            carrotsCount.setFont(new Font(48));
            carrotsCount.setLineSpacing(0.1);
            carrotsCount.setPadding(new Insets(0));

            carrotsBox = new HBox(carrotIcon, carrotsCount);
            carrotsBox.setSpacing(24);
            carrotsBox.setPadding(new Insets(16));

            FXGL.getSceneService().getOverlayRoot().getChildren().add(carrotsBox);
        }

        carrotsCount.setText(Integer.toString(geti("carrotsGathered")));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

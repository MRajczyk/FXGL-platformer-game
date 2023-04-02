package pl.cohop;

import com.almasb.fxgl.dsl.views.ScrollingBackgroundView;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;


import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static pl.cohop.EntityType.*;

public class CoHopFactory implements EntityFactory {

    @Spawns("background")
    public Entity background(SpawnData data) {
        return entityBuilder()
                .view(new ScrollingBackgroundView(texture("background.png").getImage(), getAppWidth(), getAppHeight()))
                .zIndex(-2)
                //.with(new IrremovableComponent())
                .with(new CollidableComponent(true))
                .buildScreenBounds(0);
    }

    @Spawns("start")
    public Entity start(SpawnData data) {
        return entityBuilder()
                .view(new ScrollingBackgroundView(texture("start.png").getImage(), getAppWidth(), getAppHeight()))
                //.with(new IrremovableComponent())
                .zIndex(-1)
                .buildScreenBounds(0);
    }

    @Spawns("grass")
    public Entity grass(SpawnData spawnData) {

        HitBox hitBox;
        Polyline polyline = (Polyline) spawnData.getData().get("polyline");

        if(polyline != null) {
            // TODO: kolizja z polygona, poprawa local origin
            BoundingShape shape = BoundingShape.polygonFromDoubles(polyline.getPoints());
            hitBox = new HitBox(new Point2D(0, 0), shape);
        }
        else {
            BoundingShape shape = BoundingShape.box(spawnData.<Integer>get("width"), spawnData.<Integer>get("height"));
            hitBox = new HitBox(shape);
        }

        return entityBuilder(spawnData)
                .type(GRASS)
                .bbox(hitBox)
                .with(new PhysicsComponent())
                .build();
    }

    @Spawns("carrot")
    public Entity carrot(SpawnData spawnData) {
        return entityBuilder(spawnData)
                .type(CARROT)
                .bbox(new HitBox(BoundingShape.box(spawnData.<Integer>get("width"), spawnData.<Integer>get("height"))))
                .scale(0.66, 0.66)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("keyboard")
    public Entity keyboard(SpawnData spawnData) {

        return entityBuilder(spawnData)
                .type(KEYBOARD)
                .bbox(new HitBox(BoundingShape.box(spawnData.<Integer>get("width"), spawnData.<Integer>get("height"))))
                .scale(0.75, 0.75)
                .opacity(0.80)
                .build();
    }

    @Spawns("focus_key_1")
    public Entity focus_key_1(SpawnData spawnData) {

        return entityBuilder(spawnData)
                .type(FOCUS_KEY_1)
                .bbox(new HitBox(BoundingShape.box(spawnData.<Integer>get("width"), spawnData.<Integer>get("height"))))
                .scale(0.75, 0.75)
                .build();
    }

    @Spawns("focus_key_2")
    public Entity focus_key_2(SpawnData spawnData) {

        return entityBuilder(spawnData)
                .type(FOCUS_KEY_2)
                .bbox(new HitBox(BoundingShape.box(spawnData.<Integer>get("width"), spawnData.<Integer>get("height"))))
                .scale(0.75, 0.75)
                .build();
    }

    @Spawns("focus_key_3")
    public Entity focus_key_3(SpawnData spawnData) {

        return entityBuilder(spawnData)
                .type(FOCUS_KEY_3)
                .bbox(new HitBox(BoundingShape.box(spawnData.<Integer>get("width"), spawnData.<Integer>get("height"))))
                .scale(0.75, 0.75)
                .build();
    }

    @Spawns("trapdoor")
    public Entity trapdoor(SpawnData spawnData) {
        double h = spawnData.<Integer>get("height").doubleValue();
        BoundingShape shape = BoundingShape.box(
                spawnData.<Integer>get("width"),
                h / 8
        );

        Entity trapdoor = entityBuilder(spawnData)
                .type(TRAPDOOR)
                .bbox(new HitBox(new Point2D(0, h * (7.0/8)), shape))
                .with(new PhysicsComponent())
                .collidable()
                .build();

        trapdoor.setProperty("trapdoor_group", spawnData.<Integer>get("trapdoor_group"));
        return trapdoor;
    }

    @Spawns("hole")
    public Entity hole(SpawnData spawnData) {
        EntityType type = EntityType.valueOf(spawnData.get("entity_type"));

        double h = spawnData.<Integer>get("height").doubleValue();
        BoundingShape shape = BoundingShape.circle(
                32
        );

        return entityBuilder(spawnData)
                .type(type)
                .bbox(new HitBox(new Point2D(0, 7*h/8), shape))
                .with(new PhysicsComponent())
                .collidable()
                .build();
    }

    @Spawns("spikes")
    public Entity spikes(SpawnData spawnData) {
        EntityType type = EntityType.valueOf(spawnData.get("entity_type"));

        double h = spawnData.<Integer>get("height").doubleValue();
        BoundingShape shape = BoundingShape.box(
                spawnData.<Integer>get("width"),
                h / 8
        );

        return entityBuilder(spawnData)
                .type(type)
                .bbox(new HitBox(new Point2D(0, 7*h/8), shape))
                .with(new PhysicsComponent())
                .collidable()
                .build();
    }

    @Spawns("lever")
    public Entity lever(SpawnData spawnData) {
        double h = spawnData.<Integer>get("height").doubleValue();
        BoundingShape shape = BoundingShape.box(
                spawnData.<Integer>get("width"),
                spawnData.<Integer>get("height")
        );

        Entity lever = entityBuilder(spawnData)
                .type(LEVER)
                .bbox(new HitBox(shape))
                .collidable()
//                .with(new PhysicsComponent())
                .build();
        lever.setProperty("trapdoor_group", spawnData.<Integer>get("trapdoor_group"));
        lever.setProperty("entered", Integer.valueOf(0));
        return lever;
    }

    @Spawns("player")
    public Entity player(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(8, 0), BoundingShape.box(48, 60)));
        physics.setFixtureDef(new FixtureDef().friction(0.0f));

        EntityType type = EntityType.valueOf(data.get("entity_type"));

        CollidableComponent collidableComponent = new CollidableComponent(true);
        if(type == PLAYER_BLUE) {
            collidableComponent.addIgnoredType(PURPLE_HOLE);
            collidableComponent.addIgnoredType(GREEN_HOLE);
            collidableComponent.addIgnoredType(PLAYER_PURPLE);
            collidableComponent.addIgnoredType(PLAYER_GREEN);
            collidableComponent.addIgnoredType(HIDDEN);
            collidableComponent.addIgnoredType(HIDDEN_TRAPDOORS);
        }
        else if(type == PLAYER_GREEN) {
            collidableComponent.addIgnoredType(PURPLE_HOLE);
            collidableComponent.addIgnoredType(BLUE_HOLE);
            collidableComponent.addIgnoredType(PLAYER_BLUE);
            collidableComponent.addIgnoredType(PLAYER_PURPLE);
            collidableComponent.addIgnoredType(HIDDEN);
            collidableComponent.addIgnoredType(HIDDEN_TRAPDOORS);
        }
        else if(type == PLAYER_PURPLE) {
            collidableComponent.addIgnoredType(BLUE_HOLE);
            collidableComponent.addIgnoredType(GREEN_HOLE);
            collidableComponent.addIgnoredType(PLAYER_BLUE);
            collidableComponent.addIgnoredType(PLAYER_GREEN);
            collidableComponent.addIgnoredType(HIDDEN);
            collidableComponent.addIgnoredType(HIDDEN_TRAPDOORS);
        }

        return entityBuilder(data)
                .type(type)
                .bbox(new HitBox(new Point2D(4, 0), BoundingShape.box(56, 60)))
                .with(physics)
                .with(collidableComponent)
                .with(new IrremovableComponent())
                .with(new RabbitComponent(type))
                .build();
    }
}

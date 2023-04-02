package pl.cohop;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.image;

public class RabbitComponent extends Component {
    private static final int TILE_SIZE = 64;
    public static final int FRAMES_PER_ROW = 25;

    private PhysicsComponent physics;
    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk, animJump;
    private boolean canJump = true;

    public RabbitComponent(EntityType type) {

        Image image = image("spritesheet_multicolor.png");

        if(type == EntityType.PLAYER_BLUE) {
            animIdle = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(1), 100, 100);
            animWalk = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(0.66), 125, 132);
            animJump = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(0.66), 127, 127);
        }
        else if(type == EntityType.PLAYER_GREEN) {
            animIdle = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(1), 209, 209);
            animWalk = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(0.66), 234, 241);
            animJump = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(0.66), 236, 236);
        }
        else if(type == EntityType.PLAYER_PURPLE) {
            animIdle = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(1), 200, 200);
            animWalk = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(0.66), 225, 232);
            animJump = new AnimationChannel(image, FRAMES_PER_ROW, TILE_SIZE, TILE_SIZE, Duration.seconds(0.66), 227, 227);
        }
        else {
            throw new RuntimeException("invalid player type");
        }

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(32, 32));
        entity.getViewComponent().addChild(texture);

        physics.onGroundProperty().addListener((obs, wasOnGround, isOnGround) -> {
            canJump = isOnGround;
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (physics.isMovingY()) {
            if (texture.getAnimationChannel() != animJump) {
                texture.loopAnimationChannel(animJump);
            }
        } else if (physics.isMovingX()) {
            if (texture.getAnimationChannel() != animWalk) {
                texture.loopAnimationChannel(animWalk);
            }
        } else {
            if (texture.getAnimationChannel() != animIdle) {
                texture.loopAnimationChannel(animIdle);
            }
        }
    }

    public void left() {
        getEntity().setScaleX(-1);
        physics.setVelocityX(-170);
    }

    public void right() {
        getEntity().setScaleX(1);
        physics.setVelocityX(170);
    }

    public void stop() {
        physics.setVelocityX(0);
    }

    public void jump() {
        if(!canJump)
            return;

        physics.setVelocityY(-380);
        canJump = false;
    }
}

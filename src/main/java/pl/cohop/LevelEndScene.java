package pl.cohop;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.scene.SubScene;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.FontFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class LevelEndScene extends SubScene {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;

    private Text textUserCarrots = getUIFactoryService().newText("", Color.WHITE, 24.0);
    private HBox gradeBox = new HBox();

    private FontFactory levelFont = getAssetLoader().loadFont(getSettings().getFontMono());
    private BooleanProperty isAnimationDone = new SimpleBooleanProperty(false);

    public LevelEndScene() {
        var bg = new Rectangle(WIDTH, HEIGHT, Color.color(0, 0, 0, 0.85));
        bg.setStroke(Color.BLUE);
        bg.setStrokeWidth(1.75);
        bg.setEffect(new DropShadow(28, Color.color(0,0,0, 0.9)));

        VBox.setVgrow(gradeBox, Priority.ALWAYS);

        var textContinue = getUIFactoryService().newText("Press ENTER to continue", Color.WHITE, 11.0);
        textContinue.visibleProperty().bind(isAnimationDone);

        animationBuilder(this)
                .repeatInfinitely()
                .autoReverse(true)
                .scale(textContinue)
                .from(new Point2D(1, 1))
                .to(new Point2D(1.25, 1.25))
                .buildAndPlay();

        var vbox = new VBox(15, textUserCarrots, gradeBox, textContinue);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        var root = new StackPane(
                bg, vbox
        );

        root.setTranslateX(1600 / 2 - WIDTH / 2);
        root.setTranslateY(800 / 2 - HEIGHT / 2);

        var textLevel = new Text();
        textLevel.textProperty().bind(getip("level").asString("Level %d"));
        textLevel.setFont(levelFont.newFont(72));
        textLevel.setRotate(-20);

        textLevel.setFill(Color.ORANGE);
        textLevel.setStroke(Color.BLACK);
        textLevel.setStrokeWidth(1);

        textLevel.setTranslateX(root.getTranslateX() - textLevel.getLayoutBounds().getWidth() / 3);
        textLevel.setTranslateY(root.getTranslateY() + 25);

        getContentRoot().getChildren().addAll(root, textLevel);

        getInput().addAction(new UserAction("Close Level End Screen") {
            @Override
            protected void onActionBegin() {
                if (!isAnimationDone.getValue())
                    return;

                getSceneService().popSubScene();
            }
        }, KeyCode.ENTER);
    }

    public void onLevelFinish() {
        isAnimationDone.setValue(false);

        int userCarrotsGathered = geti("carrotsGathered");

        levelCarrotData carrotsGatheredData = geto("carrotsGatheredData");

        textUserCarrots.setText(String.format("Your score is: %d!", userCarrotsGathered));

        gradeBox.getChildren().setAll(
                new Grade(carrotsGatheredData.star1, userCarrotsGathered),
                new Grade(carrotsGatheredData.star2, userCarrotsGathered),
                new Grade(carrotsGatheredData.star3, userCarrotsGathered)
        );

        for (int i = 0; i < gradeBox.getChildren().size(); i++) {
            var builder = animationBuilder(this).delay(Duration.seconds(i * 0.75))
                    .duration(Duration.seconds(0.75))
                    .interpolator(Interpolators.ELASTIC.EASE_OUT());

            // if last star animation
            if (i == gradeBox.getChildren().size() - 1) {
                builder = builder.onFinished(() -> isAnimationDone.setValue(true));
            }

            builder.translate(gradeBox.getChildren().get(i))
                    .from(new Point2D(0, -500))
                    .to(new Point2D(0, 0))
                    .buildAndPlay();
        }

        getSceneService().pushSubScene(this);
    }

    private static class Grade extends VBox {

        private static final Texture STAR_EMPTY = texture("star_empty.png", 65, 72).darker();
        private static final Texture STAR_FULL = texture("star_full.png", 65, 72);

        public Grade(int gradeCarrot, int userCarrots) {
            super(15);

            HBox.setHgrow(this, Priority.ALWAYS);

            setAlignment(Pos.CENTER);

            getChildren().add((userCarrots >= gradeCarrot) ? STAR_FULL.copy() : STAR_EMPTY.copy());

            getChildren().add(getUIFactoryService().newText(String.format("%d", gradeCarrot), Color.WHITE, 16.0));
        }
    }

    public static class levelCarrotData {

        private final int star1;
        private final int star2;
        private final int star3;

        /**
         * @param star1 in seconds
         * @param star2 in seconds
         * @param star3 in seconds
         */
        public levelCarrotData(int star1, int star2, int star3) {
            this.star1 = star1;
            this.star2 = star2;
            this.star3 = star3;
        }
    }
}

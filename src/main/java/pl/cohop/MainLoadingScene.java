package pl.cohop;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MainLoadingScene extends LoadingScene {

    public MainLoadingScene() {
        var bg = new Rectangle(getAppWidth(), getAppHeight(), Color.AZURE);

        getContentRoot().getChildren().setAll(bg);
    }
}

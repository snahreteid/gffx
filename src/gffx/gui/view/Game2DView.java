package gffx.gui.view;

import gffx.game.Game2D;
import gffx.game.entity.Player;
import gffx.game.ressource.Texture;
import gffx.gui.comp.Field2DPane;
import gffx.gui.view.OptionsDialog;
import gffx.util.Locale;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Game2DView extends Stage {
    private Game2D game;
    private BorderPane mainPane;
    private Field2DPane gamePane;
    private OptionsDialog gameOverDialog;
    private Thread aiThread;

    public Game2DView(Stage parent, Game2D game) {
        this.game = game;
        aiThread = new Thread();

        if(parent != null) {
            initOwner(parent);
            initModality(Modality.WINDOW_MODAL);
            setTitle(parent.getTitle() + " - " + game.getTitle());
        } else
            setTitle(game.getTitle());

        setOnShowing(e -> initDialog());
        setOnCloseRequest(e -> {
            game.reset();
            mainPane.setTop(null);
            if(parent != null && !parent.isShowing())
                parent.show();
        });
        
        setMinWidth(Screen.getPrimary().getVisualBounds().getWidth()/2);
        setMinHeight(Screen.getPrimary().getVisualBounds().getHeight()/2);
        setWidth(getMinWidth());
        setHeight(getMinHeight());

        centerOnScreen();
        initScene();
    }

    private void initScene() {
        gamePane = new Field2DPane(game.getField());
        mainPane = new BorderPane(gamePane);

        // EXPERIMENTAL scroll test
        gamePane.setOnScroll(e -> {
            if(e.getTouchCount() == 0) { // Mousewheel- / Trackpad-Zoom
                double y_delta = e.getDeltaY();

                if(y_delta < 0) {
                    //System.out.println("scroll down " + y_delta);

                    if(gamePane.getScaleX() < 1) {
                        gamePane.setScaleX(gamePane.getScaleX()+0.025f);
                        gamePane.setScaleY(gamePane.getScaleY()+0.025f);
                    }
                } else {
                    //System.out.println("scroll up " + y_delta);

                    if(gamePane.getScaleX() > 0.5) {
                        gamePane.setScaleX(gamePane.getScaleX()-0.025f);
                        gamePane.setScaleY(gamePane.getScaleY()-0.025f);
                    }
                }
            }
        });
        //////////

        gamePane.getHConstraints().setPercentWidth(100);
        gamePane.getVConstraints().setPercentHeight(100);
        gamePane.getCells().forEach(cell -> {
            cell.setTexture(new Texture("res/ttt/image/texture/stone_bricks.png")); // TODO game.getRessourcePath or something like that
            cell.setOnMouseClicked(e -> {
                System.out.println("cell " + gamePane.xPosOf(cell) + ", " + gamePane.yPosOf(cell) + " clicked");
                
                if(game.isRunning() && !aiThread.isAlive()) {
                    game.setCursor(gamePane.xPosOf(cell), gamePane.yPosOf(cell));                        
                    playTurn();
                }

                if(game.isRunning() && !aiThread.isAlive()) {
                    (aiThread = new Thread(() -> {
                        game.aiMove(() -> Platform.runLater(() -> playTurn()));
                    }) {{ setDaemon(true); }}).start();
                }
            });
        });

        // EXPERIMENTAL dark background test
        StackPane mainStack = new StackPane();
        mainStack.getChildren().addAll(new Rectangle() {{
            widthProperty().bind(mainStack.widthProperty());
            heightProperty().bind(mainStack.heightProperty());
            setFill(Color.BLACK);
        }}, mainPane);
        //////////////

        // in case AI goes first (TODO proper game logic thread)
        game.aiMove(() -> playTurn());

        //setScene(new Scene(mainPane));
        setScene(new Scene(mainStack));
    }

    private void initDialog() {
        gameOverDialog = new OptionsDialog(this, Locale.get("game_over"));
        
        gameOverDialog.setOnShowing(e -> {
            gameOverDialog.setWidth(mainPane.getWidth()/2);
            gameOverDialog.setHeight(mainPane.getHeight()/2);
            gameOverDialog.centerOnScreen(); // not really what I wanted but it's actually quite user friendly
        });

        gameOverDialog.addOptions(
            new Button(Locale.get("new_game")) {{
                setOnAction(e -> {
                    game.reset();
                    mainPane.setTop(null);
                    gameOverDialog.hide();
                    initDialog();
                });
            }},
            new Button(Locale.get("quit")) {{
                setOnAction(e -> Game2DView.this.fireEvent(new WindowEvent(gameOverDialog, WindowEvent.WINDOW_CLOSE_REQUEST)));
            }},
            new Button(Locale.get("hide")) {{
                setOnAction(e -> {
                    if(gameOverDialog.isShowing()) {
                        setText(Locale.get("show"));
                        mainPane.setTop(new HBox() {{
                            getChildren().addAll(gameOverDialog.getOptions());
                        }});
                        gameOverDialog.hide();
                    } else {
                        Node content = gameOverDialog.getContent();
                        initDialog();
                        mainPane.setTop(null);
                        gameOverDialog.setContent(content);
                        gameOverDialog.show();
                    }
                });
            }}
        );
    }

    private void playTurn() {
        Player p = game.turnCondition().check();

        if(!game.isRunning()) {
            if(game.winCondition().check(p))
                gameOverDialog.setContent(Locale.get("player") + " " + (game.getPlayers().indexOf(p)+1) + " " + Locale.get("wins"));
            else
                gameOverDialog.setContent(Locale.get("no_winner"));

            gameOverDialog.show();
        }
    }
}
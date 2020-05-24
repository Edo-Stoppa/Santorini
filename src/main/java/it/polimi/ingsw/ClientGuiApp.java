package it.polimi.ingsw;



import it.polimi.ingsw.Client.ClientGUI;
import it.polimi.ingsw.Client.EventHandler;
import it.polimi.ingsw.Client.GraphicElements.SceneBuilder;
import it.polimi.ingsw.Message.ServerMessage.PickGodMessage;
import it.polimi.ingsw.Message.ServerMessage.ServerMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.Serializable;


public class ClientGuiApp extends Application implements EventHandler, Serializable {
    private static final long serialVersionUID = 1L;
    private static ClientGUI  client;
    private static Stage primaryStage;
    private int phase=0;
    private static SceneBuilder sceneBuilder;

    public static ClientGUI getClient() {
        return client;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void init() throws Exception {
        client=new ClientGUI("127.0.0.1", 12345,this);
        client.run();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //fistLayout
        this.primaryStage=primaryStage;
        sceneBuilder=new SceneBuilder();
        VBox layout1= new VBox(50);
        Label welcome= new Label("welcome to santorini\n 2 or 3 player mode?");
        HBox Buttons= new HBox(150);
        Button twoPlayer= new Button("2 player");
        Button treePlayer=new Button("3 player");
        twoPlayer.setOnAction(e->{
            client.asyncWriteToSocketGUI("2");
            Scene scene=new Scene(sceneBuilder.ChooseName(),800,710);
            this.primaryStage.setScene(scene);
        });
        treePlayer.setOnAction(e->{
            client.asyncWriteToSocketGUI("3");
            Scene scene=new Scene(sceneBuilder.ChooseName(),800,710);
            primaryStage.setScene(scene);
        });
        Buttons.getChildren().addAll(twoPlayer,treePlayer);
        Buttons.setAlignment(Pos.CENTER);
        layout1.setAlignment(Pos.CENTER);
        layout1.getChildren().addAll(welcome,Buttons);
        Scene scene1= new Scene(layout1,800,710);
        primaryStage.setScene(scene1);
        primaryStage.show();
    }



    @Override
    public void update(ServerMessage message) {
        Platform.runLater(()->{
            message.buildScene();
        });

    }

}

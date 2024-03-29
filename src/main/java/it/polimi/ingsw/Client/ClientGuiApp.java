package it.polimi.ingsw.Client;


import it.polimi.ingsw.Client.GraphicElements.SceneBuilder;
import it.polimi.ingsw.Message.HelpMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;


/**
 * this class is responsible for creating the threads used by Java FX
 */
public class ClientGuiApp extends Application implements Serializable {
    private static final long serialVersionUID = 1L;
    private static ClientGUI  client;
    private static Stage primaryStage;
    public static int width=800;
    public static int height=700;
    private static String ip="",port="",definition="800x700";


    public static ClientGUI getClient() {
        return client;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ClientGuiApp.primaryStage =primaryStage;

        Scene scene = new Scene(CreateContent(HelpMessage.askIpPort), width, height);
        scene.getStylesheets().add(ClientGuiApp.class.getClassLoader().getResource("backgroundImage.css").toExternalForm());

        primaryStage.setResizable(false);
        primaryStage.setTitle("Santorini");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * method to create the first scene
     * @param message string to display
     * @return root of the constructed scene
     */
    public static Parent CreateContent (String message){
        VBox layout=new VBox(20);
        layout.setBackground(SceneBuilder.getBackground("startBackground"));
        Text textName = new Text(message);
        textName.setFont(Font.font(ClientGuiApp.height*0.02));
        HBox firstLine =new HBox(20);
        HBox secondLine =new HBox(20);
        HBox thirdLine =new HBox(20);
        Text textIp = new Text("Insert ip   ");
        Text textPort = new Text("Insert port");
        Text display =new Text("Choose the resolution for your game");
        textIp.setFont(Font.font(ClientGuiApp.height*0.02));
        textPort.setFont(Font.font(ClientGuiApp.height*0.02));
        display.setFont(Font.font(ClientGuiApp.height*0.02));
        TextField serverIp = new TextField(ip);
        serverIp.setFont(Font.font(ClientGuiApp.height*0.015));
        serverIp.setPrefWidth(ClientGuiApp.width*0.2857);
        TextField portField = new TextField(port);
        portField.setFont(Font.font(ClientGuiApp.height*0.015));
        portField.setPrefWidth(ClientGuiApp.width*0.2857);
        ChoiceBox<String> resolution= new ChoiceBox<>();
        resolution.getItems().add("640x560");
        resolution.getItems().add("800x700");
        resolution.getItems().add("1200x1050");
        resolution.setValue(definition);
        resolution.getSelectionModel().selectedItemProperty().addListener((v,oldValue,newValue)->{
            ip = serverIp.getText();
            port = portField.getText();
            definition = newValue;
            changeResolution(newValue);
        });
        thirdLine.getChildren().addAll(display,resolution);
        firstLine.getChildren().addAll(textIp,serverIp);
        secondLine.getChildren().addAll(textPort,portField);
        Button go= new Button("Start");
        go.setFont(Font.font(ClientGuiApp.height*0.0285));
        go.setOnAction(e->{
            if(serverIp.getText().equals("") && portField.getText().equals("")){
                client = new ClientGUI("127.0.0.1", 54321);
                try {
                    client.run();
                } catch (IOException ioException) {
                    Scene scene=new Scene(CreateContent(HelpMessage.noConnection),width,height);
                    scene.getStylesheets().add(ClientGuiApp.class.getClassLoader().getResource("backgroundImage.css").toExternalForm());
                    primaryStage.setScene(scene);
                }
            }else{
                client = new ClientGUI(serverIp.getText(),Integer.parseInt(portField.getText()));
                try {
                    client.run();
                } catch (IOException ioException) {
                    Scene scene=new Scene(CreateContent(HelpMessage.noConnection),width,height);
                    scene.getStylesheets().add(ClientGuiApp.class.getClassLoader().getResource("backgroundImage.css").toExternalForm());
                    primaryStage.setScene(scene);
                }
            }});
        serverIp.setMaxWidth(150);
        portField.setMaxWidth(150);
        layout.setAlignment(Pos.CENTER);
        firstLine.setAlignment(Pos.CENTER);
        secondLine.setAlignment(Pos.CENTER);
        thirdLine.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(textName,firstLine,secondLine,thirdLine,go);
        return layout;

    }

    /**
     * method to change the resolution for the scene during all the game
     * @param resolution dimension of the scene
     */
    private static void changeResolution(String resolution){
        String[] spitted = resolution.split("x");
        width = Integer.parseInt(spitted[0]);
        height = Integer.parseInt(spitted[1]);
        Scene scene = new Scene(CreateContent(HelpMessage.askIpPort),width,height);
        scene.getStylesheets().add(ClientGuiApp.class.getClassLoader().getResource("backgroundImage.css").toExternalForm());
        primaryStage.setScene(scene);
    }

}

package it.polimi.ingsw.Client;


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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;


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

        Scene scene1 = new Scene(CreateContent("Please enter the server ip and connection port(If you want to play locally, just hit enter)"), width, height);



        primaryStage.setScene(scene1);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });
    }

    public static Parent CreateContent (String message){
        VBox layout=new VBox(20);
        Text textName= new Text(message);
        HBox firstLine=new HBox(20);
        HBox secondLine=new HBox(20);
        HBox thirdLine=new HBox(20);
        Text textIp= new Text("insert ip");
        Text textPort= new Text("insert port");
        Text display=new Text("choose risolution for your game");
        TextField serverIp= new TextField(ip);
        TextField portField= new TextField(port);
        ChoiceBox<String> risolution= new ChoiceBox<>();
        risolution.getItems().add("640x560");
        risolution.getItems().add("800x700");
        risolution.getItems().add("1200x1050");
        risolution.setValue(definition);
        risolution.getSelectionModel().selectedItemProperty().addListener((v,oldValue,newValue)->{
            ip=serverIp.getText();
            port=portField.getText();
            definition=newValue;
            changeRisolution(newValue);
        });
        thirdLine.getChildren().addAll(display,risolution);
        firstLine.getChildren().addAll(textIp,serverIp);
        secondLine.getChildren().addAll(textPort,portField);
        Button go= new Button("enter");
        go.setOnAction(e->{
            if(serverIp.getText().equals("") && portField.getText().equals("")){
                client=new ClientGUI("127.0.0.1", 54321);
                try {
                    client.run();
                } catch (IOException ioException) {
                    Scene scene=new Scene(CreateContent("The connection couldn't be established, please try again!\n"),width,height);
                    primaryStage.setScene(scene);
                }
            }else{
                client= new ClientGUI(serverIp.getText(),Integer.parseInt(portField.getText()));
                try {
                    client.run();
                } catch (IOException ioException) {
                    Scene scene=new Scene(CreateContent("The connection couldn't be established, please try again!\n"),width,height);
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

    private static void changeRisolution(String risolution){
        String[] spitted=risolution.split("x");
        width=Integer.parseInt(spitted[0]);
        height=Integer.parseInt(spitted[1]);
        Scene scene=new Scene(CreateContent("Please enter the server ip and connection port(If you want to play locally, just hit enter)"),width,height);
        primaryStage.setScene(scene);

        System.out.println(risolution);
    }





}

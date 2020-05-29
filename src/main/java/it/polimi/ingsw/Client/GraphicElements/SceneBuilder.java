package it.polimi.ingsw.Client.GraphicElements;

import it.polimi.ingsw.Client.GraphicElements.Board.BoardScene;
import it.polimi.ingsw.Client.ClientGuiApp;
import it.polimi.ingsw.Message.ServerMessage.ChosenGodMessage;
import it.polimi.ingsw.Message.ServerMessage.OrderGameMessage;
import it.polimi.ingsw.Message.ServerMessage.PickGodMessage;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


import java.util.ArrayList;
import java.util.HashMap;

public class SceneBuilder {



    private  static final HashMap<String,Image> imageHashMap= new HashMap<>();

    public SceneBuilder(){
         Image Apollo= new Image("file:Apollo.png");
         Image Artemis= new Image("file:Artemis.png");
         Image Athena= new Image("file:Athena.png");
         Image Atlas= new Image("file:Atlas.png");
         Image Demeter= new Image("file:Demeter.png");
         Image Hephaestus= new Image("file:Hephaestus.png");
         Image Minotaur= new Image("file:Minotaur.png");
         Image Pan=new Image("file:Pan.png");
         Image Prometheus= new Image("file:Prometheus.png");
         imageHashMap.put("Apollo",Apollo);
        imageHashMap.put("Artemis",Artemis);
        imageHashMap.put("Athena",Athena);
        imageHashMap.put("Atlas",Atlas);
        imageHashMap.put("Demeter",Demeter);
        imageHashMap.put("Hephaestus",Hephaestus);
        imageHashMap.put("Minotaur",Minotaur);
        imageHashMap.put("Pan",Pan);
        imageHashMap.put("Prometheus",Prometheus);


    }


    public Parent ChooseName(){
        VBox layout=new VBox(40);
        Label textName= new Label("enter your name");
        TextField name= new TextField();
        Button go= new Button("go");
        go.setOnAction(e->{
            ClientGuiApp.getClient().asyncWriteToSocketGUI(name.getText());
            Scene sceneWait= new Scene(waitScene(),800,710);
            ClientGuiApp.getPrimaryStage().setScene(sceneWait);
        });
        name.setMaxWidth(150);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(textName,name,go);
        return layout;
    }

    public static void PickGod(PickGodMessage message){
        BorderPane layout=new BorderPane();
        HBox firstLine= new HBox();
        HBox secondLine=new HBox();
        HBox thirdLine=new HBox();
        HBox fourLine= new HBox();
        CheckBox apollo=new CheckBox();
        CheckBox artemis=new CheckBox();
        CheckBox athena= new CheckBox();
        CheckBox atlas= new CheckBox();
        CheckBox demeter= new CheckBox();
        CheckBox hephaestus= new CheckBox();
        CheckBox minotaur=new CheckBox();
        CheckBox pan= new CheckBox();
        CheckBox prometheus= new CheckBox();
        ImageView IWApollo= new ImageView(imageHashMap.get("Apollo"));
        setGodImage(IWApollo);
        ImageView IWArtemis= new ImageView(imageHashMap.get("Artemis"));
        setGodImage(IWArtemis);
        ImageView IWAthena= new ImageView(imageHashMap.get("Athena"));
        setGodImage(IWAthena);
        ImageView IWAtlas= new ImageView(imageHashMap.get("Atlas"));
        setGodImage(IWAtlas);
        ImageView IWDemeter = new ImageView(imageHashMap.get("Demeter"));
        setGodImage(IWDemeter);
        ImageView IWHephaestus = new ImageView(imageHashMap.get("Hephaestus"));
        setGodImage(IWHephaestus);
        ImageView IWMinotaur= new ImageView(imageHashMap.get("Minotaur"));
        setGodImage(IWMinotaur);
        ImageView IWPan= new ImageView(imageHashMap.get("Pan"));
        setGodImage(IWPan);
        ImageView IWPrometheus= new ImageView(imageHashMap.get("Prometheus"));
        setGodImage(IWPrometheus);
        Text ApolloDesctripion =new Text(message.GetGod(0).getGodPower());
        ApolloDesctripion.setWrappingWidth(130);
        Text ArtemisDesctripion =new Text(message.GetGod(1).getGodPower());
        ArtemisDesctripion.setWrappingWidth(130);
        Text AthenaDesctripion =new Text(message.GetGod(2).getGodPower());
        AthenaDesctripion.setWrappingWidth(130);
        Text AtlasDesctripion =new Text(message.GetGod(3).getGodPower());
        AtlasDesctripion.setWrappingWidth(130);
        Text DemeterDesctripion =new Text(message.GetGod(4).getGodPower());
        DemeterDesctripion.setWrappingWidth(130);
        Text HephaestusDesctripion =new Text(message.GetGod(5).getGodPower());
        HephaestusDesctripion.setWrappingWidth(130);
        Text MinotaurDesctripion =new Text(message.GetGod(6).getGodPower());
        MinotaurDesctripion.setWrappingWidth(130);
        Text PanDesctripion =new Text(message.GetGod(7).getGodPower());
        PanDesctripion.setWrappingWidth(130);
        Text PrometheusDesctripion =new Text(message.GetGod(8).getGodPower());
        PrometheusDesctripion.setWrappingWidth(130);
        firstLine.getChildren().addAll(apollo,IWApollo,ApolloDesctripion,artemis,IWArtemis,ArtemisDesctripion,athena,IWAthena,AthenaDesctripion);
        secondLine.getChildren().addAll(atlas,IWAtlas,AtlasDesctripion,demeter,IWDemeter,DemeterDesctripion,hephaestus,IWHephaestus,HephaestusDesctripion);
        thirdLine.getChildren().addAll(minotaur,IWMinotaur,MinotaurDesctripion,pan,IWPan,PanDesctripion,prometheus,IWPrometheus,PrometheusDesctripion);
        VBox griglia=new VBox(30);
        Button chosenGods=new Button("enter");
        chosenGods.setOnAction(e->handleOptions(message.getNumPlayer(),apollo,artemis,athena,atlas,demeter,hephaestus,minotaur,pan,prometheus));
        fourLine.getChildren().add(chosenGods);
        fourLine.setAlignment(Pos.CENTER);
        griglia.getChildren().addAll(firstLine,secondLine,thirdLine,fourLine);
        layout.setCenter(griglia);
        Scene scene3= new Scene(layout,850,710);
        ClientGuiApp.getPrimaryStage().setScene(scene3);
    }

    private static void handleOptions(int numPlayer, CheckBox apollo, CheckBox artemis, CheckBox athena, CheckBox atlas, CheckBox demeter, CheckBox hephaestus, CheckBox minotaur, CheckBox pan, CheckBox prometheus) {
        ArrayList<Integer>chosenGod = new ArrayList<>();
    if (apollo.isSelected()){
      chosenGod.add(0);
    }
    if(artemis.isSelected()){
        chosenGod.add(1);
    }
    if(athena.isSelected()){
        chosenGod.add(2);
    }
    if(atlas.isSelected()){
        chosenGod.add(3);
    }
    if(demeter.isSelected()){
        chosenGod.add(4);
    }
    if(hephaestus.isSelected()){
        chosenGod.add(5);
    }
    if(minotaur.isSelected()){
        chosenGod.add(6);
    }
    if(pan.isSelected()){
        chosenGod.add(7);
    }
    if(prometheus.isSelected()){
        chosenGod.add(8);
    }
    if (chosenGod.size()==numPlayer){
        StringBuilder message= new StringBuilder();
        for(int i=0;i<numPlayer;i++){
            message.append(chosenGod.get(i));
            if (i!=numPlayer-1){
                message.append(",");
            }
        }
        System.out.println(message);
        ClientGuiApp.getClient().asyncWriteToSocketGUI(message.toString());
        Scene scene=new Scene(waitScene(),810,700);
        ClientGuiApp.getPrimaryStage().setScene(scene);
    }else{
        AlertBox.displayError("select a correct number of God");
    }
    }

    public static void chooseGod(ChosenGodMessage message){
        BorderPane borderPane=new BorderPane();
        VBox layout=new VBox(20);
        layout.setAlignment(Pos.CENTER);
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton third= new RadioButton();
        HBox firstLine= new HBox(20);
        RadioButton first= new RadioButton();
        first.setToggleGroup(radioGroup);
        ImageView IWFirst = new ImageView(imageHashMap.get(message.getChosenGod(0).getGodName()));
        setGodImage(IWFirst);
        Text firstDescription=new Text(message.getChosenGod(0).getGodPower());
        firstLine.getChildren().addAll(first,IWFirst,firstDescription);
        HBox secondLine= new HBox(20);
        RadioButton second= new RadioButton();
        second.setToggleGroup(radioGroup);
        ImageView IWSecond= new ImageView(imageHashMap.get(message.getChosenGod(1).getGodName()));
        setGodImage(IWSecond);
        Text secondDescription=new Text(message.getChosenGod(1).getGodPower());
        secondLine.getChildren().addAll(second,IWSecond,secondDescription);
        layout.getChildren().addAll(firstLine,secondLine);
        if(message.getSize()==3){
            HBox thirdLine= new HBox(20);
            third.setToggleGroup(radioGroup);
            ImageView IWThird= new ImageView(imageHashMap.get(message.getChosenGod(2).getGodName()));
            setGodImage(IWThird);
            Text thirdDescription=new Text(message.getChosenGod(2).getGodPower());
            thirdLine.getChildren().addAll(third,IWThird,thirdDescription);
            layout.getChildren().add(thirdLine);
        }
        HBox fourLine= new HBox();
        Button sendMessage= new Button("enter");
        fourLine.getChildren().add(sendMessage);
        sendMessage.setOnAction(e->{
            if(first.isSelected())
                ClientGuiApp.getClient().asyncWriteToSocketGUI("0");
            if(second.isSelected())
                ClientGuiApp.getClient().asyncWriteToSocketGUI("1");
            if(third.isSelected())
                ClientGuiApp.getClient().asyncWriteToSocketGUI("2");
            Scene scene=new Scene(BoardScene.createContent(),810,700);
            ClientGuiApp.getPrimaryStage().setScene(scene);
        });
        fourLine.setAlignment(Pos.CENTER);
        layout.getChildren().add(fourLine);
        borderPane.setCenter(layout);
        Scene scene4= new Scene(borderPane,850,710);
        ClientGuiApp.getPrimaryStage().setScene(scene4);
    }

    public static void orderGame(OrderGameMessage message){
        VBox layout=new VBox();
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton first=new RadioButton();
        RadioButton second=new RadioButton();
        RadioButton third=new RadioButton();
        first.setToggleGroup(radioGroup);
        second.setToggleGroup(radioGroup);
        third.setToggleGroup(radioGroup);
        HBox firstLine=new HBox(10);
        Text firstName=new Text(message.getPlayerlist().get(0));
        firstLine.getChildren().addAll(first,firstName);
        HBox secondLine=new HBox(10);
        Text secondName=new Text(message.getPlayerlist().get(1));
        secondLine.getChildren().addAll(second,secondName);
        layout.getChildren().addAll(firstLine,secondLine);
        if (message.getPlayerlist().size()==3){
            HBox thirdLine=new HBox(10);
            Text thirdName=new Text(message.getPlayerlist().get(2));
            thirdLine.getChildren().addAll(third,thirdName);
        }
        HBox fourLine= new HBox();
        Button sendMessage= new Button("enter");
        fourLine.getChildren().add(sendMessage);
        fourLine.setAlignment(Pos.CENTER);
        layout.getChildren().add(fourLine);
        sendMessage.setOnAction(e->{
            if(first.isSelected())
                ClientGuiApp.getClient().asyncWriteToSocketGUI(message.getPlayerlist().get(0));
            if(second.isSelected())
                ClientGuiApp.getClient().asyncWriteToSocketGUI(message.getPlayerlist().get(1));
            if(third.isSelected())
                ClientGuiApp.getClient().asyncWriteToSocketGUI(message.getPlayerlist().get(2));
            Scene scene=new Scene(BoardScene.createContent(),810,700);
            ClientGuiApp.getPrimaryStage().setScene(scene);
        });
        layout.setAlignment(Pos.CENTER);
        Scene scene=new Scene(layout,710,800);
        ClientGuiApp.getPrimaryStage().setScene(scene);

    }



    private static void setGodImage(ImageView IWGod){
        IWGod.setFitHeight(190);
        IWGod.setFitWidth(133.33);
    }

    private static Parent waitScene(){
        Label label= new Label("wait");
        VBox layout=new VBox(10);

        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(label);
        return layout;

    }
}
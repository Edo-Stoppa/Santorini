package it.polimi.ingsw.Client.GraphicElements.Board;




import it.polimi.ingsw.Client.ClientGuiApp;
import it.polimi.ingsw.Client.GraphicElements.AlertBox;
import it.polimi.ingsw.Model.PossiblePhases;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BoardScene {
    public static final int TILE_SIZE=120;
    public static final int WIDTH=5;
    public static final int HEIGHT=5;
    public static TextArea messages= new TextArea();
    private static boolean yourTurn=false;
    private static PossiblePhases phase;
    private static  boolean init=true;
    private static boolean special=false;
    private static boolean checkDome=false;



    public static void setYourTurn(boolean yourTurn) {
        BoardScene.yourTurn = yourTurn;
    }


    public static boolean isYourTurn() {
        return yourTurn;
    }

    public static final TileGui[][] board= new TileGui[WIDTH][HEIGHT];

    private static final Group tileGroup = new Group();
    public static final Group pieceGroup= new Group();


    public static TileGui getTile(int x,int y) {
        return board[x][y];
    }

    public static Parent createContent(){
        Pane root= new Pane();
        root.setPrefSize(WIDTH*TILE_SIZE,HEIGHT*TILE_SIZE);
        root.getChildren().addAll(tileGroup,pieceGroup);
        HBox controller =new HBox(70);
        controller.setAlignment(Pos.BOTTOM_CENTER);
        controller.prefHeight(100);
        Pane message= new Pane();
        messages.setPrefHeight(100);
        message.getChildren().add(messages);
        Button godList= new Button("God List");
        Button endPhase= new Button("End Phase");
        godList.setOnAction(e-> AlertBox.displayGod());
        endPhase.setOnAction(e->{
            System.out.println(special+","+yourTurn);
            if(special && yourTurn){
                System.out.println("end");
                ClientGuiApp.getClient().asyncWriteToSocketGUI("end");
            }
        });
        Pane left=new Pane();
        left.setPrefSize(105,600);
        Pane right=new Pane();
        right.setPrefSize(105,600);
        VBox buttons=new VBox(10);
        buttons.getChildren().addAll(godList,endPhase);
        controller.getChildren().addAll(message,buttons);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(root);
        borderPane.setBottom(controller);
        borderPane.setLeft(left);
        borderPane.setRight(right);



        for (int y=0; y<HEIGHT;y++){
            for (int x=0; x<WIDTH;x++){
                TileGui tile= new TileGui((x+y)%2==0,x,y);
                board[x][y]=tile;

                tileGroup.getChildren().add(tile);

            }
        }

        return borderPane;
    }


    static int toBoard(double pixel){
        return (int)(pixel+TILE_SIZE/2)/TILE_SIZE;
    }

    public static Piece makePiece(PieceType type, int x, int y){
        return new Piece(type,x,y);
    }

    public static synchronized void animation(Piece piece, int x, int y){
        TranslateTransition transition= new TranslateTransition(Duration.seconds(0.5),piece);
        transition.setByY(y*TILE_SIZE);
        transition.setByX(x*TILE_SIZE);
        piece.setTranslationY(-y*TILE_SIZE);
        piece.setTranslationX(-x*TILE_SIZE);
        System.out.println("translate: "+piece.getTranslationX()+"  "+piece.getTranslationY());
        transition.play();

    }

    public static void newText(String message){
        messages.appendText(message+"\n");

    }
    public static void drawNextLevel(TileGui tile){
        switch (tile.getLevel()) {
            case 0 -> {
                tile.getText().setText("1");
                tile.setLevel(1);
            }
            case 1 -> {
                tile.getText().setText("2");
                tile.setLevel(2);
            }
            case 2 -> {
                tile.getText().setText("3");
                tile.setLevel(3);
            }
            case 3 -> {
                tile.getText().setText("D");
                tile.setLevel(4);
            }
        }
    }


    public static PossiblePhases getPhase() {
        return phase;
    }

    public static void setPhase(PossiblePhases phase) {
        BoardScene.phase = phase;
    }

    public static boolean isInit() {
        return init;
    }

    public static void setInit(boolean init) {
        BoardScene.init = init;
    }


    public static void setSpecial(boolean special) {
        BoardScene.special = special;
    }

    public static boolean isCheckDome() {
        return checkDome;
    }

    public static void setCheckDome(boolean checkDome) {
        BoardScene.checkDome = checkDome;
    }
}

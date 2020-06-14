package it.polimi.ingsw.Client.GraphicElements.Board;

import it.polimi.ingsw.Client.ClientGuiApp;
import it.polimi.ingsw.Model.PossiblePhases;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Objects;


public class TileGui extends StackPane {

    private final Text text=new Text();
    private Piece piece;
    private int level;


    public  int getLevel() {
        return level;
    }

    public  void setLevel(int level) {
        this.level = level;
    }

    public  Text getText() {
        return text;
    }


    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }


    public TileGui(boolean light, int x, int y){
        Rectangle border = new Rectangle(BoardScene.TILE_SIZE, BoardScene.TILE_SIZE);
        Image Apollo = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("God/Apollo.png")));
        border.setFill(new ImagePattern(Apollo));
        level = 0;
        if(light)
            border.setId("tile");
        else
            border.setId("brightTile");

        //border.setFill(light ? Color.valueOf("#feb") : Color.valueOf("#582"));
        getChildren().addAll(border,text);


        relocate(x*BoardScene.TILE_SIZE,y*BoardScene.TILE_SIZE);
        text.setFont(Font.font(72));


        setOnMouseClicked(e->{
            if(BoardScene.isYourTurn()){
                if(BoardScene.isInit() || BoardScene.getPhase()==PossiblePhases.MOVE || BoardScene.getPhase()==PossiblePhases.BUILD
                        || BoardScene.getPhase()==PossiblePhases.SPECIAL_MOVE || BoardScene.getPhase()==PossiblePhases.SPECIAL_BUILD){
                    ClientGuiApp.getClient().asyncWriteToSocketGUI(y+","+x);
                }
                if(BoardScene.isCheckDome() && BoardScene.getPhase()==PossiblePhases.SPECIAL_BUILD){
                    ClientGuiApp.getClient().asyncWriteToSocketGUI(y+","+x);
                }
            }
        });
    }



    public void drawDome(){
        switch (this.level) {
            case 0 -> {
                this.setId("level4");
                this.text.setText("D");
                this.level=4;

            }
            case 1 -> {
                this.setId("level5");
                this.text.setText("D");
                this.level=5;

            }
            case 2 -> {
                this.setId("level6");
                this.text.setText("D");
                this.level=6;

            }
            case 3 -> {
                this.setId("level7");
                this.text.setText("D");
                this.level=7;

            }
        }

    }



}





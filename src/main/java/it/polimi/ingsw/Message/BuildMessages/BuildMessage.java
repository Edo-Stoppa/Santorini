package it.polimi.ingsw.Message.BuildMessages;

import it.polimi.ingsw.Client.PlaySpace;
import it.polimi.ingsw.Message.GameMessage;
import it.polimi.ingsw.Model.PossiblePhases;

public abstract class BuildMessage extends GameMessage {
    private final int[][] buildingMatrix;

    public BuildMessage(String currentPlayer, PossiblePhases currentPhase, int[][] matrix) {
        super(currentPlayer, currentPhase);
        this.buildingMatrix = matrix;
    }

    public int[][] getBuildingMatrix(){
        return this.buildingMatrix;
    }

    /*@Override
    public void updatePlaySpace(PlaySpace playSpace, boolean isMyTurn) {
        playSpace.setBuildingMatrix(getBuildingMatrix());

        playSpace.reset();
    }*/

}

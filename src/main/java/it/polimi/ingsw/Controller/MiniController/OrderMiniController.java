package it.polimi.ingsw.Controller.MiniController;

import it.polimi.ingsw.Client.PlaySpace;

import java.util.List;

public class OrderMiniController implements MiniController{
    List<String> pList;
    public OrderMiniController(List<String> pList){
        this.pList = pList;
    }

    @Override
    public boolean checkPos(String input, PlaySpace playSpace, StringBuilder stringBuilder) {
        for(String s : pList){
            if(s.equals(input)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMessage(String input) {
        return input;
    }
}

package it.polimi.ingsw.Controller;

import it.polimi.ingsw.Message.GameMessage;
import it.polimi.ingsw.Message.PosMessage;
import it.polimi.ingsw.Message.TileToShowMessages.StandardTileMessage;
import it.polimi.ingsw.Message.TileToShowMessages.TileToShowMessage;
import it.polimi.ingsw.Model.*;
import it.polimi.ingsw.Observer.Observable;
import it.polimi.ingsw.Observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    private class Receiver implements Observer<GameMessage> {
        GameMessage receivedMessage;

        @Override
        public void update(GameMessage message) {
            this.receivedMessage = message;
        }
    }

    private class Sender extends Observable<PosMessage> {
        public void forceNotify(PosMessage message) {
            notify(message);
        }
    }

    Controller controller;
    Receiver r;
    Sender s;

    @BeforeEach
    void init() {
        Random random = new Random();
        List<Player> pList = createPlayer(new Artemis(), new Atlas());
        Model model = new Model(pList);
        controller = new Controller(model);
        r = new Receiver();
        s = new Sender();
        model.addObserver(r);
        s.addObserver(controller);
        for(Player p : pList)   {
            Position pos1 = new Position(random.nextInt(5), random.nextInt(5));
            while(model.getOccupied(pos1))  {
                pos1 = new Position(random.nextInt(5), random.nextInt(5));
            }
            Position pos2 = new Position(random.nextInt(5), random.nextInt(5));
            while(model.getOccupied(pos2))    {
                pos2 = new Position(random.nextInt(5), random.nextInt(5));
            }
            Constructor c = p.getAllConstructors().get(0);
            model.setCurrentConstructor(c);
            model.performMove(pos1);

            c = p.getAllConstructors().get(1);
            model.setCurrentConstructor(c);
            model.performMove(pos2);
        }
        for (int i = 0; i < 5; i++) { //creates a random board without Atlas as God
            for (int j = 0; j < 5; j++) {
                Position pos = new Position(i, j);
                int miavar = random.nextInt(5);
                for(int k = 0; k < miavar; k++) {
                    controller.getModel().performBuild(pos);
                }
            }
        }
        model.changeActiveConstructors();
        model.setCurrentConstructor(pList.get(0).getAllConstructors().get(0));
        model.startGame();
    }

    @RepeatedTest(500)
    void preparePhaseBasicTest(RepetitionInfo repetitionInfo)    {//Artemis as God
        assertTimeoutPreemptively(ofMillis(200), () ->  {
            List<Position> list = new ArrayList<>();
            Player player = null;
            Random random = new Random();
            int miavar = random.nextInt(3);

            if(miavar == 0)    {//CHOOSE_CONSTRUCTOR
                for(Player p : controller.getModel().getListPlayer())   {
                    if(p.getIdPlayer().equals(controller.getModel().getCurrentPlayerId()))   {
                        player = p;
                        break;
                    }
                }
                for(Constructor c : player.getAllConstructors())    {
                    if(c.getCanMove())  {
                        list.add(c.getPos());
                    }
                }
                controller.preparePhase();
                if(r.receivedMessage instanceof TileToShowMessage)  {
                    List<Position> receivedList = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                    assertEquals(list.size(), receivedList.size(), "The size should be the same");
                    for(int i = 0; i < receivedList.size(); i++)    {
                        assertEquals(list.get(i).getRow(), receivedList.get(i).getRow(), "The row should be the same");
                        assertEquals(list.get(i).getCol(), receivedList.get(i).getCol(), "The col should be the same");
                    }
                }
            }
            if(miavar == 1)  {//MOVE
                controller.getModel().nextPhase();
                for(Player p : controller.getModel().getListPlayer()) {
                    if(p.getIdPlayer().equals(controller.getModel().getCurrentPlayerId()))  {
                        player = p;
                        break;
                    }
                }
                controller.getModel().createPossibleMovePos(null, null);
                list = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                controller.preparePhase();
                if(r.receivedMessage instanceof TileToShowMessage)  {
                    List<Position> receivedList = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                    assertEquals(list.size(), receivedList.size(), "The size should be the same");
                    for(int i = 0; i < receivedList.size(); i++)    {
                        assertEquals(list.get(i).getRow(), receivedList.get(i).getRow(), "The row should be the same");
                        assertEquals(list.get(i).getCol(), receivedList.get(i).getCol(), "The col should be the same");
                    }
                }
            }
            if(miavar == 2)  {//BUILD
                controller.getModel().nextPhase();
                controller.getModel().nextPhase();
                controller.getModel().nextPhase();
                for(Player p : controller.getModel().getListPlayer()) {
                    if(p.getIdPlayer().equals(controller.getModel().getCurrentPlayerId()))  {
                        player = p;
                        break;
                    }
                }
                controller.getModel().createPossibleBuildPos(null, null);
                list = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                controller.preparePhase();
                if(r.receivedMessage instanceof TileToShowMessage)  {
                    List<Position> receivedList = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                    assertEquals(list.size(), receivedList.size(), "The size should be the same");
                    for(int i = 0; i < receivedList.size(); i++)    {
                        assertEquals(list.get(i).getRow(), receivedList.get(i).getRow(), "The row should be the same");
                        assertEquals(list.get(i).getCol(), receivedList.get(i).getCol(), "The col should be the same");
                    }
                }
            }
        });
    }

    @RepeatedTest(500)
    void handleActionTest()    {
        assertTimeoutPreemptively(ofMillis(500), () ->  {
            Random random = new Random();
            int miavar = random.nextInt(3);
            Player player = null;
            if(miavar == 0)  {
                for(Player p : controller.getModel().getListPlayer())   {
                    if(p.getIdPlayer().equals(controller.getModel().getCurrentPlayerId()))  {
                        player = p;
                        break;
                    }
                }
                PosMessage message = new PosMessage("Boh", player.getIdPlayer(), null, player.getAllConstructors().get(1).getPos());
                controller.handleChooseConstructor(message);
                assertEquals(player.getAllConstructors().get(1).getPos().getRow(), controller.getModel().getCurrentConstructor().getPos().getRow(),"The row should be the same");
                assertEquals(player.getAllConstructors().get(1).getPos().getCol(), controller.getModel().getCurrentConstructor().getPos().getCol(),"The col should be the same");
            }
            if(miavar == 1)  {
                controller.getModel().nextPhase();
                for(Player p : controller.getModel().getListPlayer())   {
                    if(p.getIdPlayer().equals(controller.getModel().getCurrentPlayerId()))  {
                        player = p;
                        break;
                    }
                }
                Position pos = new Position(random.nextInt(5), random.nextInt(5));
                Tile t = new Tile(pos);
                while(t.getOccupied())  {
                    pos = new Position(random.nextInt(5), random.nextInt(5));
                    t = new Tile(pos);
                }
                PosMessage message = new PosMessage("Boh", player.getIdPlayer(), null, pos);
                controller.handleMove(message);
                assertEquals(pos.getRow(), controller.getModel().getCurrentConstructor().getPos().getRow(), "The row should be the same");
                assertEquals(pos.getCol(), controller.getModel().getCurrentConstructor().getPos().getCol(), "The col should be the same");
            }
            if(miavar == 2)  {
                int row;
                int col;
                Position currConsPos = controller.getModel().getCurrentConstructor().getPos();
                controller.getModel().nextPhase();
                controller.getModel().nextPhase();
                controller.getModel().nextPhase();
                for(Player p : controller.getModel().getListPlayer())   {
                    if(p.getIdPlayer().equals(controller.getModel().getCurrentPlayerId()))  {
                        player = p;
                        break;
                    }
                }
                if(currConsPos.getRow() < 3)    {
                    row = currConsPos.getRow() + random.nextInt(2);
                    if(currConsPos.getCol() < 3)    {
                        col = currConsPos.getCol() + random.nextInt(2);
                    }
                    else    {
                        col = currConsPos.getCol() - random.nextInt(2);
                    }
                }
                else    {
                    row = currConsPos.getRow() - random.nextInt(2);
                    if(currConsPos.getCol() < 3)    {
                        col = currConsPos.getCol() + random.nextInt(2);
                    }
                    else    {
                        col = currConsPos.getCol() - random.nextInt(2);
                    }
                }
                Position pos = new Position(row, col);
                int cont = 0;
                boolean check = true;
                while(controller.getModel().getOccupied(pos) && check)  {
                    if(cont < 10)   {
                        if(currConsPos.getRow() < 3)    {
                            row = currConsPos.getRow() + random.nextInt(2);
                            if(currConsPos.getCol() < 3)    {
                                col = currConsPos.getCol() + random.nextInt(2);
                            }
                            else    {
                                col = currConsPos.getCol() - random.nextInt(2);
                            }
                        }
                        else    {
                            row = currConsPos.getRow() - random.nextInt(2);
                            if(currConsPos.getCol() < 3)    {
                                col = currConsPos.getCol() + random.nextInt(2);
                            }
                            else    {
                                col = currConsPos.getCol() - random.nextInt(2);
                            }
                        }
                        pos = new Position(row, col);
                        cont++;
                    }
                    else    {
                        check = false;
                    }
                }
                PosMessage message = new PosMessage("Boh", player.getIdPlayer(), null, pos);
                if(!(controller.getModel().getConstructionLevel(pos) == 3 && controller.getModel().getDome(pos)) && !check) {
                    controller.handleBuild(message);
                    assertEquals(pos.getRow(), controller.getModel().getCurrentConstructor().getLastBuildPos().getRow(), "The row should be the same");
                    assertEquals(pos.getCol(), controller.getModel().getCurrentConstructor().getLastBuildPos().getCol(), "The col should be the same");
                }
            }
        });
    }

    @RepeatedTest(500)
    void updateTest()  {
        assertTimeoutPreemptively(ofMillis(200), () ->  {
            Position pos;
            Random random = new Random();
            int miavar = random.nextInt(3);
            PossiblePhases possiblePhases = null;
            PosMessage message;
            List<Position> list;
            Player player = null;
            for(Player p : controller.getModel().getListPlayer())    {
                if(controller.getModel().getCurrentConstructor().getPlayerNumber() == p.getPlayerNumber())  {
                    player = p;
                    break;
                }
            }
            if(miavar == 0)  {//CHOOSE_CONSTRUCTOR
                pos = player.getAllConstructors().get(1).getPos();
                message = new PosMessage("standard", player.getIdPlayer(), null, pos);
                s.forceNotify(message);
                possiblePhases = PossiblePhases.MOVE;
                assertEquals(possiblePhases, controller.getModel().getCurrentPhase(), "The phase should be MOVE");
                assertEquals(pos.getRow(), controller.getModel().getCurrentConstructor().getPos().getRow(), "The row should be the same");
                assertEquals(pos.getCol(), controller.getModel().getCurrentConstructor().getPos().getCol(), "The col should be the same");
            }
            if(miavar == 1)  {//MOVE
                controller.getModel().nextPhase();
                controller.getModel().createPossibleMovePos(null, null);
                list = ((StandardTileMessage) r.receivedMessage).getTileToShow();
                if(list.size() > 0) {
                    message = new PosMessage("standard", player.getIdPlayer(), null, list.get(0));
                    s.forceNotify(message);
                    if(!controller.getModel().checkWin())    {
                        assertTrue(r.receivedMessage instanceof TileToShowMessage, "The message should be CanEndTile");
                        controller.getModel().createPossibleMovePos(null, null);
                        list = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                        if(list.size() == 1 && list.get(0).equals(controller.getModel().getCurrentConstructor().getPrevPos()) || list.size() == 0)   {
                            possiblePhases = possiblePhases.BUILD;
                        }
                        else    {
                            possiblePhases = possiblePhases.SPECIAL_MOVE;
                        }
                        assertEquals(possiblePhases, controller.getModel().getCurrentPhase(), "The phase should be SPECIAL_MOVE");
                    }
                }
            }
            if(miavar == 3) {//BUILD
                controller.getModel().nextPhase();
                controller.getModel().nextPhase();
                controller.getModel().nextPhase();
                controller.getModel().createPossibleBuildPos(null, null);
                list = ((StandardTileMessage) r.receivedMessage).getTileToShow();
                if (list.size() > 0) {
                    message = new PosMessage("standard", player.getIdPlayer(), null, list.get(0));
                    s.forceNotify(message);
                    assertTrue(r.receivedMessage instanceof TileToShowMessage, "The message should be a TileToShow");
                    possiblePhases = PossiblePhases.CHOOSE_CONSTRUCTOR;
                    assertEquals(possiblePhases, controller.getModel().getCurrentPhase(), "This should be the first phase of Atlas");
                    list = ((TileToShowMessage) r.receivedMessage).getTileToShow();
                    for(Player p : controller.getModel().getListPlayer())   {
                        if(!p.equals(player))   {
                            player = p;
                            break;
                        }
                    }
                    int i = 0;
                    for(Constructor c : player.getAllConstructors())    {
                        assertEquals(c.getPos().getRow(), list.get(i).getRow(), "The row should be the same");
                        assertEquals(c.getPos().getCol(), list.get(i).getCol(), "The col should be the same");
                        i++;
                    }
                }
            }
        });
    }

    private List<Player> createPlayer(God p1God, God p2God) {
        Player p1 = new Player("uno", 1);
        Player p2 = new Player("due", 2);

        p1.setGod(p1God);
        p2.setGod(p2God);

        List<Player> pList = new ArrayList<>();
        pList.add(p1);
        pList.add(p2);

        return pList;
    }
}
package it.polimi.ingsw.Model;

import it.polimi.ingsw.Message.GameMessage;
import it.polimi.ingsw.Observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private class Receiver implements Observer<GameMessage>  {
        private GameMessage receivedMessage;

        @Override
        public void update(GameMessage message) {
            this.receivedMessage = message;
        }
    }
    Model model;
    List<Player> list;
    Receiver receiver;


    @BeforeEach
    void init() {//It creates a random board, a GameState and  before every Test
        Player p1, p2;

        p1 = new Player("First", "01/01/2000", 1);
        p1.setGod(new Athena());
        p2 = new Player("Second", "02/01/2000", 2);
        p2.setGod(new Apollo());
        list = new ArrayList<>();
        list.add(p1);
        list.add(p2);

        model = new Model(list);
        receiver = new Receiver();
        model.addObserver(receiver);

        int numberConstructorsP1 = 0, numberConstructorsP2 = 0;
        Random random = new Random();
        Board board = model.getBoard();

        while(numberConstructorsP1 < 2 || numberConstructorsP2 < 2) {//It creates 2 constructors for each player and it places them in random positions
            int x = random.nextInt(4);
            int y = random.nextInt(4);
            Position pos = new Position(x, y);
            if(board.getTile(pos).getActualConstuctor() == null)    {
                if(numberConstructorsP1 < 2)    {
                    p1.getAllConstructors().get(numberConstructorsP1).setPos(pos);
                    numberConstructorsP1++;

                }
                else   {
                    if(numberConstructorsP2 == 1)   {
                        Position pos1 = new Position(3, 3);
                        p2.getAllConstructors().get(numberConstructorsP2).setPos(pos1);
                        model.setCurrentConstructor(p2.getAllConstructors().get(1));
                    }
                    else    {
                        p2.getAllConstructors().get(numberConstructorsP2).setPos(pos);
                    }
                    numberConstructorsP2++;
                }
            }
        }
        model.getGameState().setCurrentPlayer(p2);
        for (int i = 0; i < 5; i++) { //creates a random board without Atlas as God
            for (int j = 0; j < 5; j++) {
                Position pos = new Position(i, j);
                int miavar = random.nextInt(4);
                for(int k = 0; k < miavar; k++) {
                    board.getTile(pos).increaseConstructionLevel();
                }
            }
        }
    }

    @Test
    void isOccupiedTest()   {
        boolean t = false;
        Random random = new Random();
        Position pos = new Position(random.nextInt(4), random.nextInt(4));

        for(int i = 0; i < 5; i++)  {
            for(int j = 0; j < 5; j++)  {
                Position p1 = new Position(i, j);
                if(model.getBoard().getTile(p1).getOccupied())  {
                    if(p1.equals(pos))  {
                        assertTrue(model.isOccupied(pos), "The Tile should be occupied");
                        t = true;
                        break;
                    }
                }
            }
        }
        if(!t)  {
            assertFalse(model.isOccupied(pos),"The Tile should not be occupied");
        }
    }

    @RepeatedTest(3)
    void isLosingTest(RepetitionInfo repetitionInfo) {
        if(repetitionInfo.getCurrentRepetition() == 1)  {//The player can move 2 constructors
            Random random = new Random();
            Player p = model.getListPlayer().get(random.nextInt(1));//It chooses a random player
            List<Constructor> list = p.getAllConstructors();
            list.get(0).setCanMove(true);
            list.get(1).setCanMove(true);
            assertFalse(model.isLosing(p), "The player is not losing");
        }
        if(repetitionInfo.getCurrentRepetition() == 2)  {//The player can move just 1 constructor
            Random random = new Random();
            Player p = model.getListPlayer().get(random.nextInt(1));//It chooses a random player
            List<Constructor> list = p.getAllConstructors();
            list.get(0).setCanMove(false);
            list.get(1).setCanMove(true);
            assertFalse(model.isLosing(p), "The player is not losing");
        }
        if(repetitionInfo.getCurrentRepetition() == 3)  {//The player can not move any constructor
            Random random = new Random();
            Player p = model.getListPlayer().get(random.nextInt(1));//It chooses a random player
            List<Constructor> list = p.getAllConstructors();
            list.get(0).setCanMove(false);
            list.get(1).setCanMove(false);
            assertTrue(model.isLosing(p), "The player is losing");
        }
    }

    @RepeatedTest(2)
    void isPlayerTurnTest(RepetitionInfo repetitionInfo) {
        if(repetitionInfo.getCurrentRepetition() == 1)  {
            model.getGameState().startGame();
            assertAll(
                    ()  ->  assertTrue(model.isPlayerTurn(list.get(0)), "Should be the turn of player number 1"),
                    ()  ->  assertFalse(model.isPlayerTurn(list.get(1)),"Should not be the turn of player number 2")
            );
        }
        if(repetitionInfo.getCurrentRepetition() == 2)  {
            model.getGameState().startGame();
            model.getGameState().nextTurn();
            assertAll(
                    ()  ->  assertFalse(model.isPlayerTurn(list.get(0)), "Should not be the turn of player number 1"),
                    ()  ->  assertTrue(model.isPlayerTurn(list.get(1)),"Should be the turn of player number 2")
            );
        }
    }

    @Test
    void performMoveTest() {
        String expectedMessage;

        Position posCurrentConstructor = model.getCurrentConstructor().getPos();
        Position nextPosCurrentConstructor = new Position(posCurrentConstructor.getRow() + 1, posCurrentConstructor.getCol() -1);
        model.performMove(nextPosCurrentConstructor);
        expectedMessage = model.getGameState().getCurrentPlayer().getIdPlayer() + " moved to position: " + nextPosCurrentConstructor.toString();
        assertAll(
                ()  ->  assertEquals(nextPosCurrentConstructor.getRow(), model.getCurrentConstructor().getPos().getRow(), "The row should be the same"),
                ()  ->  assertEquals(nextPosCurrentConstructor.getCol(), model.getCurrentConstructor().getPos().getCol(), "The column should be the same")
        );
        assertEquals(expectedMessage, receiver.receivedMessage.getMessage(), "The message should be the same");
        assertEquals(model.getGameState().getCurrentPlayer(), receiver.receivedMessage.getPlayer(),"The player should be the same");
    }

    @Test
    void performBuild() {
        Random random = new Random();
        String expectedMessage;
        Position p1 = new Position(random.nextInt(4), random.nextInt(4));
        int expectedConstructionLevel = 0;
        boolean expectedDome = false;

        while(model.getBoard().getTile(p1).getDome())  {
            p1 = new Position(random.nextInt(4), random.nextInt(4));
        }
        if(model.getBoard().getTile(p1).getConstructionLevel() < 3)    {
            expectedConstructionLevel = model.getBoard().getTile(p1).getConstructionLevel() + 1;
            expectedDome = false;
        }
        else if(model.getBoard().getTile(p1).getConstructionLevel() == 3)  {
            expectedConstructionLevel = 3;
            expectedDome = true;
        }
        model.setCurrentConstructor(new Constructor(1));
        model.performBuild(p1);
        expectedMessage = model.getGameState().getCurrentPlayer().getIdPlayer() + " built on position: " + p1.toString();
        assertEquals(expectedConstructionLevel, model.getBoard().getTile(p1).getConstructionLevel(),"The level should be the same");
        assertEquals(expectedDome, model.getBoard().getTile(p1).getDome(),"The dome should be the same");
        assertEquals(expectedMessage, receiver.receivedMessage.getMessage(), "The message should be the same");
        assertEquals(model.getGameState().getCurrentPlayer(), receiver.receivedMessage.getPlayer(), "The player should be the same");
    }

    @RepeatedTest(3)
    void isLastStanding(RepetitionInfo repetitionInfo)   {
        Player p1 = new Player("First", "28/05/1998", 1);
        Player p2 = new Player("Second", "11/07/2001", 2);
        ArrayList<Player> list = new ArrayList<>();

        if(repetitionInfo.getCurrentRepetition() == 1)  {//The player is the last standing.
            list.add(p1);
            model.getGameState().setPlayerList(list);
            model.getGameState().setCurrentPlayer(p1);
            assertTrue(model.isLastStanding(), "The player should be the last one");
        }
        if(repetitionInfo.getCurrentRepetition() == 2) {//The player is not the last one, (playerList.size = 2)
            list.add(p1);
            list.add(p2);
            model.getGameState().setPlayerList(list);
            model.getGameState().setCurrentPlayer(p1);
            assertFalse(model.isLastStanding(), "The player should not be the last one");
        }
        if(repetitionInfo.getCurrentRepetition() == 3)  {//The player is not the last one, playerList.size = 1
            list.add(p2);
            model.getGameState().setPlayerList(list);
            model.getGameState().setCurrentPlayer(p1);
            assertFalse(model.isLastStanding(), "The player should not be the last one");
        }

    }

    @RepeatedTest(2)
    void createPossibleConstructorPosTest(RepetitionInfo repetitionInfo) {
        List<Position> expected = new ArrayList<>();

        if (repetitionInfo.getCurrentRepetition() == 1) {// 2 constructors can move
            model.getGameState().getCurrentPlayer().getAllConstructors().get(0).setCanMove(true);
            model.getGameState().getCurrentPlayer().getAllConstructors().get(1).setCanMove(true);
            model.createPossibleConstructorPos();
            expected.add(model.getGameState().getCurrentPlayer().getAllConstructors().get(0).getPos());
            expected.add(model.getGameState().getCurrentPlayer().getAllConstructors().get(1).getPos());
            String expectedMessage = model.getGameState().getCurrentPlayer().getIdPlayer() + " can choose between " + expected.size() + " constructors";
            assertAll(
                    () -> assertEquals(expected.get(0).getCol(), model.getTileToShow().get(0).getCol(), "The column should be the same"),
                    () -> assertEquals(expected.get(0).getRow(), model.getTileToShow().get(0).getRow(), "The row should be the same"),
                    () -> assertEquals(expected.get(1).getCol(), model.getTileToShow().get(1).getCol(), "The column should be the same"),
                    () -> assertEquals(expected.get(1).getRow(), model.getTileToShow().get(1).getRow(), "The row should be the same"),
                    () -> assertEquals(expectedMessage, receiver.receivedMessage.getMessage(), "The message should be the same"),
                    () -> assertEquals(model.getGameState().getCurrentPlayer(), receiver.receivedMessage.getPlayer(), "The player should be the same")
            );
        }
        if (repetitionInfo.getCurrentRepetition() == 2) {//1 constructor can move
            model.getGameState().getCurrentPlayer().getAllConstructors().get(0).setCanMove(true);
            model.getGameState().getCurrentPlayer().getAllConstructors().get(1).setCanMove(false);
            model.createPossibleConstructorPos();
            expected.add(model.getGameState().getCurrentPlayer().getAllConstructors().get(0).getPos());
            String expectedMessage = model.getGameState().getCurrentPlayer().getIdPlayer() + " can choose between " + expected.size() + " constructors";
            assertAll(
                    () -> assertEquals(expected.get(0).getCol(), model.getTileToShow().get(0).getCol(), "The column should be the same"),
                    () -> assertEquals(expected.get(0).getRow(), model.getTileToShow().get(0).getRow(), "The row should be the same"),
                    () -> assertEquals(1, model.getTileToShow().size(), "There should be just one cell"),
                    () -> assertEquals(expectedMessage, receiver.receivedMessage.getMessage(), "The message should be the same"),
                    () -> assertEquals(model.getGameState().getCurrentPlayer(), receiver.receivedMessage.getPlayer(), "The player should be the same")
            );
        }
    }

    @Test
    void deactivateConstructorIfNeededTest()   {
        Random random = new Random();
        Constructor c1 = new Constructor(model.getGameState().getCurrentPlayer().getPlayerNumber());
        Constructor c2 = new Constructor(model.getGameState().getCurrentPlayer().getPlayerNumber());
        Position pos1 = new Position(random.nextInt(4), random.nextInt(4));
        Position pos2 = new Position(random.nextInt(4), random.nextInt(4));
        while(pos1.equals(pos2))    {
            pos2 = new Position(random.nextInt(4), random.nextInt(4));
        }

        model.getGameState().getCurrentPlayer().getAllConstructors().set(0, c1);
        model.getGameState().getCurrentPlayer().getAllConstructors().set(1, c2);
        c1.setCanMove(true);
        c2.setCanMove(true);
        model.getBoard().placeConstructor(model.getBoard().getTile(pos1), c1);
        model.getBoard().placeConstructor(model.getBoard().getTile(pos2), c2);
        model.deactivateConstructorIfNeeded();
        for(Constructor c : model.getGameState().getCurrentPlayer().getAllConstructors())   {
            if(model.getBoard().possibleMoveset(c).size() == 0) {
                assertFalse(c.getCanMove(), "The constructor should not move");
            }
            else    {
                assertTrue(c.getCanMove(), "The constructor should move");
            }
        }
    }

    @Test
    void createPossibleMovePos()    {
        Random random = new Random();
        List<Position> expectedList;
        List<Position> addList = new ArrayList<>();
        List<Position> deleteList = new ArrayList<>();
        boolean flag;

        expectedList = model.getBoard().possibleMoveset(model.getCurrentConstructor());
        for(int i = 0; i < 2; i++)  {
            flag = true;
            Position pos1 = new Position(random.nextInt(4), random.nextInt(4));
            for(Position p : expectedList)    {
                if(pos1.equals(p))    {
                    flag = false;
                    break;
                }
            }
            if(!flag)    {
                i--;
            }
            else    {
                if(addList.size() == 0) {
                    addList.add(pos1);
                }
                else    {
                    for(Position p : addList) {
                        if(pos1.equals(p)) {
                            flag = false;
                            break;
                        }
                    }
                    if(!flag)   {
                        i--;
                    }
                    else    {
                        addList.add(pos1);
                    }
                }
            }
        }
        for(int j = 0; j < 2; j++)    {
            Position pos2 = expectedList.get(j);
            deleteList.add(pos2);
        }
        for(int i = 0; i < 2; i++)  {
            expectedList.add(addList.get(i));
        }
        for(int i = 0; i < 2 ; i++) {
            expectedList.remove(deleteList.get(i));
        }
        model.createPossibleMovePos(addList, deleteList);
        String message = model.getGameState().getCurrentPlayer().getIdPlayer() + " can move to any of these tiles";
        assertEquals(expectedList.size(), model.getTileToShow().size(), "The size should be the same");
        assertEquals(message, receiver.receivedMessage.getMessage(), "the message should be the same");
        assertEquals(model.getGameState().getCurrentPlayer().getIdPlayer(), receiver.receivedMessage.getPlayer().getIdPlayer(), "The idPlayer should be the same");
        for(int i = 0; i < model.getTileToShow().size(); i++)   {
            assertEquals(expectedList.get(i).getRow(), model.getTileToShow().get(i).getRow(),"The row should be the same");
            assertEquals(expectedList.get(i).getCol(), model.getTileToShow().get(i).getCol(), "The col should be the same");
        }
    }

    @Test
    void createPossibleBuildPosTest()   {
        Random random = new Random();
        List<Position> expectedList;
        List<Position> addList = new ArrayList<>();
        List<Position> deleteList = new ArrayList<>();
        boolean flag;

        expectedList = model.getBoard().possibleBuild(model.getCurrentConstructor());
        for(int i = 0; i < 2; i++)  {
            flag = true;
            Position pos1 = new Position(random.nextInt(4), random.nextInt(4));
            for(Position p1 : expectedList)    {
                if(pos1.equals(p1))    {
                    flag = false;
                    break;
                }
            }
            if(!flag)    {
                i--;
            }
            else    {
                if(addList.size() == 0) {
                    addList.add(pos1);
                }
                else    {
                    for(Position p2 : addList) {
                        if(pos1.equals(p2)) {
                            flag = false;
                            break;
                        }
                    }
                    if(!flag)   {
                        i--;
                    }
                    else    {
                        addList.add(pos1);
                    }
                }
            }
        }
        for(int j = 0; j < 2; j++)    {
            Position pos2 = expectedList.get(j);
            deleteList.add(pos2);
        }
        for(int i = 0; i < 2; i++)  {
            expectedList.add(addList.get(i));
        }
        for(int i = 0; i < 2 ; i++) {
            expectedList.remove(deleteList.get(i));
        }
        model.createPossibleBuildPos(addList, deleteList);
        String message = model.getGameState().getCurrentPlayer().getIdPlayer() + " can build on any of these tiles";
        assertEquals(expectedList.size(), model.getTileToShow().size(), "The size should be the same");
        assertEquals(message, receiver.receivedMessage.getMessage(), "the message should be the same");
        assertEquals(model.getGameState().getCurrentPlayer().getIdPlayer(), receiver.receivedMessage.getPlayer().getIdPlayer(), "The idPlayer should be the same");
        for(int i = 0; i < model.getTileToShow().size(); i++)   {
            assertEquals(expectedList.get(i).getRow(), model.getTileToShow().get(i).getRow(),"The row should be the same");
            assertEquals(expectedList.get(i).getCol(), model.getTileToShow().get(i).getCol(), "The col should be the same");
        }
    }

    @Test
    void destroyRemainingPhasesTest()   {
        model.destroyRemainingPhases();
        assertEquals(1, model.getCurrentGod().getPhasesList().size(), "The size of the list should be 1");
        assertEquals(PossiblePhases.CHOOSE_CONSTRUCTOR.toString(), model.getCurrentGod().getPhasesList().get(0).toString(), "Should be the CHOOSE_CONSTRUCTOR phase");
    }
}


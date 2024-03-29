package it.polimi.ingsw.Model;

import it.polimi.ingsw.Controller.GodController.GodController;
import it.polimi.ingsw.Message.BuildMessages.BuildMessage;
import it.polimi.ingsw.Message.BuildMessages.StandardBuildMessage;
import it.polimi.ingsw.Message.GameMessage;
import it.polimi.ingsw.Message.MoveMessages.*;
import it.polimi.ingsw.Message.TileToShowMessages.CanEndTileMessage;
import it.polimi.ingsw.Message.MoveMessages.ServerMoveMessage;
import it.polimi.ingsw.Message.TileToShowMessages.StandardTileMessage;
import it.polimi.ingsw.Message.WinMessage;
import it.polimi.ingsw.Observer.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the model of the application
 */
public class Model extends Observable<GameMessage> {
    private final GameState gameState;
    private final Board board;
    private List<Position> tileToShow;
    private Constructor currentConstructor;

    public Model(List<Player> playerList) {
        this.gameState = new GameState(playerList);
        this.board = new Board();
        this.tileToShow = null;
        this.currentConstructor = null;
    }

    /**
     * Method used to change the current phase of the game. If there's no more phases for the current
     * <em>Player</em> then the method change turn instead of the phase
     */
    public void nextPhase() {
        try {
            this.gameState.nextPhase();
        } catch (IndexOutOfBoundsException e) {
            this.gameState.nextTurn();
        }
    }

    /**
     * This method will be uused to notify a message
     * @param message GameMessage we want to notify
     */
    public void forceNotify(GameMessage message)    {
        notify(message);
    }

    /**
     * This method lets the game begins
     */
    public void startGame() {
        gameState.startGame();
    }

    //METHODS THAT PERFORM AN ACTION

    /**
     * This method places a constructor on the board during the initialization of the board
     * @param c the constructor we want to place
     * @param p the position we want to place the constructor in
     * @param idPlayer id of the current player
     */
    public void serverMove(Constructor c, Position p, String idPlayer){
        Tile t = board.getTile(p);
        board.placeConstructor(t, c);

        int[][] matrix = board.createConstructorMatrix();

        MoveMessage message = new ServerMoveMessage(idPlayer, PossiblePhases.INIT, matrix);
        message.setMessage(p.toString());
        notify(message);
    }

    /**
     * It sets currentConstructor to the actual <em>Constructor</em> in the position passed as parameter.
     *
     * @param pos   The <em>Position</em> where there's the constructor the player wants to choose.
     */
    public void performChooseConstructor(Position pos) {
        setCurrentConstructor(board.getTile(pos).getActualConstructor());
    }

    /**
     * Method used to change the <em>Position</em> of the current <em>Constructor</em> from his old one
     * to the one that's passed as input.
     * After moving the <em>Constructor</em>, the method create a new ConstructorMatrix and send (through a notify())
     * a new MoveMessage to every <em>Observer</em>
     *
     * @param pos <em>Position</em> where to move the currentConstructor
     */
    public void performMove(Position pos) {
        Tile t = board.getTile(pos);
        board.placeConstructor(t, currentConstructor);

        int[][] matrix = board.createConstructorMatrix();

        MoveMessage message = new StandardMoveMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), matrix);
        message.setMessage(pos.toString());
        notify(message);
    }

    /**
     * This method performs the special movement of the <em>God</em> Apollo: he can swap his <em>Constructor</em>
     * with one of the enemy player, but only if the enemy <em>Constructor</em> is 1 <em>Tile</em> away from him.
     *
     * @param pos   the <em>Position</em> of the enemy <em>Constructor</em>
     */
    public void performSwap(Position pos)   {
        Constructor swappedConstructor = board.getTile(pos).getActualConstructor();
        board.swapConstructors(currentConstructor, swappedConstructor);

        int[][] matrix = board.createConstructorMatrix();

        MoveMessage message = new SwapMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), matrix);
        message.setMessage(swappedConstructor.getPos().toString());
        notify(message);
    }

    /**
     * This method performs the special movement of the <em>God</em> Minotaur: he can move his <em>Constructor</em>
     * to the position of the enemy player one, but only if the enemy <em>Constructor</em> is in the same
     * row/column that he is.
     *
     * @param pos   the <em>Position</em> of the enemy <em>Constructor</em>
     */
    public  void performPush(Position pos)  {
        Constructor pushedConstructor = board.getTile(pos).getActualConstructor();

        board.pushConstructor(pushedConstructor, currentConstructor);

        int[][] matrix = board.createConstructorMatrix();

        MoveMessage message = new PushMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), matrix);
        message.setMessage(pushedConstructor.getPos().toString());
        notify(message);
    }

    /**
     * Method used to build on the <em>Tile</em> in the <em>Position</em> that's passed as input.
     * After raising the level of the building, the method create a new BuildingMatrix and send (through a notify())
     * a new BuildMessage to every <em>Observer</em>
     *
     * @param pos <em>Position</em> where to build
     */
    public void performBuild(Position pos) {
        Tile t = board.getTile(pos);
        board.placeBuilding(t);
        currentConstructor.setLastBuildPos(pos.clone());

        int[][] matrix = board.createBuildingMatrix();

        BuildMessage message = new StandardBuildMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), matrix);
        message.setMessage(pos.toString());
        notify(message);
    }

    //METHODS THAT NOTIFY A LIST OF POSITION

    /**
     * This method is used to create an array of <em>Position</em>, which contains every <em>Constructor</em> of the
     * current <em>Player</em> that can actually move. It notify observers by a <em>TileToShowMessage</em>.
     */
    public void createPossibleConstructorPos()  {
        List<Position> list = new ArrayList<>();

        for(Constructor c : gameState.getCurrentPlayer().getAllConstructors())  {
            if(c.getCanMove())  {
                list.add(c.getPos().clone());
            }
        }
        setTileToShow(list);

        notify(new StandardTileMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), list));
    }

    /**
     * This method creates an ArrayList which contains every possible move the <em>Constructor</em> can perform.
     * It helps to control the special power of certain <em>Gods</em>
     *
     * @param addList   list of <em>Positions</em> that has to be added to list
     * @param deleteList    list of <em>Positions</em> that has to be removed from list
     */
    public void createPossibleMovePos(List<Position> addList, List<Position> deleteList)   {
        List<Position> list;

        list = board.possibleMoveset(currentConstructor);
        list = checkListsParameter(list, addList, deleteList);
        setTileToShow(list);

        if(deleteList == null){
            notify(new StandardTileMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), list));
        } else {
            notify(new CanEndTileMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), list));
        }
    }

    /**
     * This method creates an ArrayList which contains every possible <em>Position</em> the <em>Constructor</em> can perform a build.
     * It helps to control the special power of certain <em>Gods</em>
     *
     * @param addList   list of <em>Positions</em> that has to be added to list
     * @param deleteList    list of <em>Positions</em> that has to be removed from list
     */
    public void createPossibleBuildPos(List<Position> addList, List<Position> deleteList)  {
        List<Position> list;

        list = board.possibleBuild(currentConstructor);
        list = checkListsParameter(list, addList, deleteList);
        setTileToShow(list);

        if(deleteList == null){
            notify(new StandardTileMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), list));
        } else {
            notify(new CanEndTileMessage(gameState.getCurrentPlayer().getIdPlayer(), gameState.getCurrentPhase(), list));
        }

    }

    //CHECK METHODS

    /**
     * Method used to check if the <em>Tile</em> corresponding to the <em>Position</em> passed
     * as input is already occupied by someone
     *
     * @param p <em>Position</em> where the <em>Tile</em> is located
     * @return if the <em>Tile</em> is already occupied
     */
    public boolean isOccupied(Position p) {
        Tile t = board.getTile(p);
        return t.getOccupied();
    }

    /**
     * This method checks if every <em>Constructor</em> of the current <em>Player</em> can perform a move. If it can
     * not, the <em>Constructor</em> is deactivated.
     */
    public void changeActiveConstructors() {
        List<Position> list;

        for(Constructor c : gameState.getCurrentPlayer().getAllConstructors())  {

            list = board.possibleMoveset(c);
            c.setCanMove(list.size() != 0);
        }
    }

    /**
     * Method used to check if a <em>Player</em> is losing due to the inability to move
     * any of his/hers <em>Constructor</em>
     *
     * @param p <em>Player</em> to check
     * @return if the <em>Player</em> is losing
     */
    public boolean isLosing(Player p) {
        List<Constructor> constructorList = p.getAllConstructors();
        for (Constructor c : constructorList) {
            if (c.getCanMove()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method used to check if the current <em>Player</em> won after performing any king of Move phase
     *
     * @return if the <em>Player</em> is now located on a level 3 construction and his position before was on a level 2
     * construction
     */
    public boolean checkWin() {
        Tile currentTile = board.getTile(currentConstructor.getPos());
        Tile previousTile = board.getTile(currentConstructor.getPrevPos());

        return (currentTile.getConstructionLevel() == 3 && previousTile.getConstructionLevel() == 2);
    }

    /**
     * The method is used to check if it's the turn of the given <em>Player</em>
     *
     * @param p <em>Player</em> to check
     * @return if it's his/hers turn
     */
    public boolean isPlayerTurn(String p) {
        Player currentP = gameState.getCurrentPlayer();
        return p.equals(currentP.getIdPlayer());
    }

    /**
     * This method checks if the <em>Player</em> is the only one that can perform a move
     *
     * @return true if he is the only one that can perform a move, false if he is not.
     */
    public boolean isLastStanding() {
        return (gameState.getPlayerList().size() == 1 && gameState.getPlayerList().get(0).equals(gameState.getCurrentPlayer()));
    }

    /**
     * This method will be used in the application to check if the current player is losing
     * @return a boolean which indicates if the player is losing the game (TRUE) or not (FALSE)
     */
    public boolean isLosing()  {
        return isLosing(gameState.getCurrentPlayer());
    }

    //END METHODS

    /**
     * It destroys every phase of the current player.
     */
    public void destroyRemainingPhases()    {
        List<PossiblePhases> list = getCurrentGod().getPhasesList();
        list.subList(1, list.size()).clear();
    }

    /**
     * This method removes a <em>Player</em> from the game
     *
     * @param playerId  id of the <em>Player</em> we want to remove
     */
    public void removePlayer(String playerId)   {
        Player playerR = null;
        List<Player> playerList;
        List<Constructor> constructorList;
        for(int i = 0; i < gameState.getPlayerList().size(); i++)   {
            if(gameState.getPlayerList().get(i).getIdPlayer().equals(playerId)) {
                playerR = gameState.getPlayerList().get(i);
            }
        }
        constructorList = playerR.getAllConstructors();
        for(Constructor c : constructorList)   {
            board.getTile(c.getPos()).setActualConstructor(null);
            board.getTile(c.getPos()).setOccupied(false);
            c.setPos(new Position(-1, -1));
        }
        constructorList.clear();
        playerList = gameState.getPlayerList();
        for(int i = 0; i < playerList.size(); i++)   {
            if(playerId.equals(playerList.get(i).getIdPlayer())) {
                playerList.remove(i);
                break;
            }
        }
        int[][] matrix = board.createConstructorMatrix();

        notify(new RemovedPlayerMessage(playerR.getIdPlayer(), getCurrentPhase(), matrix));
    }

    /**
     * This method notify all players the game ended, showing the winner id on screen
     */
    public void endGame()   {
        notify(new WinMessage(getCurrentPlayerId(), null));

        notify(new WinMessage(null, null));
    }

    //SETTER METHODS

    /**
     * This method sets the attribute tileToShow of this class
     * @param tileToShow is a List of positions
     */
    public void setTileToShow(List<Position> tileToShow) {
        this.tileToShow = tileToShow;
    }

    /**This method set the current constructor with the one passed as parameter
     * @param currentConstructor the constructor we want to become as current.
     */
    public void setCurrentConstructor(Constructor currentConstructor) {
        this.currentConstructor = currentConstructor;
    }

    //GETTER METHODS

    /**
     * This method returns the attribute tileToShow of this class
     * @return a List of positions
     */
    public List<Position> getTileToShow() {
        return tileToShow;
    }

    /**
     * This method returns the List of players who are playing the game
     * @return a List of Players
     */
    public List<Player> getListPlayer() {
        return gameState.getPlayerList();
    }

    /**
     * This method returns the phase the game is at the moment
     * @return a PossiblePhases
     */
    public PossiblePhases getCurrentPhase() {
        return gameState.getCurrentPhase();
    }

    /**
     * This method return the <em>GodController</em> of the <em>God</em> the current <em>Player</em> has.
     *
     * @return The <em>GodController</em> of the current player's <em>God</em>
     */
    public GodController getCurrentPlayerController()   {
        return gameState.getCurrentPlayer().getGod().getGodController();
    }

    /**
     *
     * @return the God of the current player
     */
    public God getCurrentGod()  {
        return gameState.getCurrentPlayer().getGod();
    }

    /**
     *
     * @return the id of the current player
     */
    public String getCurrentPlayerId(){
        return gameState.getCurrentPlayer().getIdPlayer();
    }

    /**
     *
     * @return a boolean which indicates if the currentConstructor can go up
     */
    public boolean getCanGoUp() {
        return board.getCanGoUp();
    }

    protected Board getBoard()  {
        return board;
    }

    protected GameState getGameState()  {
        return gameState;
    }

    public Constructor getCurrentConstructor()   {return  currentConstructor;}

    public boolean getDome(Position pos)   {
        return board.getTile(pos).getDome();
    }

    public boolean getOccupied(Position pos)    {
        return board.getTile(pos).getOccupied();
    }

    public int getConstructionLevel(Position pos)   {
        return board.getTile(pos).getConstructionLevel();
    }

    //HELPER

    /**
     * Helper method, it checks every parameter that will be add in the tileToShowList;
     *
     * @param list list of <em>Positions</em> where the <em>Player</em> perform a move/build
     * @param addList   other <em>Positions</em> where the <em>Player</em> can move/build
     * @param deleteList    other <em>Positions</em> where the <em>Player</em> can't move
     * @return  final list of <em>Positions</em> where the <em>Player</em> can perform a move/build
     */
    private List<Position> checkListsParameter(List<Position> list, List<Position> addList, List<Position> deleteList)  {
        if(addList != null) {
            for(Position position : addList) {
                boolean flag = true;
                for(int j = 0; j < list.size() && flag; j++) {
                    if (list.get(j).equals(position)) {
                        flag = false;
                    }
                }
                if(flag) {
                    list.add(position);
                }
            }
        }
        if(deleteList != null)  {
            for(Position delPos : deleteList)  {
                for(int i = 0; i < list.size(); i++)   {
                    if(list.get(i).equals(delPos)) {
                        list.remove(i);
                        break;
                    }
                }
            }
        }
        return list;
    }
}
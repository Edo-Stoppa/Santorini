package it.polimi.ingsw.Client;

import it.polimi.ingsw.Client.GraphicElements.AlertBox;
import it.polimi.ingsw.Client.GraphicElements.Board.BoardScene;
import it.polimi.ingsw.Client.GraphicElements.SceneBuilder;
import it.polimi.ingsw.Controller.MiniController.BaseMiniController;
import it.polimi.ingsw.Controller.MiniController.MiniController;
import it.polimi.ingsw.Message.GameMessage;
import it.polimi.ingsw.Message.HelpMessage;
import it.polimi.ingsw.Message.MoveMessages.RemovedPlayerMessage;
import it.polimi.ingsw.Message.ServerMessage.GodRecapMessage;
import it.polimi.ingsw.Message.ServerMessage.PlaceFirstConstructorMessage;
import it.polimi.ingsw.Message.ServerMessage.ServerMessage;
import it.polimi.ingsw.Message.TileToShowMessages.TileToShowMessage;
import it.polimi.ingsw.Message.WinMessage;
import it.polimi.ingsw.Model.God;
import it.polimi.ingsw.Model.PossiblePhases;
import javafx.application.Platform;
import javafx.scene.Scene;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * this class implements client for GUI's version of the game using JavaFX 14
 */
public class ClientGUI extends Client implements EventHandler {
    private String idPlayer = null;
    private MiniController miniController;
    private PrintWriter socketOut;
    private static Map<String, God> playerGodMap;
    private LocalTime lastPingTime;
    private final Object ipLock = new Object();

    public ClientGUI(String ip, int port) {
        super(ip, port);
    }


    @Override
    public void run() throws IOException {
        Socket socket = new Socket(ip, port);
        ObjectInputStream socketIn = new ObjectInputStream(socket.getInputStream());
        socketOut = new PrintWriter(socket.getOutputStream());
        SceneBuilder.initImages();
        try {
            asyncReadFromSocket(socketIn);
            asyncManagePing();
            asyncMangePong();
        } catch (NoSuchElementException e) {
            System.out.println("Connection closed from the client side");
        }
    }

    //------------------------------- Methods used to manage read/write on socket -------------------------------


    /**
     * This method will be used to process the user input, using as a support
     * the current miniController (if present). In general is able to respond to a wrong (o when is not the
     * turn of the player) input without checking the server.
     * @param message string to check and write to the socket
     */
    public void writeToSocketGUI(String message) {
        StringBuilder sBuilder = new StringBuilder();
        if (this.miniController != null) {
            if (this.miniController.checkPosGui(message, playSpace, sBuilder)) {
                String out = this.miniController.getMessageGui(message);
                BoardScene.setEndPhaseButton(false);
                miniController = null;
                playSpace.disHighlightsTile();
                playSpace.reset();
                System.out.println("Message sent: " + out);
                synchronized (ipLock){
                    socketOut.println(out);
                    socketOut.flush();
                }
            } else {
                System.out.println(sBuilder);
            }
        } else {
            System.out.println("Now you can't make a move. Please wait");
        }

    }

    @Override
    public Thread asyncReadFromSocket(ObjectInputStream socketIn) {
        Thread t= new Thread(() -> {
            try {
                while(isActive()){
                    Object inputObject = socketIn.readObject();

                    if(inputObject.equals(true)){
                        synchronized (ipLock){
                            updatePing();
                        }
                    } else synchronized(this){
                        System.out.println("Message received: " + inputObject.getClass().getSimpleName() +
                                (inputObject instanceof GameMessage? ", Current Player: " + ((GameMessage)inputObject).getIdPlayer() : ""));
                        if(inputObject instanceof String) {
                            manageString((String) inputObject);
                        }else if (inputObject instanceof ServerMessage){
                            manageServerMessage((ServerMessage)inputObject);
                        }else if (inputObject instanceof GameMessage){
                            manageGameMessage((GameMessage)inputObject);
                        }

                    }
                }
            }catch (Exception e){
                if(isActive()){
                    System.out.println("Some problem occurred!\nIf you want to play again please restart the application");
                    Platform.runLater(()-> SceneBuilder.loseScene("Some problem occurred!\nIf you want to play again please restart the application"));
                }
                setActive(false);
            }

        });
        t.start();
        return t;
    }

    //------------------------------- Methods used to check the connection between client and server -------------------------------

    /**
     * This method creates a thread used to check if the connection is still alive between client and server.
     * Every 7 seconds the thread wakes up and check if while it was sleeping another ping was received.
     * If no ping was received it means that the connection was lost, and proceed to end the game.
     *
     * @return The actual thread
     */
    public Thread asyncManagePing(){
        Thread t = new Thread(() ->{
            LocalTime lastThreadTime = LocalTime.now();
            while(isActive()){
                try{
                    Thread.sleep(7000);
                } catch(InterruptedException e){
                    setActive(false);
                    Platform.runLater(()-> SceneBuilder.loseScene("Something went horribly wrong, please restart the game"));
                    System.out.println("\n\nSomething went horribly wrong, please restart the game");
                }

                synchronized(ipLock){
                    if(lastThreadTime.equals(lastPingTime)){
                        if(isActive()){
                            System.out.println("The Server connection was lost, please restart the game");
                            Platform.runLater(()-> SceneBuilder.loseScene("The Server connection was lost, please restart the game"));
                        }
                        setActive(false);
                    } else {
                        lastThreadTime = lastPingTime;
                    }
                }

            }
        });
        t.start();
        return t;
    }

    public void asyncMangePong(){
        new Thread(() ->{
            while(isActive()){
                pong();

                try{
                    Thread.sleep(5000);
                } catch(InterruptedException e){
                    setActive(false);
                    System.out.println("Game Interrupted");
                }
            }
        }).start();
    }

    public void pong(){
        synchronized (ipLock){
            socketOut.println(HelpMessage.pong);
            socketOut.flush();
        }
    }

    private synchronized void updatePing(){
        lastPingTime = LocalTime.now();
    }

    //------------------------------- Methods used to manage incoming messages from server -------------------------------

    @Override
    public void manageGameMessage(GameMessage inputObject) {
        boolean isMyTurn = idPlayer.equals(inputObject.getIdPlayer());
        inputObject.autoSetMessage(isMyTurn, false);
        BoardScene.setYourTurn(isMyTurn);
        if(inputObject instanceof TileToShowMessage){
            if(isMyTurn) {
                BoardScene.setPhase(inputObject.getPhase());
                this.miniController = ((TileToShowMessage) inputObject).getMiniController();
                updateText(inputObject);
                updatePlaySpaceGUI(inputObject);
                BoardScene.setInit(false);
            } else {
                updateText(inputObject);
            }
            return;
        }else if(inputObject instanceof RemovedPlayerMessage) {
            updateText(inputObject);
            updatePlaySpaceGUI(inputObject);
            if (isMyTurn && playSpace.CountPlayerRemains(((RemovedPlayerMessage) inputObject).getConstructorMatrix())==4){
                Platform.runLater(()->{
                    boolean answer = AlertBox.checkDome("You lost!\nDo you want to continue to watch the game?");
                    if (!answer){
                        SceneBuilder.loseScene(null);
                        setActive(false);
                    }
                }); }
            else if(isMyTurn){
                Platform.runLater(() -> SceneBuilder.loseScene(null));
                setActive(false);
            }
            return;
        } else if(inputObject instanceof WinMessage){
            setActive(false);
            SceneBuilder.endGameTransition(isMyTurn, inputObject.getIdPlayer());
            return;
        }

        updatePlaySpaceGUI(inputObject);
    }

    @Override
    public void manageString(String input){
        executeSpecialString(input);

        if(input.startsWith(HelpMessage.noAnswer)){
            //System.out.println(input.substring(HelpMessage.noAnswer.length()));
            //System.out.println();
        } else {
            if(idPlayer == null) {
                if (!getName(input)) {
                    this.miniController = new BaseMiniController();
                }
            } else{
                this.miniController = new BaseMiniController();
            }
        }
    }

    @Override
    public void manageServerMessage(ServerMessage inputObject){
        if (inputObject instanceof GodRecapMessage) {
            String name = ((GodRecapMessage) inputObject).getFirstPlayer();
            if (!name.equals(idPlayer)) {
                BoardScene.newText("Please wait while " + name + " is choosing where to place a constructor");
            }
            playerGodMap = ((GodRecapMessage) inputObject).getPlayerGodMap();
            Platform.runLater(()->{
                Scene scene=new Scene(BoardScene.createContent(),ClientGuiApp.width,ClientGuiApp.height);
                ClientGuiApp.getPrimaryStage().setScene(scene);
                scene.getStylesheets().add(Objects.requireNonNull(ClientGUI.class.getClassLoader().getResource("backgroundImage.css")).toExternalForm());
            });
            return;
        }
        if (inputObject instanceof PlaceFirstConstructorMessage){
            BoardScene.newText(inputObject.getMessage());
        }
        this.miniController=inputObject.getMiniController();
        update(inputObject);
    }


    //---------- Methods to manage all the String messages ----------

    /**
     * This method create a new scene for special <em>HelpMessage</em> recived
     * @param input string to check
     */
    private void executeSpecialString(String input){
        if(input.equals(HelpMessage.forcedClose)){
            Platform.runLater(()->{
                SceneBuilder.loseScene(input);
                setActive(false);
            });
            return;
        }

        if (input.equals(HelpMessage.enterName)){
            Platform.runLater(()->{
                Scene scene=new Scene(SceneBuilder.ChooseName(HelpMessage.enterName),ClientGuiApp.width,ClientGuiApp.height);
                scene.getStylesheets().add(Objects.requireNonNull(ClientGUI.class.getClassLoader().getResource("backgroundImage.css")).toExternalForm());
                ClientGuiApp.getPrimaryStage().setScene(scene);
            });
            return;
        }

        if (input.equals(HelpMessage.gameMode)) {
            Platform.runLater(() -> {
                Scene scene = new Scene(SceneBuilder.ChooseGameMode(HelpMessage.gameMode), ClientGuiApp.width, ClientGuiApp.height);
                scene.getStylesheets().add(Objects.requireNonNull(ClientGUI.class.getClassLoader().getResource("backgroundImage.css")).toExternalForm());
                ClientGuiApp.getPrimaryStage().setScene(scene);
            });
            return;
        }

        if(input.startsWith(HelpMessage.noAnswer + "Please wait while"))
            BoardScene.newText(input.substring(HelpMessage.noAnswer.length()));
    }


    /**
     * Method used to get the accepted name for the player
     *
     * @param s String returned by the Server
     *
     * @return Boolean that indicates if the name is accepted ora not
     */
    private boolean getName(String s){
        String[] splitted = s.split(" ");

        if(splitted[0].equals("Accepted")){
            this.idPlayer = s.substring(splitted[0].length()+1);
            checkName(true);
            return true;
        }else if(s.equals(HelpMessage.taken)) {
            checkName(false);
        }

        return false;
    }

    /**
     * Method used to create a scene if the name is already taken
     * @param check true if the name is accepted
     */
    public void checkName(boolean check){
        Platform.runLater(()->{
            if(check) {
                SceneBuilder.waitScene();
            }else{
                Scene newName= new Scene(SceneBuilder.ChooseName("This name is already taken, please choose another one"),ClientGuiApp.width,ClientGuiApp.height);
                newName.getStylesheets().add(Objects.requireNonNull(ClientGUI.class.getClassLoader().getResource("backgroundImage.css")).toExternalForm());
                ClientGuiApp.getPrimaryStage().setScene(newName);
            }
        });
    }

    //---------- Methods to update and change the client GUI ----------
    @Override
    public void update(ServerMessage message) {
        Platform.runLater(message::buildScene);
    }

    @Override
    public void updatePlaySpaceGUI(GameMessage message) {
        Platform.runLater(()-> message.updateGUI(playSpace));
    }

    public void updateText(GameMessage message){
        Platform.runLater(()-> {
            if (message.getPhase() == PossiblePhases.CHOOSE_CONSTRUCTOR || message.getPhase() == PossiblePhases.SPECIAL_CHOOSE_CONSTRUCTOR)
                BoardScene.clearText();
            BoardScene.newText(message.getMessage());
        });
    }

    //---------- Miscellaneous ----------
    public static Map<String, God> getPlayerGodMap() {
        return playerGodMap;
    }


}

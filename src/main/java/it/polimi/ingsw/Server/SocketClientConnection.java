package it.polimi.ingsw.Server;

import it.polimi.ingsw.Message.HelpMessage;
import it.polimi.ingsw.Message.ServerMessage.ChosenGodMessage;
import it.polimi.ingsw.Message.ServerMessage.OrderGameMessage;
import it.polimi.ingsw.Message.ServerMessage.PickGodMessage;
import it.polimi.ingsw.Message.ServerMessage.PlaceFirstConstructorMessage;
import it.polimi.ingsw.Model.Position;
import it.polimi.ingsw.Observer.Observable;
import it.polimi.ingsw.Model.God;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;

/**
 * SocketClientConnection is a class used to decouple the Server from the actual connection: when a new Client connects to the Server the latter
 * creates a new SocketClientConnection.
 *
 * The main objective of a SocketClientConnection is to manage the exchange the messages between Server and Client using the Socket protocol, and
 * ensuring that the connection is still active to the client. When a Client can't be reached the SocketClientConnection proceeds to disconnect every
 * other player playing with the owner of the disconnected client.
 */
public class SocketClientConnection extends Observable<String> implements Runnable {

    private final Server server;
    private final Socket socket;
    private ObjectOutputStream out;
    private int gameMode;
    public final Object lock = new Object();

    private boolean active=true;
    private boolean playing = true;

    private LocalTime lastPong;
    private boolean isInit = true;
    private String asyncAnswer;


    public SocketClientConnection(Socket socket,Server server){
        this.server=server;
        this.socket=socket;
    }

    @Override
    public void run() {
        Scanner in;
        String name;
        gameMode = 0;
        String read;

        try {
            in = new Scanner(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            //this run a Thread to ping the client
            asyncManagePing();
            asyncManagePong();

            send(HelpMessage.gameMode);
            while(active){
                try{
                    read = in.nextLine();

                    if(read.equals(HelpMessage.pong))
                        updatePong();
                    else {
                        gameMode = Integer.parseInt(read);
                        if(gameMode != 2 && gameMode != 3)
                            throw new NumberFormatException();
                        break;
                    }
                } catch (NumberFormatException e){
                    send("Please enter a possible game mode");
                } catch (NoSuchElementException e){
                    System.out.println("Connection closed by Client while choosing game mode, proceeding closing SocketClientConnection");
                    active = false;
                    playing = false;
                    return;
                } catch (Exception e){
                    System.out.println("Unknown exception, closing SocketClientConnection");
                    active = false;
                    playing = false;
                    return;
                }
            }

            send(HelpMessage.enterName);
            while(true){
                name = in.nextLine();

                if(name.equals(HelpMessage.pong))
                    updatePong();
                else {
                    if(gameMode == 2){
                        if(server.getWaitingConnection2P().containsKey(name)){
                            name = enterNewName(server.getWaitingConnection2P());
                        }
                    } else {
                        if(server.getWaitingConnection3P().containsKey(name)){
                            name = enterNewName(server.getWaitingConnection3P());
                        }
                    }
                    if(name == null){
                        System.out.println("Failed new player authentication, closing SocketClientConnection");
                        return;
                    }
                    break;
                }
            }

            initReadThread(in);

            if (gameMode == 2) {
                System.out.println("Starting lobby2P...");
                server.lobby2P(this, name);
            } else {
                System.out.println("Starting lobby3P...");
                server.lobby3P(this, name);
            }

        }catch (IOException | NoSuchElementException e){
            endGame(e);
        }
    }


    //-------------------------- Fundamental methods ---------------------------

    /**
     * This method will send the Object to the Client though the Socket. If any problem arises, the method ends the current match
     *
     * @param message The Object to send
     */
    public synchronized void send(Object message){
        try {
            out.reset();
            out.writeObject(message);
            out.flush();
        } catch (IOException e){
            if(playing && active){
                System.err.println(e.getMessage() + " while playing == true");
                //e.printStackTrace();
                playing = false;
                close(gameMode);
            } else {
                System.out.println("Connection already closed");
            }
            active = false;
        }
    }

    /**
     * Send of the message asynchronously
     *
     * @param message Message to send
     */
    public void asyncSend(final Object message) {
        Thread thread = new Thread(() -> {
            send(message);
        });
        thread.start();
    }

    /**
     * This method start a new thread that will manage all the incoming messages from the Client
     *
     * @param in The already created Scanner for the Client's input
     */
    public void initReadThread(Scanner in){
        new Thread(() -> {
            String read;

            try {
                while (isActive()) {
                    read = in.nextLine();

                    if (read.equals(HelpMessage.pong))
                        updatePong();
                    else {
                        if (!isInit) {
                            notify(read);
                        } else {
                            asyncAnswer = read;
                            synchronized (lock) { lock.notify(); }
                        }

                    }
                }
            } catch(Exception e){
                endGame(e);
            }

        }).start();
    }


    //--------------- Methods used to check the connection between server and client ---------------

    /**
     * Creates a new thread managing the ping.
     * Every 5 seconds this thread send a ping to the Client, if any problem arises closes the match
     */
    private void asyncManagePing(){
        new Thread(() ->{
            while(playing || active){
                if(!ping()){
                    if(playing){
                        System.out.println("Closing game due to disconnected Client");
                        close(gameMode);
                    }

                    playing = false;
                    active = false;
                }

                try{
                    Thread.sleep(5000);
                } catch(InterruptedException e){
                    System.out.println("Game Interrupted");
                }
            }
        }).start();
    }

    /**
     * Creates a new thread managing the pong.
     * Every 7 seconds the thread wakes up and check if a new pong messages was received in the last 7 seconds; if not
     * proceeds to close the match
     */
    private void asyncManagePong(){
        new Thread(() ->{
            LocalTime lastPongThread = LocalTime.now();

            while(active){
                try{
                    Thread.sleep(7000);
                } catch(Exception e){
                    if(active){
                        if(playing){
                            close(gameMode);
                        }
                        System.out.println(e.getMessage() + " closed SocketClientConnection");
                    } else {
                        System.out.println("SocketClientConnection already closed");
                    }
                    active = false;
                    playing = false;
                }

                if(lastPongThread.equals(lastPong)){
                    if(active){
                        if(playing){
                            close(gameMode);
                        }
                        System.out.println("Failed pong, ending game");
                    } else {
                        System.out.println("SocketClientConnection already closed");
                    }
                    active = false;
                    playing = false;
                } else {
                    lastPongThread = lastPong;
                }
            }
        }).start();
    }

    /**
     * Sends a boolean as a ping message to Client. If any problem arises during the send catch the exception and return false
     *
     * @return Describe if the ping failed for any reason
     */
    public synchronized boolean ping(){
        try {
            out.reset();
            out.writeObject(true);
            out.flush();
            return true;
        } catch (IOException e){
            System.out.println("Failed ping");
            return false;
        }
    }

    /**
     * Any time a pong message is received this method is called, and update the last time a pong message was seen
     */
    private void updatePong(){
        lastPong = LocalTime.now();
    }


    //----------------------- Methods used to manage the (sometimes forced) end game ---------------------

    /**
     * This method launches the complete sequence of operations to close a match
     *
     * @param gameMode The number of player in the match
     */
    public void close(int gameMode) {
        closeConnection();
        System.out.println("Deregistering client...");
        if (gameMode==2) {
            server.deregisterConnection2P(this);
        }
        if(gameMode==3){
            server.deregisterConnection3P(this);
        }
        System.out.println("done");
    }

    /**
     * This method sends (if the socket is not already closed) to the Client an error message, then proceed to close the socket and
     * consequently the connection
     */
    public void closeConnection() {
        if(!socket.isClosed()){
            send(HelpMessage.forcedClose);
            try {
                socket.close();
            } catch (IOException e){
                System.err.println("Error when closing socket");
            }
        }
        playing = false;
        active=false;
    }

    /**
     * Method used to manage the forced end of a match
     *
     * @param e Exception thrown during the match
     */
    private void endGame(Exception e){
        if(active){
            if(playing){
                close(gameMode);
            }
            playing = false;
            active = false;
            System.out.println(e.getMessage() + " closed SocketClientConnection");
        } else {
            System.out.println("SocketClientConnection already closed");
        }
    }


    //------------------------ Methods used during initialization of the match ------------------------

    /**
     * The method sends to the Client a PickGodMessage, then parses the answer
     *
     * @param player Number of player in the game
     * @param pickGodMessage The PickGodMessage to send
     * @return List of god chosen
     */
    public ArrayList<God> chooseGod(int player, PickGodMessage pickGodMessage){
        ArrayList<God> pickGod=new ArrayList<>();
        int i=0;
        try{
            this.asyncSend(new PickGodMessage(player));
            synchronized (lock){ lock.wait(); }

            String[] pick= asyncAnswer.split(",");
            System.out.println("Getting God numbers...");
            while (i<player){
                pickGod.add(pickGodMessage.GetGod(Integer.parseInt(pick[i])));
                i++;
            }
        }catch (Exception e){
            System.err.println(e.getMessage() + " while choosing all Gods");
            //e.printStackTrace();
        }
        asyncAnswer = null;
        return pickGod;
    }

    /**
     * The method sends to the Client a ChosenGodMessage, then parses the answer
     *
     * @param chosenGodMessage The ChosenGodMessage to send
     * @return The God chosen
     */
    public God pickGod(ChosenGodMessage chosenGodMessage){
        try{
            this.asyncSend(chosenGodMessage);
            synchronized (lock) { lock.wait();}

            int numGod = Integer.parseInt(asyncAnswer);
            asyncAnswer = null;
            return chosenGodMessage.getChosenGod(numGod);
        }catch (Exception e){
            asyncAnswer = null;
            System.err.println(e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * Check that the name has already been chosen
     *
     * @param NameOpponent Map that contains all the name used in the server
     * @return A name not used in the server
     */
    public String enterNewName( Map <String, SocketClientConnection> NameOpponent) {
        String name = null;
        try {
            Scanner in= new Scanner(socket.getInputStream());
            this.asyncSend(HelpMessage.taken);

            while(true){
                name = in.nextLine();
                if(name.equals(HelpMessage.pong))
                    updatePong();
                else{
                    while (NameOpponent.containsKey(name)){
                        this.asyncSend(HelpMessage.taken);
                        while(true){
                            name = in.nextLine();

                            if(name.equals(HelpMessage.pong))
                                updatePong();
                            else
                                break;
                        }
                    }
                    break;
                }
            }
        }catch (IOException | NoSuchElementException e){
            System.err.println(e.getMessage() + ", failed to retrieve a new correct id");
            //e.printStackTrace();
            name = null;
        }
        return name;
    }

    /**
     * The method sends to the Client a OrderGameMessage, then parses the answer
     *
     * @param orderGameMessage the OrderGameMessage to send
     * @return The name of the player chosen
     */
    public String chooseFirstPlayer(OrderGameMessage orderGameMessage){
        String firstPlayer=null;
        try {
            this.asyncSend(orderGameMessage);
            synchronized (lock) {lock.wait();}

            firstPlayer = asyncAnswer;
        }catch (Exception e){
            System.err.println(e.getMessage());
            //e.printStackTrace();
        }
        asyncAnswer = null;
        return firstPlayer;
    }

    /**
     * The method sends to the Client a PlaceFirstConstructorMessage, then parses the answer
     *
     * @param isFirstMessage A boolean used to know if the player is the first to place a constructor on the board
     * @return The Position chosen
     */
    public Position firstPlaceConstructor(boolean isFirstMessage){
        String[] coordinates;
        try {
            this.asyncSend(new PlaceFirstConstructorMessage(isFirstMessage));
            synchronized (lock) {lock.wait();}

            coordinates = asyncAnswer.split(",");
        }catch (Exception e){
            System.err.println(e.getMessage());
            //e.printStackTrace();
            asyncAnswer = null;
            return null;
        }
        asyncAnswer = null;
        return new Position(Integer.parseInt(coordinates[0]),Integer.parseInt(coordinates[1]));
    }


    //----------------------------- Miscellaneous -------------------------------

    private synchronized boolean isActive(){
        return active;
    }

    public void setIsInit(boolean b){ isInit = b; }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}

package it.polimi.ingsw.Client;

import it.polimi.ingsw.Controller.MiniController.*;
import it.polimi.ingsw.Message.BuildMessages.BuildMessage;
import it.polimi.ingsw.Message.GameMessage;
import it.polimi.ingsw.Message.HelpMessage;
import it.polimi.ingsw.Message.MoveMessages.MoveMessage;
import it.polimi.ingsw.Message.MoveMessages.RemovedPlayerMessage;
import it.polimi.ingsw.Message.ServerMessage.*;
import it.polimi.ingsw.Message.TileToShowMessages.TileToShowMessage;
import it.polimi.ingsw.Model.God;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientCLI extends Client{
    String idPlayer = null;
    private MiniController miniController;

    public ClientCLI(String ip, int port) {
        super(ip, port);
    }

    @Override
    public Thread asyncWriteToSocket(final Scanner stdin, final PrintWriter socketOut){
        Thread t=new Thread(() -> {
            try{

                StringBuilder sBuilder = new StringBuilder();
                while (isActive()) {
                    String inputLine = stdin.nextLine();
                    synchronized(this){
                        if (this.miniController != null) {
                            sBuilder.delete(0, 100);
                            sBuilder.append("Sorry, your choice is invalid. Please try again");
                            if (this.miniController.checkPos(inputLine, playSpace, sBuilder)) {
                                socketOut.println(this.miniController.getMessage(inputLine));
                                socketOut.flush();
                                System.out.println();
                                playSpace.reset();
                                miniController = null;
                            } else {
                                System.out.println(sBuilder);
                            }
                        } else {
                            System.out.println("Now you can't make a move. Please wait");
                        }
                    }
                }
            }catch (Exception e){
                setActive(false);
            }
        });
        t.start();
        return t;
    }

    @Override
    public  Thread asyncReadFromSocket(final ObjectInputStream socketIn){
        Thread t= new Thread(() -> {
            try {
                while(isActive()){
                    Object inputObject = socketIn.readObject();

                    synchronized(this){
                        if(inputObject instanceof String){
                            manageString((String)inputObject);
                        } else if (inputObject instanceof ServerMessage){
                            manageServerMessage((ServerMessage)inputObject);
                        } else if (inputObject instanceof GameMessage){
                            manageGameMessage((GameMessage)inputObject);
                        }
                    }
                }
            }catch (Exception e){
                setActive(false);
            }

        });
        t.start();
        return t;
    }

    private void manageString(String input){
        if(idPlayer == null){
            if(!getName(input)){
                this.miniController = new BaseMiniController();
                System.out.println(input);
            }
        } else {
            this.miniController = new BaseMiniController();
            System.out.println(input);
        }
    }

    private void manageServerMessage(ServerMessage inputObject) {

        if (inputObject instanceof PickGodMessage){
            System.out.println(inputObject.getMessage());
            this.miniController = new PickGodMiniController(God.getAllGod().size(), ((PickGodMessage)inputObject).getNumPlayer());

        } else if (inputObject instanceof ChosenGodMessage){
            System.out.println(inputObject.getMessage());
            this.miniController = new ChosenMiniController(((ChosenGodMessage)inputObject).getNumPlayer());
        }else if (inputObject instanceof OrderGameMessage) {
            System.out.println(inputObject.getMessage());
            this.miniController = new OrderMiniController(((OrderGameMessage) inputObject).getPlayerlist());
        }else if (inputObject instanceof PlaceFirstConstructorMessage)  {
            System.out.println(inputObject.getMessage());
            this.miniController = new ServerMoveMiniController();
        }
    }

    private void manageGameMessage(GameMessage inputObject) {
        boolean isMyTurn = idPlayer.equals(inputObject.getIdPlayer());
        inputObject.autoSetMessage(isMyTurn, true);

        if(inputObject instanceof TileToShowMessage){
            if(isMyTurn) {
                this.miniController = ((TileToShowMessage) inputObject).getMiniController();
                //System.out.println("Escape Sequence to wipe everything");
                inputObject.updatePlaySpace(playSpace);
                playSpace.printPlaySpace();
            }
            System.out.println(inputObject.getMessage());
        }/*else if(inputObject instanceof WinMessage){

        }*/ else {
            inputObject.updatePlaySpace(playSpace);
            //System.out.println("Escape Sequence to wipe everything");
            playSpace.printPlaySpace();
            if(!isMyTurn || inputObject instanceof RemovedPlayerMessage)
                System.out.println(inputObject.getMessage());
        }

    }

    private boolean getName(String s){
        String[] splitted = s.split(" ");

        if(splitted.length == 2)
            if(splitted[0].equals("Accepted")){
                this.idPlayer = splitted[1];
                return true;
            }

        return false;
    }

    @Override
    public void run() throws IOException {
        System.out.println("\n\n" + HelpMessage.santorini);
        Socket socket= new Socket(ip,port);
        System.out.println("connection established\n");
        ObjectInputStream socketIn = new ObjectInputStream(socket.getInputStream());
        PrintWriter socketOut = new PrintWriter(socket.getOutputStream());
        Scanner stdin= new Scanner(System.in);

        try {
            Thread t0 =asyncReadFromSocket(socketIn);
            Thread t1=asyncWriteToSocket(stdin, socketOut);
            t0.join();
            t1.join();
        }catch (InterruptedException | NoSuchElementException e){
            System.out.println("Connection closed from the client side");
        } finally {
            stdin.close();
            socketIn.close();
            socketOut.close();
            socket.close();
        }
    }
}

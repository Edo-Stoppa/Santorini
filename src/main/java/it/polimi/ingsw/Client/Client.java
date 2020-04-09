package it.polimi.ingsw.Client;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client<ThreadasyncWriteToSocket> {
    private String ip;
    private int port;
    private boolean active= true;

    public Client(String ip, int port){
        this.ip=ip;
        this.port=port;
    }

    public synchronized boolean isActive(){
        return active;
    }

    public synchronized void setActive(boolean active){
        this.active=active;
    }

    public  Thread asyncReadFromSocket(final ObjectInputStream socketIn){
        Thread t= new Thread(() -> {
            try {
                while(isActive()){
                    Object inputObject = socketIn.readObject();
                    if(inputObject instanceof String){
                        System.out.println((String)inputObject);
                    } else {
                        throw new  IllegalArgumentException();
                    }
                }
            }catch (Exception e){
                setActive(false);
            }

        });
    t.start();
    return t;
    }

    public Thread asyncWriteToSocket(final Scanner stdin, final PrintWriter socketOut){
        Thread t=new Thread(() -> {
            try{
                while (isActive()){
                    String inputLine = stdin.nextLine();
                    socketOut.println(inputLine);
                    socketOut.flush();
                }
            }catch (Exception e){
                setActive(false);
            }
        });
        t.start();
        return t;
    }

    public void run() throws IOException{
        Socket socket= new Socket(ip,port);
        System.out.println("connection established");
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

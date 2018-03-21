import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jumpr on 3/15/2018.
 */
public class ServerRunnable implements Runnable {
    private static boolean serverRunning = true;
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private Peer peer;
    private int port;
    private ArrayList<Socket> neighbors;


    public ServerRunnable(Peer peer, int port) {
        this.peer = peer;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(this.getPort());
            while(serverRunning){
                connectionSocket = serverSocket.accept();
                System.out.println("Connection from: " + connectionSocket.getInetAddress().toString());
                peer.setConnectionSocket(connectionSocket);
                ClientRunnable clientRunnable = new ClientRunnable(peer);
                new Thread(clientRunnable).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing server and connection sockets");
                serverSocket.close();
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void closeConnection(){
        serverRunning = false;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public ArrayList<Socket> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Socket> neighbors) {
        this.neighbors = neighbors;
    }
}

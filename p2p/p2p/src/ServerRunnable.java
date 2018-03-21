import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jumpr on 3/15/2018.
 */
public class ServerRunnable implements Runnable {
    private static boolean serverRunning = true;
    private boolean queryNeighbors = true;
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;
    private Peer peer;
    private int port;
    private ArrayList<Socket> neighbors;

    public ServerRunnable(int port){
        this.port = port;
        this.neighbors = new ArrayList<>();
    }
    public ServerRunnable(int port, ArrayList<Socket> neighbors){
        this.port = port;
        this.neighbors = neighbors;
    }

    public ServerRunnable(Peer peer, int port) {
        this.peer = peer;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.getPort());
            while(serverRunning){
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Connection from: " + connectionSocket.getInetAddress().toString());
                peer.setConnectionSocket(connectionSocket);
                ClientRunnable clientRunnable = new ClientRunnable(peer);
                //clientRunnable.setNeighbors(neighbors);
                new Thread(clientRunnable).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing server and connection sockets");
                serverSocket.close();
                connectionSocket.close();
                inFromClient.close();
                outToClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void closeConnection(){
        serverRunning = false;
        //connectionSocket.close();
        //serverSocket.close();
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

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
    private ArrayList<Socket> activeConnections = new ArrayList<>();


    public ServerRunnable(Peer peer, int port) {
        this.peer = peer;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(this.getPort());
            serverSocket.setSoTimeout(p2p.TIMEOUT);
            while(serverRunning){
                connectionSocket = serverSocket.accept();
                activeConnections.add(connectionSocket);
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String message = inFromClient.readLine();
                if(message.charAt(0)=='H') { //heartbeat message
                    System.out.println("Heartbeat from " + connectionSocket.getInetAddress().getHostName());
                }
                else{ //new connection
                    System.out.println(message);
                    ClientRunnable clientRunnable = new ClientRunnable(peer, connectionSocket);
                    new Thread(clientRunnable).start();
                }
            }
        } catch (IOException e) {
            System.out.println("Connection timed out. No peers connected.");
        } finally {
            try {
                System.out.println("Closing connections...");
                serverSocket.close();
                connectionSocket.close();
                return;
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
}

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jumpr_000 on 3/20/2018.
 */
public class HeartbeatServerRunnable implements Runnable {
    private static boolean serverRunning = true;
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private Peer peer;
    private int port;

    public HeartbeatServerRunnable(Peer peer, int port) {
        this.peer = peer;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(this.getPort());
            while(serverRunning){
                connectionSocket = serverSocket.accept();
                System.out.println("Heartbeat from: " + connectionSocket.getInetAddress().toString());
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

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

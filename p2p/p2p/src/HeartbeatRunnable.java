import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by jumpr_000 on 3/20/2018.
 */
public class HeartbeatRunnable implements Runnable {
    private String address;
    private int port;
    private ArrayList<Socket> activeConnections;
    public HeartbeatRunnable(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(p2p.HEARTBEAT_DELAY);
            while(p2p.running) {
                Socket heartbeatSocket = new Socket(address, port);
                PrintWriter outToClient;
                outToClient = new PrintWriter(heartbeatSocket.getOutputStream(), true);
                String queryMessage = "H: " + heartbeatSocket.getInetAddress().getHostName();
                System.out.println("Sending heartbeat to " + heartbeatSocket.getInetAddress().getHostName());
                outToClient.println(queryMessage);
                heartbeatSocket.close();
                Thread.sleep(p2p.HEARTBEAT_DELAY);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(address + "'s heart has stopped beating.  Connection closed.");
            return;
        } catch (InterruptedException e) {
            System.out.println("Interrupting heartbeat");
            Thread.currentThread().interrupt();
            return;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<Socket> getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(ArrayList<Socket> activeConnections) {
        this.activeConnections = activeConnections;
    }
}

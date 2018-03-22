import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class HeartbeatRunnable implements Runnable {
    private String address;
    private int port;

    private boolean connected = true;
    public HeartbeatRunnable(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                this.wait(p2p.HEARTBEAT_DELAY);
                //send heartbeat to address then wait
                while (connected) {
                    Socket heartbeatSocket = new Socket(address, port);
                    PrintWriter outToClient;
                    outToClient = new PrintWriter(heartbeatSocket.getOutputStream(), true);
                    String queryMessage = "H: " + heartbeatSocket.getInetAddress().getHostName();
                    System.out.println("Sending heartbeat to " + heartbeatSocket.getInetAddress().getHostName());
                    outToClient.println(queryMessage);
                    heartbeatSocket.close();
                    this.wait(p2p.HEARTBEAT_DELAY);
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(address + "'s heart has stopped beating.  Connection closed.");
            return;
        } catch (InterruptedException e) {
            System.out.println("Interrupting heartbeat");
            return;
        }
    }

    public void disconnect(){
        connected = false;
    }
    public void connect(){
        connected = true;
    }
}

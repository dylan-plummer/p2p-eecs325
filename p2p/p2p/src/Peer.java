import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jumpr on 3/19/2018.
 */
public class Peer {
    private String address;
    private int port;
    private Socket connectionSocket;
    private ArrayList<Peer> neighbors;

    public Peer(String address, int port) throws IOException {
        this.address = address;
        this.port = port;
        this.connectionSocket = new Socket(address,port);
        neighbors = new ArrayList<>();
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

    public Socket getConnectionSocket() {
        return connectionSocket;
    }

    public void setConnectionSocket(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public ArrayList<Peer> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Peer> neighbors) {
        this.neighbors = neighbors;
    }
}

import java.io.*;
import java.net.Socket;

/**
 * Created by jumpr on 3/20/2018.
 */
public class ClientRunnable implements Runnable {
    private Socket connectionSocket;
    private Peer peer;
    private boolean connected = true;

    public ClientRunnable(Peer peer, Socket connectionSocket){
        this.peer = peer;
        this.connectionSocket = connectionSocket;
    }
    @Override
    public void run() {
        try {
            while(connected) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
                String message = inFromClient.readLine();
                if(message!=null) {
                    String fileName = getFileName(message);
                    if(message.charAt(0) == 'Q' && new File("shared", fileName).exists()) {
                        new Thread(new TransferRunnable(fileName, peer.getAddress(), p2p.TRANSFER_PORT)).start();
                        System.out.println(message);
                        String response = "R:" +
                                getQueryId(message) +
                                ";" +
                                connectionSocket.getLocalAddress().getHostAddress() +
                                ":" +
                                connectionSocket.getLocalPort();
                        outToClient.println(response);
                    } else if (message.charAt(0) == 'R') {
                        peer.downloadFile(fileName, peer.getAddressFromResponse(message), peer.getPortFromResponse(message));
                    } else {
                        System.out.println("File not found on " + connectionSocket.getInetAddress().getHostName());
                        if (peer.getNeighbors() != null) {
                            System.out.println(peer.getNeighbors().toString());
                            String response = peer.queryNeighbors(fileName);
                            if (!response.equals("")) {
                                outToClient.println(response);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static String getQueryId(String query){
        return query.substring(2,query.indexOf(';'));
    }
    public static String getFileName(String query){
        return query.substring(query.indexOf(';')+1);
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public Socket getSocket() {
        return connectionSocket;
    }

    public void setSocket(Socket socket) {
        this.connectionSocket = socket;
    }

}

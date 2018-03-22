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
            while(p2p.running) {
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
                        peer.downloadFile(fileName, peer.getAddressFromResponse(message), p2p.TRANSFER_PORT);
                    } else {
                        System.out.println("File not found on " + connectionSocket.getInetAddress().getHostName() + "querying neighbors");
                        if (peer.getNeighbors() != null) {
                            System.out.println(peer.getNeighbors().toString());
                            String response = peer.queryNeighbors(fileName, connectionSocket.getInetAddress().getHostAddress());
                            if (!response.equals("")) {
                                outToClient.println(response);
                            }
                            else{
                                outToClient.println("File not found");
                            }
                        } else{
                            outToClient.println("File not found");
                        }
                    }
                }
                else{
                    outToClient.println("File not found");
                }
            }

        } catch (IOException e) {
            System.out.println("Closing heartbeat threads...");
        } finally {
            try {
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

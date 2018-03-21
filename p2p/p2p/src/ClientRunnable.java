import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jumpr on 3/20/2018.
 */
public class ClientRunnable implements Runnable {
    private Socket connectionSocket;
    private Peer peer;
    private boolean connected = true;
    public ClientRunnable(Socket socket){
        this.connectionSocket = socket;
       // this.neighbors = new ArrayList<>();
    }
    public ClientRunnable(Socket socket, ArrayList<Socket> neighbors){
        this.connectionSocket = socket;
        //this.neighbors = neighbors;
    }
    public ClientRunnable(Peer peer,Socket socket){
        this.peer = peer;
        this.connectionSocket = socket;
    }
    public ClientRunnable(Peer peer){
        this.peer = peer;
        this.connectionSocket = peer.getConnectionSocket();
    }
    @Override
    public void run() {
        try {
            while(connected) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
                String clientQuery = inFromClient.readLine();
                if(clientQuery!=null) {
                    String fileName = getFileName(clientQuery);
                    if(clientQuery.charAt(0) == 'Q' && new File("shared", fileName).exists()) {
                        System.out.println("Starting transfer thread to "+ peer.getAddress());
                        new Thread(new TransferRunnable(fileName, peer.getAddress(), p2p.END_PORT)).start();
                        System.out.println(clientQuery);
                        String response = "R:" +
                                getQueryId(clientQuery) +
                                ";" +
                                connectionSocket.getLocalAddress().getHostAddress() +
                                ":" +
                                connectionSocket.getLocalPort();
                        outToClient.println(response);
                    } else if (clientQuery.charAt(0) == 'R') {
                        System.out.println("Downloading from Client Thread");
                        peer.downloadFile(fileName, peer.getAddressFromResponse(clientQuery), peer.getPortFromResponse(clientQuery));
                    } else if (clientQuery.charAt(0) == 'T') {
                        System.out.println("transfer");
                    }
                    else {
                        System.out.println("File not found on " + connectionSocket.getInetAddress().getHostName());
                        if (peer.getNeighbors() != null) {
                            System.out.println(peer.getNeighbors().toString());
                            String response = peer.queryNeighbors(fileName);
                            if (!response.equals("")) {
                                //peer.downloadFile(fileName,peer.getAddressFromResponse(response),peer.getPortFromResponse(response));
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

    public void downloadFile(String fileName, String address, int port){
        new Thread(new DownloadRunnable(fileName, address,port)).start();
    }
    public static String getAddressFromResponse(String response){
        return response.substring(response.indexOf(';')+1,response.indexOf(':'));
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

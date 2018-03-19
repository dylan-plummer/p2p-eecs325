import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jumpr on 3/15/2018.
 */
public class ServerRunnable implements Runnable {
    private static boolean serverRunning = true;
    private static ServerSocket serverSocket;
    private static Socket connectionSocket;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;
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
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            connectionSocket = serverSocket.accept();
            System.out.println("Connection from: " + connectionSocket.getInetAddress().getHostAddress());
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new PrintWriter(connectionSocket.getOutputStream(),true);
            while(serverRunning){
                String clientQuery = inFromClient.readLine();
                String fileName = getFileName(clientQuery);
                System.out.println(clientQuery);
                if (new File("shared", fileName).exists()) {
                    new Thread(new TransferRunnable(fileName, connectionSocket.getInetAddress().getHostAddress(),p2p.END_PORT)).start();
                    String response = "R:" +
                            getQueryId(clientQuery) +
                            ";" +
                            connectionSocket.getLocalAddress().getHostAddress() +
                            ":" +
                            connectionSocket.getLocalPort();
                    outToClient.println(response);
                }
                else{
                    for(Socket socket:neighbors){
                        String queryResponse = p2p.queryPeer(fileName,socket);
                        if (queryResponse.equals("File not found")){
                            System.out.println("Peer " + socket.getInetAddress().toString() + " does not have file "+ fileName);
                        }
                        else{
                            outToClient.println(queryResponse);
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connectionSocket.close();
                inFromClient.close();
                outToClient.close();
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
    public static void closeConnection(){
        try {
            serverRunning = false;
            connectionSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<Socket> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Socket> neighbors) {
        this.neighbors = neighbors;
    }
}

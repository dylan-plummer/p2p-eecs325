import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jumpr on 3/15/2018.
 */
public class ServerRunnable implements Runnable {
    private static boolean serverRunning = true;
    private static boolean initConnection = true;
    private static ServerSocket serverSocket;
    private Socket connectionSocket;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;
    private int port;

    public ServerRunnable(int port){
        this.port = port;
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
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

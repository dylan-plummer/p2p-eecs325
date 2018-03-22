import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class p2p {
    public static final int TRANSFER_PORT = 50619;
    public static final int TIMEOUT = 60000; //timeout for listening socket, 60s
    public static final long HEARTBEAT_DELAY = 30000; //heartbeat interval, 30s
    public static final String UNKNOWN_COMMAND = "Command not recognized";
    public static final String FILE_NOT_FOUND = "File not found";
    public static final String PEER_QUERIED = "Peer already queried";
    public static int localPort;
    public static String localAddress;
    public static boolean running = true;

    private static Thread serverThread;
    private static Peer peer;
    private static ServerRunnable serverRunnable;
    private static String fileName;
    private static ArrayList<HeartbeatRunnable> heartbeatThreads;

    public static void main(String[] args){
        System.out.println("Starting up peer...");
        localPort = getLocalPort("_config_local.txt");
        localAddress = getLocalAddress("_config_local.txt");
        if(localPort==-1 || localAddress.equals("")){
            System.out.println("Invalid local config file");
        }
        else {
            peer = new Peer(localAddress, localPort, true);
            serverRunnable = new ServerRunnable(peer, localPort);
            serverThread = (new Thread(serverRunnable));
            serverThread.start();
            waitForCommand();
        }
    }

    //reads local port from config file
    public static int getLocalPort(String file){
        File addressFile = new File (file);
        Scanner scanner = null;
        String line;
        try {
            scanner = new Scanner(addressFile);
            line = scanner.nextLine();
            return Integer.parseInt(line.substring(line.indexOf(':')+1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //reads local address from config file
    public static String getLocalAddress(String file){
        File addressFile = new File (file);
        Scanner scanner = null;
        String line;
        try {
            scanner = new Scanner(addressFile);
            line = scanner.nextLine();
            return line.substring(0,line.indexOf(':'));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    //gets  user input
    public static String getCommand() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().toLowerCase();
    }

    //runs user input
    public static void runCommand(String command){
        switch (command){
            case ("connect"):
                if(!serverThread.isAlive()) {
                    for(HeartbeatRunnable heartbeat:heartbeatThreads){
                        heartbeat.connect();
                    }
                    serverRunnable = new ServerRunnable(peer, localPort);
                    serverRunnable.openConnection();
                    serverThread = (new Thread(serverRunnable));
                    serverThread.start();
                }
                connectToPeers();
                break;
            case ("leave"): disconnectFromPeers();
                break;
            case ("exit"): exitNetwork();
                break;
            default:
                if(command.substring(0,3).equals("get")) {
                    runFileQuery(command);
                    break;
                }
                else{
                    System.out.println(UNKNOWN_COMMAND);
                }
        }
    }

    //takes a 'get' command and sends a query for the file
    public static void runFileQuery(String command){
        fileName = command.substring(4);
        String response = peer.queryNeighbors(fileName,peer.getAddress());
        if(response.length()>0 && response.charAt(0)=='R'){
            peer.downloadFile(fileName,peer.getAddressFromResponse(response), TRANSFER_PORT);
        }
    }

    //connect local peer to neighbors and begin heartbeat
    public static void connectToPeers(){
        peer.makeConnections();
        heartbeatThreads = new ArrayList<>();
        for(Socket socket:peer.getConnections()){
            HeartbeatRunnable heartbeatRunnable = new HeartbeatRunnable(socket.getInetAddress().getHostAddress(),socket.getPort());
            Thread heartbeatThread =  new Thread(heartbeatRunnable);
            heartbeatThreads.add(heartbeatRunnable);
            heartbeatThread.start();
        }
    }

    //disconnect from peers and stop heartbeats
    public static void disconnectFromPeers() {
        running = false;
        System.out.println("Disconnecting...");
        for (HeartbeatRunnable heartbeat : heartbeatThreads) {
            heartbeat.disconnect();
            synchronized (heartbeat) {
                heartbeat.notifyAll();
            }
        }
        serverRunnable.closeConnection();
        System.out.println("Disconnected");
        waitForCommand();
    }

    //waits for user to enter a command
    public static void waitForCommand(){
        running = true;
        while (running) {
            System.out.println("Waiting for command: ");
            runCommand(getCommand());
        }
    }

    //closes all connections and terminates
    public static void exitNetwork(){
        System.out.println("Exiting...");
        running = false;
        serverRunnable.closeConnection();
        System.exit(0);
    }


}
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class p2p {
    public static final int START_PORT = 50600;
    public static final int END_PORT = 50619;
    public static int localPort;

    private static Thread serverThread;
    private static ServerRunnable serverRunnable;
    private static boolean running = true;
    private static String fileName;
    public static ArrayList<String> addressList;
    public static ArrayList<Integer> portList;
    public static ArrayList<Socket> peerConnections = new ArrayList<>();

    public static void main(String[] args){
        System.out.println("Starting up peer...");
        //addressList = fillAddresses("config_neighbors.txt");
        //System.out.println(addressList.toString());
        localPort = getLocalPort("config_local_port.txt");
        serverRunnable = new ServerRunnable(localPort);
        serverThread = (new Thread(serverRunnable));
        serverThread.start();
        while(running){
            runCommand(getCommand()); //run user input
        }
    }

    public static ArrayList<Socket> fillAddresses(String file){
        ArrayList<Socket> addressList = new ArrayList<Socket>();
        File addressFile = new File (file);
        Scanner scanner = null;
        try {
            scanner = new Scanner(addressFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scanner.hasNext()){
            try {
                addressList.add(getSocketFromConfig(scanner.nextLine()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return addressList;
    }

    public static int getLocalPort(String file){
        File addressFile = new File (file);
        Scanner scanner = null;
        try {
            scanner = new Scanner(addressFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(scanner.nextLine());
    }

    public static Socket getSocketFromConfig(String line) throws IOException {
        return new Socket(line.substring(0,line.indexOf(':')),Integer.parseInt(line.substring(line.indexOf(':')+1)));
    }

    public static String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Waiting for command: ");
        return scanner.nextLine().toLowerCase();
    }

    public static void runCommand(String command){
        switch (command){
            case ("connect"): connectToPeers();
                break;
            case ("leave"): disconnectFromPeers();
                break;
            case ("exit"): exitNetwork();
                break;
            default: runFileQuery(command);
                break;
        }
    }

    public static void runFileQuery(String command){
        System.out.println(command);
        if (command.substring(0,3).equals("get")){
            fileName = command.substring(4);
            for(Socket socket:peerConnections){
                String queryResponse = queryPeer(fileName,socket);
                if (queryResponse.equals("File not found")){
                    System.out.println("Peer " + socket.getInetAddress().toString() + " does not have file "+ fileName);
                }
                else{
                    downloadFile(fileName,getAddressFromResponse(queryResponse.substring(2)),END_PORT);
                }
            }
        }
        else{
            System.out.println("Command not recognized");
        }
    }

    public static String getAddressFromResponse(String response){
        return response.substring(response.indexOf(';')+1,response.indexOf(':'));
    }
    public static int getPortFromResponse(String response){
        return Integer.parseInt(response.substring(response.indexOf(':')+1));
    }

    public static void downloadFile(String fileName, String address, int port){
        new Thread(new DownloadRunnable(fileName, address,port)).start();
    }

    public static void connectToPeers(){
        peerConnections = fillAddresses("config_neighbors.txt");
        serverRunnable.setNeighbors(peerConnections);
    }

    public static void disconnectFromPeers(){
        serverRunnable.closeConnection();
    }

    public static void exitNetwork(){
        disconnectFromPeers();
        running = false;
        serverRunnable.closeConnection();
        System.out.println("Exiting...");
    }

    public static String queryPeer(String fileName, Socket socket){
        int qId = (int)(Math.random() * 10000);
        PrintWriter outToClient;
        try {
            outToClient = new PrintWriter(socket.getOutputStream(),true);
            String queryMessage = "Q:" + qId + ";" + fileName;
            System.out.println("Sending query for " + fileName + " to " + socket.getInetAddress().getHostAddress());
            outToClient.println(queryMessage);
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream())); //peer will query its neighbors if it does not have the file
            String clientResponse = inFromClient.readLine();
            System.out.println(clientResponse);
            return clientResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "File not found";
    }
}
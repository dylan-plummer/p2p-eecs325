import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class p2p {
    public static final int START_PORT = 50600;
    public static final int END_PORT = 50619;

    private static Thread serverThread;
    private static ServerRunnable serverRunnable;
    private static boolean running = true;
    private static String fileName;
    public static ArrayList<String> addressList;
    public static ArrayList<Socket> peerConnections = new ArrayList<>();

    public static void main(String[] args){
        System.out.println("Starting up peer...");
        addressList = fillAddresses("config_neighbors.txt");
        System.out.println(addressList.toString());
        serverRunnable = new ServerRunnable();
        serverThread = (new Thread(serverRunnable));
        serverThread.start();
        while(running){
            runCommand(getCommand()); //run user input
        }
    }

    public static ArrayList<String> fillAddresses(String file){
        ArrayList<String> addressList = new ArrayList<String>();
        File addressFile = new File (file);
        Scanner scanner = null;
        try {
            scanner = new Scanner(addressFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scanner.hasNext()){
            addressList.add(scanner.nextLine());
        }
        return addressList;
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
                if (queryPeer(fileName,socket)){
                    System.out.println("");
                    //TODO get file
                }
            }
        }
        else{
            System.out.println("Command not recognized");
        }
    }

    public static void connectToPeers(){
        for (String address:addressList) {
            try {
                System.out.println("Connecting to: " + address);
                peerConnections.add(new Socket(address,START_PORT));
                System.out.println("Connected to: " + address);
            } catch (IOException e) {
                System.out.println("Unable to connect");
                e.printStackTrace();
            }
        }
    }

    public static void disconnectFromPeers(){
        for(Socket socket:peerConnections){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void exitNetwork(){
        disconnectFromPeers();
        running = false;
        serverRunnable.closeConnection();
        System.out.println("Exiting...");
    }

    public static boolean queryPeer(String fileName, Socket socket){
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
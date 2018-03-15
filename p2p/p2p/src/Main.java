import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final int START_PORT = 50600;
    private static final int END_PORT = 50619;
    private static boolean running = true;

    private static String fileName;
    public static ArrayList<String> addressList;
    public static ArrayList<Socket> peerConnections = new ArrayList<>();

    public static void main(String[] args){
        addressList = fillAddresses("config_neighbors.txt");
        System.out.println(addressList.toString());
        Thread getFile = new Thread(fileName);
            for(Socket socket:peerConnections){
                if(queryPeer(fileName,socket)){

                }
            }
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
        return scanner.next().toLowerCase();
    }

    public static void runCommand(String command){
        switch (command){
            case ("connect"): connectToPeers();
                break;
            case ("leave"): disconnectFromPeers();
                break;
            case ("exit"): exitNetwork();
                break;
            default: runFileQuery();
                break;
        }
    }

    public static void runFileQuery(){

    }

    public static void connectToPeers(){
        for (String address:addressList) {
            try {
                peerConnections.add(new Socket(address,START_PORT));
            } catch (IOException e) {
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
        System.out.println("Exiting...");
    }

    public static boolean queryPeer(String fileName, Socket socket){
        int qId = (int)(Math.random() * 10000);
        DataOutputStream  outToClient;
        try {
            outToClient = new DataOutputStream(socket.getOutputStream());
            String queryMessage = "Q:" + qId + ";" + fileName;
            outToClient.writeBytes(queryMessage);
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream())); //peer will query its neighbors if it does not have the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
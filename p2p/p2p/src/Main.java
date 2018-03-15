import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final int START_PORT = 50600;
    private static final int END_PORT = 50619;
    private static boolean running = true;

    private static ArrayList<String> addressList;
    private static ArrayList<Socket> peerConnections = new ArrayList<>();

    public static void main(String[] args){
        addressList = fillAddresses("config_neighbors.txt");
        System.out.println(addressList.toString());
        while(running){
            runCommand(getCommand());
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
            case ("leave"): disconnectFromPeers();
            case ("exit"): exitNetwork();
        }
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

    }

    public static void exitNetwork(){
        running = false;
        System.out.println("Exiting...");
    }
}
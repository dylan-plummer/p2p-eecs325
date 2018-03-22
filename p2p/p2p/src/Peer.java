import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jumpr on 3/19/2018.
 */
public class Peer {
    private String address;
    private int port;
    private ArrayList<Peer> neighbors;
    private ArrayList<Socket> connections;
    private boolean neighborsNeeded;


    public Peer(String address, int port, boolean neighborsNeeded) {
        this.address = address;
        this.port = port;
        this.neighborsNeeded = neighborsNeeded;
        if(neighborsNeeded) {
            neighbors = fillNeighbors("_config_neighbors.txt");
        }
    }

    public static ArrayList<Peer> fillNeighbors(String file){
        ArrayList<Peer> addressList = new ArrayList<Peer>();
        File addressFile = new File (file);
        Scanner scanner = null;
        try {
            scanner = new Scanner(addressFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scanner.hasNext()){
            String address = scanner.nextLine();
            addressList.add(new Peer(getAddressFromConfig(address),getPortFromConfig(address), false));
        }
        return addressList;
    }

    public static String getAddressFromConfig(String line){
        return line.substring(0,line.indexOf(':'));
    }
    public static int getPortFromConfig(String line){
        return Integer.parseInt(line.substring(line.indexOf(':')+1));
    }

    public ArrayList<Socket> makeConnections() {
        connections = new ArrayList<>();
        for(Peer neighbor:neighbors){
            System.out.println("Connecting to "+neighbor.getAddress());
            Socket socket = null;
            try {
                socket = new Socket(neighbor.getAddress(),neighbor.getPort());
                socket.setSoTimeout(p2p.TIMEOUT);
                System.out.println("Connection to " + neighbor.getAddress() + " successful");
                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println("Connection from "+ neighbor.getAddress());
                connections.add(socket);
            } catch (IOException e) {
                System.out.println("Failed to connect to "+neighbor.getAddress() + ".  Are you sure they are online?");
            }
        }
        return connections;
    }

    public String queryNeighbors(String fileName, String address){
        if(this.getConnections()!=null) {
            System.out.println("Checking for "+ fileName);
            for (Socket socket : this.getConnections()) {
                if(socket.getInetAddress().getHostAddress().equals(address)){
                    System.out.println("Don't query original sender");
                }
                else {
                    String queryResponse = queryPeer(fileName, socket);
                    if (queryResponse == null) {
                        System.out.println("Peer " + socket.getInetAddress().toString() + " does not have file " + fileName);
                    } else if (queryResponse.equals("File not found")) {
                        System.out.println("Peer " + socket.getInetAddress().toString() + " does not have file " + fileName);
                    } else if (queryResponse.equals("Peer already queried")) {
                        System.out.println("Peer " + socket.getInetAddress().toString() + " already queried");
                    } else {
                        return queryResponse;
                    }
                }
            }
        }
        return "";
    }
    public String getAddressFromResponse(String response){
        response = response.substring(2);
        return response.substring(response.indexOf(';')+1,response.indexOf(':'));
    }
    public int getPortFromResponse(String response){
        response = response.substring(2);
        return Integer.parseInt(response.substring(response.indexOf(':')+1));
    }

    public void downloadFile(String fileName, String address, int port){
        new Thread(new DownloadRunnable(fileName, address,port)).start();
    }

    public static String queryPeer(String fileName, Socket socket){
        if(socket.isClosed()){
            return "Peer already queried";
        }
        else {
            int qId = (int) (Math.random() * 10000);
            PrintWriter outToClient;
            try {
                outToClient = new PrintWriter(socket.getOutputStream(), true);
                String queryMessage = "Q:" + qId + ";" + fileName;
                System.out.println("Sending query for " + fileName + " to " + socket.getInetAddress().toString());
                outToClient.println(queryMessage);
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream())); //peer will query its neighbors if it does not have the file
                String clientResponse = inFromClient.readLine();
                System.out.println(clientResponse);
                //outToClient.flush();
                return clientResponse;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "File not found";
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public ArrayList<Peer> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Peer> neighbors) {
        this.neighbors = neighbors;
    }

    public ArrayList<Socket> getConnections() {
        return connections;
    }

    public String toString(){
        return this.getAddress()+":"+this.getPort();
    }
}

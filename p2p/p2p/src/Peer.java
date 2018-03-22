import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Peer {
    private String address;
    private int port;
    private ArrayList<Peer> neighbors;
    private ArrayList<Socket> connections;


    public Peer(String address, int port, boolean neighborsNeeded) {
        this.address = address;
        this.port = port;
        if(neighborsNeeded) {
            neighbors = fillNeighbors("_config_neighbors.txt");
        }
    }

    //adds neighbors to list of peers
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

    //connects to all neighbors if possible
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

    //query all neighbors for a certain file
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
                    } else if (queryResponse.equals(p2p.FILE_NOT_FOUND)) {
                        System.out.println("Peer " + socket.getInetAddress().toString() + " does not have file " + fileName);
                    } else if (queryResponse.equals(p2p.PEER_QUERIED)) {
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

    //start a new download thread to get the file
    public void downloadFile(String fileName, String address, int port){
        new Thread(new DownloadRunnable(fileName, address,port)).start();
    }

    //query a specific peer for a file
    public static String queryPeer(String fileName, Socket socket){
        if(socket.isClosed()){
            return p2p.PEER_QUERIED;
        }
        else {
            int qId = (int) (Math.random() * 10000);
            PrintWriter outToClient;
            try {
                outToClient = new PrintWriter(socket.getOutputStream(), true);
                String queryMessage = "Q:" + qId + ";" + fileName;
                System.out.println("Sending query for " + fileName + " to " + socket.getInetAddress().toString());
                outToClient.println(queryMessage);
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String clientResponse = inFromClient.readLine();
                System.out.println(clientResponse);
                return clientResponse;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return p2p.FILE_NOT_FOUND;
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

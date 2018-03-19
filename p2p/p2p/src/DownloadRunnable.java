import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jumpr on 3/15/2018.
 */
public class DownloadRunnable implements Runnable{

    private String fileName;
    private String address;
    private int port;

    private static boolean serverRunning = true;
    private static boolean initConnection = true;
    private static ServerSocket serverSocket;
    private Socket connectionSocket;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;

    public DownloadRunnable(String fileName, String address, int port){
        this.fileName = fileName;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            System.out.println(address);
            Socket downloadSocket = new Socket(address, p2p.END_PORT);
            PrintWriter outToClient;
            outToClient = new PrintWriter(downloadSocket.getOutputStream(),true);
            String queryMessage = "T:" + fileName;
            System.out.println("Sending request for " + fileName + " to " + downloadSocket.getInetAddress().getHostAddress());
            outToClient.println(queryMessage);
            int count;
            InputStream is = downloadSocket.getInputStream();
            FileOutputStream fos = new FileOutputStream("obtained/" + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            while ((count = is.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
            bos.close();
            downloadSocket.close();
            System.out.println("File downloaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
}

import java.io.*;
import java.net.Socket;

public class DownloadRunnable implements Runnable{

    private String fileName;
    private String address;
    private int port;

    public DownloadRunnable(String fileName, String address, int port){
        this.fileName = fileName;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket downloadSocket = new Socket(address, port);
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
            bos.flush();
            bos.close();
            fos.flush();
            fos.close();
            downloadSocket.close();
            System.out.println("File downloaded");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

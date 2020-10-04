import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProxyImplementation extends Thread {

    public Socket clientSocket;

    public ProxyImplementation(Socket socket) {
        clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader FromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String sentence = FromServer.readLine();
            if(sentence == null || !sentence.contains("GET")) {
                clientSocket.close();
            }
            else {
                URL url = new URL(sentence.split(" ")[1]);
                String getHost = url.getHost();
                Socket serverSocket = new Socket();
                serverSocket.connect(new InetSocketAddress(getHost, 80));
                getResponseMessage(url, serverSocket);
                logger(serverSocket, url);
                serverSocket.close();
                clientSocket.close();
            }
        }
        catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private synchronized void getResponseMessage(URL url, Socket s) throws Exception {
        String urlHost = url.getHost();
        String urlFile = url.getFile();
        PrintWriter outToServer = null;
        PrintWriter clientOut = null;
        BufferedReader serverIn = null;
        String fileName = url.toString()
                .replace("/", "-").replace(":", "-")
                .replace("+", "-").replace("?", "-")
                .replace("=", "-").replace("&", "-")
                .replace("%", "-").replace("~", "-");
        String fileSubstring = fileName.substring(7);
        try {
            outToServer = new PrintWriter(s.getOutputStream(), true);
            clientOut = new PrintWriter(clientSocket.getOutputStream(),true);
            serverIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        }
        catch (UnknownHostException e) {
            System.err.println("Invalid Host: " + urlHost);
            System.exit(1);
        }

        String message = "GET "+ urlFile + " HTTP/1.0";
        outToServer.println( message );
        outToServer.println(String.format("Host: %s", urlHost));
        outToServer.println("");

        StringBuffer response = new StringBuffer();
        String inputLine;
        if(urlFile.contains(".jpg") || urlFile.contains(".png") || urlFile.contains(".gif") || urlFile.contains(".ttf") || urlFile.contains(".ico")
                || urlFile.contains(".GIF") || urlFile.contains(".swf") || urlFile.contains(".JPG") || urlFile.contains(".PNG")
                || urlFile.contains(".jpeg") || urlFile.contains(".JPEG")) {
            imageLoader(s.getInputStream(), clientSocket.getOutputStream(), s);
        }
        else {
            if(fileSubstring.length() < 220) {
                File f = new File(fileSubstring + ".txt");
                if(!f.exists()) {
                    f.createNewFile();
                    BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(f.getName(), true));

                    while ((inputLine = serverIn.readLine()) != null) {
                        response.append(inputLine + "\n");
                        clientOut.println(inputLine);
                        bufferWriter.write(inputLine);
                        bufferWriter.newLine();
                    }
                    bufferWriter.close();
                    System.out.println(response.toString());
                    System.out.println("Caching Successful");
                }
                else {
                    System.out.println("Loading URL: " + url.toString() + " from cache." );
                    BufferedReader bufferReader = new BufferedReader(new FileReader(f.getName()));
                    inputLine = null;
                    while ((inputLine = bufferReader.readLine()) != null) {
                        response.append(inputLine);
                        clientOut.println(inputLine);
                    }
                    bufferReader.close();
                }
            }
            outToServer.flush();
            clientOut.flush();
            serverIn.close();
        }
    }

    public synchronized void imageLoader(InputStream imageInFromServer, OutputStream imageOutToClient, Socket s) throws IOException {
        int getBufferSize = s.getReceiveBufferSize();
        byte [] imageBuffer = new byte[getBufferSize];
        int readTheBytes;
        while ((readTheBytes = imageInFromServer.read(imageBuffer)) != -1) {
            imageOutToClient.write(imageBuffer, 0, readTheBytes);
            imageOutToClient.flush();
        }
        imageInFromServer.close();
    }

    public static synchronized void logger(Socket socket, URL url) throws Exception {
        Date d = new Date();
        SimpleDateFormat s = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
        try {
            File file = new File("proxy.log");

            if(!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(file.getName(), true));
            bufferWriter.append(s.format(d) + ":\t" + socket.getInetAddress().getHostAddress() + "\t\t"+ url.toString());
            bufferWriter.newLine();
            bufferWriter.close();
        }
        catch(Exception ex) {
            System.out.println("Unable to create file: " + ex.getMessage());
        }
    }
}
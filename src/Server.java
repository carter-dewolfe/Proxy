import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(5678);
        try {
            while(true) {
                new ProxyImplementation(socket.accept()).start();
            }
        }
        finally {
            socket.close();
        }
    }
}
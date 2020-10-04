import java.io.*;
import java.net.*;

/*
Resources:
https://github.com/stefano-lupo/Java-Proxy-Server
https://github.com/Tyler-Losinski/Chaka-Proxy
https://github.com/pattibabie/ProxyServer-Java
https://github.com/sundychenD/HTTPProxy
https://github.com/adamfisk/LittleProxy

Note: these were used as assistance to help create my own proxy server
 */

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
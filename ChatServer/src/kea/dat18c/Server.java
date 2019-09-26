package kea.dat18c;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;

    private ArrayList<ServerHandler> handlerList = new ArrayList<ServerHandler>();

    public Server(int serverPort)
    {
        this.serverPort = serverPort;
    }

    public List<ServerHandler> getHandlerList()
    {
        return handlerList;
    }

    /**
     * run method, Creates Serversocket that is listing on a port.
     * Infinite loop that accepts incoming connections.
     * Creates a Serverhandler that handles the communication with the clientsocket.
     */
     @Override
     public void run()
     {
         try
         {
             ServerSocket serverSocket = new ServerSocket(serverPort);
             while(true)
             {
                 System.out.println("Incoming connection...");
                 Socket clientSocket = serverSocket.accept();
                 System.out.println("Accepted connection from " + clientSocket);
                 ServerHandler handler = new ServerHandler(this, clientSocket);
                 handlerList.add(handler);
                 handler.start();
             }
         } catch (IOException e)
         {
             e.printStackTrace();
         }





     }

    /**
     * Removes a serverhandler from the list when they quit.
     * @param serverHandler
     */
    public void removeHandler(ServerHandler serverHandler) {
        handlerList.remove(serverHandler);
    }
}


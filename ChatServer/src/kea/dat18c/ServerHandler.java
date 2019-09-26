package kea.dat18c;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class ServerHandler extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private OutputStream outputStream;
    private String JOIN = null;


    public ServerHandler(Server server, Socket clientSocket)
    {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        try
        {
            handleClientSocket();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * handle client sockets
     * while loop that takes input and output from the user.
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleClientSocket() throws IOException, InterruptedException
    {


        InputStream inputStream = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        this.outputStream = clientSocket.getOutputStream();
        String line;

            while ((line = reader.readLine()) != null)
            {
                String[] tokens = line.split(" ");
                if (tokens != null && tokens.length > 0)
                {
                    String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd))
                {
                    handleLogout();
                    break;
                } else if("JOIN".equalsIgnoreCase(cmd))
                {
                    handleLogin(outputStream, tokens);
                }
                else if ("DATA".equalsIgnoreCase(cmd))
                {
                    String[] tokensMsg = line.split(" ", 3);
                    handleMessage(tokensMsg);
                }
                else
                {
                    String msg = "unknown " + cmd + "\n";
                }
            }

        }

        clientSocket.close();
    }

    /**
     * Message handling
     * @param tokens
     * @throws IOException
     */
    // format "message" "username" message
    private void handleMessage(String[] tokens) throws IOException
    {
        String receiver = tokens[1];
        String txtbody = tokens[2];

        List<ServerHandler> handlerList = server.getHandlerList();

        for(ServerHandler worker : handlerList)
        {
            if (receiver.equalsIgnoreCase(worker.getJOIN()))
            {
                String outMsg = "message from: " + JOIN + " " + txtbody + "\n";
                worker.send(outMsg);
            }
        }
    }

    /**
     * Logout handling, removes online user from the list if they quit and close the  socket.
     * @throws IOException
     */
    private void handleLogout() throws IOException
    {
        server.removeHandler(this);

        List<ServerHandler> handlerList = server.getHandlerList();

        String onlineCheck = "offline " + JOIN + "\n";

        for(ServerHandler handler : handlerList)
        {
            if (!JOIN.equals(handler.getJOIN()))
            {
                handler.send(onlineCheck + "\n");
            }
        }
        clientSocket.close();
    }

    public String getJOIN()
    {
        return JOIN;
    }

    /**
     * handling logins, 2 hardcorded users (didnt know how else to do it)
     * Tells other user when the other person is or comes online.
     * @param outputStream
     * @param tokens
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException, InterruptedException
    {
        if (tokens.length == 3)
        {
            String JOIN = tokens[1];
            String password = tokens[2];

            if (JOIN.equals("Nicklas") && password.equals("123") || JOIN.equals("Frederik") && password.equals("1234"))
            {
                String message = "J_OK\n";
                outputStream.write(message.getBytes());
                this.JOIN = JOIN;
                System.out.println("User logged in " + JOIN);



                List<ServerHandler> handlerList = server.getHandlerList();

                for(ServerHandler worker : handlerList)
                {
                        if (worker.getJOIN() != null)
                        {
                            if (!JOIN.equals(worker.getJOIN()))
                            {
                                String onlineCheck2 = "online " + worker.getJOIN() + "\n";
                                send(onlineCheck2 + "\n");
                            }
                    }
                }
                String onlineCheck = "online " + JOIN + "\n";
                for(ServerHandler worker : handlerList)
                {
                    if (!JOIN.equals(worker.getJOIN()))
                    {
                        worker.send(onlineCheck + "\n");
                    }
                }
            }
            else
            {
               String message = "Wrong Password or Username\n";
               outputStream.write(message.getBytes());
            }
        }
    }

    private void send(String onlineCheck) throws IOException
    {
        if(JOIN != null)
        {
            outputStream.write(onlineCheck.getBytes());
        }
    }

}


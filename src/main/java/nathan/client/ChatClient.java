package client;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Scanner;
import java.util.Vector;
import java.util.Vector;

import java.util.ArrayList;
import java.util.InputMismatchException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

import java.io.PrintStream;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import compute.*;


public class ChatClient {

    static String userName;
    static PresenceService service;
    static Thread acceptServerThread;
    static Thread acceptClientThread;
    static ZMQ.Socket repSocket;
    static ZMQ.Socket subSocket;

    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {

            // Argument checking
            if (args.length < 1 || args.length > 2) {
                System.out.println("Invalid input");
                System.out.println("Usage: ChatClient (userName) [host[:port]]");
                System.exit(1);
            }


            // Input from command line
            Scanner scanner = new Scanner(System.in);
            System.out.println("Input receiving port:");
            int port = scanner.nextInt();
            

            // RMI hard-coded params
            String registryName = "Chat";
            userName = args[0];

            String rmihost = InetAddress.getLocalHost().getHostAddress();
            int rmiport = 1099;


            // If they exist, get program args, otherwise
            // use defaults
            if (args.length > 1) {
                String passedHost = args[1];
                String[] splitHost = passedHost.split(":");
                if (splitHost.length > 1) {
                    rmihost = splitHost[0];
                    rmiport = Integer.parseInt(splitHost[1]);
                }
                else {
                    rmihost = passedHost;
                }
            }


            // Get my IP/Port, and concatenate them into addresses
            String host = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Your address: " + host + ":" + port + "\n");
            String rmiAddress = "tcp://" + rmihost + ":" + rmiport;
            String myAddress = "tcp://" + host + ":" + port;


            // Create client profile
            RegistrationInfo reg = new RegistrationInfo(userName, host, port, true);


            // ZeroMQ Context to manage sockets
            ZContext context = new ZContext();

            // ZeroMQ socket to receive from other clients
            repSocket = context.createSocket(ZMQ.REP);
            repSocket.bind(myAddress);

            // ZeroMQ socket to listen to server broadcasts
            System.out.println("Subscribing to tcp://" + rmihost + ":9999");
            subSocket = context.createSocket(ZMQ.SUB);
            subSocket.connect("tcp://" + rmihost + ":" + "9999");
            subSocket.subscribe("".getBytes());


            // Kick off listening threads
            acceptServerThread = new Thread(new ProcessIncomingServerMessage(subSocket, userName));
            acceptServerThread.start();

            acceptClientThread = new Thread(new ProcessIncomingClientMessage(repSocket));
            acceptClientThread.start();


            Registry registry;
            // Register client with server
            try {
                registry = LocateRegistry.getRegistry(rmihost, rmiport);
                service = (PresenceService) registry.lookup(registryName);
                service.register(reg);
            } catch (Exception e) {
                System.out.println("Registry not currently bound");
                System.exit(0);
            }


            // insert shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    System.out.println("Unregistering client with server...");
                    try {
                        service.unregister(userName);
                    } catch (RemoteException e) {
                        System.out.println("Remote Exception when unregistering user");
                    }

                    System.out.println("Closing sockets...");
                    repSocket.close();
                    subSocket.close();

                    System.out.println("Killing threads...");
                    acceptClientThread.interrupt();
                    acceptServerThread.interrupt();
                }
            });
            

            while (true) {

                // Get user input
                System.out.println("\n\n----------\n0: friends\n1: talk\n2: broadcast\n3: busy\n4: available\n5: exit");
                int userChoice = 6;
                try {
                    userChoice = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input");
                }
                // Consume \n
                scanner.nextLine();
                System.out.println();
                

                switch (userChoice) {
                    case 0:
                        System.out.println("Friends:");
                        Vector<RegistrationInfo> userList = service.listRegisteredUsers();
                        for (RegistrationInfo RI : userList) {
                            if (!RI.getUserName().equals(userName))
                                System.out.println("User: " + RI.getUserName());
                        }
                        break;
                    case 1:
                        System.out.println("Talk: to who?");
                        String targetUserName = scanner.nextLine();
                        RegistrationInfo targetRI = service.lookup(targetUserName);

                        if (targetRI == null) {
                            System.out.println("User does not exist");
                        } else if (targetRI.getStatus()) {

                            String message = scanner.nextLine();

                            // ZeroMQ socket to send to other clients
                            ZMQ.Socket reqSocket = context.createSocket(ZMQ.REQ);
                            reqSocket.connect("tcp://" + targetRI.getHost() + ":" + targetRI.getPort());
                            reqSocket.send(userName + ": " + message);
                            reqSocket.close();

                        } else if (!targetRI.getStatus()) {
                            System.out.println("User busy");
                        }
                        break;
                    case 2:
                        System.out.println("Broadcast: message?");
                        String message = scanner.nextLine();
                        service.broadcast(userName + ": " + message);
                        break;
                    case 3:
                        System.out.println("Setting status as busy");
                        service.updateRegistrationInfo(new RegistrationInfo(userName, host, port, false));
                        break;
                    case 4:
                        System.out.println("Setting status as available");
                        service.updateRegistrationInfo(new RegistrationInfo(userName, host, port, true));
                        break;
                    case 5:
                        System.exit(0);
                    default:
                        System.out.println("Invalid input");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Chat Client exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
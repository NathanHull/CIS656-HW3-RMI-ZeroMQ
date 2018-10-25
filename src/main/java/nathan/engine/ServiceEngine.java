package engine;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.net.InetAddress;

import compute.RegistrationInfo;
import compute.PresenceService;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Collections;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;


public class ServiceEngine implements PresenceService {

    static Map<String, RegistrationInfo> clients = new HashMap<>();
    static ZMQ.Socket pubSocket;


    public ServiceEngine() {
        super();
    }


    public static void main(String[] args) {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {

            int port = 1099;
            if (args.length > 0)
                port = Integer.parseInt(args[0]);


            // Set up ZMQ publisher socket
            String host = InetAddress.getLocalHost().getHostAddress();
            ZContext context = new ZContext();
            pubSocket = context.createSocket(ZMQ.PUB);
            pubSocket.bind("tcp://" + host + ":" + 9999);
            System.out.println("Publisher socket bound on tcp://" + host + ":9999");

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    System.out.println("Closing socket...");
                    pubSocket.close();
                }
            });


            String name = "Chat";
            PresenceService engine = new ServiceEngine();
            PresenceService stub =
                    (PresenceService) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind(name, stub);
            System.out.println("Presence RMI bound");

        } catch (Exception e) {
            System.err.println("Presence Service exception:");
            e.printStackTrace();
        }
    }


    /**
     * Register a client with the presence service.
     * @param reg The information that is to be registered about a client.
     * @return true if the user was successfully registered, or false if somebody
     * the given name already exists in the system.
     */
    public boolean register(RegistrationInfo reg) throws RemoteException {
        if (clients.containsKey(reg.getUserName()))
            return false;

        clients.put(reg.getUserName(), reg);
        return true;
    }


    /**
     * Updates the information of a currently registered client.
     * @param reg The updated registration info.
     * @return true if successful, or false if no user with the given
     * name is registered.
     *
     */
    public boolean updateRegistrationInfo(RegistrationInfo reg) throws RemoteException {
        if (clients.replace(reg.getUserName(), reg) == null)
            return false;

        return true;
    }


    /**
     * Unregister a client from the presence service.  Client must call this
     * method when it terminates execution.
     * @param userName The name of the user to be unregistered.
     */
    public void unregister(String userName) throws RemoteException {
        clients.remove(userName);
    }


    /**
     * Lookup the registration information of another client.
     * @name The name of the client that is to be located.
     * @return The RegistrationInfo info for the client, or null if
     * no such client was found.
     */
    public RegistrationInfo lookup(String name) throws RemoteException {
        return clients.get(name);
    }


    /**
     * Determine all users who are currently registered in the system.
     * @return An array of RegistrationInfo objects - one for each client
     * present in the system.
     */
    public Vector<RegistrationInfo> listRegisteredUsers() throws RemoteException {
        return new Vector<RegistrationInfo>(clients.values());
    }


    /**
     * Have server broadcast passed message to all clients that
     * are ZeroMQ subscribers
     * @param message The message to broadcast
     */
    public void broadcast(String message) throws RemoteException {
        pubSocket.send(message);
    }
}

package client;


import org.zeromq.ZMQ;


public class ProcessIncomingServerMessage implements Runnable {

    private ZMQ.Socket receiveSocket;
    private String userName;


    public ProcessIncomingServerMessage(ZMQ.Socket receiveSocket, String userName) {
        super();
        this.receiveSocket = receiveSocket;
        this.userName = userName;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String message = receiveSocket.recvStr();
            if (!message.split(":")[0].equals(userName)) {
                System.out.println("[Broadcast from server] " + receiveSocket.recvStr());
            }
        }
    }
}

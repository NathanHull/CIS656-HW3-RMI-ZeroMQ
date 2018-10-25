package client;


import org.zeromq.ZMQ;


public class ProcessIncomingClientMessage implements Runnable {

    private ZMQ.Socket receiveSocket;


    public ProcessIncomingClientMessage(ZMQ.Socket receiveSocket) {
        super();
        this.receiveSocket = receiveSocket;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("[Direct message] " + receiveSocket.recvStr());
            receiveSocket.send("Message received");
        };
    }
}

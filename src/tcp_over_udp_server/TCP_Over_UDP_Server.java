package tcp_over_udp_server;

import Client.Client;
import Client.SelectiveRepeat;
import Client.StopWait;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class TCP_Over_UDP_Server {

    public static void main(String[] args) throws SocketException, IOException {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        ArrayList<Client> clients = new ArrayList<Client>();
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("Listen!!");
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED!!: [" +sentence.substring(0,sentence.indexOf(0))+"]");
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
       //     System.out.println("Start Client "+IPAddress.getHostName()+" "+port);
//            new StopWait(IPAddress, port, sentence);
            System.out.println("hahahahhahahah");
            clients.add( new SelectiveRepeat(IPAddress, port, sentence.substring(0,sentence.indexOf(0)),1, (float) 0.3));
            receiveData = new byte[1024];
            sendData = new byte[1024];
        }
    }

}

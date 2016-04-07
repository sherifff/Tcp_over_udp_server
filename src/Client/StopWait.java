package Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import packet.AckPacket;
import packet.DataPacket;
import packet.Packet;

public class StopWait extends Client{

    
    public StopWait(InetAddress clientIp, int clientPort, String fileName, float prob) throws FileNotFoundException {
        this.clientPort = clientPort;
        this.clientIp = clientIp;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(StopWait.class.getName()).log(Level.SEVERE, null, ex);
        }
        File file = new File("omar.mp3");
        input = new FileInputStream(file);

        numOfChuncks = (int) Math.ceil(((double) file.length() / (double) 504));
        this.prob = prob;
        if (!file.exists()) {
            System.out.println("File Not Found!!");
        }
        System.out.println("File Transfering  "+numOfChuncks);
        Thread sendThread = new Thread(new SW_SendThread());
        sendThread.start();
    }

    void startTimer(Packet dataPacket) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                udt_send_byte(dataPacket,"");
                startTimer(dataPacket);
            }
        }, 2000);
    }
    
    public class SW_SendThread implements Runnable {

        @Override
        public void run() {
            try {
                    int seqNo = 0;
                    while (true) {
                        Packet lengthPacket = new DataPacket((short) 10, (short) String.valueOf(numOfChuncks).length(), seqNo, String.valueOf(numOfChuncks));
                        udt_send(lengthPacket);
                        startTimer(lengthPacket);
                        
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        datagramSocket.receive(receivePacket);
                        AckPacket ackPacket = new AckPacket(receivePacket.getData());
                        if (!corrupt(ackPacket) && isACK(ackPacket, seqNo)) {
                            timer.cancel();
                            break;
                        }
                    }
                    seqNo = (seqNo == 1 ? 0 : 1);
                    
                    byte[] chunk = new byte[504];
                    int read = input.read(chunk);
                    while (read != -1) {
                        // check sum calcualte
                        Packet sendPacket = new DataPacket((short) 10, (short) read, seqNo, chunk);
                        udt_send_byte(sendPacket,"");
                        startTimer(sendPacket);

                        while (true) {
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            datagramSocket.receive(receivePacket);
                            AckPacket ackPacket = new AckPacket(receivePacket.getData());
                            if (!corrupt(ackPacket) && isACK(ackPacket, seqNo)) {
                                timer.cancel();
                                break;
                            }
                        }
                        seqNo = (seqNo == 1 ? 0 : 1);
                        //send chunk
                        read = input.read(chunk);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(StopWait.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import packet.AckPacket;
import packet.DataPacket;
import packet.Packet;

/**
 *
 * @author OmarElfarouk
 */
public class Client {

    protected DatagramSocket datagramSocket;
    protected int clientPort;
    protected InetAddress clientIp;
    protected byte[] receiveData = new byte[512];
    protected Timer timer;
    protected FileInputStream input;
    protected int numOfChuncks;
    protected float prob;
    protected Random random = new Random(2);
    
    boolean getRandomBoolean(){
    return !(random.nextFloat() < prob);
//    return true;
    }
    
    void udt_send(Packet packet) {
        try {
            DatagramPacket sendPacket
                    = new DatagramPacket(packet.getDataPacket(), 512, clientIp, clientPort);
            if(getRandomBoolean())
                datagramSocket.send(sendPacket);
            //System.out.println("Send Packet " + clientIp.getHostName() + " " + clientPort + "== " + ((DataPacket) packet).getSeqno());
        } catch (IOException ex) {
            Logger.getLogger(StopWait.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void udt_send_byte(Packet packet,String debug) {
        try {
            DatagramPacket sendPacket
                    = new DatagramPacket(((DataPacket)packet).getDataPacketByte(), 512, clientIp, clientPort);
           System.out.print(debug + " wants to send " + ((DataPacket)packet).getSeqno());
            if(getRandomBoolean()){
            System.out.println(" success");
            	datagramSocket.send(sendPacket);
            }else{
            	System.out.println(" failed");
            }
            //System.out.println("Send Packet " + clientIp.getHostName() + " " + clientPort + "== " + ((DataPacket) packet).getSeqno());
        } catch (IOException ex) {
            Logger.getLogger(StopWait.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    boolean corrupt(AckPacket ackPacket) {
        return false;
    }

    boolean isACK(AckPacket ackPacket, int ackNum) {
        return ackNum == ackPacket.getAckNum();
    }

    
}

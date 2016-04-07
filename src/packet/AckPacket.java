/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packet;

import java.nio.ByteBuffer;

/**
 *
 * @author OmarElfarouk
 */
public class AckPacket extends Packet {

    private int ackNum;

    public AckPacket(short checkSum, short length, int ackNum) {
        this.checksum = checkSum;
        this.length = length;
        this.ackNum = ackNum;
    }

    public AckPacket(byte AckPacket[]) {
        byte ackNumb[] = new byte[4];
        ackNumb[0] = AckPacket[0];
        ackNumb[1] = AckPacket[1];
        ackNumb[2] = AckPacket[2];
        ackNumb[3] = AckPacket[3];
        byte checksumb[] = new byte[2];
        checksumb[0] = AckPacket[4];
        checksumb[1] = AckPacket[5];
        byte lengthb[] = new byte[2];
        lengthb[0] = AckPacket[6];
        lengthb[1] = AckPacket[7];
        this.ackNum = ByteBuffer.wrap(ackNumb).getInt();
        this.checksum = ByteBuffer.wrap(checksumb).getShort();
        this.length = ByteBuffer.wrap(lengthb).getShort();
    }

    public int getAckNum() {
        return ackNum;
    }

    public void setAckNum(int ackNum) {
        this.ackNum = ackNum;
    }

}

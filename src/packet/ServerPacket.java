/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packet;

import java.util.Timer;

/**
 *
 * @author OmarElfarouk
 */
public class ServerPacket {
    private DataPacket dataPacket;
    private Timer timer;
    private boolean ack;

    public ServerPacket(DataPacket dataPacket, Timer timer, boolean ack) {
        this.dataPacket = dataPacket;
        this.timer = timer;
        this.ack = ack;
    }

    public DataPacket getDataPacket() {
        return dataPacket;
    }

    public void setDataPacket(DataPacket dataPacket) {
        this.dataPacket = dataPacket;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author OmarElfarouk
 */
public class SelectiveRepeat extends Client {

	int seqNo;
	Base base;
	int wndSize;
	//boolean acks[];
	Timer timers[];
	DataPacket packetsInWindow[];
	File file;
	Thread sendThread;
	Thread ackRcvThread;

	int maxWNDSize;

	public SelectiveRepeat(InetAddress clientIp, int clientPort,
			String fileName, int maxWNDSize, float prob)
			throws FileNotFoundException {
		this.clientPort = clientPort;
		this.clientIp = clientIp;
		try {
			datagramSocket = new DatagramSocket();
		} catch (SocketException ex) {
			Logger.getLogger(StopWait.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		timers = new Timer[maxWNDSize];
		packetsInWindow = new DataPacket[maxWNDSize];

		base = new Base(0,new boolean[maxWNDSize]);
		seqNo = 0;
		this.maxWNDSize = maxWNDSize;
		//this.acks = new boolean[maxWNDSize];
		this.wndSize = maxWNDSize;

		file = new File(fileName);
		input = new FileInputStream(file);
		this.prob = prob;
		numOfChuncks = (int) Math.ceil(((double) file.length() / (double) 504));

		while (true) {
			try {
				Packet lengthPacket = new DataPacket((short) 10, (short) String
						.valueOf(numOfChuncks).length(), seqNo,
						String.valueOf(numOfChuncks));
				udt_send(lengthPacket);
				startTimerSW(lengthPacket);

				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				datagramSocket.receive(receivePacket);
				AckPacket ackPacket = new AckPacket(receivePacket.getData());
				if (!corrupt(ackPacket) && isACK(ackPacket, seqNo)) {
					timer.cancel();
					timer.purge();
					break;
				}
			} catch (IOException ex) {
				Logger.getLogger(SelectiveRepeat.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
		seqNo = 1;
		base.num = 1;

		if (!file.exists()) {
			System.out.println("File Not Found!!");
		}

		sendThread = new Thread(new SR_SendThread());
		ackRcvThread = new Thread(new SR_RcvAckThread());
		sendThread.start();
		ackRcvThread.start();
	}

	public class SR_SendThread implements Runnable {

		@Override
		public void run() {
			try {
				byte[] chunk = new byte[504];
				int read = input.read(chunk);
				System.out.println("Transfer Started!");

				while (read != -1) {
					synchronized (base) {
						while (seqNo < base.num + wndSize && read != -1) {
							short pktLength = 0;
							if (seqNo == numOfChuncks) {
								pktLength = (short) (file.length() - ((numOfChuncks - 1) * 504));
								System.out.println("LastPkt= " + pktLength);
							} else {
								pktLength = (short) chunk.length;
							}
							DataPacket sendPacket = new DataPacket((short) 10,
									pktLength, seqNo, chunk);
							udt_send_byte(sendPacket, "send thread");
							if (seqNo >= base.num) {
	                            base.acks[seqNo - base.num]=false;
								timers[seqNo - base.num] = startTimer(sendPacket);
								packetsInWindow[seqNo - base.num] = sendPacket;
							}
							// startTimer(sendPacket);
							seqNo++;
							read = input.read(chunk);
						}
						// System.out.println(wndSize);
					}
				}
				
				input.close();
				// System.out.println("khalaaaaaaaaaaaaaaaaaaaaaaaaaaaaastttttttttttttttttttttt");
			} catch (IOException ex) {
				Logger.getLogger(SelectiveRepeat.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}

	public class SR_RcvAckThread implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					datagramSocket.receive(receivePacket);
					AckPacket ackPacket = new AckPacket(receivePacket.getData());
				
					int tempBase = ackPacket.getAckNum() - base.num;
					if (ackPacket.getAckNum() >= base.num
							&& ackPacket.getAckNum() < (base.num + wndSize)) {
						System.out.println("Ack num:   "
								+ ackPacket.getAckNum());
						base.acks[tempBase] = true;
						if (timers[tempBase] != null) {
							try {
								timers[tempBase].cancel();
								timers[tempBase].purge();
								timers[tempBase] = null;
								// packetsInWindow[tempBase] = null;
							} catch (NullPointerException ex) {

							}
						}
					}	
			synchronized (base) {
						// timers[ackPacket.getAckNum() - base.num].cancel();
						if (ackPacket.getAckNum() == base.num) {
							int i;
							for (i = 0; i < wndSize; i++) {
								if (base.acks[i] == false) {
									break;
								}
							}
							if (i != 0) {
								boolean []tempAck=new boolean[wndSize];
								for (int j = 0; (j + i) < wndSize; j++) {
									tempAck[j] = base.acks[j + i];
								    timers[j] = timers[j + i];
								    timers[j+i]=null;
//									timers[j] = startTimer(new DataPacket(
//											packetsInWindow[j + i]
//													.getChecksum(),
//											packetsInWindow[j + i].getLength(),
//											packetsInWindow[j + i].getSeqno(),
//											packetsInWindow[j + i]
//													.getDataPacketByte()));
//									if (timers[j + i] != null) {
//										timers[j + i].cancel();
//										timers[j + i].purge();
//										timers[j + i] = null;
//									}
//									packetsInWindow[j] = new DataPacket(
//											packetsInWindow[j + i]
//													.getChecksum(),
//											packetsInWindow[j + i].getLength(),
//											packetsInWindow[j + i].getSeqno(),
//											packetsInWindow[j + i]
//													.getDataPacketByte());
								}
								base.acks=new boolean[wndSize];
								base.acks=tempAck;
							}
							base.num += i;
							if (base.num > numOfChuncks) {
								break;
							}
						}
					}
				}
				System.out.println("Transfer Completed ( Send )!!");
				//datagramSocket.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	Timer startTimer(DataPacket dataPacket) {
		Timer pktTimer = new Timer();
		pktTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!datagramSocket.isClosed()) {
					synchronized (base) {
					udt_send_byte(dataPacket, "Timer");
//					if(dataPacket.getSeqno()==17){
//						System.out.println(base.num);
//						System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh  "+ " "+acks[ dataPacket.getSeqno()
//						                                       										- base.num] );
//					}
						if ( dataPacket.getSeqno() >= base.num
								&& base.acks[ dataPacket.getSeqno()
										- base.num] == false) {
							timers[ dataPacket.getSeqno()
									- base.num] = startTimer(dataPacket);
							System.out.println("ack from timer  : "+ dataPacket.getSeqno());
						}
					}
				}
			}
		}, 1000);
		return pktTimer;
	}

	void startTimerSW(Packet dataPacket) {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				udt_send_byte(dataPacket, "Timer Length");
				startTimerSW(dataPacket);
			}
		}, 1000);
	}

	private class Base {

		int num;
		boolean acks[];
		Base(int base,	boolean acks[]) {
			this.num = base;
			this.acks=acks;
		}
	}

//	void updateAfterShrink() {
//		for (int i = 0; i < maxWNDSize; i++) {
//			try {
//				timers[i].cancel();
//				timers[i].purge();
//			} catch (Exception e) {
//			}
//		}
//		timers = new Timer[maxWNDSize];
//		acks = new boolean[maxWNDSize];
//	}
}

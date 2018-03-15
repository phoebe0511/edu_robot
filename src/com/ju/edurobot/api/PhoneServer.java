package com.ju.edurobot.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class PhoneServer extends Thread{
	DatagramSocket m_udpSocket = null;
	final int UDP_PORT = 3028;
	boolean m_bStop = false;
	String mRobotName;
	//InetAddress mAddr = null;
	public PhoneServer(String robotName)
	{
		mRobotName = robotName;
	}
	@Override
	public void run() {

		try {
			m_udpSocket = new DatagramSocket(UDP_PORT);
			m_udpSocket.setReuseAddress(true);
			m_udpSocket.setSoTimeout(1000);
        } catch (IOException e) {
        	Log.e( this.getClass().getName() + ">" + " / Cannot open port" + UDP_PORT);
        	e.printStackTrace();
            //throw new RuntimeException("Cannot open port" + UDP_PORT, e);
        	return;
        }
		byte[] udp_buffer = new byte[128];
		DatagramPacket packet_rx = new DatagramPacket(udp_buffer, udp_buffer.length);
		while (!m_bStop) {
			try {
				m_udpSocket.receive(packet_rx);
			} catch (SocketTimeoutException e1) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			String str_rx_all = new String(packet_rx.getData(), 0, packet_rx.getLength());
			Log.d("PhoneServer:rx=" + str_rx_all);
			if(str_rx_all.equals("edu_robot_hello"))
			{
				String buff = mRobotName;
				DatagramPacket packet_tx = new DatagramPacket(buff.getBytes(), buff.getBytes().length
																, packet_rx.getAddress(), packet_rx.getPort());
				try {
					m_udpSocket.send(packet_tx);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(m_udpSocket != null)
			m_udpSocket.close();
		m_udpSocket = null;
	}
	
	public void close()
	{
		m_bStop = true;
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}

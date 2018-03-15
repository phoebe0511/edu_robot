package com.ju.edurobot.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class BtCommService extends Thread{
	protected DatagramSocket m_socket 	= null;
	private boolean m_bStop = false;
	private InetAddress mPhoneAddress;
	final int TX_PORT = 9032;
	final int RX_PORT = 9031;
	Rx_cb m_cb = null;
	public static interface Rx_cb{
		public String onGotCommand(String rx_string);
	}
	public BtCommService(Rx_cb cb) {
		m_cb = cb;
		try {
			mPhoneAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		final int UDP_BUFFER_MAX = 1024;
		
		try {
			m_socket = new DatagramSocket(RX_PORT);
			m_socket.setReuseAddress(true);
			m_socket.setSoTimeout(1000);
        } catch (IOException e) {
        	Log.e(this.getClass().getName() + ">" + " / Cannot open port" + RX_PORT);
        	e.printStackTrace();
            throw new RuntimeException("Cannot open port" + RX_PORT, e);
        }
		
		byte[] udp_buffer = new byte[UDP_BUFFER_MAX];
		DatagramPacket packet_rx = new DatagramPacket(udp_buffer, udp_buffer.length);
		while (!m_bStop) {
			try {
				m_socket.receive(packet_rx);
			} catch (SocketTimeoutException e1) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			String str_rx_all = new String(packet_rx.getData(), 0, packet_rx.getLength());
			//TODO: Receive BT string here
			Log.d("rx_bt_string:" + str_rx_all);
			String rc;
			if(m_cb != null)
			{
				rc = m_cb.onGotCommand(str_rx_all);
				if(rc == null || (rc != null && rc.isEmpty()))
					rc = "*ok#";
			}
			else
				rc = "*ok#";
			//mPhoneAddress = packet_rx.getAddress();
			//mPort = packet_rx.getPort();
			//Log.d("rx:addr=" + mPhoneAddress.getHostAddress() + ",port=" + mPort);
			DatagramPacket packet_tx = new DatagramPacket(rc.getBytes(), rc.getBytes().length, packet_rx.getAddress(), packet_rx.getPort());
			Log.d("SendBTUDPData tx=" + rc);
			
			try {
				m_socket.send(packet_tx);
			} catch (IOException e) {
				e.printStackTrace();
			}
			packet_tx = null;
			
		}
		if(m_socket != null)
			m_socket.close();
		m_socket = null;
	}
	public void Send(String tx) {
		DatagramPacket packet_tx = new DatagramPacket(tx.getBytes(), tx.getBytes().length, mPhoneAddress, TX_PORT);
		Log.d("SendBTUDPData tx=" + tx);
		
		try {
			m_socket.send(packet_tx);
		} catch (IOException e) {
			e.printStackTrace();
		}
		packet_tx = null;
	}
	public void close()
	{
		m_bStop = true;
	}
	
}

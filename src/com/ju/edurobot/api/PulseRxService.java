package com.ju.edurobot.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class PulseRxService extends Thread{
	protected DatagramSocket m_socket 	= null;
	private boolean m_bStop = false;
	final int PORT = 9082;
	Pulse_Rx_cb m_cb = null;
	public static interface Pulse_Rx_cb{
		public void onGotCommand(String rx_string);
	}
	public PulseRxService(Pulse_Rx_cb cb) {
		m_cb = cb;
		Log.d("PulseRxService");
	}
	
	@Override
	public void run() {
		final int UDP_BUFFER_MAX = 64;
		//Log.d("PulseRxService run");
		try {
			m_socket = new DatagramSocket(PORT);
			m_socket.setReuseAddress(true);
			m_socket.setSoTimeout(1000);
        } catch (IOException e) {
        	Log.e(this.getClass().getName() + ">" + " / Cannot open port" + PORT);
        	e.printStackTrace();
            throw new RuntimeException("Cannot open port" + PORT, e);
        }
		
		byte[] udp_buffer = new byte[UDP_BUFFER_MAX];
		DatagramPacket packet_rx = new DatagramPacket(udp_buffer, udp_buffer.length);
		while (!m_bStop) {
			try {
				//Log.d("wait..");
				m_socket.receive(packet_rx);
			} catch (SocketTimeoutException e1) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			String str_rx_all = new String(packet_rx.getData(), 0, packet_rx.getLength());

			Log.d("pulse:" + str_rx_all);
			if(m_cb != null)
			{
				m_cb.onGotCommand(str_rx_all);
			}
			
		}
		if(m_socket != null)
			m_socket.close();
		m_socket = null;
	}
	
	public void close()
	{
		m_bStop = true;
	}
	
}

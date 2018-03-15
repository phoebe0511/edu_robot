package com.ju.edurobot.api;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.ju.edurobot.api.GpioControlClass.LED_COLOR;
import com.ju.edurobot.api.GpioControlClass.LED_STATUS;

public class CheckWifiIP {
	boolean mbStop = false;
	static String mWifiIp = null;
	boolean mbConnected = false;
	GpioControlClass m_gpio;
	public CheckWifiIP(GpioControlClass gpio) {
		m_gpio = gpio;
		mThread.start();
	}
	public static String getWifiIP()
	{
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;
	            if(!iface.getDisplayName().contains("wlan"))
	            	continue;
	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                mWifiIp = addr.getHostAddress();
	                //System.out.println(iface.getDisplayName() + " " + mWifiIp);
	                if(mWifiIp.length() > 16)
	                	continue;
	                return mWifiIp;
	            }
	        }
		}catch(SocketException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	 
	Thread mThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while (!mbStop) {
				if(mWifiIp == null || mWifiIp != null && mWifiIp.isEmpty())
				{
					getWifiIP();
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else {
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					getWifiIP();
				}
				if(mWifiIp != null && !mWifiIp.isEmpty())
				{
					if(!mbConnected)
					{
						m_gpio.led(LED_COLOR.GREEN, LED_STATUS.ON);
						mbConnected = true;
					}
				}else{
					if(mbConnected)
					{
						m_gpio.led(LED_COLOR.GREEN, LED_STATUS.OFF);
						mbConnected = false;
					}
				}
			}
			
		}
	});
	
	public static boolean hasIP() {
		return (mWifiIp != null && !mWifiIp.isEmpty());
	}
	
	void close()
	{
		mbStop = true;
	}
}

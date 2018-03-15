package com.ju.edurobot;

import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.WifiSetting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import com.ju.edurobot.api.BtCommService;
import com.ju.edurobot.api.CheckWifiIP;
import com.ju.edurobot.api.BtCommService.Rx_cb;
import com.ju.edurobot.api.GpioControlClass;
import com.ju.edurobot.api.GpioControlClass.LED_COLOR;
import com.ju.edurobot.api.GpioControlClass.LED_STATUS;

public class IIIAppBtService {
	final static String GET_WIFI_IP = "GetWifiIP";
	final static String SET_WIFI_SSID = "SetSSID";
	final static String SET_SERVER_IP = "SetServerID";
	BtCommService mBtCommService;
	//String mWifiIp = null;
	WifiSetting mWifiSetting;
	
	
	public IIIAppBtService() {
		mBtCommService = new BtCommService(m_rx_cb);
		//getWifiIP();
		mWifiSetting = new WifiSetting();
		mBtCommService.start();
	}
	
	
	Rx_cb m_rx_cb = new Rx_cb() {
		
		@Override
		public String onGotCommand(String rx_string) {
			Log.d("IIIAppBtService rx:" + rx_string);
			String[] rx_cmds = rx_string.split("#");
            for(int i = 0; i < rx_cmds.length; i++) {
                String rx_cmd = rx_cmds[i].replace("*", "");
                //String[] rx_pkg = rx_cmd.split(",");
                if (rx_cmd.contains(GET_WIFI_IP)) {
                	String ip = CheckWifiIP.getWifiIP();
                    String rcString = "*" + "WifiIP," + ip + "#";
                    return rcString;
                }else if (rx_cmd.contains(SET_WIFI_SSID)) {
                	String[] datas = rx_cmd.split(",");
					if(datas.length == 3)
					{
						if(datas[1].isEmpty())
							return "0";
						Log.d("ssid:" + datas[1] + ",psk:" + datas[2]);
						mWifiSetting.add(datas[1], datas[2]);
						mWifiSetting.writeTo();
						try {
							Runtime.getRuntime().exec("sudo reboot");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}
				}else if (rx_cmd.contains(SET_SERVER_IP)) {
					String[] datas = rx_cmd.split(",");
					if(datas.length == 2)
					{
						Log.d("Set Server IP : " + datas[1]);
						RobotMain.ChangeServerIP(datas[1]);
						try {
							Runtime.getRuntime().exec("sudo reboot");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return null;
				}
            }
			return "0";
		}
	};
	
	void close()
	{
		mBtCommService.close();
		
	}
}

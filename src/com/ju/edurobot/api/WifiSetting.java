package com.ju.edurobot.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class WifiSetting {
	public static class WifiSSID
	{
		public String ssid;
		public String password;
		@Override
		public String toString() {
			
			return "ssid:" + ssid + ",psk:" + password;
		}
		
	}
	final static String WFile = "/etc/wpa_supplicant/wpa_supplicant.conf";
	final static String WConfigSTR0 = "country=GB";
	final static String WConfigSTR1 = "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev";
	final static String WConfigSTR2 = "update_config=1";
	List<WifiSSID> mSSID = null;
	
	public List<WifiSSID> getSSIDList()
	{
		return mSSID;
	}
	
	public void add(String ssid, String psk) {
		Iterator<WifiSSID> it = mSSID.iterator();
		while (it.hasNext()) {
			WifiSSID w = it.next();
			if(ssid.equals(w.ssid))
			{
				w.password = psk;
				return;
			}
		}
		WifiSSID wifiSSID = new WifiSSID();
		wifiSSID.ssid = ssid;
		wifiSSID.password = psk;
		mSSID.add(0, wifiSSID);
	}
	
	public void writeTo() {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(WFile), 
								StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			writer.write(WConfigSTR0);
			writer.newLine();
			writer.write(WConfigSTR1);
			writer.newLine();
			writer.write(WConfigSTR2);
			writer.newLine();
			writer.newLine();
			Iterator<WifiSSID> it = mSSID.iterator();
			while (it.hasNext()) {
				WifiSSID w = it.next();
				writer.write("network={");
				writer.newLine();
				writer.write("ssid=\"" + w.ssid + "\"");
				writer.newLine();
				writer.write("psk=\"" + w.password + "\"");
				writer.newLine();
				writer.write("}");
				writer.newLine();
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public WifiSetting() {
		mSSID = new ArrayList<WifiSSID>();
		List<String> txt_list = new ArrayList<String>();
		
		Path path = Paths.get(WFile);
		try {
			Files.lines(path).forEach(s -> txt_list.add(s));;
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<String> iterator = txt_list.iterator();
		while (iterator.hasNext()) {
			String ssid = iterator.next();
			if(ssid.contains("ssid"))
			{
				String psk = iterator.next();
				if(psk.contains("psk"))
				{
					WifiSSID w = new WifiSSID();
					String[] tmp = ssid.split("=");
					String str = tmp[1].replace("\"", "");
					w.ssid = str;
					tmp = psk.split("=");
					str = tmp[1].replace("\"", "");
					w.password = str;
					mSSID.add(w);
					Log.d(w.toString());
				}
			}
		}
		//add("phoebe ssid @", "0987654321");
		//writeTo();
	}
}

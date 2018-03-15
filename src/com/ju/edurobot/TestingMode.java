package com.ju.edurobot;

import com.ju.edurobot.api.ButtonTask.PRESSED;

import java.io.IOException;

import com.ju.edurobot.api.DisplayTask;
import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.Utility;

public class TestingMode implements BaseService{
	DisplayTask m_playTask = null;
	boolean m_bPlaying = false;
	@Override
	public void Init() {
		Utility.drawPic("/home/pi/edu_robot/media/test/test_mode.png");
	}

	@Override
	public void close() {
		if(m_playTask != null)
		{
			m_playTask.stopAndWaitProcess();
		}
		m_playTask = null;
		String command = "killall fbcp";
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.gc();
	}

	@Override
	public void onHeartPressed(PRESSED status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHeadPressed(PRESSED status) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isPlayingVideo() {
		return m_bPlaying;
	}

	public void playVideo(String videoName) {
		
		if(m_bPlaying)
			return;
		m_bPlaying = true;
		String command = "sudo fbi -T 1 -noverbose /home/pi/edu_robot/media/b_back.png";
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		command = "fbcp";
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(m_playTask != null)
		{
			m_playTask.stopAndWaitProcess();
			m_playTask = null;
		}
		m_playTask = new DisplayTask(videoName, 0, 0, 720, 480, false);
		m_playTask.setNoLoop();
		m_playTask.run();
		
//		String command = "sudo SDL_VIDEODRIVER=fbcon SDL_FBDEV=/dev/fb1 mplayer " + videoName;
//		Log.d("get command to play one video :" + command);
//    	try {
//    		Process proc = Runtime.getRuntime().exec(command);
//			proc.waitFor();
//		} catch (IOException | InterruptedException e) {
//			e.printStackTrace();
//		}
		m_bPlaying = false;
		command = "killall fbcp";
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Log.d("playVideo end");
	}
	
}

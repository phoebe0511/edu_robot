package com.ju.edurobot;


import java.io.File;

import org.omg.CosNaming._BindingIteratorImplBase;

import com.ju.edurobot.api.ButtonTask;
import com.ju.edurobot.api.CheckWifiIP;
import com.ju.edurobot.api.GpioControlClass;
import com.ju.edurobot.api.GpioControlClass.LED_COLOR;
import com.ju.edurobot.api.GpioControlClass.LED_STATUS;
import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.MusicPlayer;
import com.ju.edurobot.api.PhoneServer;
import com.ju.edurobot.api.Utility;
import com.ju.edurobot.api.ButtonTask.BtnCB;
import com.ju.edurobot.api.ButtonTask.PRESSED;

public class RobotService {
	GpioControlClass m_gpio;
	BaseService m_current_service = null;
	ButtonTask m_btnHead;
	ButtonTask m_btnHeart;
	ButtonTask m_btnVUp;
	ButtonTask m_btnVDown;
	TestingMode m_testingMode;
	TomatoRobotService m_tomato = null;
	StoryRobotService m_storyRobotService = null;
	VoiceRobotService m_voiceRobotService = null;
	enum SMODE{TOMATO, STORY, VOICE, TEST};
	SMODE mSMode = SMODE.TOMATO;
	boolean m_bStop = false;
	MusicPlayer mPlayer = MusicPlayer.getInstance();
	PhoneServer mPhoneServer;
	IIIAppBtService mBt;
	TcpMainServer mTcpMainServer;
	CheckWifiIP mCheckWifiIP;
	
	public RobotService(GpioControlClass gpio) {
		mPhoneServer = new PhoneServer(RobotMain.mRobotName);
		m_bStop = false;
		m_gpio = gpio;
		mCheckWifiIP = new CheckWifiIP(gpio);
		m_btnHeart = new ButtonTask(m_gpio.GPI_BTN_HEART, m_heart_cb);
		m_btnHeart.start();
		m_btnHead = new ButtonTask(m_gpio.GPI_BTN_HEAD, m_head_cb);
		m_btnHead.start();
		m_btnVUp = new ButtonTask(m_gpio.GPI_BTN_VUP, m_vup_cb);
		m_btnVUp.start();
		m_btnVDown = new ButtonTask(m_gpio.GPI_BTN_VDOWN, m_vdown_cb);
		m_btnVDown.start();
		mBt = new IIIAppBtService();
		m_tomato = new TomatoRobotService(mBt.mBtCommService);
		mTcpMainServer = new TcpMainServer(mCb);
		mPhoneServer.start();
		mTcpMainServer.start();
		//m_voiceRobotService = new VoiceRobotService(m_gpio);
		m_current_service = m_tomato;
		m_current_service.Init();
	}
	
	TcpServerThread.RxPhoneCmdCB mCb = new TcpServerThread.RxPhoneCmdCB() {
		
		@Override
		public String onGotCommand(String[] cmd, final TcpServerThread phoneSocket) {
			if(cmd[0].equals(WebCommand.CHANGE_MODE))
			{
				mSMode = SMODE.TEST;
				m_current_service.close();
				m_testingMode = new TestingMode();
				m_current_service = m_testingMode;
				m_current_service.Init();
				
			}else if (cmd[0].equals(WebCommand.SHOW_GIF)) {
				Utility.drawPic("/home/pi/edu_robot/media/test/pica.gif");
				return "ok";
			}else if (cmd[0].equals(WebCommand.CAPTURE_PIC)) {
				if(cmd.length != 2)
					return "wrong file name";
				Utility.capturePic(cmd[1]);
				uploadFile_led("/home/pi/camera/" + cmd[1] + ".jpg", m_gpio);
				//sendSocket("capture and upload end", phoneSocket);
				return "capture and upload end";
			}else if (cmd[0].equals(WebCommand.PLAY_PIC)) {
				if(cmd.length != 2)
					return "wrong file name";
				Utility.drawPic("/home/pi/camera/" + cmd[1] + ".jpg");
			}else if (cmd[0].equals(WebCommand.PLAY_VIDEO)) {
				if(cmd.length != 2)
					return "wrong file name";
				if(mSMode == SMODE.TEST)
				{
					if(!m_testingMode.isPlayingVideo())
					{
						Thread thread = new Thread(new Runnable() {
							
							@Override
							public void run() {
								m_testingMode.playVideo("/home/pi/camera/" + cmd[1] + ".mp4");
								sendSocket("video player end", phoneSocket);
							}
						});
						thread.start();
						return "playing";
					}else {
						return "Robot is busy...";
					}
				}
				return "not test mode";
			}else if (cmd[0].equals(WebCommand.CAPTURE_VIDEO)) {
				if(cmd.length < 2)
					return "wrong file name";
				if(cmd.length == 2){
					Utility.captureVideo(cmd[1], 5);
					return "capture video end";
				}
				else {
					final String fName = cmd[1];
					final int time = Integer.parseInt(cmd[2]);
					Thread thread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							Utility.captureVideo(fName, time);
							uploadFile_led("/home/pi/camera/" + fName + ".mp4", m_gpio);
							sendSocket("capture and upload end", phoneSocket);
						}
					});
					thread.start();
				}
				return "capturing 1min video, please wait~";
			}else if (cmd[0].equals(WebCommand.DOWNLOAD_MP3)) {
				if(cmd.length < 2)
					return "wrong file name";
				final String fName = cmd[1];
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						downloadFile_led(WebCommand.BASE_PATH + fName, m_gpio);
						sendSocket("download end", phoneSocket);
					}
				});
				thread.start();
			}else if (cmd[0].equals(WebCommand.PLAY_MP3)) {
				if(cmd.length < 2)
					return "wrong file name";
				final String fName = cmd[1];
				File mp3 = new File(WebCommand.BASE_PATH + fName);
				if(!mp3.exists())
				{
					return "file is not exist";
				}
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						mPlayer.play_block(WebCommand.BASE_PATH + fName);
						sendSocket("play mp3 end", phoneSocket);
					}
				});
				thread.start();
				return "playing";
			}else if (cmd[0].equals(WebCommand.SHOW_JPG)) {
				if(cmd.length < 2)
					return "wrong file name";
				final String fName = cmd[1];
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Utility.drawPic(WebCommand.BASE_PATH + fName);
					}
				});
				thread.start();
			}else if (cmd[0].equals(WebCommand.DOWNLOAD_JPG)) {
				if(cmd.length < 2)
					return "wrong file name";
				final String fName = cmd[1];
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						downloadFile_led(WebCommand.BASE_PATH + fName, m_gpio);
						sendSocket("download jpg end", phoneSocket);
					}
				});
				thread.start();
			}
			return "ok";
		}
	};
	
	void sendSocket(String str, final TcpServerThread phoneSocket)
	{
		try{
			phoneSocket.write(str);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	static void uploadFile_led(String file, GpioControlClass gpio)
	{
		if(CheckWifiIP.hasIP())
		{
			gpio.led(LED_COLOR.GREEN, LED_STATUS.BLINK);
			Utility.uploadToServer(file, RobotMain.mRobotName, RobotMain.mServerIP);
			gpio.led(LED_COLOR.GREEN, LED_STATUS.ON);
		}
	}
	
	static void downloadFile_led(String file, GpioControlClass gpio)
	{
		if(CheckWifiIP.hasIP())
		{
			gpio.led(LED_COLOR.GREEN, LED_STATUS.BLINK);
			Utility.downloadFromServer(file, RobotMain.mRobotName, RobotMain.mServerIP);
			gpio.led(LED_COLOR.GREEN, LED_STATUS.ON);
		}
	}
	
	private void restartHeartBtn()
	{
		if(mSMode != SMODE.VOICE)
			return;
		m_btnHeart = new ButtonTask(m_gpio.GPI_BTN_HEART, m_heart_cb);
		m_btnHeart.start();
	}
	
	BtnCB m_vup_cb = new BtnCB(){

		@Override
		public void onPressed(PRESSED status) {
			Log.d("VUp " + status.toString());
			mPlayer.VUp();
		}
	};
	BtnCB m_vdown_cb = new BtnCB() {

		@Override
		public void onPressed(PRESSED status) {
			Log.d("VDown " + status.toString());
			mPlayer.VDown();
		}
	};
	BtnCB m_heart_cb = new BtnCB() {
		
		@Override
		public void onPressed(PRESSED status) {
			Log.d("Btn Heart pressed " + status.toString());
			m_current_service.onHeartPressed(status);
		}
	};
	BtnCB m_head_cb = new BtnCB() {
		
		@Override
		public void onPressed(PRESSED status) {
			Log.d("Btn Head pressed " + status.toString());
			switch(status)
			{
			case SHORT:
				if(mSMode == SMODE.TOMATO)
				{
					m_current_service.onHeadPressed(status);
				}else {
					restartHeartBtn();
					mSMode = SMODE.TOMATO;
					m_current_service.close();
					m_tomato = new TomatoRobotService(mBt.mBtCommService);
					m_current_service = m_tomato;
					m_current_service.Init();
				}
				break;
			case DOUBLE:
				if(mSMode == SMODE.STORY)
				{
					m_current_service.onHeadPressed(status);
				}else {
					restartHeartBtn();
					mSMode = SMODE.STORY;
					m_current_service.close();
					m_storyRobotService = new StoryRobotService();
					m_current_service = m_storyRobotService;
					m_current_service.Init();
				}
				break;
			case TRIPLE:
				if(mSMode == SMODE.VOICE)
				{
					m_current_service.onHeadPressed(status);
				}else {
					mSMode = SMODE.VOICE;
					m_current_service.close();
					m_btnHeart.close();
					try {
						m_btnHeart.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					m_voiceRobotService = new VoiceRobotService(m_gpio);
					m_current_service = m_voiceRobotService;
					m_current_service.Init();
				}
				break;
			case LONG:
				m_current_service.onHeadPressed(status);
				break;
			}
		}
	};
	
	
	
	void close()
	{
		Log.d("close program");
		m_bStop = true;
		mPhoneServer.close();
		m_btnHeart.close();
		m_btnHead.close();
		if(m_current_service != null)
			m_current_service.close();
		mBt.close();
	}
}

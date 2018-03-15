package com.ju.edurobot;

import java.io.IOException;
import java.io.PrintStream;

import com.ju.edurobot.api.ButtonSimpleTask;
import com.ju.edurobot.api.ButtonSimpleTask.BtnSimpleCB;
import com.ju.edurobot.api.GpioControlClass;
import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.MusicPlayer;
import com.ju.edurobot.api.PulseRxService;
import com.ju.edurobot.api.Utility;
import com.ju.edurobot.api.ButtonTask.PRESSED;

public class VoiceRobotService implements BaseService{
	boolean mbStop;
	boolean mbEnd;

	MusicPlayer mPlayer;
	ButtonSimpleTask m_btnHeart;
	Process mProcess;
	PrintStream mPrintStream;
	String mTmpFileMp3 = "";
	PulseTcpClient mTcpClient;
	PulseRxService mPulseRxService;
	GpioControlClass m_gpio;
	int mFaceID = -1;
	final String PATH = "/home/pi/edu_robot/media/voice/";
	//int mStartPulse;
	
	public VoiceRobotService(GpioControlClass gpio) {
		mPlayer = MusicPlayer.getInstance();
		mbStop = false;
		mbEnd = false;
		m_gpio = gpio;
		m_btnHeart = new ButtonSimpleTask(gpio.GPI_BTN_HEART, m_heart_cb);
		m_btnHeart.start();
		mPulseRxService = new PulseRxService(mPulse_Rx_cb);
		mTcpClient = new PulseTcpClient(mTcp_Rx_cb);
	}
	
	PulseTcpClient.Tcp_Rx_cb mTcp_Rx_cb = new PulseTcpClient.Tcp_Rx_cb() {
		
		@Override
		public void onGotCommand(int id) {
			String fName = PATH;
			boolean bChange = false;
			if(mFaceID != id)
			{
				bChange = true;
			}
			switch(id)
			{
			case 0:
				fName += "0.png";
				break;
			case 1:
				fName += "1.png";
				break;
			case 2:
				fName += "2.png";
				break;
			case 3:
				fName += "3.png";
				break;
			default:
				bChange = false;
				Log.d("unknow id " + id);
				break;
			}
			if(bChange)
				Utility.drawPic(fName);
		}
	};
	PulseRxService.Pulse_Rx_cb mPulse_Rx_cb = new PulseRxService.Pulse_Rx_cb() {
		
		@Override
		public void onGotCommand(String rx_string) {
			
			Log.d("onGotCommand:" + rx_string);
			mTcpClient.SendPulseCmd((byte)Integer.parseInt(rx_string));
		}
	};
	
	BtnSimpleCB m_heart_cb = new BtnSimpleCB() {
		
		@Override
		public void onUp() {
			Log.d("voice mode heart Up");
			try {
				Runtime.getRuntime().exec("pkill lame");
				Runtime.getRuntime().exec("pkill arecord");
				mPlayer.play_block("/home/pi/edu_robot/" + mTmpFileMp3);
				Utility.drawPic(PATH + "recv_msg.jpg");
				mPlayer.play_block(PATH + "recv_msg.mp3");
				RobotService.uploadFile_led("/home/pi/edu_robot/" + mTmpFileMp3, m_gpio);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onDown() {
			Log.d("voice mode heart Down");
			Utility.drawPic(PATH + "input_voice.jpg");
			//mPlayer.play_block(PATH + "input_voice.mp3");
			//String cmmd = "arecord -D plughw:1,0 -f cd -t raw | lame -r - test.mp3";
			try {
				mTmpFileMp3 = "voice" + ".mp3";
				Runtime.getRuntime().exec("rm -f /home/pi/edu_robot/voice.mp3");
				mProcess = Runtime.getRuntime().exec("/home/pi/edu_robot/t.sh " + mTmpFileMp3);
				mPrintStream = new PrintStream(mProcess.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	@Override
	public void close() {
		m_btnHeart.close();
		mPlayer.Stop();
		mTcpClient.close();
		mPulseRxService.close();
	}

	@Override
	public void onHeartPressed(PRESSED status) {
		Log.d("VoiceRobot heart pressed:" + status.toString());
	}

	@Override
	public void onHeadPressed(PRESSED status) {
		Log.d("VoiceRobot heard pressed:" + status.toString());
	}

	@Override
	public void Init() {
		mPulseRxService.start();
		mTcpClient.start();
		Utility.drawPic("/home/pi/edu_robot/media/voice/v1.png");
		mPlayer.play_block("/home/pi/edu_robot/media/modes/vmode.mp3");
	}

}

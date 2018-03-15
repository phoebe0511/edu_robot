package com.ju.edurobot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ju.edurobot.api.BtCommService;
import com.ju.edurobot.api.ButtonTask.PRESSED;
import com.ju.edurobot.api.DisplayTask;
import com.ju.edurobot.api.MusicPlayer.PlayingEnd;
import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.MusicPlayer;

public class TomatoRobotService extends Thread implements BaseService{
	enum TMODE{NONE, WORKING, BREAK};
	final String PATH = "/home/pi/edu_robot/media/tomato/";
	BtCommService m_bt;
	MusicPlayer mPlayer;
	boolean mbStop;
	boolean mbEnd;
	int m_working_id = 0;
	int m_break_id = 0;
	String[] mListBreak = null;//{PATH + "let_it_go.mp3", PATH + "we.mp3", PATH + "rose_motel_style.mp3", PATH + "dream.mp3"};
	String[] mListWorking = null;
	TMODE mMode = TMODE.NONE;
	long mStart;
	boolean mbBusy = true;
	DisplayTask mDisplayFaceTask = null;
	DisplayTask mDisplaySecTask = null;
	DisplayTask mDisplayMinTask = null;
	//LinuxExecObj mLinuxExecObj = new LinuxExecObj();
	
	public TomatoRobotService(BtCommService bt) {
		mPlayer = MusicPlayer.getInstance();
		mbStop = false;
		mbEnd = false;
		m_bt = bt;
		List<String> content = new ArrayList<String>();
		try {
			Files.lines(Paths.get(PATH + "list_working.txt")).forEach(s -> content.add(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		mListWorking = new String[content.size()];
		Iterator<String> it = content.iterator();
		int i = 0;
		while (it.hasNext()) {
			mListWorking[i++] = PATH + (String) it.next();
		}
		
		List<String> contentB = new ArrayList<String>();
		try {
			Files.lines(Paths.get(PATH + "list_break.txt")).forEach(s -> contentB.add(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//mListBreak = (String[]) contentB.toArray(new String[contentB.size()]);
		mListBreak = new String[contentB.size()];
		Iterator<String> itB = contentB.iterator();
		i = 0;
		while (itB.hasNext()) {
			mListBreak[i++] = PATH + (String) itB.next();
		}
	}
	
	
	

	@Override
	public void close() {
		String command = "killall fbcp";
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.Stop();
		if(mDisplayFaceTask != null)
			mDisplayFaceTask.stopAndWaitProcess();
		mDisplayFaceTask = null;
		endWorkingMode();
		
		mbStop = true;
	}


	@Override
	public void onHeartPressed(PRESSED status) {
		Log.d("Tomato heart pressed:" + status.toString());
		if(status == PRESSED.SHORT)
		{
			mPlayer.next();
		}
	}


	@Override
	public void onHeadPressed(PRESSED status) {
		Log.d("Tomato head pressed:" + status.toString());
		if(status == PRESSED.SHORT)
		{
			switch (mMode) {
			case WORKING:
				BreakMode();
				break;

			case BREAK:
				WorkingMode();
				break;
			case NONE:
				WorkingMode();
				break;
			}
		}
	}


	@Override
	public void Init() {
		mbBusy = true;
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
		StandbyMode();
		mPlayer.play_block("/home/pi/edu_robot/media/modes/tmode.mp3");
		//mPlayer.play_block(PATH + "start_work.mp3");
		//mPlayer.playlist(mList, 0, m_end_cb);
		start();
	}

	PlayingEnd m_mp3_end_cb = new PlayingEnd() {
		
		@Override
		public void onEndOneSoung(int id, boolean bAlive) {
			Log.d("End id=" + id + ",alive=" + bAlive);
			switch(mMode)
			{
			case BREAK:
				m_break_id = id;
				break;
			case WORKING:
				m_break_id = id;
				break;
			default:
				break;
			}
			
			m_working_id = id;//bAlive ? id + 1 : 0;
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					mbBusy = true;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mbBusy = false;
				}
			});
			thread.start();
		}
	};

	@Override
	public void run() {
		
		//mMode = TMODE.WORKING;
		//mStart = System.currentTimeMillis();
		
		while (!mbStop) {
			switch (mMode) {
			case WORKING:
				if((System.currentTimeMillis() - mStart) >= (25 * 60 * 1000))
				{
					Log.d("WORKING to Break");
					m_bt.Send("*done#");
					BreakMode();
				}
				break;

			case BREAK:
				if((System.currentTimeMillis() - mStart) >= (5 * 60 * 1000))
				{
					Log.d("BREAK to Working");
					WorkingMode();
				}
				break;
			case NONE:
				break;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	void StandbyMode()
	{
		mMode = TMODE.NONE;
		Log.d("StandbyMode");
		mPlayer.Stop();
		showFace(PATH + "standby_long.mp4", 0, 0, 720, 480);
		endWorkingMode();
		//mDisplayPlayer.stopAndWaitProcess();
		
		//mDisplayPlayer.showGif(0, PATH + "standby.swf");
	}
	void BreakMode()
	{
		mStart = System.currentTimeMillis();
		mMode = TMODE.BREAK;
		Log.d("BreakMode");
		showFace(PATH + "standby_long.mp4", 0, 0, 720, 480);
		endWorkingMode();
		//mDisplayPlayer.stopAndWaitProcess();
		//mDisplayPlayer.showGif(0, PATH + "standby.swf");
		mPlayer.play_block(PATH + "start_break.mp3");
		mPlayer.playlist(mListBreak, m_break_id, m_mp3_end_cb);
		mbBusy = false;
	}
	void WorkingMode()
	{
		mStart = System.currentTimeMillis();
		mMode = TMODE.WORKING;
		Log.d("WorkingMode m_playing_id=" + m_working_id);
		m_bt.Send("*start#");
		showFace(PATH + "sing_long.mp4", 0, 0, 720, 390);
		showSec(PATH + "sec.mp4", 635,390,720,480);
		showMin(PATH + "min.mp4", 0,380,635,480);
		//mDisplayPlayer.stopAndWaitProcess();
		//mDisplayPlayer.showGif(2, PATH + "sing.gif");
		mPlayer.play_block(PATH + "start_work.mp3");
		mPlayer.playlist(mListWorking, m_working_id, m_mp3_end_cb);
		mbBusy = false;
	}
	void endWorkingMode()
	{
		if(mDisplaySecTask != null)
			mDisplaySecTask.stopAndWaitProcess();
		mDisplaySecTask = null;
		if(mDisplayMinTask != null)
			mDisplayMinTask.stopAndWaitProcess();
		mDisplayMinTask = null;
		
		//System.gc();
	}

	void showFace(String fileName, int x, int y, int x1, int y1)
	{
		if(mDisplayFaceTask != null)
			mDisplayFaceTask.stopAndWaitProcess();
		mDisplayFaceTask = null;
		System.gc();
		mDisplayFaceTask = new DisplayTask(fileName, x, y, x1, y1, true);
	}
	
	void showSec(String fileName, int x, int y, int x1, int y1)
	{
		mDisplaySecTask = new DisplayTask(fileName, x, y, x1, y1, true);
	}
	
	void showMin(String fileName, int x, int y, int x1, int y1)
	{
		mDisplayMinTask = new DisplayTask(fileName, x, y, x1, y1, true);
	}
}

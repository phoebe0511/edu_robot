package com.ju.edurobot.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MusicPlayer {
	static MusicPlayer mInstance = null;
	String[] mList = null;
	int mPlayingID = 0;
	private PrintStream mPrintStream = null;
	private Process mProcess = null;
	boolean mbNextPressed = false;
	boolean mbStop = false;
	enum PMODE{SINGLE, LIST, NONE};
	PMODE mMode = PMODE.NONE;
	boolean mbQuiting = true;
	Semaphore mMutex = new Semaphore(1);
	int mSound_db = 0;
	Thread mPlayListTask = null;
	//LinuxExecCB mExecCB;
	
	public MusicPlayer() {
		//mExecCB = eCB;
		mProcess = null;
		mPrintStream = null;
		mbQuiting = true;
		Init();
	}
	void Init()
	{
		FileReader fr;
		try {
			fr = new FileReader("/home/pi/edu_robot/sound.cfg");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String str_vol = br.readLine();
				mSound_db = Integer.parseInt(str_vol);
			}
			fr.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			WriteConfig(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	void WriteConfig(int vol)
	{
		FileWriter fw;
		try {
			fw = new FileWriter("/home/pi/edu_robot/sound.cfg");
			fw.write(Integer.toString(vol));
			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	static public interface PlayingEnd {
		public void onEndOneSoung(int id, boolean bAlive);
	} 
	
	static public MusicPlayer getInstance(){
		if(mInstance == null)
			mInstance = new MusicPlayer();
		return mInstance;
	}
	
	public void Stop()
	{
		if(mMode == PMODE.LIST)
		{
			mbStop = true;
			try {
				mPlayListTask.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			stop();
		}
	}
	
	public void play_block(String fileName) {
		//stop();
		Stop();
		mMode = PMODE.SINGLE;
		//String command = "omxplayer " + fileName;
		goRun(fileName);
		
		waitEnd();
		mMode = PMODE.NONE;
		Log.d("play_block end " + fileName);
	}
	
	public void playlist(String[] fileNames, int start_id, PlayingEnd end_cb) {
		//stop();
		mPlayingID = start_id;
		mbStop = false;
		mbNextPressed = false;
		mList = fileNames;

		mPlayListTask = new Thread(new Runnable() {
			
			@Override
			public void run() {
				mMode = PMODE.LIST;
				while (!mbStop) {
					if(mPlayingID >= mList.length)
						mPlayingID = 0;
					
					//String command = "omxplayer " + mList[mPlayingID++];
					goRun(mList[mPlayingID++]);
					boolean bQuit = false;
					try {
						while (mProcess != null && !mProcess.waitFor(20, TimeUnit.MILLISECONDS) && !mbStop) {
							//Log.d("" + mbStop);
							if(mbNextPressed)
							{
								mbNextPressed = false;
								bQuit = true;
						        break;
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (NullPointerException exc) {
						exc.printStackTrace();
					}
					if(mbStop)
					{
						if(mProcess != null) {
							quit_comm();
							waitEnd();
						}

						if(end_cb != null)
						{
							end_cb.onEndOneSoung(mPlayingID - 1, false);
						}
						break;
					}else if(bQuit && mProcess != null)
					{
						quit_comm();
					}
					waitEnd();
					if(end_cb != null)
					{
						end_cb.onEndOneSoung(mPlayingID - 1, (mPlayingID != mList.length));
					}
				}
				
				mMode = PMODE.NONE;
				Log.d("End Play List");
			}
		});
		mPlayListTask.start();
	}
	
	public int get_playing_id() {
		return mPlayingID;
	}
	
	public void next()
	{
		if(mProcess == null)
			return;
		if(mMode == PMODE.LIST)
			mbNextPressed = true;
	}
	
	private void changeSound(int delta)
	{
		mSound_db += delta;
		WriteConfig(mSound_db);
	}
	
	public void VUp()
	{
		if(mbQuiting)
		{
			if(mSound_db < 6)
				changeSound(3);
			if(mSound_db < 6)
				changeSound(3);
			return;
		}
		if(mSound_db >= 6)
			return;
		try {
			mMutex.acquire();
			
			if(mPrintStream != null)
			{
				changeSound(3);
				mPrintStream.print("+");
				mPrintStream.flush();
				Thread.sleep(50);
				if(mSound_db >= 6)
					return;
				changeSound(3);
				mPrintStream.print("+");
				mPrintStream.flush();
			}else{
				if(mSound_db < 6)
					changeSound(3);
				if(mSound_db < 6)
					changeSound(3);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mMutex.release(1);
		}

	}
	
	public void VDown()
	{
		if(mbQuiting)
		{
			if(mSound_db > -60)
				changeSound(-3);
			if(mSound_db > -60)
				changeSound(-3);
			return;
		}
		if(mSound_db <= -60)
			return;
		try {
			mMutex.acquire();
			if(mPrintStream != null)
			{
				changeSound(-3);
				mPrintStream.print("-");
				mPrintStream.flush();
				Thread.sleep(50);
				if(mSound_db <= -60)
					return;
				changeSound(-3);
				mPrintStream.print("-");
				mPrintStream.flush();
			}else{
				if(mSound_db > -60)
					changeSound(-3);
				if(mSound_db > -60)
					changeSound(-3);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			mMutex.release(1);
		}
		
	}
	
	private void quit_comm() {
		if(mbQuiting)
			return;
		mbQuiting = true;
		if(mPrintStream == null)
			return;
		Log.d("quit_comm");
		mPrintStream.print("q");
		mPrintStream.flush();
	}
	
	private void goRun(String sound_file) {
		
		try {
			String commd = "omxplayer --vol " + Integer.toString(mSound_db * 100) + " " + sound_file;
			Log.d(commd);
			mProcess = Runtime.getRuntime().exec(commd);
			mPrintStream = new PrintStream(mProcess.getOutputStream());
			mbQuiting = false;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void stop() {
		if(mProcess == null)
			return;
//		if(mMode == PMODE.SINGLE)
//		{
//			quit_comm();	        
//		}
		quit_comm();
		waitEnd();
        mMode = PMODE.NONE;
        mbStop = true;
	}
	
	private void waitEnd() {
		if(mProcess == null)
			return;
		try {
			mProcess.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NullPointerException exc) {
			exc.printStackTrace();
		}finally{
			mPrintStream = null;
			mProcess = null;
			
		}
	}
	
}

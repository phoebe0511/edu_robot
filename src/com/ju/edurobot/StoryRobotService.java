package com.ju.edurobot;

import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.MusicPlayer;
import com.ju.edurobot.api.Utility;
import com.ju.edurobot.api.MusicPlayer.PlayingEnd;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import com.ju.edurobot.api.ButtonTask.PRESSED;

public class StoryRobotService implements BaseService{
	
	final String PATH = "/home/pi/edu_robot/media/story/";
	boolean mbStop;
	boolean mbEnd;
	//LinuxExecObj mLinuxExecObj = new LinuxExecObj();
	String[] mPicList = null;
	String[] mStoryList = null;
	MusicPlayer mPlayer = MusicPlayer.getInstance();
	
	public StoryRobotService() {
		List<String> content = new ArrayList<String>();
		try {
			Files.lines(Paths.get(PATH + "list.txt")).forEach(s -> content.add(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPicList = new String[content.size() / 2];
		mStoryList = new String[content.size() / 2];
		Iterator<String> it = content.iterator();
		int i = 0;
		while (it.hasNext()) {	
			mPicList[i] = PATH + it.next();
			mStoryList[i] = PATH + it.next();
			i++;
		}
	}
	public void Init(){
		mbStop = false;
		mbEnd = false;
		Utility.drawPic(mPicList[0]);
		mPlayer.play_block("/home/pi/edu_robot/media/modes/smode.mp3");
		mPlayer.playlist(mStoryList, 0, m_end_cb);
	}
	
	PlayingEnd m_end_cb = new PlayingEnd() {
		
		@Override
		public void onEndOneSoung(int id, boolean bAlive) {
			if(!bAlive)
			{
				id = 0;
			}else
			{
				id++;
			}
			Utility.drawPic(mPicList[id]);
		}
	};
	
	
	
	@Override
	public void close() {
		mPlayer.Stop();
	}

	@Override
	public void onHeartPressed(PRESSED status) {
		if(mPlayer != null)
			mPlayer.next();
		Log.d("StoryRobot heart pressed:" + status.toString());
	}

	@Override
	public void onHeadPressed(PRESSED status) {
		Log.d("StoryRobot heard pressed:" + status.toString());
	}
	

}

package com.ju.edurobot;

import com.ju.edurobot.api.ButtonTask.PRESSED;

public interface BaseService {
	void Init();
	//void MainLoog();
	void close();
	void onHeartPressed(PRESSED status);
	void onHeadPressed(PRESSED status);
	//boolean IsEnd();
}

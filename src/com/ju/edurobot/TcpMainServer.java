package com.ju.edurobot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.ju.edurobot.api.Log;

public class TcpMainServer extends Thread {
	final static int PORT = 3082;
	boolean mbStop = false;
	ServerSocket server = null;
	Queue<TcpServerThread> mClient = new LinkedList<TcpServerThread>();
	TcpServerThread.RxPhoneCmdCB mCb;
	
	TcpMainServer(TcpServerThread.RxPhoneCmdCB cb){
		mCb = cb;
	}

	@Override
	public void run() {

		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("End TcpMainServer");
			return;
		}
		Socket client = null;

		while (!mbStop) {
			try {
				client = server.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d("Connected a client!");
			// create a thread for every client
			TcpServerThread c = new TcpServerThread(client, mCb);
			mClient.add(c);
			c.start();
		}

	}

	void close() {
		mbStop = true;
		while (!mClient.isEmpty()) {
			TcpServerThread c = mClient.poll();
			c.close();
		}
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

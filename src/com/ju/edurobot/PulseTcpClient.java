package com.ju.edurobot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

import com.ju.edurobot.api.Log;

public class PulseTcpClient extends Thread{
	boolean mbStop = false;
	Socket mClient = null;
	Queue<String> mTxQueue = null;
	Tcp_Rx_cb m_cb = null;
	public PulseTcpClient(Tcp_Rx_cb cb) {
		mTxQueue = new LinkedList<String>();
		m_cb = cb;
	}
	
	public static interface Tcp_Rx_cb{
		public void onGotCommand(int id);
	}
	
	@Override
	public void run() {
		//客户端请求与本机在9013端口建立TCP连接   
		try {
			mClient = new Socket(RobotMain.mServerIP, 9013);
			mClient.setSoTimeout(5000);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
  
        //BufferedReader input = new BufferedReader(new InputStreamReader(System.in));  
        //获取Socket的输出流，用来发送数据到服务端    
        PrintStream out_socket = null;
      //获取Socket的输入流，用来接收从服务端发送过来的数据    
        BufferedReader rx_socket_buf = null;
        
		try {
			out_socket = new PrintStream(mClient.getOutputStream());
			rx_socket_buf = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
			Log.d("Connected to server");
	        while(!mbStop){ 
	        	if(mTxQueue.isEmpty())
	        	{
	        		Thread.sleep(200);
	        		continue;
	        	}
	            String str = mTxQueue.poll();   
	            out_socket.println(str);  
	            try{   
                    String rc = rx_socket_buf.readLine(); 
                    IIIPkg rx_pkg = new IIIPkg(rc);
                    if(m_cb != null)
                    {
                    	m_cb.onGotCommand(rx_pkg.body);
                    }
                    //Log.d(rc);  
                }catch(SocketTimeoutException e){  
                	Log.w("Time out, No response");  
                }  
	        }  
	       
	        if(mClient != null){  
	        	mClient.close(); 
	        }  
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
        
	}
	
	void SendPulseCmd(byte data)
	{
		IIIPkg txPkg = new IIIPkg(data);
		String str = txPkg.getBase64Data();
		mTxQueue.add(str);
	}
	
	void close()
	{
		mbStop = true;
	}
	
}

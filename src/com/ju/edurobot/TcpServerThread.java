package com.ju.edurobot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import com.ju.edurobot.api.Log;

public class TcpServerThread extends Thread{
	public static interface RxPhoneCmdCB
	{
		public String onGotCommand(String[] cmd, final TcpServerThread phoneSocket);
	}
	RxPhoneCmdCB mCb;
	boolean mbStop = false;
	private Socket client_socket = null;  
	PrintStream m_tx_buf;
    public TcpServerThread(Socket client, RxPhoneCmdCB cb){  
    	client_socket = client; 
    	mCb = cb;
    }  
      
    @Override  
    public void run() {  
        try{  
            //Get Socket Output Stream to send client data
        	m_tx_buf = new PrintStream(client_socket.getOutputStream());  
            //Get Socket Input Stream to receive data from client 
            BufferedReader rx_buf = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));  
            
            while(!mbStop){    
                String str =  rx_buf.readLine();  
                if(str == null || str.isEmpty()){  
                	break;  
                }
                Log.d("RxTCP : " + str);
                if(str.startsWith(WebCommand.FOR_PI_HEAD))
                {
                	String rx_pkg = str.replace(WebCommand.FOR_PI_HEAD, "");
                	String[] cmds = rx_pkg.split(",");
                	String rc = mCb.onGotCommand(cmds, this);
                	if(!rc.isEmpty())
                	{
                		m_tx_buf.println(WebCommand.FOR_PHONE_HEAD + rc);
                	}
                }
            }  
            m_tx_buf.close();  
            client_socket.close();  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }
    public void write(String tx) {
    	try {
    		m_tx_buf.println("*eduPhone@" + tx);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}
    void close()
    {
    	mbStop = true;
    }
}

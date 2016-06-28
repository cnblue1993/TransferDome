package com.example.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class FileTcpClient {
	Msg msg = null;
	String path = null;
	
	public FileTcpClient(Msg msg, String path){
		this.msg = msg;
		this.path = path;
	}
	
	public void start(){
		Client c = new Client();
		c.start();
	}
	
	class Client extends Thread{
		
		public void run(){
			try{
				creatClient();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void creatClient() throws Exception{
		Socket s = new Socket(msg.getSendUserIp(), 2222);
		
		File file = new File(path);
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream os = new BufferedOutputStream(s.getOutputStream());
		
		byte[] data = new byte[Tools.byteSize];
		int len = -1;
		while((len = is.read(data)) != -1){
			os.write(data, 0, len);
			Tools.sendProgress += len;
		}
		Tools.sendProgress = -1;
		
		is.close();
		os.flush();
		os.close();
	}
}

package com.example.model;

import java.io.Serializable;

public class User implements Serializable{
	private String name = "";
	private String ip;
	private int headIcon = 0;
	private int unReadMegCount = 0;
	
	public User(String name, String ip){
		super();
		
		this.name = name;
		this.ip = ip;
	}
	
	public User(String name, String ip, int unReadMsgCount){
		super();
		
		this.name = name;
		this.ip = ip;
		this.unReadMegCount = unReadMsgCount;
	}
	
	public User getCopy(){
		User user = new User(this.name, this.ip, this.unReadMegCount);
		user.setHeadIcon(this.headIcon);
		return user;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getHeadIcon() {
		return headIcon;
	}
	public void setHeadIcon(int headIcon) {
		this.headIcon = headIcon;
	}
	public int getUnReadMegCount() {
		return unReadMegCount;
	}
	public void setUnReadMegCount(int unReadMegCount) {
		this.unReadMegCount = unReadMegCount;
	}
	
	
}

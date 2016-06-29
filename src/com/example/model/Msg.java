package com.example.model;

import java.io.Serializable;

public class Msg implements Serializable{
	
		private final String edition = "version1.0";
		private long packId;	//Ê±¼ä´Á
		
		private String sendUser;
		private String sendUserIp;
		private String receiveUser;
		private String receiveUserIp;
		
		private int headIconPos;
		private int MsgType;
		private Object body;
		
		public Msg(){}
		
		public Msg(int headIconPos, String sendUser, String sendUserIp, String receiveUser, String receiveUserIp,int MsgType, Object object){
			super();
			this.headIconPos = headIconPos;
			this.sendUser = sendUser;
			this.sendUserIp = sendUserIp;
			this.receiveUser = receiveUser;
			this.receiveUserIp  = receiveUserIp;
			this.MsgType = MsgType;
			this.body = object;
		}
		public long getPackId() {
			return packId;
		}
		public void setPackId(long packId) {
			this.packId = packId;
		}
		public String getSendUser() {
			return sendUser;
		}
		public void setSendUser(String sendUser) {
			this.sendUser = sendUser;
		}
		public String getSendUserIp() {
			return sendUserIp;
		}
		public void setSendUserIp(String sendUserIp) {
			this.sendUserIp = sendUserIp;
		}
		public String getReceiveUser() {
			return receiveUser;
		}
		public void setReceiveUser(String receiveUser) {
			this.receiveUser = receiveUser;
		}
		public String getReceiveUserIp() {
			return receiveUserIp;
		}
		public void setReceiveUserIp(String receiveUserIp) {
			this.receiveUserIp = receiveUserIp;
		}
		public int getHeadIconPos() {
			return headIconPos;
		}
		public void setHeadIconPos(int headIconPos) {
			this.headIconPos = headIconPos;
		}
		public int getMsgType() {
			return MsgType;
		}
		public void setMsgType(int msgType) {
			MsgType = msgType;
		}
		public Object getBody() {
			return body;
		}
		public void setBody(Object body) {
			this.body = body;
		}
		public String getEdition() {
			return edition;
		}
		
		
		
		

}

package com.example.model;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.example.transferdome.ChatActivity;
import com.example.transferdome.MainActivity;
import com.example.transferdome.R;

import org.apache.http.conn.util.InetAddressUtils;

public class Tools {
	// 协议命令
	public static final int CMD_ONLINE = 10;// 上线
	public static final int CMD_REPLYONLINE = 11;// 回应上线
	public static final int CMD_CHECK = 12;// 心跳广播
	public static final int CMD_SENDMSG=13;// 发送信息
	public static final int CMD_STARTTALK=14;// 发送呼叫请求
	public static final int CMD_STOPTALK=15;// 发送结束呼叫请求
	public static final int CMD_ACCEPTTALK=16;// 发送接收呼叫请求

	public static final int CMD_FILEREQUEST=20;//请求传送文件
	public static final int CMD_FILEACCEPT=21;//接受文件请求
	public static final int CMD_FILEREFUSE=22;//拒绝文件请求
	public static final int CMD_UPDATEINFORMATION=23;//更新信息
	public static final int PORT_SEND=2426;// 发送端口
	public static final int PORT_RECEIVE=2425;// 接收端口
	public static final int PORT = 5760;//
	
	// 消息命令
	public static final int MAINACTIVITY=7998;//当前是MAINACTIVITY
	public static final int CHATACTIVITY=7999;//CHATACTIVITY
	public static final int ACTIVITY_MAIN=0;//mainA 构造函数专用
	public static final int ACTIVITY_CHART=1;//mainB
	public static MainActivity mainA=null;
	public static ChatActivity chart=null;
	public static int State=Tools.MAINACTIVITY;//状态，显示当前活跃activity
	
	//文件传送模块
	public static String newfileName=null;
	public static long newfileSize=0;
	public static final int ISFILE=1001;//是文件
	public static String startPath = "/mnt";
	public static String newsavepath="/mnt/sdcard/ShareFiles";
	public static int byteSize = 1024*5;// 每次读写文件的字节数
	public static double sendProgress = -1;// 每次读写文件的字节数s
	public static final int FILE_JINDU=2001;//进度命令
	public static final int PROGRESS_FLUSH=2002;//更新进度
	public static final int PROGRESS_COL=2003;//关闭进度条
	
	// 消息命令
	public static final int SHOW=8000;//显示消息
	public static final int FLUSH=8001;//刷新界面
	public static final int ADDUSER=8002;//添加用户
	public static final int DESTROYUSER=8003;//删除用户
	public static final int RECEIVEMSG=8004;//删除用户
	public static final int REFLESHCOUNT=8005;//更新计数
	public static String sign=":";
	
	public static User me=null;
	public static Map<String,List<Msg>> msgContainer = new HashMap<String,List<Msg>>();
	public static int[] headIconIds = {R.drawable.user, R.drawable.user,R.drawable.user,R.drawable.user,
		R.drawable.user,R.drawable.user,R.drawable.user,R.drawable.user,R.drawable.user,R.drawable.user};
	
	// 构造函数
		public Tools(Object o,int type){
			switch(type)
			{
				case Tools.ACTIVITY_MAIN:
					this.mainA=(MainActivity)o;
					break;
				case Tools.ACTIVITY_CHART:
					this.chart=(ChatActivity)o;
					break;
			}
			
		}
	// 发送消息
		public void sendMsg(Msg msg)
		{
			(new UdpSend(msg)).start();
		}
		// 发送消息线程
		class UdpSend extends Thread {
			Msg msg=null;
			UdpSend(Msg msg) {
				this.msg=msg;
			}

			public void run() {
				try {
					byte[] data = Tools.toByteArray(msg);
					DatagramSocket ds = new DatagramSocket(Tools.PORT_SEND);
					DatagramPacket packet = new DatagramPacket(data, data.length,
							InetAddress.getByName(msg.getReceiveUserIp()), Tools.PORT_RECEIVE);
					packet.setData(data);
					ds.send(packet);
					ds.close();
					//Tools.out("发送广播通知上线");
				} catch (Exception e) {
				}

			}
		}
		
		
		// 接收消息
		public void receiveMsg()
		{
			new UdpReceive().start();
		}
		// 接收消息线程
		class UdpReceive extends Thread {
			Msg msg=null;
			UdpReceive() {}

			public void run() {
				//消息循环
				while(true)
				{
					try {
						DatagramSocket ds = new DatagramSocket(Tools.PORT_RECEIVE);
						byte[] data = new byte[1024 * 4];
						DatagramPacket dp = new DatagramPacket(data, data.length);
						dp.setData(data);
						ds.receive(dp);
						byte[] data2 = new byte[dp.getLength()];
						System.arraycopy(data, 0, data2, 0, data2.length);// 得到接收的数据
						Msg msg = (Msg) Tools.toObject(data2);
						System.out.println("receivethread+msg:"+msg);
						ds.close();
						//解析消息
						parse(msg);
					} catch (Exception e) {
					}
				}

			}
		}
		// 接收消息
		public void receiveMsg(Msg msg)
		{
			Tips(Tools.SHOW,msg.getSendUser() + " 发来消息！");
			if (!judgeUser(msg)) {// 如果列表无此人
				this.addUser(msg);// 列表添加此人
			}
			if(Tools.State == Tools.MAINACTIVITY){
				Tools.out("mainactivity界面");
				List<Msg> mes = null;
				if(msgContainer.containsKey(msg.getSendUserIp())){
					mes = msgContainer.get(msg.getSendUserIp());
					Tools.out("存在缓存");
				}else{
					mes = new ArrayList<Msg>();
					Tools.out("不存在");
				}
				mes.add(msg);
				msgContainer.put(msg.getSendUserIp(), mes);
				Tools.out("更新计数");
				Tips(Tools.REFLESHCOUNT, msg.getSendUserIp());
			}
			if (Tools.State == Tools.CHATACTIVITY) {
				Tools.out("chat界面");
				TipsChat(Tools.RECEIVEMSG, msg);
			}

		}
		// 解析接收的
		public void parse(Msg msg)
		{
			switch (msg.getMsgType()) {
			case Tools.CMD_FILEACCEPT:
				//收到确认接受
				String path = Tools.chart.choosePath;
				Tools.TipsChat(Tools.SHOW, "正在发送文件:" + new File(path).getName()); //toast提示
				Tools.sendProgress=0;
				FileTcpClient tc0 = new FileTcpClient(msg, path);  //创建tcp+socket
				tc0.start();
				Tools.TipsChat(Tools.FILE_JINDU, "发送文件"+Tools.sign+"正在发送："+new File(path).getName()+Tools.sign+ (new File(path).length()));
				fileProgress();//启动进度条线程
				break;
				
			case Tools.CMD_FILEREFUSE:
				//收到拒绝接受
				Tools.TipsChat(Tools.SHOW, "对方拒绝接受文件");
				break;
				
			case Tools.CMD_FILEREQUEST:
				//收到传送文件请求
				Tools.out("收到文件传送请求");
				System.out.println("收到文件传送请求");
				String[] newfileInfo = ((String) msg.getBody()).split(Tools.sign);
				Tools.newfileName = newfileInfo[0];// 记录下文件名称
				Tools.newfileSize = Long.parseLong(newfileInfo[1].trim());// 文件大小
				if(Tools.State==Tools.MAINACTIVITY)
				{
					System.out.println("正在main界面");
					Tools.out("正在main界面");
					Tools.Tips(Tools.CMD_FILEREQUEST, msg); //mainAcitvityHandler
				}
				else if(Tools.State==Tools.CHATACTIVITY)
				{
					System.out.println("正在chat界面");
					Tools.out("正在chat界面");
					Intent it = new Intent(Tools.chart, MainActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					Tools.chart.startActivity(it);
					Tools.Tips(Tools.CMD_FILEREQUEST, msg);

				}
				break;

			case Tools.CMD_SENDMSG:// 接收到对方发送的消息
				Tools.out(msg.getSendUser()+"来消息了");
				receiveMsg(msg);
				break;
			case Tools.CMD_ONLINE:// 上线
				upline(msg);
				break;
			case Tools.CMD_REPLYONLINE:// 响应上线
				replyUpline(msg);
				break;
			}
		}
		// 得到广播ip, 192.168.0.255之类的格式
		public static String getBroadCastIP() {
			String ip = getLocalHostIp().substring(0,
					getLocalHostIp().lastIndexOf(".") + 1)
					+ "255";
			return ip;
		}
		// 接收到上线广播
		public void upline(Msg msg){
			if (!judgeUser(msg)) {// 如果不存在
				//Tips(Tools.SHOW,msg.getSendUser() + " 上线···");
				addUser(msg);// 添加此人
			}
			// 发送响应上线
			Msg msgsend=new Msg();
			msgsend.setSendUser(Tools.me.getName());
			msgsend.setSendUserIp(Tools.me.getIp());
			msgsend.setHeadIconPos(Tools.me.getHeadIcon());
			msgsend.setMsgType(Tools.CMD_REPLYONLINE);
			msgsend.setReceiveUserIp(msg.getSendUserIp());
			Tools.out(Tools.me.getIp()+"回复广播"+msg.getSendUserIp());
			// 发送消息
			sendMsg(msgsend);
		}
		// 接收响应上线
		public void replyUpline(Msg msg){
			Tools.out("接受响应上线"+msg.getSendUserIp());
			if (!judgeUser(msg)) {// 如果不存在
				Tips(Tools.SHOW,msg.getSendUser() + " 上线···");
				addUser(msg);// 添加此人
			}
		}
		// 对象封装成消息 object->byte
				public static byte[] toByteArray(Object obj) {
					
					byte[] bytes = null;
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try {
						ObjectOutputStream oos = new ObjectOutputStream(bos);
						oos.writeObject(obj);
						oos.flush();
						bytes = bos.toByteArray();
						oos.close();
						bos.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					return bytes;
				}
				// 消息解析成对象 byte->object
				public static Object toObject(byte[] bytes) {
					Object obj = null;
					try {
						ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
						ObjectInputStream ois = new ObjectInputStream(bis);
						obj = ois.readObject();
						ois.close();
						bis.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (ClassNotFoundException ex) {
						ex.printStackTrace();
					}
					return obj;
				}
		
		// 判断是否有此人 更新
		public boolean judgeUser(Msg msg) {// false 表示不存在
			for (int i = 0; i < mainA.userList.size(); i++) 
			{
				if (mainA.userList.get(i).getIp().equals(msg.getSendUserIp())) 
				{
					// 如果存在 改名字
					if (!mainA.userList.get(i).getName().equals(msg.getSendUser()))
					{
						mainA.userList.get(i).setName(msg.getSendUser());// 该在线列表的名字
						mainA.adapterList.get(i).put("name", msg.getSendUser());
						//刷新列表													
						Tips(Tools.FLUSH,null);
					}
					if (mainA.userList.get(i).getHeadIcon()!=msg.getHeadIconPos())
					{
						mainA.userList.get(i).setHeadIcon(msg.getHeadIconPos());// 该在线列表的名字
						mainA.adapterList.get(i).put("headicon", Tools.headIconIds[msg.getHeadIconPos()]);
						//刷新列表													
						Tips(Tools.FLUSH,null);
					}
					return true;
				}
			}
			return false;
		}
		// 添加在线用户
		public void addUser(Msg msg) {
			User user = new User(msg.getSendUser(), msg.getSendUserIp(), 0);
			// 在线列表加人
			mainA.userList.add(user);
			// 为其创建聊天记录
			// Tools.MsgEx.put(msg.getSendUserIp(), "");
			// listView加人
			Map map = new HashMap<String, String>();
			map.put("name", user.getName());
			map.put("ip", "IP:"+user.getIp());
			map.put("UnReadMsgCount", "");
			map.put("headicon", Tools.headIconIds[user.getHeadIcon()]);
			// 刷新列表
			Tips(Tools.ADDUSER,map);
		}
		// Tips-Handler
		public static void Tips(int cmd,Object str) {
			Message m = new Message();
			m.what = cmd;
			m.obj = str;
			mainA.handler.sendMessage(m);
		}
		
		public static void TipsChat(int cmd,Object str)
		{
			Message m = new Message();
			m.what = cmd;
			m.obj = str;
			Tools.chart.handler.sendMessage(m);
		}
		public void fileProgress() {
			new Thread() { 
				public void run() {
	
					while (Tools.sendProgress != -1) {
						System.out.println("tools fileprogress");
						 Message m = new Message();
							m.what = Tools.PROGRESS_FLUSH;
						Tools.chart.handler.sendMessage(m);
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// 关闭进度条
					Message m1 = new Message();
					m1.what = Tools.PROGRESS_COL;
					Tools.chart.handler.sendMessage(m1);
				}
			}.start();
		}
		public static void out(String s)
		{
			Log.v("mes", s);
		}
		// 获取本机IP
		public static String getLocalHostIp() {
			String ipaddress = "";
			try {
				Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces();
				// 遍历所用的网络接口
				while (en.hasMoreElements()) {
					NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
					Enumeration<InetAddress> inet = nif.getInetAddresses();
					// 遍历每一个接口绑定的所有ip
					while (inet.hasMoreElements()) {
						InetAddress ip = inet.nextElement();
						if (!ip.isLoopbackAddress()
								&& InetAddressUtils.isIPv4Address(ip
										.getHostAddress())) {
							return ipaddress = ip.getHostAddress();
						}
					}

				}
			} catch (SocketException e) {
				System.out.print("获取IP失败");
				e.printStackTrace();
			}
			return ipaddress;

		}
}

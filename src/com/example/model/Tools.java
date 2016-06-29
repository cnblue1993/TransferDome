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
	// Э������
	public static final int CMD_ONLINE = 10;// ����
	public static final int CMD_REPLYONLINE = 11;// ��Ӧ����
	public static final int CMD_CHECK = 12;// �����㲥
	public static final int CMD_SENDMSG=13;// ������Ϣ
	public static final int CMD_STARTTALK=14;// ���ͺ�������
	public static final int CMD_STOPTALK=15;// ���ͽ�����������
	public static final int CMD_ACCEPTTALK=16;// ���ͽ��պ�������

	public static final int CMD_FILEREQUEST=20;//�������ļ�
	public static final int CMD_FILEACCEPT=21;//�����ļ�����
	public static final int CMD_FILEREFUSE=22;//�ܾ��ļ�����
	public static final int CMD_UPDATEINFORMATION=23;//������Ϣ
	public static final int PORT_SEND=2426;// ���Ͷ˿�
	public static final int PORT_RECEIVE=2425;// ���ն˿�
	public static final int PORT = 5760;//
	
	// ��Ϣ����
	public static final int MAINACTIVITY=7998;//��ǰ��MAINACTIVITY
	public static final int CHATACTIVITY=7999;//CHATACTIVITY
	public static final int ACTIVITY_MAIN=0;//mainA ���캯��ר��
	public static final int ACTIVITY_CHART=1;//mainB
	public static MainActivity mainA=null;
	public static ChatActivity chart=null;
	public static int State=Tools.MAINACTIVITY;//״̬����ʾ��ǰ��Ծactivity
	
	//�ļ�����ģ��
	public static String newfileName=null;
	public static long newfileSize=0;
	public static final int ISFILE=1001;//���ļ�
	public static String startPath = "/mnt";
	public static String newsavepath="/mnt/sdcard/ShareFiles";
	public static int byteSize = 1024*5;// ÿ�ζ�д�ļ����ֽ���
	public static double sendProgress = -1;// ÿ�ζ�д�ļ����ֽ���s
	public static final int FILE_JINDU=2001;//��������
	public static final int PROGRESS_FLUSH=2002;//���½���
	public static final int PROGRESS_COL=2003;//�رս�����
	
	// ��Ϣ����
	public static final int SHOW=8000;//��ʾ��Ϣ
	public static final int FLUSH=8001;//ˢ�½���
	public static final int ADDUSER=8002;//����û�
	public static final int DESTROYUSER=8003;//ɾ���û�
	public static final int RECEIVEMSG=8004;//ɾ���û�
	public static final int REFLESHCOUNT=8005;//���¼���
	public static String sign=":";
	
	public static User me=null;
	public static Map<String,List<Msg>> msgContainer = new HashMap<String,List<Msg>>();
	public static int[] headIconIds = {R.drawable.user, R.drawable.user,R.drawable.user,R.drawable.user,
		R.drawable.user,R.drawable.user,R.drawable.user,R.drawable.user,R.drawable.user,R.drawable.user};
	
	// ���캯��
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
	// ������Ϣ
		public void sendMsg(Msg msg)
		{
			(new UdpSend(msg)).start();
		}
		// ������Ϣ�߳�
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
					//Tools.out("���͹㲥֪ͨ����");
				} catch (Exception e) {
				}

			}
		}
		
		
		// ������Ϣ
		public void receiveMsg()
		{
			new UdpReceive().start();
		}
		// ������Ϣ�߳�
		class UdpReceive extends Thread {
			Msg msg=null;
			UdpReceive() {}

			public void run() {
				//��Ϣѭ��
				while(true)
				{
					try {
						DatagramSocket ds = new DatagramSocket(Tools.PORT_RECEIVE);
						byte[] data = new byte[1024 * 4];
						DatagramPacket dp = new DatagramPacket(data, data.length);
						dp.setData(data);
						ds.receive(dp);
						byte[] data2 = new byte[dp.getLength()];
						System.arraycopy(data, 0, data2, 0, data2.length);// �õ����յ�����
						Msg msg = (Msg) Tools.toObject(data2);
						System.out.println("receivethread+msg:"+msg);
						ds.close();
						//������Ϣ
						parse(msg);
					} catch (Exception e) {
					}
				}

			}
		}
		// ������Ϣ
		public void receiveMsg(Msg msg)
		{
			Tips(Tools.SHOW,msg.getSendUser() + " ������Ϣ��");
			if (!judgeUser(msg)) {// ����б��޴���
				this.addUser(msg);// �б���Ӵ���
			}
			if(Tools.State == Tools.MAINACTIVITY){
				Tools.out("mainactivity����");
				List<Msg> mes = null;
				if(msgContainer.containsKey(msg.getSendUserIp())){
					mes = msgContainer.get(msg.getSendUserIp());
					Tools.out("���ڻ���");
				}else{
					mes = new ArrayList<Msg>();
					Tools.out("������");
				}
				mes.add(msg);
				msgContainer.put(msg.getSendUserIp(), mes);
				Tools.out("���¼���");
				Tips(Tools.REFLESHCOUNT, msg.getSendUserIp());
			}
			if (Tools.State == Tools.CHATACTIVITY) {
				Tools.out("chat����");
				TipsChat(Tools.RECEIVEMSG, msg);
			}

		}
		// �������յ�
		public void parse(Msg msg)
		{
			switch (msg.getMsgType()) {
			case Tools.CMD_FILEACCEPT:
				//�յ�ȷ�Ͻ���
				String path = Tools.chart.choosePath;
				Tools.TipsChat(Tools.SHOW, "���ڷ����ļ�:" + new File(path).getName()); //toast��ʾ
				Tools.sendProgress=0;
				FileTcpClient tc0 = new FileTcpClient(msg, path);  //����tcp+socket
				tc0.start();
				Tools.TipsChat(Tools.FILE_JINDU, "�����ļ�"+Tools.sign+"���ڷ��ͣ�"+new File(path).getName()+Tools.sign+ (new File(path).length()));
				fileProgress();//�����������߳�
				break;
				
			case Tools.CMD_FILEREFUSE:
				//�յ��ܾ�����
				Tools.TipsChat(Tools.SHOW, "�Է��ܾ������ļ�");
				break;
				
			case Tools.CMD_FILEREQUEST:
				//�յ������ļ�����
				Tools.out("�յ��ļ���������");
				System.out.println("�յ��ļ���������");
				String[] newfileInfo = ((String) msg.getBody()).split(Tools.sign);
				Tools.newfileName = newfileInfo[0];// ��¼���ļ�����
				Tools.newfileSize = Long.parseLong(newfileInfo[1].trim());// �ļ���С
				if(Tools.State==Tools.MAINACTIVITY)
				{
					System.out.println("����main����");
					Tools.out("����main����");
					Tools.Tips(Tools.CMD_FILEREQUEST, msg); //mainAcitvityHandler
				}
				else if(Tools.State==Tools.CHATACTIVITY)
				{
					System.out.println("����chat����");
					Tools.out("����chat����");
					Intent it = new Intent(Tools.chart, MainActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					Tools.chart.startActivity(it);
					Tools.Tips(Tools.CMD_FILEREQUEST, msg);

				}
				break;

			case Tools.CMD_SENDMSG:// ���յ��Է����͵���Ϣ
				Tools.out(msg.getSendUser()+"����Ϣ��");
				receiveMsg(msg);
				break;
			case Tools.CMD_ONLINE:// ����
				upline(msg);
				break;
			case Tools.CMD_REPLYONLINE:// ��Ӧ����
				replyUpline(msg);
				break;
			}
		}
		// �õ��㲥ip, 192.168.0.255֮��ĸ�ʽ
		public static String getBroadCastIP() {
			String ip = getLocalHostIp().substring(0,
					getLocalHostIp().lastIndexOf(".") + 1)
					+ "255";
			return ip;
		}
		// ���յ����߹㲥
		public void upline(Msg msg){
			if (!judgeUser(msg)) {// ���������
				//Tips(Tools.SHOW,msg.getSendUser() + " ���ߡ�����");
				addUser(msg);// ��Ӵ���
			}
			// ������Ӧ����
			Msg msgsend=new Msg();
			msgsend.setSendUser(Tools.me.getName());
			msgsend.setSendUserIp(Tools.me.getIp());
			msgsend.setHeadIconPos(Tools.me.getHeadIcon());
			msgsend.setMsgType(Tools.CMD_REPLYONLINE);
			msgsend.setReceiveUserIp(msg.getSendUserIp());
			Tools.out(Tools.me.getIp()+"�ظ��㲥"+msg.getSendUserIp());
			// ������Ϣ
			sendMsg(msgsend);
		}
		// ������Ӧ����
		public void replyUpline(Msg msg){
			Tools.out("������Ӧ����"+msg.getSendUserIp());
			if (!judgeUser(msg)) {// ���������
				Tips(Tools.SHOW,msg.getSendUser() + " ���ߡ�����");
				addUser(msg);// ��Ӵ���
			}
		}
		// �����װ����Ϣ object->byte
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
				// ��Ϣ�����ɶ��� byte->object
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
		
		// �ж��Ƿ��д��� ����
		public boolean judgeUser(Msg msg) {// false ��ʾ������
			for (int i = 0; i < mainA.userList.size(); i++) 
			{
				if (mainA.userList.get(i).getIp().equals(msg.getSendUserIp())) 
				{
					// ������� ������
					if (!mainA.userList.get(i).getName().equals(msg.getSendUser()))
					{
						mainA.userList.get(i).setName(msg.getSendUser());// �������б������
						mainA.adapterList.get(i).put("name", msg.getSendUser());
						//ˢ���б�													
						Tips(Tools.FLUSH,null);
					}
					if (mainA.userList.get(i).getHeadIcon()!=msg.getHeadIconPos())
					{
						mainA.userList.get(i).setHeadIcon(msg.getHeadIconPos());// �������б������
						mainA.adapterList.get(i).put("headicon", Tools.headIconIds[msg.getHeadIconPos()]);
						//ˢ���б�													
						Tips(Tools.FLUSH,null);
					}
					return true;
				}
			}
			return false;
		}
		// ��������û�
		public void addUser(Msg msg) {
			User user = new User(msg.getSendUser(), msg.getSendUserIp(), 0);
			// �����б����
			mainA.userList.add(user);
			// Ϊ�䴴�������¼
			// Tools.MsgEx.put(msg.getSendUserIp(), "");
			// listView����
			Map map = new HashMap<String, String>();
			map.put("name", user.getName());
			map.put("ip", "IP:"+user.getIp());
			map.put("UnReadMsgCount", "");
			map.put("headicon", Tools.headIconIds[user.getHeadIcon()]);
			// ˢ���б�
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
					// �رս�����
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
		// ��ȡ����IP
		public static String getLocalHostIp() {
			String ipaddress = "";
			try {
				Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces();
				// �������õ�����ӿ�
				while (en.hasMoreElements()) {
					NetworkInterface nif = en.nextElement();// �õ�ÿһ������ӿڰ󶨵�����ip
					Enumeration<InetAddress> inet = nif.getInetAddresses();
					// ����ÿһ���ӿڰ󶨵�����ip
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
				System.out.print("��ȡIPʧ��");
				e.printStackTrace();
			}
			return ipaddress;

		}
}

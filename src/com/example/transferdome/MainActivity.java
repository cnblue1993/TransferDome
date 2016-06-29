package com.example.transferdome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.model.FileTcpServer;
import com.example.model.Msg;
import com.example.model.Tools;
import com.example.model.User;




public class MainActivity extends Activity {
	ListView listView;
	ImageView myicon;
	TextView titlename;
	TextView titleip;
	
	
	public List<User> userList = null;
	public List<Map<String, Object>> adapterList = null;
	SimpleAdapter adapter=null;

	Tools tools=null;
	Msg m=null;
	ProgressDialog proDia = null;
	Double fileSize=0.0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
      //��ʼ������
      		Tools.State=Tools.MAINACTIVITY;//״̬
      		Tools.mainA=this;
      		init();
      		
      		tools=new Tools(this,Tools.ACTIVITY_MAIN);
      		
      		//�㲥����(�����Լ�)
    		reBroad();
    		
    		// �������ն� ʱʱ���������б�
    		tools.receiveMsg();
      		
      		
    }

 // ��ʼ������
 	public void init()
 	{
 		myicon=(ImageView)findViewById(R.id.me_icon);
 		listView = (ListView) super.findViewById(R.id.listView);
 		// �б������
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				//ת��chatActivity
 				Intent it = new Intent(MainActivity.this, ChatActivity.class);
 				it.putExtra("person", userList.get(arg2));
 				MainActivity.this.startActivityForResult(it, 1);
 				// ��ǰ�˵�������ʾ��Ϣ����
 				for (int i = 0; i < adapterList.size(); i++) {
 					if (adapterList.get(i).get("ip").equals("IP:"+userList.get(arg2).getIp())) { // ����
 						adapterList.get(i).put("UnReadMsgCount", "");
 						tools.Tips(Tools.FLUSH, null);
 						Tools.out("����");
 						Tools.out("ת��chart");
 					}
 				}
 			}
 		});
 		// ��ʼ���Լ�
 		userList = new ArrayList<User>();
 		Tools.me = new User(Build.MODEL,Tools.getLocalHostIp(),0);

 		userList.add(Tools.me.getCopy());
 		
 		adapterList = new ArrayList<Map<String, Object>>();
 		Map map = new HashMap<String, Object>();
 		map.put("headicon", Tools.headIconIds[Tools.me.getHeadIcon()]);
 		map.put("name", Tools.me.getName());
 		map.put("ip", " IP:"+Tools.me.getIp());
 		map.put("UnReadMsgCount", "");
 		myicon.setImageResource(Tools.headIconIds[Tools.me.getHeadIcon()]);
 		
 		adapterList.add(map);
 		
 		//��ʼ��view������
 		adapter = new SimpleAdapter(this, adapterList, R.layout.item,
 				new String[] {"headicon","name", "ip", "UnReadMsgCount" }, new int[] {
 						R.id.headicon,R.id.name, R.id.ip, R.id.UnReadMsgCount });
 		listView.setAdapter(adapter);
 		
 		titlename=(TextView)findViewById(R.id.me_name);
 		titlename.setText(Tools.me.getName());
 		titleip = (TextView) findViewById(R.id.me_ip);
 		titleip.setText(Tools.me.getIp());
 		
 		proDia = new ProgressDialog(this);
 		proDia.setTitle("�ļ�����");// ���ñ���
 		proDia.setMessage("�ļ�");// ������ʾ��Ϣ
 		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// ˮƽ������
 		proDia.setMax(100);// ����������ָ
 		proDia.setProgress(10);// ��ʼ��
 		
 	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Tools.FLUSH:
					adapter.notifyDataSetChanged();
					break;
				case Tools.ADDUSER:
					adapterList.add((Map)msg.obj);
					adapter.notifyDataSetChanged();
					break;
				case Tools.DESTROYUSER:
					int i=(Integer) msg.obj;
					userList.remove(i);
					adapterList.remove(i);
					adapter.notifyDataSetChanged();
				case Tools.REFLESHCOUNT:
					String ip=msg.obj.toString();
					Tools.out("ˢ����Ŀ"+ip);
					for (int k = 0; k < adapterList.size(); k++) {
						if (adapterList.get(k).get("ip").equals("IP:"+ip)) 
						{ // ����
							if(Tools.msgContainer.get(ip)==null)
							{
								adapterList.get(k).put("UnReadMsgCount", "");	
							}
							else {
								adapterList.get(k).put("UnReadMsgCount", "δ��:"+Tools.msgContainer.get(ip).size());	
								Tools.out("�ҵ���:"+Tools.msgContainer.get(ip));
							}
						}
					}
					adapter.notifyDataSetChanged();
					break;
				case Tools.CMD_FILEREQUEST:
					//�ļ�����
					receiveFile((Msg)msg.obj);
					break;
				case Tools.FILE_JINDU:
					String[] pi = ((String) msg.obj).split(Tools.sign);
					fileSize = Double.parseDouble(pi[2]);
					proDia.setTitle(pi[0]);// ���ñ���
					proDia.setMessage(pi[1] + " ��С��"
							+ FileActivity.getFormatSize(fileSize));// ������ʾ��Ϣ
					proDia.onStart();
					proDia.show();
					break;
				case Tools.PROGRESS_FLUSH:
					System.out.println("main  progress flush");
					int i0 = (int) ((Tools.sendProgress / (fileSize)) * 100);
					proDia.setProgress(i0);
					break;
				case Tools.PROGRESS_COL:// �رս�����
					
					proDia.dismiss();
					break;
			}
		}
	};
	// �յ������ļ�����  �����ļ����նԻ���
	private void receiveFile(Msg mes)
	{
		this.m=mes;
		String str=mes.getBody().toString();
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("�Ƿ�����ļ���" + str.split(Tools.sign)[0] +" ��С��"+FileActivity.getFormatSize(Double.parseDouble(str.split(Tools.sign)[1])))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// �����ļ� ������ʾ���� ����tcp ������ �����ļ�
				FileTcpServer ts = new FileTcpServer(MainActivity.this);
				ts.start();
				Tools.sendProgress = 0;
				Message m1 = new Message();
				m1.what = Tools.FILE_JINDU;
				m1.obj = "�����ļ�" + Tools.sign + "���ڽ��գ�" + Tools.newfileName
						+ Tools.sign + Tools.newfileSize;
				
				handler.sendMessage(m1);
				fileProgress();// �����������߳�
				
				// ������Ϣ �öԷ���ʼ�����ļ�
				Msg msg=new Msg(0,Tools.me.getName(), Tools.me.getIp(), m.getSendUser(), m.getSendUserIp(),Tools.CMD_FILEACCEPT, null);
				
				tools.sendMsg(msg);
				
				return;
			}
		})
		.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// ������ ������ʾ������
				Msg msg=new Msg(0,Tools.me.getName(), Tools.me.getIp(), m.getSendUser(), m.getSendUserIp(),Tools.CMD_FILEREFUSE, null);
				tools.sendMsg(msg);
				return;
			}
		}).show();
	}
	
	// �ļ����ͽ�����
		public void fileProgress() {
			new Thread() {
				public void run() {
					while (Tools.sendProgress != -1) {
						System.out.println("file fileprogress");
						Message m = new Message();
						m.what = Tools.PROGRESS_FLUSH;
						handler.sendMessage(m);
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// �رս�����
					Message m1 = new Message();
					m1.what = Tools.PROGRESS_COL;
					handler.sendMessage(m1);
				}
			}.start();
		}
		@Override
	    protected void onResume() {
	    	super.onResume();
	    	//isPaused = false;
	    	Tools.out("Resume");
	    	reBroad();
			Tools.State=Tools.MAINACTIVITY;
	    }
	    @Override
	    protected void onPause() {
	    	super.onPause();
	    	//isPaused = false;
	    	Tools.out("PAUSE");
	    }
	    @Override
	    protected void onDestroy() {
	    	super.onDestroy();
	    	//isPaused=true;
	    	Tools.out("Destroy");
	    }
		//�㲥�Լ�
	    public void reBroad()
	    {
	    	//�㲥����(�����Լ�)
			Msg msg=new Msg();
			msg.setSendUser(Tools.me.getName());//�ǳ�Ĭ��Ϊ�Լ��Ļ�����
			msg.setHeadIconPos(Tools.me.getHeadIcon());
			msg.setSendUserIp(Tools.me.getIp());
			msg.setReceiveUserIp(Tools.getBroadCastIP());
			msg.setMsgType(Tools.CMD_ONLINE);//֪ͨ��������
			// ���͹㲥֪ͨ����
			tools.sendMsg(msg);
	    }
}

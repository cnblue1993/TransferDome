package com.example.transferdome;


import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.model.Msg;
import com.example.model.Tools;
import com.example.model.User;

public class ChatActivity extends Activity {
	public String choosePath = null;// 选中的文件
	ProgressDialog proDia = null;
	Double fileSize=0.0;
	User person = null;
	public Tools tools=null;
	
	private Button btn_send;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		Intent intent = getIntent();
		person = (User)intent.getExtras().getSerializable("person");
		System.out.println(person.getIp() + person.getName());
		
		Tools.State=Tools.CHATACTIVITY;
		Tools.chart=this;
		tools=new Tools(this,Tools.MAINACTIVITY);
		
		btn_send = (Button) findViewById(R.id.chat_send);
		btn_send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it = new Intent(ChatActivity.this, FileActivity.class);
				ChatActivity.this.startActivityForResult(it, 1);
			}
		});
		
		proDia = new ProgressDialog(this);
		proDia.setTitle("文件发送");// 设置标题
		proDia.setMessage("文件");// 设置显示信息
		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 水平进度条
		proDia.setMax(100);// 设置最大进度指
		proDia.setProgress(10);// 开始点
	}
	// Handler
		public Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Tools.SHOW:
					Toast.makeText(ChatActivity.this, (String) msg.obj,
							Toast.LENGTH_SHORT).show();
					break;
//				case Tools.RECEIVEMSG:
//					//接收消息
//					receiveMsg((Msg)msg.obj);
//					break;
				case Tools.FILE_JINDU:
					String[] pi = ((String) msg.obj).split(Tools.sign);
					fileSize = Double.parseDouble(pi[2]);
					proDia.setTitle(pi[0]);// 设置标题
					proDia.setMessage(pi[1] + " 大小："
							+ FileActivity.getFormatSize(fileSize));// 设置显示信息
					proDia.onStart();
					proDia.show();
					break;
				case Tools.PROGRESS_FLUSH:
					int i0 = (int) ((Tools.sendProgress / (fileSize)) * 100);
					proDia.setProgress(i0);
					break;
				case Tools.PROGRESS_COL:// 关闭进度条
					proDia.dismiss();
					break;	
				}
			}
		};

		// 发送文件取得路径
	 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	 		// TODO Auto-generated method stub
	 		switch (resultCode) {
	 		case RESULT_OK:
	 			choosePath = data.getStringExtra("path");
	 			System.out.println(choosePath);
	 			Tools.out("文件路径:"+choosePath);
	 			//发送请求传送文件
	 			Msg msg=new Msg(Tools.me.getHeadIcon(),Tools.me.getName(), Tools.me.getIp(), person.getName(), person.getIp(),Tools.CMD_FILEREQUEST, 
	 					(new File(choosePath)).getName()+Tools.sign+(new File(choosePath)).length());
	 			System.out.println("sendname" + Tools.me.getName());
	 			System.out.println("sendheadicon" + Tools.me.getHeadIcon());
	 			System.out.println("sendip" + Tools.me.getIp());
	 			System.out.println("name" + person.getName());
	 			System.out.println("headicon" + person.getHeadIcon());
	 			System.out.println("ip" + person.getIp());
	 			System.out.println((new File(choosePath)).getName()+Tools.sign+(new File(choosePath)).length());
	 			System.out.println("msg:"+msg);
	 			tools.sendMsg(msg);
	 			Tools.out("发送请求传送文件");
	 		}
	 	}
	 	@Override
	    protected void onResume() {
	    	super.onResume();
			Tools.State=Tools.CHATACTIVITY;
	    }
		@Override
	    protected void onPause() {
	    	super.onPause();
	    	Tools.State=Tools.MAINACTIVITY;
	    }
	    @Override
	    protected void onDestroy() {
	    	super.onDestroy();
	    	//Tools.pretime=0;
	    	Tools.out("destroy");
	    	Tools.State=Tools.MAINACTIVITY;
	    } 
}

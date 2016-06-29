package com.example.transferdome;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.model.Tools;

public class FileActivity extends Activity{

	private Button btn_send;
	private String choosePath = "/sdcard/ShareFiles/2.m4a";
	//private String choosePath = "/sdcard/DCIM/1.jpg";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
		btn_send = (Button) findViewById(R.id.send_file);
		btn_send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 返回主好友在线界面 取回选中的文件路径
				Intent it = FileActivity.this.getIntent();
				it.putExtra("path", choosePath);
				FileActivity.this.setResult(RESULT_OK, it);
				//Tools.startPath=FileActivity.getStartPath(this.choosePath);
				FileActivity.this.finish();
			}
		});
		
	}

		// 计算文件大小
		public static String getFormatSize(double size) {
			double kiloByte = size / 1024;
			if (kiloByte < 1) {
				return size + "Byte(s)";
			}

			double megaByte = kiloByte / 1024;
			if (megaByte < 1) {
				BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
				return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
						.toPlainString() + "KB";
			}

			double gigaByte = megaByte / 1024;
			if (gigaByte < 1) {
				BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
				return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
						.toPlainString() + "MB";
			}

			double teraBytes = gigaByte / 1024;
			if (teraBytes < 1) {
				BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
				return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
						.toPlainString() + "GB";
			}
			BigDecimal result4 = new BigDecimal(teraBytes);
			return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
					+ "TB";
		}
}

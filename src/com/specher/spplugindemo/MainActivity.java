package com.specher.spplugindemo;

/**
 * 此程序仅为演示SDK功能，并无实际用途
 * 2017年12月26日 Specher
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import com.specher.qqrobotsdk.QQRobot;
import com.specher.qqrobotsdk.data.MessageRecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	QQRobot qqrobot;
	TextView tv;
	ScrollView scrollView;
	String init=null;
	Handler handler = new Handler();
	//实例化一个广播接收器
	BroadcastReceiver rec= new BroadcastReceiver() {
		//这里为了方便用了匿名内部类，你也可以用独立的外部类来处理
		@Override
		public void onReceive(Context context, Intent intent) {
		// TODO 接收到消息，注意也会收到自己发送的消息，处理时请过滤自己的QQ
		int cmd = 	intent.getIntExtra(QQRobot.CMD,0);
		String qq  = intent.getStringExtra(QQRobot.ACTION_QQ);
		String group="", member="",msg="";
		
		//根据CMD分类消息
		switch(cmd){
		case QQRobot.CMD_GET_GROUP_MEMBER_NICKNAME://得到群昵称，和下面的参数一样所以共用
		case QQRobot.CMD_REC_GROUP_TXTMSG :
		group =	intent.getStringExtra(QQRobot.DEAL_UIN);
		member = intent.getStringExtra(QQRobot.DEAL_UIN2);
		//可以直接读取消息文本内容
		msg = intent.getStringExtra(QQRobot.DEAL_STR);
		
		//也通过传递过来的Parcel对象可以解析消息的详细信息
		 if(intent.hasExtra(QQRobot.ParcelObj)){
		 MessageRecord messageRecord1  =  (MessageRecord) intent.getSerializableExtra(QQRobot.ParcelObj);
		//通过msgtype可以判断消息类型的更多信息,比如加群消息
		 msg = messageRecord1.toString();
		 }
			break;
		case QQRobot.CMD_REC_FRIEND_TXTMSG:	 
			member = intent.getStringExtra(QQRobot.DEAL_UIN2);
			//可以直接读取消息文本内容
			msg = intent.getStringExtra(QQRobot.DEAL_STR);
			//也通过传递过来的Parcel对象可以解析消息的详细信息
			if(intent.hasExtra(QQRobot.ParcelObj)){
			 MessageRecord messageRecord  = (MessageRecord) intent.getSerializableExtra(QQRobot.ParcelObj);
			 //通过msgtype可以判断消息类型的更多信息,比如加群消息
			//if( messageRecord.msgtype == messageRecord.MSG_TYPE_TROOP_TIPS_ADD_MEMBER);
			 msg = messageRecord.toString();
			 }
		break;
		case QQRobot.CMD_GET_CURRENT_NICKNAME:
			msg = intent.getStringExtra(QQRobot.DEAL_STR);
			break;
		case QQRobot.CMD_GET_CURRENT_QQ:
			msg = intent.getStringExtra(QQRobot.DEAL_STR);
			break;
		case QQRobot.CMD_INIT:
			
			init = intent.getStringExtra(QQRobot.DEAL_STR);
			msg = init;
			break;
		}
		
		//更新UI最好用Handler，这里只为了演示就不写了
		tv.setText(tv.getText() +"\n"
		+(
		(getTimeText())
		+(group==""?"": group+ "|") 
		+ (member==""?"":member + "|") 
		+  ( msg==""?"":msg)
		)
		);//显示出收到的消息
		
		//通过handler在新线程中更新
		handler.post(new Runnable() {  
		    @Override  
		    public void run() {  	
		    	//滚动到底部
		        scrollView.fullScroll(ScrollView.FOCUS_DOWN);  
		    }  
		}); 
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.textView1);
		scrollView = (ScrollView) findViewById(R.id.scrollView1);
		
		//实例化一个QQ机器人对象
		qqrobot = new QQRobot(this,rec);
		
		//初始化QQ机器人插件
		qqrobot.initQQRobot();
		
		//检测是否初始化成功
		 new Handler().postDelayed(new Runnable(){
             public void run() {
            	 if(init==null){ 
            		 tv.setText("初始化插件失败！");
            	 }
             }
		 }, 2000);//延迟2秒执行
	
	}

	public static String getTimeText(){
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");  
		Date date =  new Date();
		return "["+format.format(date)+"]";   
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//取昵称按钮被点击
	public void onGetNickClick(View v){
		qqrobot.getCurrentNickName();
	}
	//取QQ按钮被点击
	public void onGetQQClick(View v){
		qqrobot.getCurrentQQ();
	}
	//禁言按钮被点击
	public void onGagTestClick(View v){
		//构造一个对话框
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("禁言测试");
        View dlgview= getLayoutInflater().inflate(R.layout.testlayout, null);
        builder2.setView(dlgview);
        final AlertDialog alertDialog =  builder2.create();
        final EditText text1 = (EditText) dlgview.findViewById(R.id.editText1);
        final EditText text2 = (EditText) dlgview.findViewById(R.id.editText2);
        text1.setHint("群号+空格+QQ号 例如123456 10000");
        text2.setHint("禁言时间（秒）");
        final CheckBox cb1=(CheckBox) dlgview.findViewById(R.id.checkBox1);
        cb1.setVisibility(View.GONE);
        Button btn1 = (Button) dlgview.findViewById(R.id.button1);
        Button btn2 = (Button) dlgview.findViewById(R.id.button2);
        btn1.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				
			String[] a = 	text1.getText().toString().split(" ");
			if(a.length==2){
				qqrobot.doGagMember(a[0], a[1], text2.getText().toString());
			}else{
				Toast.makeText(MainActivity.this, "QQ格式错误",Toast.LENGTH_SHORT).show();
			}
					
			}
		});
        btn2.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				alertDialog.dismiss();
			}
		});
      
        
       alertDialog.show();
	}
	
	
	//发消息按钮被点击
	public void onMsgTestClick(View v){
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("消息测试");
        View dlgview= getLayoutInflater().inflate(R.layout.testlayout, null);
        builder2.setView(dlgview);
        final AlertDialog alertDialog =  builder2.create();
        final EditText text1 = (EditText) dlgview.findViewById(R.id.editText1);
        final EditText text2 = (EditText) dlgview.findViewById(R.id.editText2);
        final CheckBox cb1=(CheckBox) dlgview.findViewById(R.id.checkBox1);
        Button btn1 = (Button) dlgview.findViewById(R.id.button1);
        Button btn2 = (Button) dlgview.findViewById(R.id.button2);
        btn1.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				if(!cb1.isChecked()){
					qqrobot.sendFriendTxt(QQRobot.DEFAULT_ACTION_QQ, text1.getText().toString(), text2.getText().toString());
				}else{
					qqrobot.sendGroupTxt(QQRobot.DEFAULT_ACTION_QQ, text1.getText().toString(), text2.getText().toString());
				}		
			}
		});
        
        btn2.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				alertDialog.dismiss();
			}
		});
      
        
       alertDialog.show();
	}
	
	
	//发消息带艾特被点击
	public void onAtMsgClick(View v){
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("艾特测试");
        View dlgview= getLayoutInflater().inflate(R.layout.testlayout2, null);
        builder2.setView(dlgview);
        final AlertDialog alertDialog =  builder2.create();
        final EditText text1 = (EditText) dlgview.findViewById(R.id.editText1);
        final EditText text2 = (EditText) dlgview.findViewById(R.id.editText2);
        final EditText text3 = (EditText) dlgview.findViewById(R.id.editText3);
        Button btn1 = (Button) dlgview.findViewById(R.id.button1);
        Button btn2 = (Button) dlgview.findViewById(R.id.button2);
        btn1.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				
					qqrobot.sendGroupTxtWithAT(QQRobot.DEFAULT_ACTION_QQ, text1.getText().toString(), text2.getText().toString(),text3.getText().toString());
			
			}
		});
        
        btn2.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				alertDialog.dismiss();
			}
		});
      
        
       alertDialog.show();
	}
	
	// 取群成员昵称被单击
	public void onGetMemberNickClick(View v){

		//构造一个对话框
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("取群成员昵称测试");
        View dlgview= getLayoutInflater().inflate(R.layout.testlayout, null);
        builder2.setView(dlgview);
        final AlertDialog alertDialog =  builder2.create();
        final EditText text1 = (EditText) dlgview.findViewById(R.id.editText1);
        final EditText text2 = (EditText) dlgview.findViewById(R.id.editText2);
        text1.setHint("群号");
        text2.setHint("群成员QQ号");
        final CheckBox cb1=(CheckBox) dlgview.findViewById(R.id.checkBox1);
        cb1.setVisibility(View.GONE);
        Button btn1 = (Button) dlgview.findViewById(R.id.button1);
        Button btn2 = (Button) dlgview.findViewById(R.id.button2);
        btn1.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				qqrobot.getGroupMemberNickName(text1.getText().toString(),text2.getText().toString());
			
					
			}
		});
        btn2.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				alertDialog.dismiss();
			}
		});
      
        
       alertDialog.show();
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_exit) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	protected void onDestroy() {
	//取消注册广播接收器，记得在界面关闭的时候注销广播，不然会有多个广播接受者存在，程序也会报错
		this.unregisterReceiver(rec);
		super.onDestroy();
	}
	
	
}

package com.example.ping_test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button btn_ping;
	EditText et_ip;
	private int CHOOSE = 0;
	TextView tv_show;
	String lost = "";// 丢包
	String delay = "";// 延迟
	String ip_adress = "";// ip地址
	String countCmd = "";// ping -c
	String sizeCmd = "", timeCmd = "";// ping -s ;ping -i
	String result = "";
	private static final String tag = "TAG";// Log标志
	int status = -1;// 状态
	String ping, ip, count, size, time;
	long delaytime = 0;
	// Myhandler handler=null;
	Handler handler1 = null;
	Thread a = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn_ping = (Button) findViewById(R.id.btn_ping);
		et_ip = (EditText) findViewById(R.id.edit_ip);

		btn_ping.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				
				ip=et_ip.getText().toString();
				String countCmd = " -c " + "4" + " ";
				String sizeCmd = " -s " + "64" + " ";
				String timeCmd = " -i " + "1" + " ";
				String ip_adress = ip;
				ping = "ping" + countCmd + timeCmd + sizeCmd + ip_adress;


					tv_show = (TextView) findViewById(R.id.tv_show);

					delaytime = (long) Double.parseDouble(String.valueOf(1.0));
					Log.i(tag, "====MainThread====:" + Thread.currentThread().getId());


					handler1 = new Handler() {// 创建一个handler对象 ，用于监听子线程发送的消息
						public void handleMessage(Message msg)// 接收消息的方法
						{
							// String str = (String) msg.obj;// 类型转化
							// tv_show.setText(str);// 执行
							switch (msg.what) {
								case 10:
									String resultmsg = (String) msg.obj;
									tv_show.append(resultmsg);
									Log.i(tag, "====handlerThread====:"
											+ Thread.currentThread().getId());
									Log.i(tag, "====resultmsg====:" + msg.what);
									Log.i(tag, "====resultmsg====:" + resultmsg);
									break;
								default:
									break;
							}
						}
					};

					a = new Thread()// 创建子线程
					{
						public void run() {
							// for (int i = 0; i < 100; i++) {
							// try {
							// sleep(500);
							// } catch (InterruptedException e) {
							// // TODO Auto-generated catch block
							// e.printStackTrace();
							// }
							// Message msg = new Message();// 创建消息类
							// msg.obj = "线程进度 ：" + i;// 消息类对象中存入消息
							// handler1.sendMessage(msg);// 通过handler对象发送消息
							// }
							delay = "";
							lost = "";

							Process process = null;
							BufferedReader successReader = null;
							BufferedReader errorReader = null;

							DataOutputStream dos = null;
							try {
								// 闃诲澶勭悊
								process = Runtime.getRuntime().exec(ping);
								// dos = new DataOutputStream(process.getOutputStream());
								Log.i(tag, "====receive====:");



								// status = process.waitFor();
								InputStream in = process.getInputStream();

								OutputStream out = process.getOutputStream();
								// success

								successReader = new BufferedReader(
										new InputStreamReader(in));

								// error
								errorReader = new BufferedReader(new InputStreamReader(
										process.getErrorStream()));

								String lineStr;

								while ((lineStr = successReader.readLine()) != null) {

									Log.i(tag, "====receive====:" + lineStr);
									Message msg = handler1.obtainMessage();
									msg.obj = lineStr + "\r\n";
									msg.what = 10;
									msg.sendToTarget();
									result = result + lineStr + "\n";
									if (lineStr.contains("packet loss")) {
										Log.i(tag, "=====Message=====" + lineStr.toString());
										int i = lineStr.indexOf("received");
										int j = lineStr.indexOf("%");
										Log.i(tag,
												"====丢包率====:"
														+ lineStr.substring(i + 10, j + 1));//
										lost = lineStr.substring(i + 10, j + 1);
									}
									if (lineStr.contains("avg")) {
										int i = lineStr.indexOf("/", 20);
										int j = lineStr.indexOf(".", i);
										Log.i(tag,
												"====平均时延:===="
														+ lineStr.substring(i + 1, j));
										delay = lineStr.substring(i + 1, j);
										delay = delay + "ms";
									}
									// tv_show.setText("丢包率:" + lost.toString() + "\n" +
									// "平均时延:"
									// + delay.toString() + "\n" + "IP地址:");// +
									// getNetIpAddress()
									// + getLocalIPAdress() + "\n" + "MAC地址:" +
									// getLocalMacAddress() + getGateWay());
									sleep(delaytime * 1000);
								}
								// tv_show.setText(result);

								while ((lineStr = errorReader.readLine()) != null) {
									Log.i(tag, "==error======" + lineStr);
									// tv_show.setText(lineStr);
								}

							} catch (IOException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								try {
									if (dos != null) {
										dos.close();
									}
									if (successReader != null) {
										successReader.close();
									}
									if (errorReader != null) {
										errorReader.close();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}

								if (process != null) {
									process.destroy();
								}
							}
						}
					};
					a.start();






			}
		});
	}

}

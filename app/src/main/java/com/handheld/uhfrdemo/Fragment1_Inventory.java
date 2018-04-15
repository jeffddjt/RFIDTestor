package com.handheld.uhfrdemo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.handheld.uhfr.R;
import com.uhf.api.cls.Reader.TAGINFO;
import com.uhf.scanlable.UHfData.UHfGetData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Fragment1_Inventory extends Fragment implements OnCheckedChangeListener, OnClickListener {
	private static String TAG = Fragment1_Inventory.class.getSimpleName();

	private View view;// this fragment UI
	private TextView tvTagCount;// tag count text view
	private TextView tvTagSum;// tag sum text view
	private ListView lvEpc;// epc list view
	private Button btnStart;// inventory button
	private Button btnClear;// clear button
	private Button btnUpload;//upload button
	private CheckBox checkMulti;// multi model check box

	private Set<String> epcSet = null; // store different EPC
	private List<EpcDataModel> listEpc = null;// EPC list
	private Map<String, Integer> mapEpc = null; // store EPC position
	private EPCadapter adapter;// epc list adapter

	private boolean isMulti = true;// multi mode flag
	private int allCount = 0;// inventory count

	private long lastTime = 0L;// record play sound time
	// handler

	private static class MyHandler extends Handler {
		private WeakReference<Fragment1_Inventory> mWeakReference;

		public MyHandler(Fragment1_Inventory fragment1_Inventory) {
			mWeakReference = new WeakReference<Fragment1_Inventory>(fragment1_Inventory);
		}

		@Override
		public void handleMessage(Message msg) {
			Fragment1_Inventory fragment1_Inventory = mWeakReference.get();
			switch (msg.what) {
			case 1:
				String epc = msg.getData().getString("epc");
				String rssi = msg.getData().getString("rssi");
				if (epc == null || epc.length() == 0) {
					epc = "";
				}
				int position;
				fragment1_Inventory.allCount++;

				if (fragment1_Inventory.epcSet == null) {// first add
					fragment1_Inventory.epcSet = new HashSet<String>();
					fragment1_Inventory.listEpc = new ArrayList<EpcDataModel>();
					fragment1_Inventory.mapEpc = new HashMap<String, Integer>();
					fragment1_Inventory.epcSet.add(epc);
					fragment1_Inventory.mapEpc.put(epc, 0);
					EpcDataModel epcTag = new EpcDataModel();
					epcTag.setepc(epc);
					epcTag.setrssi(rssi);
					epcTag.setCount(1);
					fragment1_Inventory.listEpc.add(epcTag);
					fragment1_Inventory.adapter = new EPCadapter(fragment1_Inventory.getActivity(),
							fragment1_Inventory.listEpc);
					fragment1_Inventory.lvEpc.setAdapter(fragment1_Inventory.adapter);
					Util.play(1, 0);
					MainActivity.mSetEpcs = fragment1_Inventory.epcSet;
				} else {
					if (fragment1_Inventory.epcSet.contains(epc)) {// set already exit
						position = fragment1_Inventory.mapEpc.get(epc);
						EpcDataModel epcOld = fragment1_Inventory.listEpc.get(position);
						epcOld.setCount(epcOld.getCount() + 1);
						epcOld.setrssi(rssi);
						fragment1_Inventory.listEpc.set(position, epcOld);
					} else {
						fragment1_Inventory.epcSet.add(epc);
						fragment1_Inventory.mapEpc.put(epc, fragment1_Inventory.listEpc.size());
						EpcDataModel epcTag = new EpcDataModel();
						epcTag.setepc(epc);
						epcTag.setrssi(rssi);
						epcTag.setCount(1);
						fragment1_Inventory.listEpc.add(epcTag);
						MainActivity.mSetEpcs = fragment1_Inventory.epcSet;
					}

					if (System.currentTimeMillis() - fragment1_Inventory.lastTime > 100) {
						fragment1_Inventory.lastTime = System.currentTimeMillis();
						Util.play(1, 0);
					}
					fragment1_Inventory.tvTagCount.setText("" + fragment1_Inventory.allCount);
					fragment1_Inventory.tvTagSum.setText("" + fragment1_Inventory.listEpc.size());
					fragment1_Inventory.adapter.notifyDataSetChanged();

				}

				//sendToCloud(epc,rssi);

				break;
			}

			super.handleMessage(msg);
		}

//		private  static void sendToCloud(String epc,String rssi){
//			OkHttpClient client =new OkHttpClient();
//			FormBody body = new FormBody.Builder()
//					.add("epc",epc)
//					.add("rssi",rssi)
//					.build();
//			Request request=new Request.Builder()
//					.url("http://192.168.3.2:52280/api/rfid/add")
//					.post(body)
//					.build();
//			Call call=client.newCall(request);
//			call.enqueue(new okhttp3.Callback() {
//				@Override
//				public void onFailure(Call call, IOException e) {
//
//				}
//
//				@Override
//				public void onResponse(Call call, Response response) throws IOException {
//
//				}
//			});
//
//		}
	}

	private Handler handler = new MyHandler(Fragment1_Inventory.this);

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("f1", "create view");
		view = inflater.inflate(R.layout.fragment_inventory, null);
		initView();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.rfid.FUN_KEY");
		getActivity().registerReceiver(keyReceiver, filter);

		return view/* super.onCreateView(inflater, container, savedInstanceState) */;
	}

	private void initView() {
		tvTagCount = (TextView) view.findViewById(R.id.textView_tag_count);
		lvEpc = (ListView) view.findViewById(R.id.listView_epc);
		btnStart = (Button) view.findViewById(R.id.button_start);
		tvTagSum = (TextView) view.findViewById(R.id.textView_tag);
		checkMulti = (CheckBox) view.findViewById(R.id.checkBox_multi);
		checkMulti.setOnCheckedChangeListener(this);
		checkMulti.setChecked(false);
		btnClear = (Button) view.findViewById(R.id.button_clear_epc);
		btnUpload = (Button) view.findViewById(R.id.button_upload_epc);

		lvEpc.setFocusable(false);
		lvEpc.setClickable(false);
		lvEpc.setItemsCanFocus(false);
		lvEpc.setScrollingCacheEnabled(false);
		lvEpc.setOnItemClickListener(null);
		btnStart.setOnClickListener(this);
		btnClear.setOnClickListener(this);
		btnUpload.setOnClickListener(this);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		// Log.e("f1","destroy view");
		if (isStart) {
			isStart = false;
			isRunning = false;
			MainActivity.mUhfrManager.stopTagInventory();
		}
		getActivity().unregisterReceiver(keyReceiver);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// Log.e("f1","pause");
		if (isStart) {
			runInventory();
		}
	}

	private boolean f1hidden = false;

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		f1hidden = hidden;
		// Log.e("hidden", "hide"+hidden) ;
		if (hidden) {
			if (isStart)
				runInventory();// stop inventory
		}
		if (MainActivity.mUhfrManager != null)
			MainActivity.mUhfrManager.setCancleInventoryFilter();
	}

	private boolean isRunning = false;
	private boolean isStart = false;
	byte[] epc;
	// inventory epc
	private Runnable inventoryTask = new Runnable() {
		@Override
		public void run() {
			while (isRunning) {
				if (isStart) {
					List<TAGINFO> list1;
					if (isMulti) { // multi mode
						Log.i(TAG, "run, multi mode >>>>>> ");
						list1 = MainActivity.mUhfrManager.tagInventoryRealTime();

//						list1 = MainActivity.mUhfrManager.tagInventoryByTimer((short) 50);
					} else {
						// sleep can save electricity
						// try {
						// Thread.sleep(250);
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }
						// list1 = MainActivity.mUhfrManager.tagInventoryByTimer((short) 50);
						// list1=MainActivity.mUhfrManager.tagEpcOtherInventoryByTimer((short)
						// 50,3,1,12,new byte[4]);
						// MainActivity.mUhfrManager.setCancleInventoryFilter();
						list1 = MainActivity.mUhfrManager.tagInventoryByTimer((short) 50);
						// get epc and tid :
						Log.i(TAG, "run, single mode, list1.size>>>>>> " + list1.size());
						// list1 = MainActivity.mUhfrManager.tagEpcTidInventoryByTimer((short) 50);
					}
					Log.i(TAG, "list1.size = " + list1.size());
					if (list1 != null && list1.size() > 0) {
						for (TAGINFO tfs : list1) {
							epc = tfs.EpcId;
							int rssi = tfs.RSSI;

							Log.i(TAG, "enclosing_method >>>>>> PC = " + UHfGetData.bytesToHexString(tfs.PC));
							Log.i(TAG, "enclosing_method >>>>>> CRC = " + UHfGetData.bytesToHexString(tfs.CRC));
							Log.i(TAG, "enclosing_method >>>>>> EPC = " + UHfGetData.bytesToHexString(tfs.EpcId));
							Log.i(TAG, "enclosing_method >>>>>> RSSI = " + String.valueOf(tfs.RSSI));
							Log.i(TAG, "enclosing_method >>>>>> EmbededData = "
									+ UHfGetData.bytesToHexString(tfs.EmbededData));
							Log.i(TAG, "enclosing_method >>>>>> EmbededData.Length = " + tfs.EmbededDatalen);

							//fbank: 1 epc,2 tid ,3 user
							byte[] readBytes = MainActivity.mUhfrManager
		                            .getTagDataByFilter(3, 0, 1,
		                            		Tools.HexString2Bytes("00000000"), (short) 1000, epc,
		                                    1, 2, true);
							Log.i(TAG, "enclosing_method >>>>>> readBytes = " + UHfGetData.bytesToHexString(readBytes));
//							byte[] passwordBytes = MainActivity.mUhfrManager.getTagDataByFilter(0, 0, 4,
//									Tools.HexString2Bytes("00000000"), (short) 1000, tfs.EmbededData, 2, 0, true);
//
//							Log.i(TAG,
//									"enclosing_method >>>>>> password = " + UHfGetData.bytesToHexString(passwordBytes));

							Message msg = new Message();
							msg.what = 1;
							Bundle b = new Bundle();
							b.putString("epc", Tools.Bytes2HexString(epc, epc.length));
							b.putString("rssi", rssi + "");
							msg.setData(b);
							handler.sendMessage(msg);
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	private boolean keyControl = true;

	private void runInventory() {
		if (keyControl) {
			keyControl = false;
			if (!isStart) {
				MainActivity.mUhfrManager.setCancleInventoryFilter();
				// MainActivity.mUhfrManager.setInventoryFilter(Tools.HexString2Bytes("3008"),
				// 3, 0, false);
				isRunning = true;
				if (isMulti) {
					//1200ģ��֧�ֿ���ģʽ�����ÿ���ģʽ���̵��ǩ�ٶȴ����ߡ�����Ҳ��Ҫע��ģ���¶�
//					MainActivity.mUhfrManager.setFastMode();
					MainActivity.mUhfrManager.asyncStartReading();
				} else {
//					MainActivity.mUhfrManager.setFastMode();
				}
				new Thread(inventoryTask).start();
				checkMulti.setClickable(false);
				checkMulti.setTextColor(Color.GRAY);
				btnStart.setText(getResources().getString(R.string.stop_inventory_epc));
				// Log.e("inventoryTask", "start inventory") ;
				isStart = true;
			} else {
				checkMulti.setClickable(true);
				checkMulti.setTextColor(Color.BLACK);
				if (isMulti) {
					MainActivity.mUhfrManager.asyncStopReading();
				}
				else {
					MainActivity.mUhfrManager.stopTagInventory();
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRunning = false;
				btnStart.setText(getResources().getString(R.string.start_inventory_epc));
				isStart = false;
			}
			keyControl = true;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked)
			isMulti = true;
		else
			isMulti = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_start:
			runInventory();
			break;
		case R.id.button_clear_epc:
			clearEpc();
			break;
		case R.id.button_upload_epc:
			uploadEpc();
			break;
		}
	}

	private void uploadEpc(){
		//JSONArray array = new JSONArray();
		//try {
			for (EpcDataModel model : this.listEpc) {
//				JSONObject obj = new JSONObject();
//				obj.put("id", model.getepcid());
//				obj.put("epc", model.getepc());
//				obj.put("rssi", model.getrssi());
//				array.put(obj);
//					}

//				RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), array.toString());
				FormBody body=new FormBody.Builder()
						.add("epc",model.getepc())
						.add("rssi",model.getrssi())
						.build();
				Request request = new Request.Builder()
						.url("http://rfid.bclzdd.com/api/rfid/add")
						.post(body)
						.build();
				OkHttpClient client = new OkHttpClient();
				Call call = client.newCall(request);
				call.enqueue(new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {

					}
				});
			}
			//Toast.makeText(this.getActivity(),array.toString(), Toast.LENGTH_SHORT).show();
		//}catch(Exception e){
		//	e.printStackTrace();
		//}

	}

	private void clearEpc() {
		if (epcSet != null) {
			epcSet.removeAll(epcSet); // store different EPC
		}
		if (listEpc != null)
			listEpc.removeAll(listEpc);// EPC list
		if (mapEpc != null)
			mapEpc.clear(); // store EPC position
		if (adapter != null)
			adapter.notifyDataSetChanged();
		allCount = 0;
		tvTagSum.setText("0");
		tvTagCount.setText("0");
		MainActivity.mSetEpcs.clear();

		Request request=new Request.Builder()
                .url("http://rfid.bclzdd.com/api/rfid/clear")
                .build();
		OkHttpClient client=new OkHttpClient();
		Call call=client.newCall(request);
		call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

		// lvEpc.removeAllViews();
	}

	// key receiver
	private long startTime = 0;
	private boolean keyUpFalg = true;
	private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (f1hidden)
				return;
			int keyCode = intent.getIntExtra("keyCode", 0);
			if (keyCode == 0) {// H941
				keyCode = intent.getIntExtra("keycode", 0);
			}
			// Log.e("key ","keyCode = " + keyCode) ;
			boolean keyDown = intent.getBooleanExtra("keydown", false);
			// Log.e("key ", "down = " + keyDown);
			if (keyUpFalg && keyDown && System.currentTimeMillis() - startTime > 500) {
				keyUpFalg = false;
				startTime = System.currentTimeMillis();
				if ((keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2 || keyCode == KeyEvent.KEYCODE_F3
						|| keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_F5)) {
					Log.i(TAG, "enclosing_method, keyCode >>>>>> " + keyCode);
					runInventory();
				}
				return;
			} else if (keyDown) {
				startTime = System.currentTimeMillis();
			} else {
				keyUpFalg = true;
			}

		}
	};

}

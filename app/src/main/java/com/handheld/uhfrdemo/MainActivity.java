package com.handheld.uhfrdemo;

import java.util.Set;

import com.handheld.uhfr.R;
import com.handheld.uhfr.UHFRManager;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

	private FragmentManager mFm; // fragment manager
	private FragmentTransaction mFt;// fragment transaction
	private Fragment1_Inventory fragment1;//
	private Fragment2_ReadAndWrite fragment2;
	private Fragment3_Lock fragment3;
	private Fragment4_Kill fragment4;
	private Fragment5_Settings fragment5;
	// private ArrayList<Fragment> fragments;

	public static UHFRManager mUhfrManager;// uhf
	public static Set<String> mSetEpcs; // epc set ,epc list

	private TextView textView_title;
	private TextView textView_f1;
	private TextView textView_f2;
	private TextView textView_f3;
	private TextView textView_f4;
	private TextView textView_f5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView(); // Init UI

		Util.initSoundPool(this);// Init sound pool
	}

	@Override
	protected void onResume() {
		super.onResume();
		mUhfrManager = UHFRManager.getIntance(this);// Init Uhf module
		if (mUhfrManager != null) {
			Util.showToast(MainActivity.this, getString(R.string.inituhfsuccess));
		} else {
			Util.showToast(MainActivity.this, getString(R.string.inituhffail));
		}
		// getRfidTest();
	}

	// when
	@Override
	protected void onPause() {
		super.onPause();
		if (mUhfrManager != null) {// close uhf module
			mUhfrManager.close();
			mUhfrManager = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mUhfrManager != null) {// close uhf module
			mUhfrManager.close();
			mUhfrManager = null;
		}

	}

	private void initView() {
		// Log.e("main","init view" );
		fragment1 = new Fragment1_Inventory();
		mFragmentCurrent = fragment1;
		fragment2 = new Fragment2_ReadAndWrite();
		fragment3 = new Fragment3_Lock();
		fragment4 = new Fragment4_Kill();
		fragment5 = new Fragment5_Settings();
		// fragments = new ArrayList<Fragment>();
		// fragments.add(fragment1);
		// fragments.add(fragment2);
		// fragments.add(fragment3);
		// fragments.add(fragment4);
		// fragments.add(fragment5);

		mFm = getSupportFragmentManager();
		mFt = mFm.beginTransaction();
		mFt.add(R.id.framelayout_main, fragment1);
		mFt.commit();

		textView_title = (TextView) findViewById(R.id.title);
		textView_f1 = (TextView) findViewById(R.id.textView_f1);
		textView_f2 = (TextView) findViewById(R.id.textView_f2);
		textView_f3 = (TextView) findViewById(R.id.textView_f3);
		textView_f4 = (TextView) findViewById(R.id.textView_f4);
		textView_f5 = (TextView) findViewById(R.id.textView_f5);
		textView_f1.setOnClickListener(this);
		textView_f2.setOnClickListener(this);
		textView_f3.setOnClickListener(this);
		textView_f4.setOnClickListener(this);
		textView_f5.setOnClickListener(this);
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
		if (id == R.id.action_about) {
			PackageManager packageManager = getPackageManager();
			PackageInfo packInfo = null;
			try {
				packInfo = packageManager.getPackageInfo(getPackageName(), 0);
				String version = packInfo.versionName;// get this version
				Util.showToast(MainActivity.this,
						"Version:" + version + "\nDate:" + "2017-05-20" + "\nType:" + mUhfrManager.getHardware());
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private Fragment mFragmentCurrent;

	// switch fragments
	public void switchContent(Fragment to) {
		// Log.e("switch",""+to.getId());
		textView_f1.setTextColor(getResources().getColor(R.color.gre));
		textView_f2.setTextColor(getResources().getColor(R.color.gre));
		textView_f3.setTextColor(getResources().getColor(R.color.gre));
		textView_f4.setTextColor(getResources().getColor(R.color.gre));
		textView_f5.setTextColor(getResources().getColor(R.color.gre));
		if (mFragmentCurrent != to) {
			mFt = mFm.beginTransaction();
			if (!to.isAdded()) { //
				mFt.hide(mFragmentCurrent).add(R.id.framelayout_main, to).commit(); //
			} else {
				mFt.hide(mFragmentCurrent).show(to).commit(); //
			}
			mFragmentCurrent = to;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textView_f1:
			switchContent(fragment1);
			textView_f1.setTextColor(getResources().getColor(R.color.blu));
			textView_title.setText(R.string.inventory_epc);
			break;
		case R.id.textView_f2:
			switchContent(fragment2);
			textView_f2.setTextColor(getResources().getColor(R.color.blu));
			textView_title.setText(R.string.read_write_tag);
			break;
		case R.id.textView_f3:
			switchContent(fragment3);
			textView_f3.setTextColor(getResources().getColor(R.color.blu));
			textView_title.setText(R.string.lock);
			break;
		case R.id.textView_f4:
			switchContent(fragment4);
			textView_f4.setTextColor(getResources().getColor(R.color.blu));
			textView_title.setText(R.string.kill);
			break;
		case R.id.textView_f5:
			switchContent(fragment5);
			textView_f5.setTextColor(getResources().getColor(R.color.blu));
			textView_title.setText(R.string.setting_);
			break;
		}
	}

	/**
	 * double clcick to exit
	 */
	private long lastClickTimeMillis = 0;

	@Override
	public void onBackPressed() {
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - lastClickTimeMillis <= 2000) {
			// cancel toast
			Util.mToast.cancel();
			super.onBackPressed();
		} else {
			lastClickTimeMillis = currentTimeMillis;
			Util.showToast(MainActivity.this, getString(R.string.quit_on_double_click_));
		}
	}

}

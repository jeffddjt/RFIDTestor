package com.handheld.uhfrdemo;

import com.handheld.uhfr.R;
import com.uhf.api.cls.Reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Fragment5_Settings extends Fragment implements OnClickListener {
	private static String TAG = Fragment5_Settings.class.getSimpleName();
	
	private View view;

	private Spinner spinnerReadPower;// read power select spinner
	private Spinner spinnerWritePower;// write power select spinner
	private Spinner spinnerFreq;// frequent
	private Button btnGetPower;
	private Button btnSetPower;
	private Button btnGetFreq;

	private Button btnSetFreq;

	private String[] powers;
	private String[] freqs;
	int[] mPowers = new int[2];
	private Reader.Region_Conf currentFreRegion;

	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.fragment_settings, null);
		initView();
		// init share preferences
		mSharedPreferences = getActivity().getSharedPreferences("UHF", Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		return view/* super.onCreateView(inflater, container, savedInstanceState) */;
	}

	private void initView() {
		powers = getResources().getStringArray(R.array.read_power);
		freqs = getResources().getStringArray(R.array.freregions);
		spinnerReadPower = (Spinner) view.findViewById(R.id.spinner_read_power);
		spinnerWritePower = (Spinner) view.findViewById(R.id.spinner_write_power);
		spinnerFreq = (Spinner) view.findViewById(R.id.spinner_freq);
		spinnerReadPower
				.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, powers));
		spinnerWritePower
				.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, powers));
		spinnerFreq.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, freqs));

		if (MainActivity.mUhfrManager != null) {
			int[] ps = MainActivity.mUhfrManager.getPower();
			if (ps != null)
				mPowers = ps;
			currentFreRegion = MainActivity.mUhfrManager.getRegion();
			if (currentFreRegion == Reader.Region_Conf.RG_PRC)
				spinnerFreq.setSelection(0);
			if (currentFreRegion == Reader.Region_Conf.RG_NA)
				spinnerFreq.setSelection(1);
			if (currentFreRegion == Reader.Region_Conf.RG_NONE)
				spinnerFreq.setSelection(2);
			if (currentFreRegion == Reader.Region_Conf.RG_KR)
				spinnerFreq.setSelection(3);
			if (currentFreRegion == Reader.Region_Conf.RG_EU)
				spinnerFreq.setSelection(4);
			if (currentFreRegion == Reader.Region_Conf.RG_EU2)
				spinnerFreq.setSelection(5);
			if (currentFreRegion == Reader.Region_Conf.RG_EU3)
				spinnerFreq.setSelection(6);
		}
		btnGetPower = (Button) view.findViewById(R.id.button_get_power);
		btnSetPower = (Button) view.findViewById(R.id.button_set_power);

		btnGetFreq = (Button) view.findViewById(R.id.button_get_freq);
		btnSetFreq = (Button) view.findViewById(R.id.button_set_freq);

		spinnerReadPower.setSelection(mPowers[0]);
		spinnerWritePower.setSelection(mPowers[1]);
		listenSpinner();
		btnGetPower.setOnClickListener(this);
		btnSetPower.setOnClickListener(this);
		btnGetFreq.setOnClickListener(this);
		btnSetFreq.setOnClickListener(this);
	}

	private void listenSpinner() {
		spinnerReadPower.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mPowers[0] = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		spinnerWritePower.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mPowers[1] = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		spinnerFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					currentFreRegion = Reader.Region_Conf.RG_PRC;
					break;
				case 1:
					currentFreRegion = Reader.Region_Conf.RG_NA;
					break;
				case 2:
					currentFreRegion = Reader.Region_Conf.RG_NONE;
					break;
				case 3:
					currentFreRegion = Reader.Region_Conf.RG_KR;
					break;
				case 4:
					currentFreRegion = Reader.Region_Conf.RG_EU;
					break;
				case 5:
					currentFreRegion = Reader.Region_Conf.RG_EU2;
					break;
				case 6:
					currentFreRegion = Reader.Region_Conf.RG_EU3;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_get_power:
			int[] ps = MainActivity.mUhfrManager.getPower();
			if (ps != null) {
				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.success));
				spinnerReadPower.setSelection(ps[0]);
				spinnerWritePower.setSelection(ps[1]);
			} else
				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.fail));

				String hardware = MainActivity.mUhfrManager.getHardware();
				Log.i("huang >>>>> ", "hardware: " + hardware);
			break;

		case R.id.button_set_power:

			Reader.READER_ERR err = MainActivity.mUhfrManager.setPower(mPowers[0], mPowers[1]);
			if (err == Reader.READER_ERR.MT_OK_ERR) {
				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.success));
				mEditor.putInt("readPower", mPowers[0]);
				mEditor.putInt("writePower", mPowers[1]);
				mEditor.commit();
			} else
				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.fail));
			break;
		case R.id.button_get_freq:
			int[] frequencyPoints = MainActivity.mUhfrManager.getFrequencyPoints();
			Log.i(TAG, "get_freq, frequencyPoints >>>> " + frequencyPoints.length);
			Log.i(TAG, "get_freq, fre1 >>>> " + frequencyPoints[0]);
			Log.i(TAG, "get_freq, fre2 >>>> " + frequencyPoints[1]);
			Log.i(TAG, "get_freq, fre3 >>>> " + frequencyPoints[2]);
			Log.i(TAG, "get_freq, fre4 >>>> " + frequencyPoints[3]);
			Log.i(TAG, "get_freq, fre5 >>>> " + frequencyPoints[49]);
//			Reader.Region_Conf region = MainActivity.mUhfrManager.getRegion();
//			if (region != null) {
//				currentFreRegion = region;
//				if (currentFreRegion == Reader.Region_Conf.RG_PRC)
//					spinnerFreq.setSelection(0);
//				if (currentFreRegion == Reader.Region_Conf.RG_NA)
//					spinnerFreq.setSelection(1);
//				if (currentFreRegion == Reader.Region_Conf.RG_NONE)
//					spinnerFreq.setSelection(2);
//				if (currentFreRegion == Reader.Region_Conf.RG_KR)
//					spinnerFreq.setSelection(3);
//				if (currentFreRegion == Reader.Region_Conf.RG_EU)
//					spinnerFreq.setSelection(4);
//				if (currentFreRegion == Reader.Region_Conf.RG_EU2)
//					spinnerFreq.setSelection(5);
//				if (currentFreRegion == Reader.Region_Conf.RG_EU3)
//					spinnerFreq.setSelection(6);
//				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.success));
//			} else
//				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.fail));
			break;
		case R.id.button_set_freq:
//			Reader.READER_ERR er = MainActivity.mUhfrManager.setRegion(currentFreRegion);
			int[] strminFrm = null;
			strminFrm = new int[40];
			for (int i = 0; i < 40; i++) {
				float values = (float) (902.75 + i * 0.5);
				strminFrm[i] = (int)(values*1000);
			}
			Reader.READER_ERR er = MainActivity.mUhfrManager.setFrequencyPoints(strminFrm);
			if (er == Reader.READER_ERR.MT_OK_ERR) {
				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.success));
				mEditor.putInt("freRegion", currentFreRegion.value());
				mEditor.commit();
			} else
				Util.showToast(Fragment5_Settings.this.getActivity(), getString(R.string.fail));
			break;
		}
	}
}

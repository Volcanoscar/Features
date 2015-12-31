package com.android.launcher3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransitionEffectChooserActivity extends Activity {
    
    private final String TAG = "Launcher3.TransitionChooserActivity";
    
	private List<String> mDatas;
	private RecyclerView mRecycler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transition_effect_chooser);
		mRecycler = (RecyclerView) findViewById(R.id.recycler);
		initDatas();
	}

	private void initDatas() {
		mDatas = new ArrayList<>();
		mDatas.addAll(Arrays.asList(PagedView.TransitionEffect.EFFECT_ARRAY));
		mRecycler.setAdapter(new TransitionEffectAdapter(mDatas, getLayoutInflater()));
		mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
	}


	class TransitionEffectAdapter extends RecyclerView.Adapter<TransitionEffectViewHolder> {
		private LayoutInflater inflater;
		private List<String> mDatas;

		public TransitionEffectAdapter(List<String> datas, LayoutInflater inflater) {
			this.mDatas = datas;
			this.inflater = inflater;
		}

		@Override
		public TransitionEffectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = inflater.inflate(R.layout.transition_effect_item, null);
			return new TransitionEffectViewHolder(view);
		}

		@Override
		public int getItemCount() {
			if (mDatas != null) {
				return mDatas.size();
			}
			return 0;
		}

		@Override
		public void onBindViewHolder(TransitionEffectViewHolder holder, int position) {
			holder.tv.setText(mDatas.get(position));
			holder.tv.setOnClickListener(new ItemOnClickListener(mDatas.get(position)));
		}
	}


	class TransitionEffectViewHolder extends RecyclerView.ViewHolder {

		TextView tv;

		public TransitionEffectViewHolder(View view) {
			super(view);
			tv = (TextView) view.findViewById(R.id.transition_effect);
		}
	}

	class ItemOnClickListener implements View.OnClickListener {

		String result;

		public ItemOnClickListener(String result) {
			this.result = result;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(PagedView.TransitionEffect.TRANSITION_EFFECT, result);
			intent.putExtras(bundle);
			setResult(PagedView.TransitionEffect.RESULT_CODE, intent);
            Log.e(TAG,"transition effect "+result+" has been choosed");
			finish();
		}
	}
}

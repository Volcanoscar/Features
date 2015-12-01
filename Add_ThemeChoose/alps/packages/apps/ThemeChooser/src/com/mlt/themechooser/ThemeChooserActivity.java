package com.mlt.themechooser;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ThemeChooserActivity extends FragmentActivity {


	int mCurrentTheme;
	Button mSetTheme;
	ViewPager mPager;
	List<ImageFragment> mList;
	List<View> mIndicates;
	SharedPreferences mPreferences;

	private final String TAG = "ThemeChooserActivity";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_chooser);
		init();
	}

	public void init() {
		mSetTheme = (Button) findViewById(R.id.button);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPreferences = getSharedPreferences(Theme.HAS_SET_THEME, Context.MODE_PRIVATE);

		mList = new ArrayList<>();
		mList.add(ImageFragment.newInstance(R.drawable.preview_1));
		mList.add(ImageFragment.newInstance(R.drawable.preview_2));
		mList.add(ImageFragment.newInstance(R.drawable.preview_3));
		mList.add(ImageFragment.newInstance(R.drawable.preview_4));
		mList.add(ImageFragment.newInstance(R.drawable.preview_5));
		mPager.setAdapter(new MyAdapter(getSupportFragmentManager(), mList));

		mIndicates = new ArrayList<>();
		mIndicates.add(findViewById(R.id.indicate_1));
		mIndicates.add(findViewById(R.id.indicate_2));
		mIndicates.add(findViewById(R.id.indicate_3));
		mIndicates.add(findViewById(R.id.indicate_4));
		mIndicates.add(findViewById(R.id.indicate_default));
		mPager.setOnPageChangeListener(new MyOnPageChangeListener(mIndicates));

		mSetTheme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentTheme = mPager.getCurrentItem();
				saveThemeSetting();
			}
		});
	}


	public void saveThemeSetting() {
		setWallpaper();
		Intent intent = new Intent();
		intent.setAction(Theme.INTENT_THEME_CHOOSER_ACTIVITY_SWITCH_THEME);
		intent.putExtra(Theme.ICON_CONFIG, Theme.ICON_CONFIGS[mCurrentTheme]);
		sendBroadcast(intent);
		finish();
		simulationOnHomeKeyDown();

	}

	public void simulationOnHomeKeyDown() {
		Intent intent = new Intent();
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setAction(Intent.ACTION_MAIN);
		startActivity(intent);
	}

	public void setWallpaper() {
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

		int resId = Theme.WALLPAPERS[mCurrentTheme];
		try {
			if (resId != -1) {
				saveDefaultWallpaper(wallpaperManager, resId);
				wallpaperManager.setResource(Theme.WALLPAPERS[mCurrentTheme]);
			} else {
				resetWallpaper(wallpaperManager);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void saveDefaultWallpaper(WallpaperManager manager, int resId) throws Exception {
		int value = mPreferences.getInt(Theme.HAS_SET_THEME, -1);
		if (value != -1) {
			return;
		}
		File wallpaper = new File(getFilesDir() + "/wallpaper.png");
		if (!wallpaper.exists()) {
			wallpaper.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(wallpaper);
		Bitmap bitmap = ((BitmapDrawable) manager.getDrawable()).getBitmap();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		bitmap.recycle();
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(Theme.HAS_SET_THEME, resId);
		editor.apply();
	}

	void resetWallpaper(WallpaperManager manager) throws Exception {
		File wallpaper = new File(getFilesDir() + "/wallpaper.png");
		if (wallpaper.exists()) {
			manager.setStream(new FileInputStream(wallpaper));
		}
		wallpaper.delete();
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(Theme.HAS_SET_THEME, -1);
		editor.apply();
	}

	class MyAdapter extends FragmentPagerAdapter {

		private List<ImageFragment> list;


		public MyAdapter(FragmentManager fragmentManager, List<ImageFragment> list) {
			super(fragmentManager);
			this.list = list;
		}

		@Override
		public Fragment getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}
	}

	public static class ImageFragment extends Fragment {

		ImageView image;
		TextView text;

		public ImageFragment() {

		}

		public static ImageFragment newInstance(int resId) {

			Bundle args = new Bundle();
			args.putInt(Theme.PREVIEW, resId);
			ImageFragment fragment = new ImageFragment();
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_perview, null);
			image = (ImageView) root.findViewById(R.id.image);
			text = (TextView) root.findViewById(R.id.text);
			Bundle args = getArguments();
			int resId = args == null ? 0 : args.getInt(Theme.PREVIEW);
			if (resId > 0) {
				image.setImageResource(resId);
				text.setVisibility(View.INVISIBLE);
			}
			return root;
		}

	}

	class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

		private List<View> mIndicates;

		public MyOnPageChangeListener(List<View> list) {
			this.mIndicates = list;
		}

		@Override
		public void onPageSelected(int position) {
			for (View view : mIndicates) {
				if (position == mIndicates.indexOf(view)) {
					view.setBackground(getDrawable(R.drawable.indicate_shape_selected));
				} else {
					view.setBackground(getDrawable(R.drawable.indicate_shape_not_selected));
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}
	}


}

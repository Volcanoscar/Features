
package com.mlt.filemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.filemanager.FileManagerOperationActivity;
import com.mlt.filemanager.FavoriteDatabaseHelper.FavoriteDatabaseListener;
import com.mlt.filemanager.FileManagerTabActivity.IBackPressedListener;
import com.mlt.filemanager.FileViewInteractionHub.Mode;
import com.mlt.filemanager.ftp.FTPControlActivity;
import com.mlt.filemanager.helper.FileCategoryHelper;
import com.mlt.filemanager.helper.FileSortHelper;
import com.mlt.filemanager.helper.FileCategoryHelper.CategoryInfo;
import com.mlt.filemanager.helper.FileCategoryHelper.FileCategory;
import com.mlt.filemanager.helper.FileIconHelper;
import com.mlt.filemanager.utils.Util;
import com.mlt.filemanager.utils.Util.SDCardInfo;

public class FileCategoryActivity extends Fragment implements IFileInteractionListener,
        FavoriteDatabaseListener, IBackPressedListener {

    public static final String EXT_FILETER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileCategoryActivity";
   // public static FileCategoryActivity mFileCategoryActivity;
    /** 
    * @Fields: button2Category 
    * @Description: 分类列表，装载分类文件；点击进入分类文件列表
    */
    @SuppressLint("UseSparseArrays")
	private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();

    private HashMap<FileCategory, Integer> categoryIndex = new HashMap<FileCategory, Integer>();

    private FileListCursorAdapter mAdapter;
  
    private FileViewInteractionHub mFileViewInteractionHub;
    
    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private CategoryBar mCategoryBar;

    private ScannerReceiver mScannerReceiver;

    private FavoriteList mFavoriteList;

    private ViewPage curViewPage = ViewPage.Invalid;

    private ViewPage preViewPage = ViewPage.Invalid;

    private Activity mActivity;

    private View mRootView;

   // private FileViewActivity mFileViewActivity;

    private boolean mConfigurationChanged = false;
    
    private LinearLayout mPhoneCardLayout;//chb add
    private LinearLayout mFtpViewLayout;//chb add

    public void setConfigurationChanged(boolean changed) {
        mConfigurationChanged = changed;
    }

    static { //在分类列表中装入分类类型
        button2Category.put(R.id.category_music, FileCategory.Music);
        button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_picture, FileCategory.Picture);
        button2Category.put(R.id.category_theme, FileCategory.Theme);
        button2Category.put(R.id.category_apk, FileCategory.Apk);
        button2Category.put(R.id.category_zip, FileCategory.Zip);
        button2Category.put(R.id.category_document, FileCategory.Doc);
        button2Category.put(R.id.category_favorite, FileCategory.Favorite);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        /*mFileViewActivity = (FileViewActivity) ((FileManagerTabActivity) mActivity)
                .getFragment(Util.SDCARD_TAB_INDEX);*/
        mRootView = inflater.inflate(R.layout.mlt_file_manager_category, container, false); //加载布局界面
        curViewPage = ViewPage.Invalid;
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        mFileViewInteractionHub.setMode(Mode.View);
        mFileViewInteractionHub.setRootPath("/");
        mFileIconHelper = new FileIconHelper(mActivity);
        mFavoriteList = new FavoriteList(mActivity, (ListView) mRootView.findViewById(R.id.favorite_list), this, mFileIconHelper);
        mFavoriteList.initList();
        mAdapter = new FileListCursorAdapter(mActivity, null, mFileViewInteractionHub, mFileIconHelper);
        //chb add begin
        mPhoneCardLayout = (LinearLayout) mRootView.findViewById(R.id.phone_linearlayout);
        mPhoneCardLayout.setOnClickListener(mOnClickLististener);
        mFtpViewLayout = (LinearLayout) mRootView.findViewById(R.id.ftp_list);
        mFtpViewLayout.setOnClickListener(mOnClickLististener);
        
        
        //chb add end 

        ListView fileListView = (ListView) mRootView.findViewById(R.id.file_path_list); // 文件列表
        fileListView.setAdapter(mAdapter);

        setupClick();
        setupCategoryInfo();
        updateUI();
        registerScannerReceiver(); //注册广播过滤器

        return mRootView;
    }  
    
    View.OnClickListener mOnClickLististener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	switch (v.getId()) {
			case R.id.phone_linearlayout: // 文件目录浏览
				//Toast.makeText(getContext(), "你点击了该Linearlayout", Toast.LENGTH_LONG).show();
	        	Intent mPhoneIntent = new Intent();
	        	mPhoneIntent.setClass(getActivity(), FileManagerOperationActivity.class);
	        	startActivity(mPhoneIntent);
				break;
			case R.id.ftp_list://FTP远程管理
				Intent mFtpIntent = new Intent();
				mFtpIntent.setClass(getActivity(), FTPControlActivity.class);
	        	startActivity(mFtpIntent);
			default:
				break;
			}
        	
        }
    };

    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);//表示MediaScanner扫描结束
        // 不允许发送 not allowed to send broadcast android.intent.action.MEDIA_MOUNTED
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);//插入SD卡并且已正确安装（识别）时发出的广播  //广播：扩展介质被插入，而且已经被挂载。
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// 广播：扩展介质存在，但是还没有被挂载 (mount)。 
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mScannerReceiver, intentFilter);
    }

    private void setupCategoryInfo() {
        mFileCagetoryHelper = new FileCategoryHelper(mActivity);

        mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        int[] imgs = new int[] {
                R.drawable.category_bar_music, R.drawable.category_bar_video,
                R.drawable.category_bar_picture, R.drawable.category_bar_theme,
                R.drawable.category_bar_apk, R.drawable.category_bar_zip,
                R.drawable.category_bar_document, R.drawable.category_bar_other
        };

        for (int i = 0; i < imgs.length; i++) {
            mCategoryBar.addCategory(imgs[i]);
        }

        for (int i = 0; i < FileCategoryHelper.sCategories.length; i++) {
            categoryIndex.put(FileCategoryHelper.sCategories[i], i);
        }
    }

    public void refreshCategoryInfo() {
        SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (sdCardInfo != null) {
            mCategoryBar.setFullValue(sdCardInfo.total);
            setTextView(R.id.sd_card_capacity, getString(R.string.sd_card_size, Util.convertStorage(sdCardInfo.total)));
            setTextView(R.id.sd_card_available, getString(R.string.sd_card_available, Util.convertStorage(sdCardInfo.free)));
        }

        mFileCagetoryHelper.refreshCategoryInfo();

        // the other category size should include those files didn't get scanned.
        long size = 0;
        for (FileCategory fc : FileCategoryHelper.sCategories) {
            CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfos().get(fc);
            setCategoryCount(fc, categoryInfo.count);

            // other category size should be set separately with calibration
            if(fc == FileCategory.Other)
                continue;

            setCategorySize(fc, categoryInfo.size);
            setCategoryBarValue(fc, categoryInfo.size);
            size += categoryInfo.size;
        }

        if (sdCardInfo != null) {
            long otherSize = sdCardInfo.total - sdCardInfo.free - size;
            setCategorySize(FileCategory.Other, otherSize);
            setCategoryBarValue(FileCategory.Other, otherSize);
        }

        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());

        if (mCategoryBar.getVisibility() == View.VISIBLE) {
            mCategoryBar.startAnimation();
        }
    }

    public enum ViewPage {
        Home, Favorite, Category, NoSD, Invalid
    }

    /** 
    * @MethodName: showPage 
    * @Functions:显示相关页面
    * @param p  ：传入参数
    * @return	:void   
    */
    private void showPage(ViewPage p) {
        if (curViewPage == p) 
        	return;

        curViewPage = p;
        showView(R.id.file_path_list, false);
        showView(R.id.navigation_bar, false);
        showView(R.id.category_page, false);
        showView(R.id.operation_bar, false);
        showView(R.id.sd_not_available_page, false);
        mFavoriteList.show(false);
        showEmptyView(false);

        switch (p) {
            case Home: //显示分类主页
                showView(R.id.category_page, true);
                FileManagerTabActivity.bar.getSelectedTab().setText(R.string.tab_category);
                if (mConfigurationChanged) {
                    ((FileManagerTabActivity) mActivity).reInstantiateCategoryTab();
                    mConfigurationChanged = false;
                }
                break;
            case Favorite://显示收藏界面
                showView(R.id.navigation_bar, true);//显示文件目录bar
                mFavoriteList.show(true);
                showEmptyView(mFavoriteList.getCount() == 0); 
                break;
            case Category://显示文件列表
                showView(R.id.navigation_bar, false);//chb change true——>false
                showView(R.id.file_path_list, true);
                showEmptyView(mAdapter.getCount() == 0);
                break;
            case NoSD://如果SD卡不存在，显示
                showView(R.id.sd_not_available_page, true);
                break;
		default:
			break;
        }
    }

    private void showEmptyView(boolean show) {
        View emptyView = mActivity.findViewById(R.id.empty_view); //SD 卡 不存在
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /** 
    * @MethodName: showView 
    * @Functions:显示控件
    * @param id :布局控件ID
    * @param show  :boolean变量，是否显示该控件
    * @return	:void   
    */
    private void showView(int id, boolean show) {
        View view = mRootView.findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());
            if (f != null) {
                onCategorySelected(f);
                if (f != FileCategory.Favorite) {
                    setHasOptionsMenu(true);
                }
            }
        }

    };

    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;

        setTextView(id, "(" + count + ")");
    }

    private void setTextView(int id, String t) {
        TextView text = (TextView) mRootView.findViewById(id);
        text.setText(t);
    }

    //点击事件
    @SuppressWarnings("deprecation")
	private void onCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
                    + getString(mFileCagetoryHelper.getCurCategoryNameResId()));
            mFileViewInteractionHub.refreshFileList();
        }
        if (f == FileCategory.Favorite) {
            showPage(ViewPage.Favorite);
            FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_favorite);
        } else {
            showPage(ViewPage.Category);
            if (f== FileCategory.Music) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_music);
			}
            else if (f== FileCategory.Video) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_video);
			}
            else if (f== FileCategory.Picture) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_picture);
			}
            else if (f== FileCategory.Theme) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_theme);
			}
            else if (f== FileCategory.Apk) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_apk);
			}
            else if (f== FileCategory.Zip) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_zip);
			}
            else if (f== FileCategory.Doc) {
            	FileManagerTabActivity.bar.getSelectedTab().setText(R.string.category_document);
			}
        }
    }

    private void setupClick(int id) {
        View button = mRootView.findViewById(id);
        button.setOnClickListener(onClickListener);
    }  

    //点击事件
    private void setupClick() {
    	//调用有参函数 setupClick(int id)
        setupClick(R.id.category_music);
        setupClick(R.id.category_video);
        setupClick(R.id.category_picture);
        setupClick(R.id.category_theme);
        setupClick(R.id.category_apk);
        setupClick(R.id.category_zip);
        setupClick(R.id.category_document);
        setupClick(R.id.category_favorite);
    }

    @Override
    public boolean onBack() {
        if (isHomePage() || curViewPage == ViewPage.NoSD || mFileViewInteractionHub == null) {
            return false;
        }

        return mFileViewInteractionHub.onBackPressed1();
    }

    public boolean isHomePage() {
        return curViewPage == ViewPage.Home;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (curViewPage != ViewPage.Category && curViewPage != ViewPage.Favorite) { //不在文件列表和收藏界面 则不进行菜单操作
            return;
        }
        mFileViewInteractionHub.onCreateOptionsMenu(menu); // menu 选项菜单
       // mFileManagerOperationActivity.onCreateOptionsMenu(menu);
        
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!isHomePage() && mFileCagetoryHelper.getCurCategory() != FileCategory.Favorite) {
        	mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        }
    }

    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
        if (curCategory == FileCategory.Favorite || curCategory == FileCategory.All)
            return false;

        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
        showEmptyView(c == null || c.getCount() == 0);
        mAdapter.changeCursor(c);

        return true;
    }

    @Override
    public View getViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mFavoriteList.getArrayAdapter().notifyDataSetChanged();
                showEmptyView(mAdapter.getCount() == 0);
            }

        });
    }

    @Override
    public void onPick(FileInfo f) {
        // do nothing
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        mFileViewInteractionHub.addContextMenuSelectedItem();
        switch (id) {
            case R.id.button_operation_copy: //bar  复制
            case GlobalConsts.MENU_COPY:
                copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case R.id.button_operation_move:
            case GlobalConsts.MENU_MOVE:// bar 粘贴
                startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.OPERATION_UP_LEVEL: //返回上一级目录 （Home）
                setHasOptionsMenu(false);
                showPage(ViewPage.Home);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public String getDisplayPath(String path) {
        return getString(R.string.tab_category) + path;
    }

    @Override
    public String getRealPath(String displayPath) {
        return "";
    }

    @Override
    public boolean onNavigation(String path) {
        showPage(ViewPage.Home);
        return true;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return (menu == GlobalConsts.MENU_NEW_FOLDER || menu == GlobalConsts.MENU_FAVORITE
        	|| menu == GlobalConsts.MENU_PASTE 
                // || menu == GlobalConsts.MENU_SHOWHIDE
        );
    }

    @Override
    public void addSingleFile(FileInfo file) {
        refreshList();
    }

    @Override
    public Collection<FileInfo> getAllFiles() {
        return mAdapter.getAllFiles();
    }

    @Override
    public FileInfo getItem(int pos) {
        return mAdapter.getFileItem(pos);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

    @Override
    public void sortCurrentList(FileSortHelper sort) {
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

    private void copyFileInFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
       /* mFileViewActivity.copyFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);*/
    }

    private void startMoveToFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
       /* mFileViewActivity.moveToFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);*/
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case Music:
                return R.id.category_music_count;
            case Video:
                return R.id.category_video_count;
            case Picture:
                return R.id.category_picture_count;
            case Theme:
                return R.id.category_theme_count;
            case Doc:
                return R.id.category_document_count;
            case Zip:
                return R.id.category_zip_count;
            case Apk:
                return R.id.category_apk_count;
            case Favorite:
                return R.id.category_favorite_count;
        }

        return 0;
    }

    private void setCategorySize(FileCategory fc, long size) {
        int txtId = 0;
        int resId = 0;
        switch (fc) {
            case Music:
                txtId = R.id.category_legend_music;
                resId = R.string.category_music;
                break;
            case Video:
                txtId = R.id.category_legend_video;
                resId = R.string.category_video;
                break;
            case Picture:
                txtId = R.id.category_legend_picture;
                resId = R.string.category_picture;
                break;
            case Theme:
                txtId = R.id.category_legend_theme;
                resId = R.string.category_theme;
                break;
            case Doc:
                txtId = R.id.category_legend_document;
                resId = R.string.category_document;
                break;
            case Zip:
                txtId = R.id.category_legend_zip;
                resId = R.string.category_zip;
                break;
            case Apk:
                txtId = R.id.category_legend_apk;
                resId = R.string.category_apk;
                break;
            case Other:
                txtId = R.id.category_legend_other;
                resId = R.string.category_other;
                break;
        }

        if (txtId == 0 || resId == 0)
            return;

        setTextView(txtId, getString(resId) + ":" + Util.convertStorage(size));
    }

    private void setCategoryBarValue(FileCategory f, long size) {
        if (mCategoryBar == null) {
            mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        }
        mCategoryBar.setCategoryValue(categoryIndex.get(f), size);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mActivity != null) {
            mActivity.unregisterReceiver(mScannerReceiver); // 注销文件广播扫描接收
        }
    }

    /** 
    * @ClassName: ScannerReceiver 
    * @Function: 扫描 广播接收
    * @author:   chehongbin
    * @date:     2015-8-14 上午9:55:24  <br>
    * Copyright (c) 2015 MALATA,All Rights Reserved.
    */
    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action.toString());
            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) 
            		|| action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            	updateUI(); //更新UI
            	mFileViewInteractionHub.refreshFileList();
            	notifyFileChanged(); //如果扫描接收 or SD卡挂载 or SD卡卸载 均进行该方法
            	
            }
        }
    }

    /** 
    * @MethodName: updateUI 
    * @Functions:更新页面布局内容
    * @return	:void   
    */
    public void updateUI() {
        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) { //如果SD卡存在
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
            refreshCategoryInfo();
            // refresh file list
            mFileViewInteractionHub.refreshFileList();
            // refresh file list view in another tab
            //mFileViewActivity.refresh();
        } else { //sd 卡不存在
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER; //发送文件 改变消息
                handler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;

    private Timer timer;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI(); //更新UI
                    break;
            }
            super.handleMessage(msg);
        }

    };

    // update the count of favorite
    @Override
    public void onFavoriteDatabaseChanged() {
        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }
}


package com.mlt.filemanager;

import java.util.Locale;

import com.mediatek.filemanager.AbsBaseActivity;
import com.mlt.filemanager.FileViewInteractionHub.Mode;
import com.mlt.filemanager.FileViewInteractionHub;
import com.mlt.filemanager.helper.FileIconHelper;
import com.mlt.filemanager.utils.Util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;


/** 
* @ClassName: FileListItem 
* @PackageName:com.mlt.filemanager
* @Function: 分类列表，选中菜单 选项
* @author:   chehongbin
* @date:     2015-8-13 上午9:20:54  
* Copyright (c) 2015 MALATA,All Rights Reserved.
*/
public class FileListItem extends AbsBaseActivity{
    public static void setupFileListItemInfo(Context context, View view,
            FileInfo fileInfo, FileIconHelper fileIcon,
            FileViewInteractionHub fileViewInteractionHub) {

        // if in moving mode, show selected file always
        if (fileViewInteractionHub.isMoveState()) {
            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
        }

        ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
        if (fileViewInteractionHub.getMode() == Mode.Pick) {
            checkbox.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox() ? View.VISIBLE : View.GONE);
            checkbox.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            checkbox.setTag(fileInfo);
            view.setSelected(fileInfo.Selected);
        }

        Util.setText(view, R.id.file_name, fileInfo.fileName);
        Util.setText(view, R.id.file_count, fileInfo.IsDir ? "(" + fileInfo.Count + ")" : "");
        Util.setText(view, R.id.modified_time, Util.formatDateString(context, fileInfo.ModifiedDate));
        Util.setText(view, R.id.file_size, (fileInfo.IsDir ? "" : Util.convertStorage(fileInfo.fileSize)));

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);

        if (fileInfo.IsDir) {
            lFileImageFrame.setVisibility(View.GONE);
            lFileImage.setImageResource(R.drawable.folder);
        } else {
            fileIcon.setIcon(fileInfo, lFileImage, lFileImageFrame);
        }
    }

    public static class FileItemOnClickListener implements OnClickListener {
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        public FileItemOnClickListener(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public void onClick(View v) {
            ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
            assert (img != null && img.getTag() != null);

            FileInfo tag = (FileInfo) img.getTag();
            tag.Selected = !tag.Selected;
            ActionMode actionMode = ((FileManagerTabActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileManagerTabActivity) mContext)
                        .startActionMode(new ModeCallback(mContext,
                                mFileViewInteractionHub));
                ((FileManagerTabActivity) mContext).setActionMode(actionMode);
            } else {
                actionMode.invalidate();
            }
            if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                img.setImageResource(tag.Selected ? R.drawable.btn_check_on_holo_light
                        : R.drawable.btn_check_off_holo_light);
            } else {
                tag.Selected = !tag.Selected;
            }
            Util.updateActionModeTitle(actionMode, mContext,
                    mFileViewInteractionHub.getSelectedFileList().size());
        }
    }

    private static ActionMode mActionMode;
    public final ModeCallback mActionModeCallBack = new ModeCallback(mService, null);
    public static class ModeCallback implements ActionMode.Callback {
        private Menu mMenu;
        private Context mContext;
      
        private FileViewInteractionHub mFileViewInteractionHub;
        private PopupMenu mSelectPopupMenu = null;
        private boolean mSelectedAll = true;
        private Button mTextSelect = null;

        private void initMenuItemSelectAllOrCancel() {
            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
            mMenu.findItem(R.id.action_rename).setEnabled(!mFileViewInteractionHub.isSelecteds());
            mMenu.findItem(R.id.action_details).setEnabled(!mFileViewInteractionHub.isSelecteds());
        }

        private void scrollToSDcardTab() {
            ActionBar bar = ((FileManagerTabActivity) mContext).getActionBar();
           /* if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
                bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
            }*/
        }

        public ModeCallback(Context context,FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }
        
        private void updateSelectPopupMenu() {
            if (mSelectPopupMenu == null) {
                mSelectPopupMenu = createSelectPopupMenu(mTextSelect);
                return;
            }
            final Menu menu = mSelectPopupMenu.getMenu();
            int selectedCount = mAdapter.getCheckedItemsCount();
            if (mAdapter.getCount() == 0) {
                menu.findItem(R.id.select).setEnabled(false);
            } else {
                menu.findItem(R.id.select).setEnabled(true);
            }
            if (mAdapter.getCount() != selectedCount) {
                menu.findItem(R.id.select).setTitle(R.string.select_all);
                mSelectedAll = true;
            } else {
                menu.findItem(R.id.select).setTitle(R.string.deselect_all);
                mSelectedAll = false;
            }
        }
        
        public void updateActionMode() {
            int selectedCount = mAdapter.getCheckedItemsCount();
            String selected = "";
            if (Locale.getDefault().getLanguage().equals("fr") && selectedCount > 1) {
                try {
                    selected = mContext.getResources().getString(R.string.mutil_selected);
                } catch (Resources.NotFoundException e) {
                    selected = mContext.getResources().getString(R.string.selected);
                }
            } else {
                selected = mContext.getResources().getString(R.string.selected);
            }
            selected = "" + selectedCount + " " + selected;
            mTextSelect.setText(selected);

            updateSelectPopupMenu();
            if (mActionMode != null) {
                mActionMode.invalidate();
            }
        }

        /* (non-Javadoc)
         * @see android.view.ActionMode.Callback#onCreateActionMode(android.view.ActionMode, android.view.Menu)
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        	//{@ chb add 
        	/* LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             View customView = layoutInflater.inflate(R.layout.actionbar_edit, null);
             mode.setCustomView(customView);
             mTextSelect = (Button) customView.findViewById(R.id.text_select);
             mTextSelect.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if (mSelectPopupMenu == null) {
                         mSelectPopupMenu = createSelectPopupMenu(mTextSelect);
                     } else {
                    	 updateSelectPopupMenu();
                         mSelectPopupMenu.show();
                     }
                 }
             });*/
             // @}
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.mlt_operation_menu, mMenu); //选中 操作菜单
            initMenuItemSelectAllOrCancel();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        	//@{在特定的情况下才出现
        	mMenu.findItem(R.id.action_cancel).setVisible(
            		mFileViewInteractionHub.isSelected());
        	mMenu.findItem(R.id.action_select_all).setVisible(
            		!mFileViewInteractionHub.isSelectedAll());
        	//选中超过两个的情况下不能点击
        	mMenu.findItem(R.id.action_rename).setEnabled(
            		!mFileViewInteractionHub.isSelecteds());
        	mMenu.findItem(R.id.action_details).setEnabled(
            		!mFileViewInteractionHub.isSelecteds());
            //@}
            //@{ 在该环境下不能执行复制和剪切操作
        	mMenu.findItem(R.id.action_copy).setEnabled(false);
        	mMenu.findItem(R.id.action_move).setEnabled(false);
            //@}
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    mFileViewInteractionHub.onOperationDelete();
                    mode.finish();
                    break;
               /* case R.id.action_copy:
                    ((FileViewActivity) ((FileManagerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .copyFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_move:
                    ((FileViewActivity) ((FileManagerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .moveToFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;*/
                case R.id.action_send:
                    mFileViewInteractionHub.onOperationSend();
                    mode.finish();
                    break;
                case R.id.action_cancel:
                    mFileViewInteractionHub.clearSelection();
                    initMenuItemSelectAllOrCancel();
                    mode.finish();
                    break;
                case R.id.action_select_all:
                    mFileViewInteractionHub.onOperationSelectAll();
                    initMenuItemSelectAllOrCancel();
                    break;
                //{@ chb add 
                case R.id.action_rename:
                	mFileViewInteractionHub.onOperationRename();
                	initMenuItemSelectAllOrCancel();
                	 mode.finish();
                    break;
                case R.id.action_details:
                	mFileViewInteractionHub.onOperationInfo();
                	initMenuItemSelectAllOrCancel();
                	mode.finish();
                    break;
                //@}
            }
            Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub
                    .getSelectedFileList().size());
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFileViewInteractionHub.clearSelection();
            ((FileManagerTabActivity) mContext).setActionMode(null);
        }
        
        private PopupMenu createSelectPopupMenu(View anchorView) {
            final PopupMenu popupMenu = new PopupMenu(this.mContext, anchorView);
            popupMenu.inflate(R.menu.select_popup_menu);
            popupMenu.setOnMenuItemClickListener((OnMenuItemClickListener) mContext);
            return popupMenu;
        }
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setMainContentView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String initCurrentFileInfo() {
		// TODO Auto-generated method stub
		return null;
	}
}

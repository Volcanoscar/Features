/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import com.android.settings.R;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.os.Bundle;

import android.content.Context;
//caoqiaofeng add MTSFEFL-14 20150323
public class GesturesExperienceGrid extends Fragment {

    ListView mGrid;
    private View mContentView;
    private String sGesturesChar;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
    
        loadApps(); // do this in onresume?		
        mContentView = inflater.inflate(R.layout.gestures_experience_grid, null);
        
        mGrid = (ListView) mContentView.findViewById(R.id.myGesturesExperienceGrid);
        mGrid.setAdapter(new AppsAdapter());
        
        mGrid.setOnItemClickListener(listener);

        Bundle bundle = this.getArguments();
	 if (bundle != null) {
            sGesturesChar = bundle.getString("gestures_char");
        }
	
        return mContentView;
    }
    
    public void saveSelectedAPP(String type, String pkg, String cls, String pName){
        SharedPreferences.Editor editor = getActivity().getSharedPreferences( type, /*MODE_WORLD_WRITEABLE*/2).edit();
        editor.putString("package_name", pkg);
        editor.putString("activity_name", cls);
        editor.putString("app_name", pName);		
        editor.commit();
    }
	
    private OnItemClickListener listener = new OnItemClickListener() {
    @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        
		Context context = mContentView.getContext();		
		if(position == 0){
			String pkg = null; 		   
			String cls = null;	
			String pName = context.getString(R.string.gestures_settings_unlock);
			saveSelectedAPP(sGesturesChar, pkg, cls, pName);
		}
		else{
			ResolveInfo info = mApps.get(position-1);
	        	String pkg = info.activityInfo.packageName;            
	        	String cls = info.activityInfo.name;          
	        	String pName = (String)info.activityInfo.loadLabel(getActivity().getPackageManager());
	        	saveSelectedAPP(sGesturesChar, pkg, cls, pName);
		}
        	getActivity().finish(); 
    }
    
    };
    
    private List<ResolveInfo> mApps;
    
    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        mApps = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public class AppsAdapter extends BaseAdapter {
        public AppsAdapter() {
        }

        private class GridHolder {  
            ImageView appImage;  
            TextView appName;  
        }  
        
        public View getView(int position, View convertView, ViewGroup parent) {
               
            GridHolder holder;
            if (convertView == null) {
            	convertView=getActivity().getLayoutInflater().inflate(R.layout.gestures_experience_grid_item, null);                           
                holder = new GridHolder();  
                holder.appImage = (ImageView)convertView.findViewById(R.id.ItemImage);  
                holder.appName = (TextView)convertView.findViewById(R.id.ItemText);  
                convertView.setTag(holder);     
            } else {
            	holder = (GridHolder) convertView.getTag();    
            }
			
		if(position == 0)
			{
			ResolveInfo info = mApps.get(position); 		   
			holder.appName.setText(R.string.gestures_settings_unlock);
			holder.appImage.setImageResource(R.drawable.encroid_progress);
		}
		else
			{
			ResolveInfo info = mApps.get(position-1); 		   
			holder.appName.setText(info.activityInfo.loadLabel(getActivity().getPackageManager()));
			holder.appImage.setImageDrawable(info.activityInfo.loadIcon(getActivity().getPackageManager()));
		}
			
            return convertView;
        }

        public final int getCount() {
            return mApps.size()+1;
        }

        public final Object getItem(int position) {
            return mApps.get(position-1);
        }

        public final long getItemId(int position) {
            return position-1;
        }
    }

}

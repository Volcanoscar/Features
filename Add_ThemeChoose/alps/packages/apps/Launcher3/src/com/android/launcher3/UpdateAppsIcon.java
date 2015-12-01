package com.android.launcher3;

import android.content.Context;
import android.content.ComponentName;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Xml;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;


public class UpdateAppsIcon {

	private XmlResourceParser parser;
	private HashMap<String, Integer> mAppsIcon = new HashMap<>();
	private final String ROOT_TAG = "appicon";
    private final String TAG = "UpdateAppsIcon";
	private Bitmap mIcon = null;
	private Context mContext;
    
    public UpdateAppsIcon(Context context,String icon_config){
        this.mContext = context;
        int resId = context.getResources().getIdentifier(icon_config, "xml", "com.android.launcher3");
        try{
           mAppsIcon = loadIconsConfig(resId); 
        }catch(Exception e){
            mAppsIcon = null;
        }
        
    }

	public  Bitmap getAppIconBitmap(ComponentName compentName) {
		return  BitmapFactory.decodeResource(mContext.getResources(),mAppsIcon.get(compentName.toString()));
	}
    
    public Drawable getAppIconDrawable(ComponentName compentName){
        int resId = mAppsIcon.get(compentName.toString());
        if(resId <= 0x0){
            return null;
        }
        return mContext.getResources().getDrawable(mAppsIcon.get(compentName.toString()));
    }
    
    public byte[] getAppIconByteArray(ComponentName compentName){
        Bitmap bitmap = getAppIconBitmap(compentName);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        bitmap.recycle();
        return os.toByteArray();
    }

	public HashMap<String, Integer> loadIconsConfig(int resId) throws XmlPullParserException, IOException {
		parser = mContext.getResources().getXml(resId);
		beginDocument(parser, ROOT_TAG);
		final int depth = parser.getDepth();
		AttributeSet attrs = Xml.asAttributeSet(parser);
		int type=-1;

		while (((type = parser.next()) != XmlResourceParser.END_TAG || parser.getDepth() > depth) && type != XmlResourceParser.END_DOCUMENT) {
			if (type != XmlPullParser.START_TAG) {continue;}
			TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.AppIcon);
			String cmpName = array.getString(R.styleable.AppIcon_component);
			String iconName = array.getString(R.styleable.AppIcon_drawable);
			int iconId = mContext.getResources().getIdentifier(iconName,"drawable","com.android.launcher3");
			mAppsIcon.put(cmpName,iconId);
			array.recycle();
		}
        
		return mAppsIcon;
	}

	protected static final void beginDocument(XmlPullParser parser, String firstElementName)
			throws XmlPullParserException, IOException {
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG
				&& type != XmlPullParser.END_DOCUMENT) ;

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No start tag found");
		}

		if (!parser.getName().equals(firstElementName)) {
			throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
					", expected " + firstElementName);
		}
	}

	public boolean isInternalApp(ComponentName componentName){
        return mAppsIcon.containsKey(componentName.toString());
	}
}
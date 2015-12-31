package com.malata.superclean.base;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xuxiantao on 2015/9/7.
 */
public class ActivityTack {

    public List<Activity> activityList = new ArrayList<Activity>();
    public static ActivityTack tack = new ActivityTack();

    public static ActivityTack getInstanse() {
        return tack;
    }

    public ActivityTack() {

    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    /**
     * 完全退出
     */
    public void exit(Context context) {
        while(activityList.size() > 0) {
            activityList.get(activityList.size() - 1).finish();
        }
        System.exit(0);
    }

    /**
     * 根据Class Name获取activity
     * @param name
     * @return
     */
    public Activity getActivityByClassName(String name) {
        for(Activity activity : activityList) {
            if(activity.getClass().getName().indexOf(name) >= 0) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 根据class获取activity
     * @param clazz
     * @return
     */
    public Activity getActivityByClass(Class clazz) {
        for(Activity activity : activityList) {
            if(activity.getClass().equals(clazz)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 弹出activity
     * @param activity
     */
    public void popActivity(Activity activity) {
        removeActivity(activity);
        activity.finish();
    }

    /**
     * 弹出activity不包含指定的class
     * @param classes
     */
    public void popUntilActivity(Class... classes) {
        List<Activity> list = new ArrayList<Activity>();
        for(int i = activityList.size() - 1; i >= 0; i--) {
            Activity activity = activityList.get(i);
            boolean isTop = false;
            for(int j = 0; j < classes.length; j++) {
                if(activity.getClass().equals(classes[j])) {
                    isTop = true;
                    break;
                }
            }

            if(!isTop) {
                list.add(activity);
            } else {
                break;
            }
        }

        for(Iterator<Activity> iterator = list.iterator(); iterator.hasNext();) {
            Activity activity = iterator.next();
            popActivity(activity);
        }
    }

}

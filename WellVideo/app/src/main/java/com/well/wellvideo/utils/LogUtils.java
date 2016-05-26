package com.well.wellvideo.utils;

import android.util.Log;

public class LogUtils {
	public static boolean islog = false;

	public static void e(String tag, String content) {
		if (islog)
			Log.e(tag, content);
	}

}

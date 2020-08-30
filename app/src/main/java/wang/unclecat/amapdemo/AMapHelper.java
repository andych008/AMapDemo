package wang.unclecat.amapdemo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;

/**
 * 高德定位帮助类
 * <p>
 * https://lbs.amap.com/api/android-location-sdk/locationsummary/
 *
 * @author: 喵叔catuncle
 * @date: 2020/8/29 0029 22:23
 */
public class AMapHelper {

    private static final String[] LBS_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            "android.permission.ACCESS_BACKGROUND_LOCATION"};

    /**
     * 检查定位权限,如果没有请求权限
     *
     * https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/location-guidev4
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean checkPermission(Activity activity, int requestCode) {
        //28：Android 9.0 P
        String[] permissions = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ? new String[]{LBS_PERMISSIONS[0], LBS_PERMISSIONS[1]} : LBS_PERMISSIONS;

        if (checkSelfPermissions(activity, permissions)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            return false;
        }
    }

    private static boolean checkSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 定位类型对照表
     * <p>
     * https://lbs.amap.com/api/android-location-sdk/guide/utilities/location-type
     */
    public static String getTypeDesc(int locationType) {

        switch (locationType) {
            case 0:
                return "定位失败";
            case AMapLocation.LOCATION_TYPE_GPS:
                return "GPS定位结果";
            case AMapLocation.LOCATION_TYPE_SAME_REQ:
                return "前次定位结果";
            case AMapLocation.LOCATION_TYPE_FIX_CACHE:
                return "缓存定位结果";
            case AMapLocation.LOCATION_TYPE_WIFI:
                return "Wifi定位结果";
            case AMapLocation.LOCATION_TYPE_CELL:
                return "基站定位结果";
            case AMapLocation.LOCATION_TYPE_OFFLINE:
                return "离线定位结果";
            case AMapLocation.LOCATION_TYPE_LAST_LOCATION_CACHE:
                return "最后位置缓存";
            default:
                return "未知类型";
        }
    }
}

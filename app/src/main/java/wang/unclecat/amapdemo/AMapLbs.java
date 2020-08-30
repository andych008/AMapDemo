package wang.unclecat.amapdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 高德定位封装
 * <p>
 * https://lbs.amap.com/api/android-location-sdk/locationsummary/
 *
 * @author: 喵叔catuncle
 * @date: 2020/8/29 0029 22:23
 */
public class AMapLbs {

    public static final String TAG = AMapLbs.class.getSimpleName();

    private LocationListener locationListener;
    private Set<LocationListener> listeners = new HashSet<>();

    public static AMapLbs getInstance() {
        return InstanceHolder.instance;
    }

    public void addListener(LocationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LocationListener listener) {
        listeners.remove(listener);
    }

    /**
     * 执行一次定位
     * <p>
     * https://lbs.amap.com/api/android-location-sdk/guide/utilities/errorcode/
     * <p>
     * https://lbs.amap.com/api/android-sdk/guide/map-data/geo#reverse-geocode
     *
     * @param context
     */
    public void startOnceLocation(final Context context) {
        startOnceLocation(context, true);
    }

    /**
     * 执行一次定位
     * <p>
     * https://lbs.amap.com/api/android-location-sdk/guide/utilities/errorcode/
     * <p>
     * https://lbs.amap.com/api/android-sdk/guide/map-data/geo#reverse-geocode
     *
     * @param context
     * @param useGPS 是否启用gps定位
     */
    public void startOnceLocation(final Context context, boolean useGPS) {

        android.location.LocationManager locationManager
                = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> allProviders = locationManager.getAllProviders();
        Log.d(TAG, "allProviders = [" + allProviders + "]");//[passive, gps, network]//雷电模拟器没有network provider

        if (useGPS) {
            //如果useGPS为true，提前判断gps定位权限，为的是执行定位操作成功率更高。
            //判断GPS定位权限
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (locationListener != null) {
                    //<!--用于访问GPS定位-->
                    //<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
                    locationListener.onLocationFailed(AMapLocation.ERROR_CODE_FAILURE_LOCATION_PERMISSION, context.getString(R.string.amap_no_lbs_permission));
                }
                return;
            }

            //判断GPS是否打开
            if (!isGPSOpen(context)) {
                if (locationListener != null) {
                    locationListener.onLocationFailed(AMapLocation.ERROR_CODE_FAILURE_NOENOUGHSATELLITES, context.getString(R.string.amap_gps_unopend));
                }
                return;
            }
        }

        Log.d(TAG, "begin location");
        AMapLocationClient locationClient = new AMapLocationClient(context);

        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        Log.d(TAG, "onLocationChanged() called with: amapLocation.getLocationType() = [" + AMapHelper.getTypeDesc(amapLocation.getLocationType()) + "]");
                        Log.d(TAG, "onLocationChanged() called with: amapLocation = [" + amapLocation + "]");

                        GeocodeSearch search = new GeocodeSearch(context);

                        LatLonPoint latLonPoint = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
                        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
                        search.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                            @Override
                            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
                                Log.d(TAG, "onRegeocodeSearched() called with: regeocodeResult = [" + regeocodeResult + "], rCode = [" + rCode + "]");
                                if (rCode == 1000) {
                                    try {
                                        RegeocodeQuery regeocodeQuery = regeocodeResult.getRegeocodeQuery();
                                        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
                                        if (locationListener != null) {
                                            locationListener.onLocationResult(regeocodeQuery.getPoint(), address);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        if (locationListener != null) {
                                            locationListener.onLocationFailed(-2, context.getString(R.string.amap_undefined));
                                        }
                                    }
                                } else {
                                    if (locationListener != null) {
                                        locationListener.onLocationFailed(rCode, context.getString(R.string.amap_regeocode_failed));
                                    }
                                }
                            }

                            @Override
                            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                            }
                        });

                        search.getFromLocationAsyn(regeocodeQuery);
                    } else {
                        Log.e(TAG, "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());

                        if (locationListener != null) {
                            locationListener.onLocationFailed(amapLocation.getErrorCode(), amapLocation.getErrorInfo());
                        }
                    }
                }
            }
        });

        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置返回地址信息，默认为true
        mLocationOption.setNeedAddress(false);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(2000);
        mLocationOption.setHttpTimeOut(10000);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //启动单次定位
        mLocationOption.setOnceLocation(true);

        locationClient.setLocationOption(mLocationOption);
        locationClient.startLocation();
    }


    /**
     * 定位设备是否已打开(gps)
     *
     * @param context
     * @return
     */
    private boolean isGPSOpen(Context context) {
        android.location.LocationManager locationManager;
        locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private static class InstanceHolder {
        private final static AMapLbs instance = new AMapLbs();

    }

    private AMapLbs() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationResult(LatLonPoint point, RegeocodeAddress location) {
                for (LocationListener listener : listeners) {
                    listener.onLocationResult(point, location);
                }
            }

            @Override
            public void onLocationFailed(int errorCode, String errorMsg) {
                for (LocationListener listener : listeners) {
                    listener.onLocationFailed(errorCode, errorMsg);
                }
            }
        };
    }

    public interface LocationListener {
        /**
         * 定位成功回调
         *
         * @param location 定位信息
         */
        void onLocationResult(LatLonPoint point, RegeocodeAddress location);

        /**
         * 定位失败回调
         *
         * @param errorCode 异常信息
         */
        void onLocationFailed(int errorCode, String errorMsg);
    }
}

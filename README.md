# AMapDemo
- 描述：高德定位简单封装
- 目标：降低使用高德定位的难度，能够覆盖安卓开发的常见使用场景，提高高德定位的易用性。

---
## 使用
1. 回调接口的注册、注销
    ```java
    locationListener = new AMapLbs.LocationListener() {
        @Override
        public void onLocationResult(LatLonPoint point, RegeocodeAddress location) {
            //成功
        }

        @Override
        public void onLocationFailed(int errorCode, String errorMsg) {
            //失败
        }
    };
    AMapLbs.getInstance().addListener(locationListener);
    ```
    ```java
    //一般在Activity的onDestroy()中调用
    AMapLbs.getInstance().removeListener(locationListener);
    ```
1. 发起定位
    - 一次定位(使用gps)
        ```java
        AMapLbs.getInstance().startOnceLocation(MainActivity.this);
        ```
    - 一次定位(不使用gps)
        ```java
        AMapLbs.getInstance().startOnceLocation(MainActivity.this, false);
        ```

---
## 笔记
1. `setNeedAddress(true)`相当于定位后会默认执行逆地理编码，但是返回的数据不全。所以，本次封装手动实现定位、逆地理编码两步的执行。
    ```
    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    //设置返回地址信息，默认为true
    mLocationOption.setNeedAddress(false);
    ```
1. `setLocationCacheEnable(false)`关闭缓存机制。
    ```
    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    //关闭缓存机制，默认为true
    mLocationOption.setLocationCacheEnable(false);
    ```
1. `setOnceLocation(true)`启动单次定位。对于有些使用场景（只需要一次定位），设置这个属性，就不需要在定位完成后手动调用`locationClient.stopLocation()`了。
    ```
    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    //启动单次定位，默认为false
    mLocationOption.setOnceLocation(true);
    ```


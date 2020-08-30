package wang.unclecat.amapdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.RegeocodeAddress;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AMapLbs.LocationListener locationListener;

    private TextView consoleView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consoleView = findViewById(R.id.console);


        findViewById(R.id.lbs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    consoleView.setText("");
                    AMapLbs.getInstance().startOnceLocation(MainActivity.this);
                }
            }
        });


        locationListener = new AMapLbs.LocationListener() {
            @Override
            public void onLocationResult(LatLonPoint point, RegeocodeAddress location) {
                try {
                    JSONObject jsonResult = getJsonResult(point, location);
                    Log.d(AMapLbs.TAG, "onLocationResult() called with: point = [" + point + "], location = \n" + jsonResult.toString(2) + "");
                    consoleView.setText(jsonResult.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLocationFailed(int errorCode, String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        };
        AMapLbs.getInstance().addListener(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AMapLbs.getInstance().removeListener(locationListener);
    }

    private static final int PERMISSION_REQUEST_CODE = 0X01;
    private boolean checkPermission() {
        return AMapHelper.checkPermission(this, PERMISSION_REQUEST_CODE);
    }



    private JSONObject getJsonResult(LatLonPoint point, RegeocodeAddress location) throws JSONException {
        JSONObject jsonObject = null;
        if (location != null) {
            jsonObject = new JSONObject();

//            LatLonPoint point = location.getStreetNumber().getLatLonPoint();//这样取到的point可能为null
            // 获取经度
            jsonObject.put("longitude", point.getLongitude());
            // 获取纬度
            jsonObject.put("latitude", point.getLatitude());
            // 国家
            jsonObject.put("country", location.getCountry());
            // 省
            jsonObject.put("province", location.getProvince());
            // 城市信息
            jsonObject.put("city", location.getCity());
            // 城市编码
            jsonObject.put("citycode", location.getCityCode());
            // 区/县
            jsonObject.put("district", location.getDistrict());
            // 地区编码
            jsonObject.put("adcode", location.getAdCode());
            // 镇
            jsonObject.put("township", location.getTownship());

            jsonObject.put("formattedAddress", location.getFormatAddress());
            jsonObject.put("street", location.getStreetNumber().getStreet());
            jsonObject.put("streetNumber", location.getStreetNumber().getNumber());
            jsonObject.put("building", location.getBuilding());
            jsonObject.put("neighborhood", location.getNeighborhood());
        }
        return jsonObject;
    }
}

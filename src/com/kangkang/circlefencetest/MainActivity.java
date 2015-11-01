package com.kangkang.circlefencetest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.kangkang.connection.JsonRequestProxy;
import com.kangkang.connection.JsonRequestProxy.JsonResponseListener;
import com.kangkang.spotData.SpotData;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	MapView mMapView = null;
	public BaiduMap mBaiduMap;
	private RequestQueue mRequestQueue;
	private JsonRequestProxy jsonRequest;
	private String url = "http://121.43.233.154:8080/AnyGuide/Point/selectusingBoundary";
	private ArrayList<SpotData> fenceData = new ArrayList<SpotData>();
	// 定位相关
	private LocationClient mLocationClient = null;
	private BDLocationListener mLocationListener = null;
	public boolean isFirstLoc = true;
	private double mLatitude;
	private double mLongitude;
	private double mRedius;
	private Context context;
	// 围栏相关
	private boolean isShowFence = true;
	private boolean isInFence = false;
	private String inFenceID = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		// 使用volley 通信
		mRequestQueue = Volley.newRequestQueue(this);
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);

		this.context = this;

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(msu);

		initLocation();
		getFenceData();

	}

	private void getFenceData() {
		jsonRequest = new JsonRequestProxy(url);

		jsonRequest.setJsonResponseListener(new JsonResponseListener() {

			@Override
			public void onResponseSuccess(String response) {
				// 解析得到围栏信息
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray fenceArray = jsonObject.getJSONArray("data");
					for (int i = 0; i < fenceArray.length(); ++i) {
						JSONObject jObject = fenceArray.getJSONObject(i);
						String name = jObject.getString("boundaryName");
						String id = jObject.getString("boundaryIdNo");
						String polygon = jObject.getString("boundaryNodeValue");
						LatLng center = parseStringGetCenter(polygon);
						SpotData newPoint = new SpotData(name, id, center);
						fenceData.add(newPoint);

						Log.d("TAG", name);
						Log.d("TAG", id);
						Log.d("TAG", fenceData.size()
								+ "############################");
					}
					// Log.d("TAG", fenceArray.length()+"      length");
					// Log.d("TAG", fenceArray.get(48).toString());

				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Log.d("TAG", response);
			}

			@Override
			public void onResponseError(String erroMessage) {
				Log.d("TAG", erroMessage);
			}
		});

		Map<String, String> map = new HashMap<String, String>();
		map.put("spotIdNo", "11");
		map.put("currentStatus", "1");
		jsonRequest.post(map);

	}

	private LatLng parseStringGetCenter(String polygon) {
		int index_start = polygon.indexOf("(") + 1;
		int index_end = polygon.indexOf(")");
		String ss = polygon.substring(index_start + 1, index_end);
		String[] list_s = ss.split(",");
		// ArrayList<Double> list_latitude = new ArrayList<Double>();
		// ArrayList<Double> list_longitude = new ArrayList<Double>();
		Double sumLatitude = 0.0;
		Double sumLongitude = 0.0;
		for (String str : list_s) {
			int index_split = str.indexOf(" ");
			// Log.d("TAG",Double.parseDouble(str.substring(0,
			// index_split))+"latitude");
			// Log.d("TAG",Double.parseDouble(str.substring(index_split+1))+"longitude");
			sumLongitude += Double.parseDouble(str.substring(0, index_split));
			sumLatitude += Double.parseDouble(str.substring(index_split + 1));
		}
		Log.d("TAG", sumLatitude / list_s.length + "****************latitude");
		Log.d("TAG", sumLongitude / list_s.length + "****************longitude");
		LatLng center = new LatLng(sumLatitude / list_s.length, sumLongitude
				/ list_s.length);
		Log.d("TAG", center.toString() + "LatLng");
		// 转化到百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.COMMON);
		// sourceLatLng待转换坐标
		converter.coord(center);
		LatLng desLatLng = converter.convert();
		return desLatLng;

	}

	@Override
	protected void onStart() {
		super.onStart();
		mBaiduMap.setMyLocationEnabled(true);

		if (!mLocationClient.isStarted())
			mLocationClient.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.id_map_location:
			centerToMyLocation();
			break;
		case R.id.id_map_show_fence:
			if (isShowFence) {
				isShowFence = false;
				item.setTitle("围栏显示(off)");

			} else {
				isShowFence = true;
				item.setTitle("围栏显示(on)");

			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);

	}

	private void initLocation() {
		mLocationClient = new LocationClient(context);
		mLocationListener = new MyLocationListenner();

		mLocationClient.registerLocationListener(mLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		// 可能泄露隐私
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setScanSpan(3000);

		mLocationClient.setLocOption(option);
	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				Toast.makeText(MainActivity.this, "服务器没有响应", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (mMapView == null) {
				Toast.makeText(MainActivity.this, "地图没有正确初始化",
						Toast.LENGTH_SHORT).show();
			}

			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			// 更新经纬度
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			mRedius = location.getRadius();
			mBaiduMap.setMyLocationData(locData);

			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}

			// 清除原有绘图
			mBaiduMap.clear();
			if (isShowFence) {
				// Toast.makeText(MainActivity.this, "显示围栏"+fenceData.size() ,
				// Toast.LENGTH_SHORT).show();

				for (SpotData fence : fenceData) {

					OverlayOptions circleOption = new CircleOptions()
							.center(fence.getCenter()).radius(60)
							.fillColor(0x00000000)
							.stroke(new Stroke(5, 0xAA0000FF));
					mBaiduMap.addOverlay(circleOption);

					// //显示中点图标
					// LatLng point = new
					// LatLng(fence.getCenter().latitude,fence.getCenter().longitude);
					// //构建Marker图标
					// BitmapDescriptor bitmap = BitmapDescriptorFactory
					// .fromResource(R.drawable.marker);
					// //构建MarkerOption，用于在地图上添加Marker
					// OverlayOptions option = new MarkerOptions()
					// .position(point)
					// .icon(bitmap);
					// //在地图上添加Marker，并显示
					// mBaiduMap.addOverlay(option);

				}
			}

			if (location.getLocType() == BDLocation.TypeGpsLocation || // GPS定位
					location.getLocType() == BDLocation.TypeNetWorkLocation || // 网络定位
					location.getLocType() == BDLocation.TypeOffLineLocation) { // 离线定位

				// ******************************************************************************************************************
				

				int min_index = -1;
				double min_distance = Double.MAX_VALUE;
				LatLng currentPoint = new LatLng(mLatitude, mLongitude);
				for (int i = 0; i < fenceData.size(); ++i) {
					double distance = DistanceUtil.getDistance(currentPoint,
							fenceData.get(i).getCenter());

					if (distance != -1 && distance < min_distance) {
						min_index = i;
						min_distance = distance;
					}
//					Log.d("DIS", ""+mLatitude+":"+mLongitude);
//					Log.d("DIS", ""+fenceData.get(i).getCenter().latitude+":"+fenceData.get(i).getCenter().longitude);

				}


				
				if (min_distance < 60) {
					OverlayOptions circleOption = new CircleOptions()
							.center(fenceData.get(min_index).getCenter())
							.radius(60).fillColor(0x00000000)
							.stroke(new Stroke(5, 0xFFFF0000));
					mBaiduMap.addOverlay(circleOption);
					
					
					if (!fenceData.get(min_index).getId().equals(inFenceID)) {

						
						
						Toast.makeText(
								MainActivity.this,
								"您已进入围栏" + fenceData.get(min_index).getId()
										+ fenceData.get(min_index).getName(),
								Toast.LENGTH_SHORT).show();
					}
					isInFence = true;
					inFenceID = fenceData.get(min_index).getId();

				} else {
					if(isInFence){
						Toast.makeText(
								MainActivity.this,
								"您离开围栏:" + inFenceID,
								Toast.LENGTH_SHORT).show();
						
					}
					isInFence = false;
				}

			} else { // 定位失败

			}

		}
	}

	private void centerToMyLocation() {
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);

	}

}

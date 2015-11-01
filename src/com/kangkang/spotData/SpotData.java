package com.kangkang.spotData;

import com.baidu.mapapi.model.LatLng;

public class SpotData {

	String name;
	String id;
	LatLng center;
	public SpotData(String name,String id, LatLng center){
		this.name = name;
		this.id = id;
		this.center = center;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LatLng getCenter() {
		return center;
	}
	public void setCenter(LatLng center) {
		this.center = center;
	}

}

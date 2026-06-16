package org.lkp.car.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.lkp.car.common.cache.CacheConstants;
import org.lkp.car.dto.ReverseGeocodeResponse;
import org.lkp.car.service.ReverseGeocodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ReverseGeocodeServiceImpl implements ReverseGeocodeService {

    @Value("${tencent.map.key:}")
    private String tencentMapKey;

    @Override
    @Cacheable(
            value = CacheConstants.REDIS_GEOCODE,
            key = "T(String).format('%.4f:%.4f', #latitude, #longitude)",
            cacheManager = "redisCacheManager",
            unless = "#result == null"
    )
    public ReverseGeocodeResponse reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            ReverseGeocodeResponse response = new ReverseGeocodeResponse();
            response.setAddress("位置信息不完整");
            return response;
        }

        ReverseGeocodeResponse response = null;
        if (StrUtil.isNotBlank(tencentMapKey)) {
            response = reverseGeocodeWithTencent(latitude, longitude);
        }
        if (response == null) {
            response = reverseGeocodeWithNominatim(latitude, longitude);
        }
        if (response == null) {
            response = new ReverseGeocodeResponse();
            response.setAddress(String.format("%.6f, %.6f", latitude, longitude));
        }
        return response;
    }

    private ReverseGeocodeResponse reverseGeocodeWithTencent(Double lat, Double lng) {
        try {
            String url = "https://apis.map.qq.com/ws/geocoder/v1/";
            HttpResponse response = HttpRequest.get(url)
                    .form("location", lat + "," + lng)
                    .form("key", tencentMapKey)
                    .form("get_poi", "0")
                    .timeout(5000)
                    .execute();

            if (response.isOk()) {
                JSONObject json = JSONUtil.parseObj(response.body());
                if (json.getInt("status") == 0) {
                    JSONObject result = json.getJSONObject("result");
                    JSONObject addressComponent = result.getJSONObject("address_component");

                    ReverseGeocodeResponse res = new ReverseGeocodeResponse();
                    res.setAddress(result.getStr("address"));
                    res.setCountry(addressComponent.getStr("nation"));
                    res.setProvince(addressComponent.getStr("province"));
                    res.setCity(addressComponent.getStr("city"));
                    res.setDistrict(addressComponent.getStr("district"));
                    res.setStreet(addressComponent.getStr("street"));
                    res.setStreetNumber(addressComponent.getStr("street_number"));
                    return res;
                }
            }
        } catch (Exception ignored) {
            // 腾讯地图失败，继续尝试其他方式
        }
        return null;
    }

    private ReverseGeocodeResponse reverseGeocodeWithNominatim(Double lat, Double lng) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse";
            HttpResponse response = HttpRequest.get(url)
                    .form("format", "jsonv2")
                    .form("lat", lat)
                    .form("lon", lng)
                    .form("zoom", "18")
                    .form("accept-language", "zh-CN")
                    .header("User-Agent", "CarAdmin/1.0")
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                JSONObject json = JSONUtil.parseObj(response.body());

                ReverseGeocodeResponse res = new ReverseGeocodeResponse();
                res.setAddress(json.getStr("display_name"));

                JSONObject address = json.getJSONObject("address");
                if (address != null) {
                    res.setCountry(address.getStr("country"));
                    String province = address.getStr("province");
                    if (StrUtil.isBlank(province)) {
                        province = address.getStr("state");
                    }
                    res.setProvince(province);
                    String city = address.getStr("city");
                    if (StrUtil.isBlank(city)) {
                        city = address.getStr("town");
                    }
                    res.setCity(city);
                    res.setDistrict(address.getStr("district"));
                    res.setStreet(address.getStr("road"));
                    res.setStreetNumber(address.getStr("house_number"));
                }
                return res;
            }
        } catch (Exception ignored) {
            // Nominatim 失败
        }
        return null;
    }
}

package org.lkp.car.service;

import org.lkp.car.dto.ReverseGeocodeResponse;

public interface ReverseGeocodeService {

    ReverseGeocodeResponse reverseGeocode(Double latitude, Double longitude);
}

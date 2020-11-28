package org.sylab.geolego.model.Storage;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

public class GeoFeature {
    protected Geometry _shape;
    protected Map<String, Object> _atrributes;


    public Geometry get_shape() {
        return _shape;
    }

    public Map<String, Object> get_arributes() {
        return _atrributes;
    }

    public GeoFeature(Map<String, Object> atrr, Geometry geom) {

        this._atrributes = atrr;
        this._shape = geom;

    }

    public boolean setValue(String fieldName, Object value) {

        if (_atrributes.containsKey(fieldName)) {
            return false;
        }
        _atrributes.put(fieldName, value);

        return true;
    }

    public Object getValue(String fieldName) {

        if (_atrributes.containsKey(fieldName)) {
            return _atrributes.get(fieldName);
        }

        return false;
    }
}

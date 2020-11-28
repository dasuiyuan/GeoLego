package org.sylab.geolego.model.Storage;

import java.lang.reflect.Type;

public class GeoField {
    private String _name;
    private Type _field_type;
    private Integer _index;
    private boolean _isPK;
    private boolean _isGeometry;

    public boolean is_isPK() {
        return _isPK;
    }

    public void set_isPK(boolean _isPK) {
        this._isPK = _isPK;
    }

    public boolean is_isGeometry() {
        return _isGeometry;
    }

    public void set_isGeometry(boolean _isGeometry) {
        this._isGeometry = _isGeometry;
    }

    public String getName() {
        return _name;
    }

    public Type getType() {
        return _field_type;
    }

    public Integer get_index() {
        return _index;
    }

    public GeoField(Integer index, String name, Type type) {
        this._index = index;
        this._name = name;
        this._field_type = type;
    }


}

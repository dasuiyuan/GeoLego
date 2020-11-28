package org.sylab.geolego.model.Storage;

import java.util.ArrayList;
import java.util.List;

public class GeoFeatureClass {

    private List<GeoFeature> _features;

    private GeoField _primaryField;

    private List<GeoField> _fields;

    public List<GeoField> get_fields() {
        return _fields;
    }

    public List<GeoFeature> getFeatures() {
        return _features;
    }

    public GeoFeatureClass(List<GeoFeature> feats, List<GeoField> flds, String primaryKey) {

        _features = feats;
        _fields = flds;
        _primaryField = _fields.stream().filter((f) -> f.getName() == primaryKey).findFirst().get();

    }

    public GeoFeatureClass(List<GeoField> flds, String primaryKey) {

        _fields = flds;
        _primaryField = _fields.stream().filter((f) -> f.getName() == primaryKey).findFirst().get();
    }

    public GeoFeatureClass(List<GeoField> flds) {
        _fields = flds;
        _primaryField = _fields.stream().filter((f) -> f.is_isPK()).findFirst().get();
    }


    public void addFeature(GeoFeature feat) {
        if (_features == null) {
            _features = new ArrayList<>();
        }

        this._features.add(feat);
    }

    public void delFeature(Object key) {
        try {
            _features.removeIf((f) -> f.getValue(_primaryField.getName()) == key);
        } catch (Exception ex) {

        }
    }
}

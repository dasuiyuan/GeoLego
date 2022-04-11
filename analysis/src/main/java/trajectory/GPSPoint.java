package trajectory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Point;

import java.sql.Timestamp;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/7 18:32
 * @since:
 **/
@Data
@AllArgsConstructor
public class GPSPoint {
    private final static WKTWriter2 WKT_WRITER = new WKTWriter2();
    private String id;
    private Timestamp timestamp;
    private Point raw;

    @Override
    public String toString() {
        return id + "|" + timestamp + "|" + WKT_WRITER.write(raw);
    }
}

package trajectory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.geomesa.utils.interop.WKTUtils;
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
    private String id;
    private Timestamp timestamp;
    private Point raw;

    @Override
    public String toString() {
        return id + "|" + timestamp + "|" + WKTUtils.write(raw);
    }
}

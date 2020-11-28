import spark.Request;
import spark.Response;
import spark.Route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.port;
import static spark.Spark.threadPool;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2019-06-14 10:40
 * @modified by :
 **/
public class restfultest {
    public static void main(String[] args) {
        port(18989);
        //threadPool(8, 2, 3600000);
        spark.Spark.get("/test", (request, response) -> "hello!");
    }
}

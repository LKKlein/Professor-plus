import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by Klein on 2017/5/3.
 */

public class Test {
    public static void main(String[] args) {
        String s = "{\n" +
                "\t\"time_used\": 5,\n" +
                "\t\"error_message\": \"INVALID_OUTER_ID\",\n" +
                "\t\"request_id\": \"1469761051,ec285c20-8660-47d3-8b91-5dc2bffa0049\"\n" +
                "}";
        JSONObject json = JSON.parseObject(s);
        System.out.println(json.getString("error_message"));
    }
}

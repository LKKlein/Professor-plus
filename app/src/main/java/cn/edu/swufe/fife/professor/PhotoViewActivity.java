package cn.edu.swufe.fife.professor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.utils.UrlSafeBase64;

import java.io.File;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PhotoViewActivity extends AppCompatActivity {

    private ImageView img;
    private AppCompatButton back, reco;

    //七牛后台的key
    private static String access_key = "TNfTzPtk6UeSes61YPNmVnT_436FmRgaS7yAd_F1";
    //七牛后台的secret
    private static String secret_key = "3_2YqgvdPsOUL3LEdP1p7I1X9KAt8P2HxaMJTC_Y";

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    private String local_img_name, local_img_path;
    private Context context = PhotoViewActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo_view);

        Bundle extra = getIntent().getExtras();
        local_img_name = extra.getString("name");
        local_img_path = extra.getString("path");

        img = (ImageView) findViewById(R.id.photo_view_photo);
        back = (AppCompatButton) findViewById(R.id.photo_view_back);
        reco = (AppCompatButton) findViewById(R.id.photo_view_reco);

        up2qiniu();

        Glide.with(this)
                .load(new File(local_img_path, local_img_name))
                .placeholder(R.drawable.miao)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new CornersTransform(this, 10))
                .crossFade()
                .into(img);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoViewActivity.this.finish();
            }
        });

        reco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PhotoViewActivity.this, RecoResultActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey)
            throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        Mac mac = Mac.getInstance(MAC_NAME);
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        return mac.doFinal(text);
    }

    private void up2qiniu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String scope = "search-photo";
                long time = (System.currentTimeMillis() + 3600000) / 1000;
                String serial = "{\"scope\":\"" + scope + "\", \"deadline\":" + time + "}";
                String encode_serial = UrlSafeBase64.encodeToString(serial.getBytes());
                byte[] _sercet = new byte[0];
                try {
                    _sercet = HmacSHA1Encrypt(encode_serial, secret_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String encode_secret = UrlSafeBase64.encodeToString(_sercet);
                String token = access_key + ":" + encode_secret + ":" + encode_serial;

                Configuration config = new Configuration.Builder()
                        .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认256K
                        .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认512K
                        .connectTimeout(10) // 链接超时。默认10秒
                        .responseTimeout(60) // 服务器响应超时。默认60秒
                        .build();
                // 重用uploadManager。一般地，只需要创建一个uploadManager对象
                UploadManager uploadManager = new UploadManager(config);
                uploadManager.put(local_img_path + "/" + local_img_name, local_img_name, token,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, org.json.JSONObject response) {
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + response);
                            }
                        }, null);
            }
        });
    }
}
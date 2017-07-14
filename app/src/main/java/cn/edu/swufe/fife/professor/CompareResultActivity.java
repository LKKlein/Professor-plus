package cn.edu.swufe.fife.professor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.megvii.cloud.http.CommonOperate;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.utils.UrlSafeBase64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompareResultActivity extends AppCompatActivity {

    private ImageView origin, simi, no_result_img;
    private TextView search_text, name_value, university_value, page_value, no_result_simi;
    private FloatingActionButton compare_fab;
    private LinearLayout success_linear;
    private String api_key = "l0ACAAzKtGUnOmaM4_0Xgq2CV-D_P_hk";
    private String api_secret = "xNg1R0Bt1tmkWNrjIkEQ2JYCKhPiCvz6";

    //七牛后台的key
    private static String access_key = "TNfTzPtk6UeSes61YPNmVnT_436FmRgaS7yAd_F1";
    //七牛后台的secret
    private static String secret_key = "3_2YqgvdPsOUL3LEdP1p7I1X9KAt8P2HxaMJTC_Y";

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    private String name, path;

    private int width, height, img_width, img_height;
    private Context context = CompareResultActivity.this;

    private static final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.compare_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extra = getIntent().getExtras();
        name = extra.getString("name");
        path = extra.getString("path");

        origin = (ImageView) findViewById(R.id.compare_origin_photo);
        simi = (ImageView) findViewById(R.id.compare_simi_photo);

        search_text = (TextView) findViewById(R.id.compare_search_text);
        name_value = (TextView) findViewById(R.id.compare_found_name_value);
        university_value = (TextView) findViewById(R.id.compare_found_university_value);
        page_value = (TextView) findViewById(R.id.compare_found_webpage_value);
        compare_fab = (FloatingActionButton) findViewById(R.id.compare_fab);
        no_result_img = (ImageView) findViewById(R.id.compare_no_found_image);
        no_result_simi = (TextView) findViewById(R.id.simi_text_placeholder);
        success_linear = (LinearLayout) findViewById(R.id.compare_success_result_linear);

        ViewTreeObserver vto = no_result_img.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                no_result_img.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                img_width = no_result_img.getWidth();
                img_height = no_result_img.getHeight();
                System.out.println(img_height);
                System.out.println(img_width);
            }
        });

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path + "/" + name, options);
        width = options.outWidth;
        height = options.outHeight;
        File photo = new File(path, name);
        Glide.with(this)
                .load(photo)
                .placeholder(R.drawable.miao)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new CornersTransform(this, 10))
                .crossFade()
                .into(origin);
        Glide.with(this)
                .load("https://dummyimage.com/" + width + "x" + height + "/C3C3C3/d8dbd4&text=Have+a+Rest~")
                .placeholder(R.drawable.miao)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new CornersTransform(this, 10))
                .crossFade()
                .into(simi);
        recognize(path, name);
        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognize(path, name);
            }
        });

    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);// 利用矩阵进行缩放不会造成内存溢出
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    public static byte[] getBytesFromFile(String path, String name) {
        Bitmap bitmap = BitmapFactory.decodeFile(path + '/' + name);
        Bitmap newbmp = zoomBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        newbmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    public static String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public static String postStringFromServer(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
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

    private void recognize(final String path, final String name) {
        search_text.setVisibility(View.VISIBLE);
        success_linear.setVisibility(View.GONE);
        no_result_img.setVisibility(View.GONE);
        compare_fab.setVisibility(View.GONE);
        search_text.setText(R.string.upload_ing);
        simi.setVisibility(View.VISIBLE);
        no_result_simi.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {

                //------------------------   上传图片至七牛云   ---------------------------//

                final boolean[] Net_flag = {false};

                String scope = "search-photo";
                long time = (System.currentTimeMillis()+ 3600000)/1000;
                String serial = "{\"scope\":\""+scope+"\", \"deadline\":"+time+"}";
                String encode_serial = UrlSafeBase64.encodeToString(serial.getBytes());
                byte[] _sercet = new byte[0];
                try {
                    _sercet = HmacSHA1Encrypt(encode_serial, secret_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String encode_secret = UrlSafeBase64.encodeToString(_sercet);
                String token = access_key+":"+encode_secret+":"+encode_serial;

                Configuration config = new Configuration.Builder()
                        .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认256K
                        .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认512K
                        .connectTimeout(10) // 链接超时。默认10秒
                        .responseTimeout(60) // 服务器响应超时。默认60秒
                        .build();
                // 重用uploadManager。一般地，只需要创建一个uploadManager对象
                UploadManager uploadManager = new UploadManager(config);
                uploadManager.put(path + "/" + name, name, token,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, org.json.JSONObject response) {
                                if(info.isOK()) {
                                    Net_flag[0] = false;
                                    Log.i("qiniu", "Upload Success");
                                } else {
                                    Net_flag[0] = true;
                                    Log.i("qiniu", "Upload Fail");
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + response);
                            }
                        }, null);

                //------------------------   上传图片至七牛云   ---------------------------//

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        search_text.setText(R.string.search_ing);
                        Log.e("上传的IMAGE_URL", Constant.search_domain +name);
                    }
                });

                if (Net_flag[0]) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CompareResultActivity.this, "Please check your network!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //------------------------   使用face++搜索   ---------------------------//

                    CommonOperate commonoperate = new CommonOperate(api_key, api_secret, false);
                    boolean flag = true;
                    String result = null;
                    while (flag) {
                        try {
                            Thread.sleep(200);
                            com.megvii.cloud.http.Response res = commonoperate.searchByOuterId(null, Constant.search_domain +name,
                                    null, "20170503", 1);
                            result = new String(res.getContent());
                            Log.e("result ====== >>>>>>> ", result);
                            JSONObject json0 = JSON.parseObject(result);
                            flag = json0.containsKey("error_message");
                        } catch (Exception e) {
                            flag = false;
                        }
                    }

                    JSONObject json = JSON.parseObject(result);
                    int faces = json.getJSONArray("faces").size();

                    //------------------------   没有找到人脸   ---------------------------//

                    if (faces == 0) {
                        System.out.println("没有找到脸在哪里。。。");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                search_text.setVisibility(View.GONE);
                                success_linear.setVisibility(View.GONE);
                                no_result_img.setVisibility(View.VISIBLE);
                                compare_fab.setVisibility(View.GONE);
                                simi.setVisibility(View.GONE);
                                no_result_simi.setVisibility(View.VISIBLE);
                                no_result_simi.setText(R.string.no_face);

                                Glide.with(CompareResultActivity.this)
                                        .load("https://dummyimage.com/" + img_width + "x" + img_height + "/D3D3D3/263238&text=Can+I+be+your+friend%EF%BC%9F")
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .transform(new CornersTransform(CompareResultActivity.this, 10))
                                        .crossFade()
                                        .into(no_result_img);
                            }
                        });
                    }

                    //------------------------   没有找到人脸   ---------------------------//

                    //------------------------   找到了人脸   ---------------------------//

                    else {
                        JSONArray reco_array = json.getJSONArray("results");
                        JSONObject reco_result = reco_array.getJSONObject(0);
                        int confidence = reco_result.getInteger("confidence");
                        final String face_token = reco_result.getString("face_token");

                        //------------------------   置信度在0.75以上的结果   ---------------------------//

                        if (confidence >= 75) {
                            String out = "";
                            try {
                                out = getStringFromServer(Constant.search_url + face_token);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            JSONObject out_json = JSON.parseObject(out);
                            final String names = out_json.getString("name");
                            final String university = out_json.getString("university");
                            final String page = out_json.getString("web_page");
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    search_text.setVisibility(View.GONE);
                                    success_linear.setVisibility(View.VISIBLE);
                                    no_result_img.setVisibility(View.GONE);
                                    compare_fab.setVisibility(View.GONE);
                                    simi.setVisibility(View.VISIBLE);
                                    no_result_simi.setVisibility(View.GONE);

                                    SharedPreferences s = context.getSharedPreferences("recent", Context.MODE_PRIVATE);
                                    Set<String> recent = new LinkedHashSet<>(s.getStringSet("recent faces", new LinkedHashSet<String>()));
                                    Professor professor = new Professor(names, university, page, path, name);
                                    String professor_json = JSON.toJSONString(professor);
                                    Log.e("professor_json == >>", professor_json);
                                    recent.add(professor_json);

                                    SharedPreferences.Editor editor = s.edit();
                                    editor.putStringSet("recent faces", recent);
                                    boolean editor_result = editor.commit();
                                    Log.e("out -- editor_result", String.valueOf(editor_result));

                                    Glide.with(CompareResultActivity.this)
                                            .load(Constant.download_domain + face_token + ".png?imageslim")
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .transform(new CornersTransform(CompareResultActivity.this, 10))
                                            .crossFade()
                                            .into(simi);

                                    name_value.setText(names);
                                    university_value.setText(university);
                                    page_value.setText(getResources().getString(R.string.compare_found_text_reddirect));
                                    SpannableString ss = new SpannableString(getResources().getString(R.string.compare_found_text_reddirect));
                                    ss.setSpan(new URLSpan(getResources().getString(R.string.compare_found_text_reddirect)), 0, ss.length(),
                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    page_value.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent i = new Intent(CompareResultActivity.this, WebViewActivity.class);
                                            i.putExtra("url", page);
                                            i.putExtra("name", names);
                                            startActivity(i);
                                        }
                                    });

                                }
                            });

                        }

                        //------------------------   置信度在0.75以上的结果   ---------------------------//

                        //------------------------   置信度在0.75以下的结果   ---------------------------//

                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    search_text.setVisibility(View.GONE);
                                    success_linear.setVisibility(View.GONE);
                                    no_result_img.setVisibility(View.VISIBLE);
                                    compare_fab.setVisibility(View.VISIBLE);
                                    simi.setVisibility(View.GONE);
                                    no_result_simi.setVisibility(View.VISIBLE);
                                    no_result_simi.setText(R.string.no_recognize);

                                    Glide.with(CompareResultActivity.this)
                                            .load("https://dummyimage.com/" + img_width + "x" + img_height + "/D3D3D3/263238&text=Can+I+be+your+friend%EF%BC%9F")
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .transform(new CornersTransform(CompareResultActivity.this, 10))
                                            .crossFade()
                                            .into(no_result_img);
                                    compare_fab.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent i = new Intent(CompareResultActivity.this, AddingProfessorActivity.class);
                                            i.putExtra("path", path);
                                            i.putExtra("name", name);
                                            startActivity(i);
                                        }
                                    });
                                }
                            });

                            //------------------------   置信度在0.75以下的结果   ---------------------------//
                        }
                        //------------------------   找到了人脸   ---------------------------//
                    }
                    //------------------------   使用face++搜索   ---------------------------//
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

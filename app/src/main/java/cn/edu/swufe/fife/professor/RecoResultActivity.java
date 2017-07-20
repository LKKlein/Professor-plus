package cn.edu.swufe.fife.professor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.megvii.cloud.http.CommonOperate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecoResultActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private ZoomHeaderView mZoomHeader;

    private String api_key = "l0ACAAzKtGUnOmaM4_0Xgq2CV-D_P_hk";
    private String api_secret = "xNg1R0Bt1tmkWNrjIkEQ2JYCKhPiCvz6";

    private static final OkHttpClient client = new OkHttpClient();
    private String local_img_name, local_img_path;

    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco_result);

        activity = RecoResultActivity.this;

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mZoomHeader = (ZoomHeaderView) findViewById(R.id.zoomHeader);

        Bundle extra = getIntent().getExtras();
        local_img_name = extra.getString("name");
        local_img_path = extra.getString("path");

        new RecognizingTask().execute(Constant.search_domain + local_img_name + "?imageView2/3/w/1080/h/1920/q/100%7Cimageslim");
    }

    private class RecognizingTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mViewPager.setAdapter(new Adapter(null, null, 1, R.layout.item_viewpager_loading));
            mViewPager.setOffscreenPageLimit(10);
        }

        @Override
        protected String doInBackground(String[] params) {
            CommonOperate commonoperate = new CommonOperate(api_key, api_secret, false);
            boolean flag = true;
            String result = null;
            while (flag) {
                try {
                    com.megvii.cloud.http.Response res = commonoperate.searchByOuterId(null, params[0],
                            null, "20170503", 5);
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
            if (faces != 0) {
                JSONArray reco_array = json.getJSONArray("results");
                for (int i = 0; i < reco_array.size(); i++) {
                    JSONObject reco_result = reco_array.getJSONObject(i);
                    int confidence = reco_result.getInteger("confidence");
                    String face_token = reco_result.getString("face_token");
                    if (confidence > 70) {
                        String out = getStringFromServer(Constant.search_url + face_token);
                        json.put(face_token, out);
                    }
                }
            }
            return json.toJSONString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JSONObject json = JSON.parseObject(result);
            int faces = json.getJSONArray("faces").size();

            System.out.println(result);

            if (faces == 0) {
                System.out.println("没有找到脸在哪里。。。");
                mViewPager.setAdapter(new Adapter(null, null, 1, R.layout.item_viewpager_noface));
                mViewPager.setOffscreenPageLimit(10);
            } else {
                JSONArray reco_array = json.getJSONArray("results");
                List<String> imgs = new ArrayList<>();
                List<String> infos = new ArrayList<>();
                for (int i = 0; i < reco_array.size(); i++) {
                    JSONObject reco_result = reco_array.getJSONObject(i);
                    int confidence = reco_result.getInteger("confidence");
                    String face_token = reco_result.getString("face_token");
                    if (confidence > 70) {
                        imgs.add(face_token);
                        infos.add(json.getString(face_token));
                    }
                }

                if (imgs.size() == 0) {
                    mViewPager.setAdapter(new Adapter(null, null, 1, R.layout.item_viewpager_not_recognize));
                    mViewPager.setOffscreenPageLimit(10);
                } else {
                    mViewPager.setAdapter(new Adapter(imgs, infos, imgs.size(), R.layout.item_viewpager_complete));
                    mViewPager.setOffscreenPageLimit(10);
                }
            }

        }
    }

    public static String getStringFromServer(final String url) {
        String result = null;
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class Adapter extends PagerAdapter {
        private List<String> imgs;
        private List<String> infos;
        private ArrayList<View> views;

        Adapter(List<String> imgs, List<String> infos, int view_nums, int view) {
            views = new ArrayList<>();
            this.imgs = imgs;
            this.infos = infos;
            for (int i = 0; i < view_nums; i++) {
                views.add(View.inflate(RecoResultActivity.this, view, null));
                if (infos != null) {
                    JSONObject out_json = JSON.parseObject(infos.get(i));
                    final String names = out_json.getString("name");
                    final String page = out_json.getString("web_page");
                    String university = out_json.getString("university");
                    SharedPreferences s = RecoResultActivity.this.getSharedPreferences("recent", Context.MODE_PRIVATE);
                    Set<String> recent = new LinkedHashSet<>(s.getStringSet("recent faces", new LinkedHashSet<String>()));
                    Professor professor = new Professor(names, university, page, local_img_path, local_img_name);
                    String professor_json = JSON.toJSONString(professor);
                    Log.e("professor_json == >>", professor_json);
                    recent.add(professor_json);

                    SharedPreferences.Editor editor = s.edit();
                    editor.putStringSet("recent faces", recent);
                    boolean editor_result = editor.commit();
                    Log.e("out -- editor_result", String.valueOf(editor_result));
                    views.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (page.startsWith("http")) {
                                Intent i = new Intent(RecoResultActivity.this, WebViewActivity.class);
                                i.putExtra("url", page);
                                i.putExtra("name", names);
                                i.putExtra("from", 1);
                                startActivity(i);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (imgs != null) {
                Glide.with(RecoResultActivity.this)
                        .load(Constant.download_domain + imgs.get(position) + ".png?imageslim")
                        .placeholder(R.drawable.pic_bg)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .transform(new CornersTransform(this, 10))
                        .crossFade()
                        .into((ImageView) views.get(position).findViewById(R.id.viewpager_card_img));

                TextView name_tv = (TextView) views.get(position).findViewById(R.id.viewpager_card_name);
                TextView uni_tv = (TextView) views.get(position).findViewById(R.id.viewpager_card_university);

                JSONObject out_json = JSON.parseObject(infos.get(position));
                final String names = out_json.getString("name");
                final String university = out_json.getString("university");

                name_tv.setText(names);
                uni_tv.setText(university);
            }
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

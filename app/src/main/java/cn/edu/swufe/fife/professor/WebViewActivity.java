package cn.edu.swufe.fife.professor;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

public class WebViewActivity extends AppCompatActivity {
    X5WebView tbsContent;
    private ProgressBar mPageLoadingProgressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String name = getIntent().getStringExtra("name");
        int from = getIntent().getIntExtra("from", 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.web_toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mTestHandler.sendEmptyMessageDelayed(MSG_INIT_UI, 5);
//        if(from == 1){
//            PhotoViewActivity.activity.finish();
//            RecoResultActivity.activity.finish();
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && tbsContent.canGoBack()) {
            tbsContent.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initProgressBar() {
        mPageLoadingProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPageLoadingProgressBar.setMax(100);
        mPageLoadingProgressBar.setProgressDrawable(this.getResources()
                .getDrawable(R.drawable.color_progressbar));
    }

    public void init(){
        String url = getIntent().getStringExtra("url");
        tbsContent = (X5WebView) findViewById(R.id.web);
        initProgressBar();

        tbsContent.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView webView, int i) {
                if (i == 100) {
                    mPageLoadingProgressBar.setVisibility(View.GONE);
                } else {
                    if (View.GONE == mPageLoadingProgressBar.getVisibility()) {
                        mPageLoadingProgressBar.setVisibility(View.VISIBLE);
                    }
                    mPageLoadingProgressBar.setProgress(i);
                }
                super.onProgressChanged(webView, i);
            }
        });

        tbsContent.loadUrl(url);
    }

    public static final int MSG_INIT_UI = 1;
    private Handler mTestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_UI:
                    init();
                    break;
            }
            super.handleMessage(msg);
        }
    };

}

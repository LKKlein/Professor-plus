package cn.edu.swufe.fife.professor.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import cn.edu.swufe.fife.professor.R;
import cn.edu.swufe.fife.professor.customView.X5WebView;
import cn.edu.swufe.fife.professor.fragment.WebViewFragment;

public class WebViewActivity extends AppCompatActivity implements WebViewFragment.OnFragmentInteractionListener {
    private X5WebView tbsWebContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String name = getIntent().getStringExtra("name");
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

        Fragment web = WebViewFragment.newInstance(getIntent().getStringExtra("url"));
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.web_fragment, web);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && tbsWebContent.canGoBack()) {
            tbsWebContent.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFragmentInteraction(X5WebView x5WebView) {
        this.tbsWebContent = x5WebView;
    }
}

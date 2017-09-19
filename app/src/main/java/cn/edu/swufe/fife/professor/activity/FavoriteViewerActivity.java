package cn.edu.swufe.fife.professor.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import cn.edu.swufe.fife.professor.R;
import cn.edu.swufe.fife.professor.Utils.CommonUtils;
import cn.edu.swufe.fife.professor.bean.FavoriteItem;
import cn.edu.swufe.fife.professor.customView.X5WebView;
import cn.edu.swufe.fife.professor.fragment.WebViewFragment;

public class FavoriteViewerActivity extends AppCompatActivity implements WebViewFragment.OnFragmentInteractionListener {
    private X5WebView tbsWebContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_viewer);

        FavoriteItem favoriteItem = getIntent().getParcelableExtra("item");
        Toolbar toolbar = (Toolbar) findViewById(R.id.favorite_viewer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.favorite_viewer_collapsing_toolbar);
        collapsingToolbar.setTitle(favoriteItem.getName());

        ImageView img = (ImageView) findViewById(R.id.favorite_viewer_img);
        Glide.with(this)
                .load(CommonUtils.isFileExists(favoriteItem.getLocal_path()) ?
                        favoriteItem.getLocal_path() : favoriteItem.getUrl())
                .placeholder(R.drawable.pictures_loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(img);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Fragment web = WebViewFragment.newInstance(favoriteItem.getWeb_url());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.favorite_viewer_web_fragment, web);
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

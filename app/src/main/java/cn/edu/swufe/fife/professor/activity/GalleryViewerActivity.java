package cn.edu.swufe.fife.professor.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import cn.edu.swufe.fife.professor.R;
import cn.edu.swufe.fife.professor.bean.GalleryBean;

public class GalleryViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_viewer);

        GalleryBean bean = getIntent().getParcelableExtra("bean");
        Toolbar toolbar = (Toolbar) findViewById(R.id.gallery_viewer_toolbar);
        toolbar.setTitle(bean.getFace_name());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

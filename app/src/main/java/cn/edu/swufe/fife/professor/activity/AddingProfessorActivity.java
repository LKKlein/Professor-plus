package cn.edu.swufe.fife.professor.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import java.io.File;

import cn.edu.swufe.fife.professor.BaseApplication;
import cn.edu.swufe.fife.professor.R;
import cn.edu.swufe.fife.professor.Utils.AipFaceUtils;
import cn.edu.swufe.fife.professor.Utils.CommonUtils;
import cn.edu.swufe.fife.professor.Utils.Constant;
import cn.edu.swufe.fife.professor.Utils.QiniuUtils;
import cn.edu.swufe.fife.professor.bean.DaoSession;
import cn.edu.swufe.fife.professor.bean.Professors;
import cn.edu.swufe.fife.professor.bean.ProfessorsDao;
import cn.edu.swufe.fife.professor.customView.GlideRoundTransform;

import static cn.edu.swufe.fife.professor.Utils.CommonUtils.createFileName;
import static cn.edu.swufe.fife.professor.Utils.Constant.img_path;

public class AddingProfessorActivity extends AppCompatActivity {
    private ImageView adding_img;
    private ImageView adding_img_edit;
    private TextView adding_img_tv;
    private EditText adding_nameEdit;
    private EditText adding_uni;
    private EditText web_page;
    private Button adding_complete;
    private PopupWindow popupWindow = null;

    private boolean hasFaceInPicture = false;
    private boolean isExist = false;

    private String image_path = "";
    private String image_name = "";
    private String professor_name = "";
    private String professor_uni = "";
    private String professor_page = "";

    public final int REQUEST_IMAGE_CAPTURE = 1000;
    public final int CODE_GALLERY_REQUEST = 1001;

    private Professors professors = null;
    private ProfessorsDao professorsDao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_professor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.submit_toolbar);
        toolbar.setTitle("添加教授");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DaoSession daoSession = ((BaseApplication) getApplication()).getDaoSession();
        professorsDao = daoSession.getProfessorsDao();
        professors = new Professors();

        initView();
        initListener();
    }

    private void initView() {
        adding_img = (ImageView) findViewById(R.id.adding_imageView);
        adding_img_edit = (ImageView) findViewById(R.id.adding_imageView_edit);
        adding_img_tv = (TextView) findViewById(R.id.adding_imageView_tv);
        adding_nameEdit = (EditText) findViewById(R.id.adding_nameEdit);
        adding_uni = (EditText) findViewById(R.id.adding_university);
        web_page = (EditText) findViewById(R.id.adding_webPage);
        adding_complete = (Button) findViewById(R.id.adding_complete);

        Glide.with(this)
                .load(R.mipmap.ic_account_box_black_48dp)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade()
                .into(adding_img);

        Glide.with(this)
                .load(R.drawable.edit_circle_8bc34a)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(adding_img_edit);
    }

    private void initListener() {
        adding_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        View.OnClickListener imageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopu();
            }
        };
        adding_img.setOnClickListener(imageListener);
        adding_img_edit.setOnClickListener(imageListener);
    }

    /**
     * 弹出底部对话框，达到背景背景透明效果
     * <p>
     * 实现原理：弹出一个全屏popupWindow,将Gravity属性设置bottom,根背景设置为一个半透明的颜色，
     * 弹出时popupWindow的半透明效果背景覆盖了在Activity之上 给人感觉上是popupWindow弹出后，背景变成半透明
     */
    public void openPopu() {
        View rootView = findViewById(R.id.adding_root);
        View popView = LayoutInflater.from(this)
                .inflate(R.layout.popuwindow_photo, null);
        popupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        setBackgroundAlpha(0.5f);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);  // 点击空白处时，隐藏掉pop窗口
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1.0f);
            }
        });

        TextView popuCancel = (TextView) popView.findViewById(R.id.popbtn_cancle);
        popuCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popView.findViewById(R.id.popbtn_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                File dir = new File(img_path);
                image_name = createFileName();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri imageUri = FileProvider.getUriForFile(AddingProfessorActivity.this, "cn.edu.swufe.fife.professor", new File(img_path, image_name));
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                } else {
                    Uri imageUri = Uri.fromFile(new File(img_path, image_name));
                    i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                }
                popupWindow.dismiss();
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            }
        });

        popView.findViewById(R.id.popbtn_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                popupWindow.dismiss();
                startActivityForResult(pickIntent, CODE_GALLERY_REQUEST);
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha 屏幕透明度0.0-1.0 1表示完全不透明
     */
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        this.getWindow().setAttributes(lp);
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                image_path = img_path;
            } else if (requestCode == CODE_GALLERY_REQUEST) {
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                image_path = path.substring(0, path.lastIndexOf("/"));
                image_name = path.substring(path.lastIndexOf("/") + 1);
            }

            Glide.with(AddingProfessorActivity.this)
                    .load(image_path + "/" + image_name)
                    .placeholder(R.mipmap.ic_account_box_black_48dp)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .transform(new GlideRoundTransform(AddingProfessorActivity.this))
                    .into(adding_img);
            adding_img_edit.setVisibility(View.GONE);
            adding_img_tv.setVisibility(View.GONE);

            final AipFaceUtils aipFaceUtils = new AipFaceUtils();
            aipFaceUtils.syncManager(new AipFaceUtils.OnSyncTask() {
                @Override
                public String OnRequestGoing() {
                    return aipFaceUtils.identifyProfessor(image_path + "/" + image_name, 1);
                }

                @Override
                public void OnRequestComplete(String result) {
                    final JSONObject jsonObject = JSON.parseObject(result);
                    if (jsonObject.containsKey("result_num") && jsonObject.getInteger("result_num") != 0) {
                        JSONObject jsonResult = jsonObject.getJSONArray("result").getJSONObject(0);
                        float scores = jsonResult.getJSONArray("scores").getFloat(0);
                        if (scores >= 80) {

                            /* 识别到某位教授, 提示该教授已存在 */
                            isExist = true;
                            final String professor_name = jsonResult.getJSONObject("user_info").getString("name");
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddingProfessorActivity.this);
                            builder.setTitle("提示");
                            builder.setIcon(tintDrawable(getResources().getDrawable(R.drawable.ic_warning_black_18dp),
                                    ColorStateList.valueOf(Color.parseColor("#FFEB3B"))));
                            builder.setMessage("对不起，" + professor_name + "已经存在，无需再添加了");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AppCompatDialog dialog = builder.create();
                            dialog.show();
                            adding_img_edit.setVisibility(View.VISIBLE);
                            adding_img_tv.setVisibility(View.VISIBLE);
                        } else {
                            isExist = false;
                            QiniuUtils qiniuUtils = new QiniuUtils();
                            qiniuUtils.setBucket(2);
                            qiniuUtils.upload(image_path + "/" + image_name, CommonUtils.getMd5ByFile(image_path + "/" + image_name),
                                    new UpCompletionHandler() {
                                        @Override
                                        public void complete(String key, ResponseInfo info, org.json.JSONObject response) {
                                            professors.setFace_token(key);
                                            professors.setWeb_url(Constant.professor_domain + "key");
                                        }
                                    });
                        }

                    } else {
                        adding_img_tv.setText("请上传教授本人照片");
                        adding_img_tv.setTextColor(getResources().getColor(R.color.md_red_500));
                        Toast.makeText(AddingProfessorActivity.this, "请务必以个人真实照片为头像",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

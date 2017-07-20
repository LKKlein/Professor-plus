package cn.edu.swufe.fife.professor;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnFragmentInteractionListener, EasyPermissions.PermissionCallbacks{

    public final int REQUEST_IMAGE_CAPTURE = 1000;
    public final int CODE_GALLERY_REQUEST = 1001;
    String img_path = Environment.getExternalStorageDirectory().getPath()+"/Professor+/photos";
    String img_name = "";
    FloatingActionMenu fab_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

//        Bmob.initialize(this, "78ad704fb5b3239b0e981002bb2e4b55", "Bmob");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.photo_fab);
        FloatingActionButton pic_fab = (FloatingActionButton) findViewById(R.id.pick_fab);
        fab_menu = (FloatingActionMenu) findViewById(R.id.main_menu);
        fab_menu.setClosedOnTouchOutside(true);
        fab_menu.setAnimated(true);

        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE};

        if (EasyPermissions.hasPermissions(this, perms)) {//检查是否获取该权限
            Log.i("权限申请", "已获取权限");
        } else {
            EasyPermissions.requestPermissions(this, "Professor+ needs you to grant us...", 0, perms);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                File dir = new File(img_path);
                img_name = createFileName();
                if (!dir.exists()){
                    dir.mkdirs();
                }
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                   Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "cn.edu.swufe.fife.professor", new File(img_path, img_name));
                   i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                   i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
               } else {
                   Uri imageUri = Uri.fromFile(new File(img_path, img_name));
                   i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                   i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
               }
               if (fab_menu.isShown()){
                   fab_menu.close(true);
               }
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            }
        });

        pic_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                File mGalleryFile = new File(Constants.me().getExternalDir(), IMAGE_GALLERY_NAME);//相册的File对象
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                if (fab_menu.isShown()){
                    fab_menu.close(true);
                }
                startActivityForResult(pickIntent, CODE_GALLERY_REQUEST);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static String createFileName(){
        String fileName;
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        fileName = sdf.format(date) + ".png";
        return fileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Bundle extra = new Bundle();
            if (requestCode == REQUEST_IMAGE_CAPTURE){
                extra.putString("path", img_path);
                extra.putString("name", img_name);
            } else if (requestCode == CODE_GALLERY_REQUEST){
                Uri uri = data.getData();
                String [] proj={MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery( uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                extra.putString("path", path.substring(0, path.lastIndexOf("/")));
                extra.putString("name", path.substring(path.lastIndexOf("/")+1));
            }
            Intent i = new Intent(MainActivity.this, PhotoViewActivity.class);
            i.putExtras(extra);
            startActivity(i);
        }else{
            Toast.makeText(this, "Sorry~ Failed to take photo!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFragmentInteraction(boolean up) {
        if (up){
            if (fab_menu.isShown()){
                fab_menu.hideMenu(true);
            }
        }else{
            if (!fab_menu.isShown()){
                fab_menu.showMenu(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i("tag", "获取成功的权限" + perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.i("tag", "获取失败的权限" + perms);
    }
}

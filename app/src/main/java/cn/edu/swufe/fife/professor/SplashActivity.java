package cn.edu.swufe.fife.professor;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        new SplashAsyncTask().execute();
    }

    private class SplashAsyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            SplashActivity.this.finish();
        }
    }
}
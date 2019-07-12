package in.edureal.securememos;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    SweetAlertDialog loading;
    String memoTitle;
    String memo;
    String secretKey;
    String memoDate;
    String memoTime;

    EditText memoTitleET;
    EditText memoET;
    EditText secretKeyET;

    MySQLiteHelper helper;
    SQLiteDatabase database;
    int numOfMemos;

    private void showError(String error){
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText(error)
                .show();
    }

    private void setDateTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        memoDate=dateFormat.format(date);
        memoTime=dateFormat1.format(date);
    }

    public void viewMyMemos(View view){
        loading.show();
        Cursor cursor = database.rawQuery("SELECT _id FROM list", new String[]{});
        if(cursor!=null && cursor.getCount()>=0){
            numOfMemos=cursor.getCount();
        }else{
            numOfMemos=0;
        }

        try{
            cursor.close();
        }catch(NullPointerException e) {
            Log.i("Error",e.toString());
        }

        if(numOfMemos==0){
            loading.hide();
            showError("You don't have any memos.");
        }else{
            loading.hide();
            Intent intent=new Intent(MainActivity.this, MemoList.class);
            startActivity(intent);
        }

    }

    public void githubSources(View view){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_about);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((TextView) dialog.findViewById(R.id.tv_version)).setText("Version " + BuildConfig.VERSION_NAME);

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ((Button) dialog.findViewById(R.id.bt_openGitHub)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vishu103/secure-memos"));
                startActivity(browserIntent);
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    public void submitMemo(View view){

        loading.show();
        memoTitle=memoTitleET.getText().toString().trim();
        if(memoTitle.length()==0){
            loading.hide();
            showError("Please enter a title.");
        }else {
            if(memoTitle.matches("[A-Za-z0-9 !@#%&*,.]+")){

                memo=memoET.getText().toString().trim();
                if(memo.length()==0){
                    loading.hide();
                    showError("There is no use of saving a blank memo.");
                }else{

                    secretKey=secretKeyET.getText().toString().trim();
                    if(secretKey.length()==0){
                        loading.hide();
                        showError("Please enter a secret key to encrypt this memo.");
                    }else{

                        if(secretKey.matches("[A-Za-z0-9 !@#%&*,.]+")){

                            setDateTime();
                            // encrypt memo
                            AESAlgorithm algorithm=new AESAlgorithm();
                            String encryptedMemo="";
                            boolean goOn=false;

                            try{
                                encryptedMemo=algorithm.encrypt(memo,secretKey);
                                goOn=true;
                            }catch(Exception e){
                                loading.hide();
                                showError("There was a problem encrypting your memo. Please try something else or contact us.");
                            }

                            if(goOn){
                                // save to database
                                helper.insertData(memoTitle,encryptedMemo,memoDate,memoTime,database);
                                secretKeyET.setText("");
                                secretKey="";
                                memoET.setText("");
                                memo="";
                                memoTitleET.setText("");
                                memoTitle="";
                                loading.hide();
                                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Yipee...")
                                        .setContentText("Your memo is encrypted and stored successfully. Click on 'View my memos' to check it out.")
                                        .show();

                            }

                        }else{
                            loading.hide();
                            showError("Allowed characters for secret key: A-Z a-z 0-9 !@#%&*,.");
                        }

                    }

                }

            }else{
                loading.hide();
                showError("Allowed characters for title: A-Z a-z 0-9 !@#%&*,.");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        loading.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loading.setTitleText("Loading");
        loading.setCancelable(false);

        memoTitle="";
        memo="";
        secretKey="";
        memoDate="";
        memoTime="";
        numOfMemos=0;

        memoTitleET = (EditText) findViewById(R.id.memoTitle);
        memoET = (EditText) findViewById(R.id.memoET);
        secretKeyET = (EditText) findViewById(R.id.secretKeyET);

        helper=new MySQLiteHelper(this);
        database=helper.getWritableDatabase();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading.dismiss();
    }
}

package in.edureal.securememos;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MemoEditor extends AppCompatActivity{

    private EditText memoET;
    private EditText secretKeyET;
    private TextView memoTitle;

    private String memo;
    private String secretkey;
    private int memoUID;

    MySQLiteHelper helper;
    SQLiteDatabase database;

    public void backTomain(View view){
        Intent intent=new Intent(MemoEditor.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void backtoList(View view){
        Intent intent=new Intent(MemoEditor.this, MemoList.class);
        startActivity(intent);
        finish();
    }

    private void showError(String error){
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText(error)
                .show();
    }

    public void updatememo(View view){

        memo=memoET.getText().toString().trim();
        if(memo.length()==0){
            showError("There is no use of saving a blank memo.");
        }else{

            secretkey=secretKeyET.getText().toString().trim();
            if(secretkey.length()==0){
                showError("Please enter a secret key to encrypt this memo.");
            }else{

                if(secretkey.matches("[A-Za-z0-9 !@#%&*,.]+")){

                    AESAlgorithm algorithm=new AESAlgorithm();
                    String encryption="";
                    boolean goFurther=false;

                    try{
                        encryption=algorithm.encrypt(memo,secretkey);
                        goFurther=true;
                    }catch(Exception e){
                        showError("There was a problem encrypting your memo. Please try something else or contact us.");
                    }

                    if(goFurther){

                        ContentValues values=new ContentValues();
                        values.put("MEMO", encryption);
                        database.update("list",values,"_id=?",new String[]{Integer.toString(memoUID)});
                        Toast.makeText(this, "Memo Updated", Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(MemoEditor.this, MemoList.class);
                        startActivity(intent);
                        finish();

                    }

                }else{
                    showError("Allowed characters for secret key: A-Z a-z 0-9 !@#%&*,.");
                }

            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_editor);

        memoUID=0;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if(extras.containsKey("uid")) {
                int uid=extras.getInt("uid", 0);
                if(uid!=0){

                    memoUID=uid;
                    memoET= (EditText) findViewById(R.id.memoET);
                    secretKeyET= (EditText) findViewById(R.id.secretKeyET);
                    memoTitle= (TextView) findViewById(R.id.memoTitle);
                    memo="";
                    secretkey="";

                    helper=new MySQLiteHelper(this);
                    database=helper.getWritableDatabase();

                    AlertDialog.Builder builder=new AlertDialog.Builder(MemoEditor.this);
                    LayoutInflater inflater=getLayoutInflater();

                    final EditText secretKeyEnterInput;
                    final TextView errorMsg;

                    View view=inflater.inflate(R.layout.secret_key_input,null);
                    secretKeyEnterInput=(EditText) view.findViewById(R.id.secretET);
                    errorMsg=(TextView) view.findViewById(R.id.errorMsg);

                    builder.setView(view);
                    builder.setTitle("Secret Key");
                    builder.setNegativeButton("Cancel",null);
                    builder.setPositiveButton("Submit",null);

                    final AlertDialog finalDialog=builder.create();

                    finalDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {

                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            Button button1 = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String enteredSecret=secretKeyEnterInput.getText().toString().trim();
                                    if(enteredSecret.length()==0){
                                        errorMsg.setText("Please enter the secret key");
                                        errorMsg.setVisibility(View.VISIBLE);

                                        new CountDownTimer(3000, 3000) {
                                            public void onTick(long millisUntilFinished) {
                                            }

                                            public void onFinish() {
                                                errorMsg.setVisibility(View.GONE);
                                            }
                                        }.start();

                                    }else{
                                        if(enteredSecret.matches("[A-Za-z0-9 !@#%&*,.]+")){

                                            errorMsg.setText("Please Wait...");
                                            errorMsg.setVisibility(View.VISIBLE);

                                            Cursor cursor = database.rawQuery("SELECT title, memo FROM list WHERE _id="+memoUID, new String[]{});
                                            boolean goOn=false;
                                            String decryptedText="";

                                            if(cursor!=null && cursor.getCount()==1){
                                                cursor.moveToFirst();
                                                try{
                                                    AESAlgorithm algorithm=new AESAlgorithm();
                                                    decryptedText=algorithm.decrypt(cursor.getString(1),enteredSecret);
                                                    goOn=true;
                                                }catch(Exception e){
                                                    errorMsg.setText("Wrong secret key");

                                                    new CountDownTimer(3000, 3000) {
                                                        public void onTick(long millisUntilFinished) {
                                                        }

                                                        public void onFinish() {
                                                            errorMsg.setVisibility(View.GONE);
                                                        }
                                                    }.start();
                                                }

                                                if(goOn){
                                                    errorMsg.setVisibility(View.GONE);
                                                    memoTitle.setText(cursor.getString(0)+":");
                                                    memoET.setText(decryptedText);
                                                    memo=decryptedText;
                                                    finalDialog.dismiss();
                                                }

                                                try{
                                                    cursor.close();
                                                }catch(NullPointerException e) {
                                                    Log.i("Error",e.toString());
                                                }

                                            }else{
                                                Intent intent11=new Intent(MemoEditor.this, MemoList.class);
                                                startActivity(intent11);
                                                finalDialog.dismiss();
                                                finish();
                                            }

                                        }else{
                                            errorMsg.setText("Please enter a valid secret key");
                                            errorMsg.setVisibility(View.VISIBLE);

                                            new CountDownTimer(3000, 3000) {
                                                public void onTick(long millisUntilFinished) {
                                                }

                                                public void onFinish() {
                                                    errorMsg.setVisibility(View.GONE);
                                                }
                                            }.start();
                                        }
                                    }
                                }
                            });

                            button1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent4=new Intent(MemoEditor.this, MemoList.class);
                                    startActivity(intent4);
                                    finalDialog.dismiss();
                                    finish();
                                }
                            });

                        }
                    });

                    finalDialog.setCancelable(false);
                    finalDialog.show();


                }else{
                    Intent intent1=new Intent(MemoEditor.this, MemoList.class);
                    startActivity(intent1);
                    finish();
                }
            }else{
                Intent intent2=new Intent(MemoEditor.this, MemoList.class);
                startActivity(intent2);
                finish();
            }
        }else{
            Intent intent3=new Intent(MemoEditor.this, MemoList.class);
            startActivity(intent3);
            finish();
        }

    }
}

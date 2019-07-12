package in.edureal.securememos;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MemoList extends AppCompatActivity {

    private RecyclerView recyclerView;

    MySQLiteHelper helper;
    SQLiteDatabase database;
    int numOfMemos;

    SweetAlertDialog loading;
    SwipeRefreshLayout swiperefresh;

    public void backToHome(View view){
        Intent intent=new Intent(MemoList.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView(){

        loading.show();
        int countTemp=0;
        Cursor cursor = database.rawQuery("SELECT _id,title,memodate,memotime FROM list ORDER BY _id DESC", new String[]{});
        if(cursor!=null){
            numOfMemos=cursor.getCount();
            countTemp=numOfMemos;
            if(numOfMemos>0){
                cursor.moveToFirst();
            }else{
                loading.hide();
                Intent intent=new Intent(MemoList.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }else{
            loading.hide();
            Intent intent=new Intent(MemoList.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ListItem> listItems = new ArrayList<>();

        while(countTemp>=1){
            ListItem listItem=new ListItem(cursor.getString(1),cursor.getString(2)+" "+cursor.getString(3),cursor.getInt(0));
            listItems.add(listItem);
            countTemp--;
            cursor.moveToNext();
        }

        cursor.close();

        RecyclerView.Adapter adapter=new MyAdapter(listItems, this);
        recyclerView.setAdapter(adapter);
        loading.hide();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        loading = new SweetAlertDialog(MemoList.this, SweetAlertDialog.PROGRESS_TYPE);
        loading.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loading.setTitleText("Loading");
        loading.setCancelable(false);

        swiperefresh= (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        helper=new MySQLiteHelper(this);
        database=helper.getWritableDatabase();
        numOfMemos=0;
        recyclerView=(RecyclerView) findViewById(R.id.memosRecyclerView);
        recyclerView.setHasFixedSize(true);

        setupRecyclerView();

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiperefresh.setRefreshing(false);
                setupRecyclerView();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading.dismiss();
    }
}

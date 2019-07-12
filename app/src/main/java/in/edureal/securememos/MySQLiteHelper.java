package in.edureal.securememos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String dbname="memodb";
    private static final int version=1;

    public MySQLiteHelper(Context context) {
        super(context, dbname, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="CREATE TABLE list (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, memo TEXT, memodate TEXT, memotime TEXT)";
        db.execSQL(sql);
    }

    public void insertData(String title, String memo, String memodate, String memotime, SQLiteDatabase db){
        ContentValues values=new ContentValues();
        values.put("TITLE", title);
        values.put("MEMO", memo);
        values.put("MEMODATE", memodate);
        values.put("MEMOTIME", memotime);
        db.insert("list",null,values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

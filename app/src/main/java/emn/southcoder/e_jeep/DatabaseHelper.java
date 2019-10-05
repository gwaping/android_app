package emn.southcoder.e_jeep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.support.annotation.Nullable;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "MCC.db";
    public static final String EJEEP_TABLE = "ejeep_table";
    public static final String COL_ID = "id";
    public static final String COL_CARDSERIAL = "cardserial";
    public static final String COL_MCCNUMBER = "mccnumber";
    public static final String COL_ETIMESTAMP = "etimestamp";
    public static final String STRUCT_EJEEP_TABLE = "(id INTEGER PRIMARY KEY AUTOINCREMENT,cardserial TEXT,mccnumber TEXT,etimestamp TEXT)";
    public static final String USER_TABLE = "user_table";
    public static final String COL_USER_ACCESS = "access";
    public static final String STRUCT_USER_TABLE = "(id INTEGER PRIMARY KEY AUTOINCREMENT,mccnumber TEXT, access TEXT, etimestamp TEXT)";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + EJEEP_TABLE + " " + STRUCT_EJEEP_TABLE);
        db.execSQL("create table " + USER_TABLE + " " + STRUCT_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EJEEP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        onCreate(db);
    }

    public boolean savePassengerLog(String cardserial,String mccnumber) {
        SimpleDateFormat databaseDateTimeFormate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime = databaseDateTimeFormate.format(new Date());     //2009-06-30 08:29:36
        SQLiteDatabase db = this.getWritableDatabase();
        boolean retVal = false;

        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_CARDSERIAL, cardserial);
            contentValues.put(COL_MCCNUMBER, mccnumber);
            contentValues.put(COL_ETIMESTAMP, currentDateTime);

            long result = db.insert(EJEEP_TABLE,null, contentValues);

            if (result != -1)
                retVal = true;
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to add log to database");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return retVal;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + EJEEP_TABLE,null);

        return  res;
    }

    public boolean isValidLogin(String mccno, String access) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + USER_TABLE + " where mccnumber='" + mccno + "' and access LIKE '%" + access + "'%",null);
        res.moveToFirst();
        int counter = 0;

        while(res.isAfterLast() == false) {
            counter++;
            res.moveToNext();
        }
        if (counter>0) {
            return true;
        }else {
            return false;
        }
    }

    public Cursor getAllUserData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + USER_TABLE,null);

        return  res;
    }
}

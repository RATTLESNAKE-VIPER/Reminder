package com.example.reminder.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{

	//The Android's default system path of your application database.
	private static final String DB_PATH = "/data/data/com.example.reminder/databases/";
	private static final String DB_NAME = "reminder.db";
	private static final int DATABASE_VERSION = 1;


	private SQLiteDatabase myDataBase; 
	private final Context myContext;


	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, DATABASE_VERSION);
		this.myContext = context;
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * */
	public void createDataBase() throws IOException{

		boolean dbExist = checkDataBase(SQLiteDatabase.OPEN_READWRITE);

		if(dbExist){
			//do nothing - database already exist
		}else{

			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase(int flags){

		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, flags);

		}catch(SQLiteException e){

			//database does't exist yet.

		}

		if(checkDB != null){

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase(int flags) throws SQLException{

		//Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, flags);
	}

	public void openOrCreateDatabase() throws SQLException{
		//Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openOrCreateDatabase(myPath, null);
	}

	@Override
	public synchronized void close() {

		if(myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.createTaskTable(db);
		this.createInderminateTaskTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.dropTaskTable(db);
		this.dropInderminateTaskTable(db);
		onCreate(db);
	}



	// Add your public helper methods to access and get content from the database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
	// to you to create adapters for your views.

	public void createTaskTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE " + TaskTableSchema.TABLE_NAME + " ("
					+ TaskTableSchema._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ TaskTableSchema.KEY_TASK + " TEXT not null,"
					+ TaskTableSchema.KEY_DESCRIPTION + " TEXT,"
					+ TaskTableSchema.KEY_TIMER_ON + " INTEGER,"
					+ TaskTableSchema.KEY_TIME + " TEXT,"
					+ TaskTableSchema.KEY_LOCATION_ON + " INTEGER,"
					+ TaskTableSchema.KEY_LATITUDE + " INTEGER,"
					+ TaskTableSchema.KEY_LONGITUDE + " INTEGER,"
					+ TaskTableSchema.KEY_ON_ARRIVE + " INTEGER,"
					+ TaskTableSchema.KEY_ON_LEAVE + " INTEGER,"
					+ TaskTableSchema.KEY_RADIUS + " FLOAT,"
					+ TaskTableSchema.KEY_PRIORITY + " INTEGER,"
					+ TaskTableSchema.KEY_TASK_COMPLETED + " INTEGER,"
					+ TaskTableSchema.KEY_SNOOZE_ON + " INTEGER,"
					+ TaskTableSchema.KEY_REMINDER_ON + " INTEGER,"
					+ TaskTableSchema.KEY_NOTES + " TEXT"
					+ ");");            
		} catch (Exception e) {
			Log.v("createTaskTable", e.getMessage());
		}
	}
	
	public void createInderminateTaskTable(SQLiteDatabase db) {
		try {			
			db.execSQL("CREATE TABLE " + IndeterminateTaskSchema.TABLE_NAME + " ("
					+ IndeterminateTaskSchema._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ IndeterminateTaskSchema.KEY_TASK_ID + " INTEGER,"
					+ "UNIQUE(" + IndeterminateTaskSchema.KEY_TASK_ID + ") FOREIGN KEY(" + IndeterminateTaskSchema.KEY_TASK_ID + ") REFERENCES " + TaskTableSchema.TABLE_NAME + "(" + TaskTableSchema._ID + " ) ON DELETE CASCADE"
					+ ");");            
		} catch (Exception e) {
			Log.v("createInderminateTaskTable", e.getMessage());
		}
	}

	public void dropTaskTable(SQLiteDatabase db) {
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TaskTableSchema.TABLE_NAME);

		} catch (Exception e) {
			Log.v("dropTaskTable", e.getMessage());
		}
	}
	
	public void dropInderminateTaskTable(SQLiteDatabase db) {
		try {
			db.execSQL("DROP TABLE IF EXISTS " + IndeterminateTaskSchema.TABLE_NAME);

		} catch (Exception e) {
			Log.v("dropInderminateTaskTable", e.getMessage());
		}
	}
}
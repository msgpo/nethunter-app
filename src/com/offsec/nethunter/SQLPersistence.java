/**
 * Created by AnglerVonMur on 26.07.15.
 */
package com.offsec.nethunter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class SQLPersistence extends SQLiteOpenHelper {
    NhUtil nh;
    final static int DATABASE_VERSION = 2;
    final static String DATABASE_NAME = "KaliLaunchers";

    final static String CREATE_LAUNCHER_TABLE = "CREATE TABLE " +
            CustomCommand.TABLE + " (" +
            CustomCommand.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CustomCommand.BTN_LABEL + " TEXT, " +
            CustomCommand.CMD + " TEXT, " +
            CustomCommand.EXEC_MODE + " TEXT, " +
            CustomCommand.SEND_TO_SHELL + " TEXT, " +
            CustomCommand.RUN_AT_BOOT + " INTEGER )";
    Context context;
    public SQLPersistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        nh = new NhUtil();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LAUNCHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS");
        this.onCreate(db);
    }

    public CustomCommand addCommand(final String command_tag, final String command, final String mode, final String send_to_shell, final Integer run_at_boot) {
        long id = 0;
        if (command_tag.length() > 0 &&
                command.length() > 0) {

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(CustomCommand.BTN_LABEL, command_tag);
            values.put(CustomCommand.CMD, command);
            values.put(CustomCommand.EXEC_MODE, mode);
            values.put(CustomCommand.SEND_TO_SHELL, send_to_shell);
            values.put(CustomCommand.RUN_AT_BOOT, run_at_boot);
            id = db.insert(CustomCommand.TABLE, null, values);
            db.close();
        }
        return new CustomCommand(id,command_tag, command, mode, send_to_shell, run_at_boot);
    }
    public void updateCommand(final CustomCommand updatedCommand) {
        if (updatedCommand != null) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(CustomCommand.BTN_LABEL, updatedCommand.getCommand_label());
            values.put(CustomCommand.CMD, updatedCommand.getCommand());
            values.put(CustomCommand.EXEC_MODE, updatedCommand.getExec_Mode());
            values.put(CustomCommand.SEND_TO_SHELL, updatedCommand.getSend_To_Shell());
            values.put(CustomCommand.RUN_AT_BOOT, updatedCommand.getRun_At_Boot());
            db.update(CustomCommand.TABLE, values,
                    CustomCommand.ID + " = ?",
                    new String[]{String.valueOf(updatedCommand.getId())});

            db.close();
        }
    }

    public void deleteCommand(final long id) {
        if (id != 0) {
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(CustomCommand.TABLE,
                    CustomCommand.ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.close();

        }
    }
    public List<CustomCommand> getAllCommands() {
        String query = "SELECT  * FROM " + CustomCommand.TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<CustomCommand> _List = createCommandList(cursor);
        db.close();
        return _List;
    }
    public List<CustomCommand> getAllCommandsFiltered(String filter) {
        String wildcard = "%" + filter + "%";
        String query = "SELECT * FROM " + CustomCommand.TABLE
                + " WHERE BTN_LABEL like ?" ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{wildcard});
        List<CustomCommand> _List = createCommandList(cursor);
        db.close();
        return _List;
    }
    public List<CustomCommand> getAllCommandsAtBoot() {
        String query = "SELECT * FROM " + CustomCommand.TABLE
                + " WHERE RUN_AT_BOOT = 1" ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<CustomCommand> _List = createCommandList(cursor);
        db.close();
        return _List;
    }
    private List<CustomCommand> createCommandList(Cursor cursor){

        List<CustomCommand> commandList = new LinkedList<>();
        if (cursor.moveToFirst()) {
            do {
                CustomCommand _command = new CustomCommand();

                _command.setId(cursor.getLong(0));                  // id
                _command.setCommand_label(cursor.getString(1));    // tag
                _command.setCommand(cursor.getString(2));          // command
                _command.setExec_Mode(cursor.getString(3));        // mode
                _command.setSend_To_Shell(cursor.getString(4));    // run in shell
                _command.setRun_At_Boot(cursor.getInt(5));         // run at boot
                commandList.add(_command);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return commandList;

    }
    public void importDB() {
        try {
            String sd = nh.SD_PATH;
            String data = nh.APP_PATH;
            String currentDBPath = "../databases/"  + DATABASE_NAME;
            String backupDBPath = "/nh_bak_" + DATABASE_NAME + "_" + DATABASE_VERSION; // From SD directory.
            File backupDB = new File(data, currentDBPath);
            File currentDB = new File(sd, backupDBPath);

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Log.d("importDB", "Successful");
            Toast.makeText(context.getApplicationContext(),
                    "Import DB Successful",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d("importDB", e.toString());
        }
    }

    public void exportDB() {
        try {
            String sd = nh.SD_PATH;
            String data = nh.APP_PATH;
            Log.d("ExportDB sd =", sd.toString());
            Log.d("ExportDB sd =", data.toString());
            String currentDBPath = "../databases/"  + DATABASE_NAME;
            String backupDBPath = "/nh_bak_" + DATABASE_NAME + "_" + DATABASE_VERSION;
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Log.d("ExportDB", "Successful");
            Toast.makeText(context.getApplicationContext(),
                    "Export DB Successful",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d("ExportDB", "ExportDB Failed!");
        }
    }
}
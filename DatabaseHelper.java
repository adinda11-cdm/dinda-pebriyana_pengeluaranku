package com.umko.pengeluaranku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pengeluaran_simple.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "pengeluaran";

    // Model data sederhana (digabung di sini supaya tidak perlu file tambahan)
    public static class Item {
        public long id;
        public String keterangan;
        public long jumlah;
        public String tanggal;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "keterangan TEXT NOT NULL, "
                + "jumlah INTEGER NOT NULL, "
                + "tanggal TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void tambah(String keterangan, long jumlah, String tanggal) {
        ContentValues cv = new ContentValues();
        cv.put("keterangan", keterangan);
        cv.put("jumlah", jumlah);
        cv.put("tanggal", tanggal);
        getWritableDatabase().insert(TABLE, null, cv);
    }

    public void hapus(long id) {
        getWritableDatabase().delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Item> ambilSemua(boolean urutTerbesar) {
        List<Item> list = new ArrayList<>();
        String order = urutTerbesar ? "jumlah DESC" : "tanggal DESC, id DESC";
        Cursor c = getReadableDatabase().query(TABLE, null, null, null, null, null, order);
        while (c.moveToNext()) {
            Item it = new Item();
            it.id = c.getLong(c.getColumnIndexOrThrow("id"));
            it.keterangan = c.getString(c.getColumnIndexOrThrow("keterangan"));
            it.jumlah = c.getLong(c.getColumnIndexOrThrow("jumlah"));
            it.tanggal = c.getString(c.getColumnIndexOrThrow("tanggal"));
            list.add(it);
        }
        c.close();
        return list;
    }

    // Total pengeluaran satu bulan, contoh prefixBulan: "2026-11"
    public long totalBulan(String prefixBulan) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(jumlah) FROM " + TABLE + " WHERE tanggal LIKE ?",
                new String[]{prefixBulan + "%"});
        long total = 0;
        if (c.moveToFirst()) total = c.getLong(0);
        c.close();
        return total;
    }
}
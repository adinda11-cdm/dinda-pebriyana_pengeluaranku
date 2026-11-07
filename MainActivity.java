package com.umko.pengeluaranku;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private PrefsManager prefs;
    private DatabaseHelper db;
    private ArrayAdapter<String> adapter;
    private final List<String> teksList = new ArrayList<>();
    private List<DatabaseHelper.Item> items = new ArrayList<>();

    private TextView tvSapaan, tvRingkasan;
    private EditText etNama, etBudget, etKeterangan, etJumlah;
    private int warnaAwal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new PrefsManager(this);
        db = new DatabaseHelper(this);

        tvSapaan = findViewById(R.id.tvSapaan);
        tvRingkasan = findViewById(R.id.tvRingkasan);
        etNama = findViewById(R.id.etNama);
        etBudget = findViewById(R.id.etBudget);
        etKeterangan = findViewById(R.id.etKeterangan);
        etJumlah = findViewById(R.id.etJumlah);
        Button btnSimpanNama = findViewById(R.id.btnSimpanNama);
        Button btnSimpanBudget = findViewById(R.id.btnSimpanBudget);
        Button btnTambah = findViewById(R.id.btnTambah);
        SwitchCompat swDark = findViewById(R.id.swDark);
        SwitchCompat swSort = findViewById(R.id.swSort);
        ListView lv = findViewById(R.id.lvPengeluaran);

        warnaAwal = tvRingkasan.getTextColors().getDefaultColor();

        // Tampilkan nilai tersimpan dulu, baru pasang listener
        etNama.setText(prefs.getUserName());
        etBudget.setText(String.valueOf(prefs.getBudget()));
        swDark.setChecked(prefs.isDarkMode());
        swSort.setChecked(prefs.isSortTerbesar());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teksList);
        lv.setAdapter(adapter);

        // Tekan lama pada item untuk menghapus
        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            DatabaseHelper.Item it = items.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Hapus pengeluaran?")
                    .setMessage(it.keterangan + " - " + rupiah(it.jumlah))
                    .setPositiveButton("Hapus", (d, w) -> {
                        db.hapus(it.id);
                        muatData();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
            return true;
        });

        btnSimpanNama.setOnClickListener(v -> {
            prefs.setUserName(etNama.getText().toString().trim());
            tampilkanSapaan();
            Toast.makeText(this, "Nama disimpan", Toast.LENGTH_SHORT).show();
        });

        btnSimpanBudget.setOnClickListener(v -> {
            try {
                long budget = Long.parseLong(etBudget.getText().toString().trim());
                prefs.setBudget(budget);
                muatData();
                Toast.makeText(this, "Anggaran disimpan", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Isi anggaran dengan angka", Toast.LENGTH_SHORT).show();
            }
        });

        swDark.setOnCheckedChangeListener((b, on) -> {
            prefs.setDarkMode(on);
            terapkanTema();
        });
        swSort.setOnCheckedChangeListener((b, on) -> {
            prefs.setSortTerbesar(on);
            muatData();
        });

        btnTambah.setOnClickListener(v -> {
            String ket = etKeterangan.getText().toString().trim();
            long jumlah;
            try {
                jumlah = Long.parseLong(etJumlah.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ket.isEmpty() || jumlah <= 0) {
                Toast.makeText(this, "Keterangan dan jumlah wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            db.tambah(ket, jumlah, hariIni());
            etKeterangan.setText("");
            etJumlah.setText("");
            muatData();
        });
    }

    // Pengaturan dibaca ulang di onResume()
    @Override
    protected void onResume() {
        super.onResume();
        terapkanTema();
        tampilkanSapaan();
        muatData();
    }

    private void terapkanTema() {
        AppCompatDelegate.setDefaultNightMode(prefs.isDarkMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void tampilkanSapaan() {
        String nama = prefs.getUserName();
        tvSapaan.setText(nama.isEmpty()
                ? "Halo! Catatan Pengeluaran"
                : "Halo, " + nama + "! Catatan Pengeluaran");
    }

    private String hariIni() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private String bulanIni() {
        return hariIni().substring(0, 7);
    }

    private String rupiah(long angka) {
        return "Rp " + String.format(Locale.US, "%,d", angka).replace(',', '.');
    }

    private void muatData() {
        items = db.ambilSemua(prefs.isSortTerbesar());
        teksList.clear();
        for (DatabaseHelper.Item it : items) {
            teksList.add(it.keterangan + "\n" + rupiah(it.jumlah) + "  •  " + it.tanggal);
        }
        adapter.notifyDataSetChanged();

        long total = db.totalBulan(bulanIni());
        long budget = prefs.getBudget();
        long sisa = budget - total;

        String teks = "Total bulan ini : " + rupiah(total)
                + "\nAnggaran        : " + rupiah(budget)
                + "\nSisa            : " + rupiah(sisa);
        if (sisa < 0) {
            teks += "\n⚠ Melebihi anggaran!";
            tvRingkasan.setTextColor(Color.RED);
        } else {
            tvRingkasan.setTextColor(warnaAwal);
        }
        tvRingkasan.setText(teks);
    }
}
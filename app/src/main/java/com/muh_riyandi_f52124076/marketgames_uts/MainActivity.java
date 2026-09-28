package com.muh_riyandi_f52124076.marketgames_uts;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

@SuppressWarnings({"SetTextI18n", "StringFormatMatches", "DefaultLocale", "Convert2Comparator"})
public class MainActivity extends AppCompatActivity {
    ArrayList<Game> allGames = new ArrayList<>();
    ArrayList<Game> shownGames = new ArrayList<>();
    ArrayList<Game> cartList = new ArrayList<>();

    GameAdapter adapter;
    ImageLoader loader = new ImageLoader();
    ListView list;
    TextView emptyText, txtSectionTitle, btnFavHeader, btnCartHeader, btnSort, btnAddGame;

    EditText searchInput;
    String currentQuery = "";
    String selectedCategory = "Semua";
    boolean isFavoriteOnly = false;
    int currentSortOption = 0; // 0: Default, 1: Termurah, 2: Termahal, 3: Rating, 4: A-Z

    TextView chipAll, chipOpenWorld, chipAction, chipRacing, chipRpg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        seedGames();
        shownGames.addAll(allGames);

        list = findViewById(R.id.gameList);
        emptyText = findViewById(R.id.emptyText);
        txtSectionTitle = findViewById(R.id.txtSectionTitle);
        btnFavHeader = findViewById(R.id.btnFavHeader);
        btnCartHeader = findViewById(R.id.btnCartHeader);
        btnSort = findViewById(R.id.btnSort);
        btnAddGame = findViewById(R.id.btnAddGame);

        adapter = new GameAdapter(this, shownGames, new GameAdapter.OnGameActionListener() {
            @Override
            public void onFavoriteChanged(Game game) {
                if (isFavoriteOnly) {
                    filter();
                }
            }

            @Override
            public void onAddToCart(Game game) {
                addToCart(game);
            }
        });
        list.setAdapter(adapter);

        updateListHeight();

        list.setOnItemClickListener((parent, view, position, id) -> showDetail(shownGames.get(position)));

        // Long Click to Delete Game
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            Game targetGame = shownGames.get(position);
            new AlertDialog.Builder(this)
                .setTitle("Hapus Game")
                .setMessage("Apakah Anda yakin ingin menghapus \"" + targetGame.title + "\" dari marketplace?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    allGames.remove(targetGame);
                    cartList.remove(targetGame);
                    updateCartHeader();
                    filter();
                    Toast.makeText(this, targetGame.title + " berhasil dihapus.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
            return true;
        });

        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                currentQuery = s.toString();
                filter();
            }
            public void afterTextChanged(Editable e) {}
        });

        initChips();

        View btnSeeAll = findViewById(R.id.btnSeeAll);
        if (btnSeeAll != null) {
            btnSeeAll.setOnClickListener(v -> {
                if (searchInput != null) searchInput.setText("");
                isFavoriteOnly = false;
                currentSortOption = 0;
                if (btnFavHeader != null) btnFavHeader.setText("♡");
                selectCategory("Semua");
            });
        }

        if (btnFavHeader != null) {
            btnFavHeader.setOnClickListener(v -> {
                isFavoriteOnly = !isFavoriteOnly;
                btnFavHeader.setText(isFavoriteOnly ? "❤️" : "♡");
                Toast.makeText(this, isFavoriteOnly ? "Menampilkan game favorit" : "Menampilkan semua game", Toast.LENGTH_SHORT).show();
                filter();
            });
        }

        if (btnCartHeader != null) {
            btnCartHeader.setOnClickListener(v -> showCartDialog());
        }

        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDialog());
        }

        if (btnAddGame != null) {
            btnAddGame.setOnClickListener(v -> showAddGameDialog());
        }
    }

    private void addToCart(Game g) {
        if (!cartList.contains(g)) {
            cartList.add(g);
            updateCartHeader();
            Toast.makeText(this, g.title + " ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, g.title + " sudah ada di keranjang.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartHeader() {
        if (btnCartHeader != null) {
            btnCartHeader.setText("🛒 " + cartList.size());
        }
    }

    private void showCartDialog() {
        if (cartList.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Keranjang Belanja 🛒")
                .setMessage("Keranjang belanja Anda masih kosong. Tekan ikon 🛒 pada item game untuk menambahkan.")
                .setPositiveButton("Tutup", null)
                .show();
            return;
        }

        int totalCost = 0;
        StringBuilder sb = new StringBuilder("Item di keranjang:\n\n");
        for (int i = 0; i < cartList.size(); i++) {
            Game g = cartList.get(i);
            totalCost += g.getPriceValue();
            sb.append(i + 1).append(". ").append(g.title).append(" - ").append(g.price).append("\n");
        }
        
        String formattedTotal = String.format(Locale.GERMAN, "Rp %,d", totalCost).replace(',', '.');
        sb.append("\n----------------------------------------\n");
        sb.append("Total Biaya: ").append(formattedTotal);

        new AlertDialog.Builder(this)
            .setTitle("Keranjang Belanja 🛒 (" + cartList.size() + ")")
            .setMessage(sb.toString())
            .setPositiveButton("Checkout Belanja", (dialog, which) -> {
                cartList.clear();
                updateCartHeader();
                Toast.makeText(this, "Checkout Berhasil! Total " + formattedTotal + " telah dibayar.", Toast.LENGTH_LONG).show();
            })
            .setNeutralButton("Kosongkan Cart", (dialog, which) -> {
                cartList.clear();
                updateCartHeader();
                Toast.makeText(this, "Keranjang belanja dikosongkan.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void showSortDialog() {
        String[] options = {"Default (Urutan Awal)", "Harga Termurah", "Harga Termahal", "Rating Tertinggi", "Nama (A - Z)"};
        new AlertDialog.Builder(this)
            .setTitle("Urutkan Daftar Game 🔃")
            .setSingleChoiceItems(options, currentSortOption, (dialog, which) -> {
                currentSortOption = which;
                applySorting();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            })
            .show();
    }

    private void applySorting() {
        switch (currentSortOption) {
            case 1: // Harga Termurah
                Collections.sort(shownGames, (g1, g2) -> Integer.compare(g1.getPriceValue(), g2.getPriceValue()));
                break;
            case 2: // Harga Termahal
                Collections.sort(shownGames, (g1, g2) -> Integer.compare(g2.getPriceValue(), g1.getPriceValue()));
                break;
            case 3: // Rating Tertinggi
                Collections.sort(shownGames, (g1, g2) -> Double.compare(g2.getRatingValue(), g1.getRatingValue()));
                break;
            case 4: // Nama A-Z
                Collections.sort(shownGames, (g1, g2) -> g1.title.compareToIgnoreCase(g2.title));
                break;
            default: // Default
                break;
        }
    }

    private void showAddGameDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputTitle = new EditText(this);
        inputTitle.setHint("Judul Game (misal: FIFA 24)");
        layout.addView(inputTitle);

        final EditText inputGenre = new EditText(this);
        inputGenre.setHint("Genre (misal: Sports • Simulation)");
        layout.addView(inputGenre);

        final EditText inputPublisher = new EditText(this);
        inputPublisher.setHint("Publisher (misal: EA Sports)");
        layout.addView(inputPublisher);

        final EditText inputPrice = new EditText(this);
        inputPrice.setHint("Harga (misal: Rp 599.000)");
        layout.addView(inputPrice);

        final EditText inputRating = new EditText(this);
        inputRating.setHint("Rating (misal: 4.6)");
        layout.addView(inputRating);

        final EditText inputDesc = new EditText(this);
        inputDesc.setHint("Deskripsi Singkat");
        layout.addView(inputDesc);

        new AlertDialog.Builder(this)
            .setTitle("Tambah Game Baru +")
            .setView(layout)
            .setPositiveButton("Simpan Game", (dialog, which) -> {
                String title = inputTitle.getText().toString().trim();
                String genre = inputGenre.getText().toString().trim();
                String publisher = inputPublisher.getText().toString().trim();
                String price = inputPrice.getText().toString().trim();
                String rating = inputRating.getText().toString().trim();
                String desc = inputDesc.getText().toString().trim();

                if (title.isEmpty()) title = "Game Baru";
                if (genre.isEmpty()) genre = "Action • RPG";
                if (publisher.isEmpty()) publisher = "Indie Studio";
                if (price.isEmpty()) price = "Rp 199.000";
                if (rating.isEmpty()) rating = "4.5";
                if (desc.isEmpty()) desc = "Deskripsi produk simulasi game baru.";

                String defaultCover = "https://cdn.cloudflare.steamstatic.com/steam/apps/271590/header.jpg";
                Game newGame = new Game(title, genre, publisher, rating, price, "-20%", defaultCover, desc, title.toLowerCase());
                allGames.add(0, newGame);
                filter();
                Toast.makeText(this, title + " berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void initChips() {
        chipAll = findViewById(R.id.chipAll);
        chipOpenWorld = findViewById(R.id.chipOpenWorld);
        chipAction = findViewById(R.id.chipAction);
        chipRacing = findViewById(R.id.chipRacing);
        chipRpg = findViewById(R.id.chipRpg);

        if (chipAll != null) chipAll.setOnClickListener(v -> selectCategory("Semua"));
        if (chipOpenWorld != null) chipOpenWorld.setOnClickListener(v -> selectCategory("Open World"));
        if (chipAction != null) chipAction.setOnClickListener(v -> selectCategory("Action"));
        if (chipRacing != null) chipRacing.setOnClickListener(v -> selectCategory("Racing"));
        if (chipRpg != null) chipRpg.setOnClickListener(v -> selectCategory("RPG"));

        updateChipStyles();
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        updateChipStyles();
        filter();
    }

    private void updateChipStyles() {
        TextView[] chips = {chipAll, chipOpenWorld, chipAction, chipRacing, chipRpg};
        String[] categories = {"Semua", "Open World", "Action", "Racing", "RPG"};

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] != null) {
                if (categories[i].equalsIgnoreCase(selectedCategory)) {
                    chips[i].setBackgroundResource(R.drawable.bg_pill);
                    chips[i].setTextColor(ContextCompat.getColor(this, R.color.text));
                } else {
                    chips[i].setBackground(null);
                    chips[i].setTextColor(ContextCompat.getColor(this, R.color.muted));
                }
            }
        }
    }

    private void updateListHeight() {
        if (adapter == null || list == null) return;
        int count = adapter.getCount();

        if (count == 0) {
            list.setVisibility(View.GONE);
            if (emptyText != null) {
                emptyText.setVisibility(View.VISIBLE);
                if (isFavoriteOnly) {
                    emptyText.setText("Belum ada game favorit\nTekan tombol ♡ pada game untuk menyimpannya.");
                } else {
                    emptyText.setText("Game tidak ditemukan");
                }
            }
            ViewGroup.LayoutParams params = list.getLayoutParams();
            params.height = 0;
            list.setLayoutParams(params);
            return;
        }

        list.setVisibility(View.VISIBLE);
        if (emptyText != null) {
            emptyText.setVisibility(View.GONE);
        }

        View dummyItem = LayoutInflater.from(this).inflate(R.layout.item_game, list, false);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        dummyItem.measure(widthSpec, heightSpec);
        int itemHeight = dummyItem.getMeasuredHeight();

        int dividerHeight = list.getDividerHeight();
        int totalHeight = (itemHeight * count) + (dividerHeight * Math.max(0, count - 1));

        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = totalHeight;
        list.setLayoutParams(params);
        list.requestLayout();
    }

    private void seedGames() {
        allGames.add(new Game("Grand Theft Auto V", "Action • Open World", "Rockstar Games", "4.8",
            "Rp 299.000", "-40%", "https://cdn.cloudflare.steamstatic.com/steam/apps/271590/header.jpg",
            "Masuki Los Santos dalam petualangan open-world dengan tiga karakter utama. Aplikasi ini menampilkan GTA V sebagai contoh produk marketplace PC.",
            "gta gta5 gta v grand theft auto rockstar"));
        allGames.add(new Game("Red Dead Redemption 2", "Action • Open World", "Rockstar Games", "4.9",
            "Rp 879.000", "-35%", "https://cdn.cloudflare.steamstatic.com/steam/apps/1174180/header.jpg",
            "Ikuti kisah Arthur Morgan dan Van der Linde gang pada akhir era Wild West. Data deskripsi mengikuti ringkasan resmi Rockstar Store.",
            "rdr rdr2 red dead redemption rockstar arthur morgan"));
        allGames.add(new Game("Cyberpunk 2077", "RPG • Open World", "CD PROJEKT RED", "4.7",
            "Rp 699.000", "-30%", "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
            "RPG open-world berlatar Night City. Ditampilkan sebagai produk simulasi untuk kebutuhan tugas UTS.",
            "cyberpunk cp2077 cd projekt red cdpr night city"));
        allGames.add(new Game("Forza Horizon 5", "Racing • Open World", "Xbox Game Studios", "4.8",
            "Rp 790.000", "-60%", "https://cdn.cloudflare.steamstatic.com/steam/apps/1551360/header.jpg",
            "Eksplorasi lanskap open-world Meksiko dan berbagai mobil dalam game balap Forza Horizon 5.",
            "forza fh5 racing balap mobil xbox meksiko"));
        allGames.add(new Game("Elden Ring", "RPG • Action", "FromSoftware", "4.9",
            "Rp 799.000", "-25%", "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg",
            "Action RPG dengan dunia luas untuk dijelajahi. Produk ditampilkan sebagai contoh data array Custom ListView.",
            "elden ring souls fromsoftware soulsborne rpg"));
        allGames.add(new Game("The Witcher 3: Wild Hunt", "RPG • Open World", "CD PROJEKT RED", "4.9",
            "Rp 399.000", "-50%", "https://cdn.cloudflare.steamstatic.com/steam/apps/292030/header.jpg",
            "Petualangan Geralt of Rivia dalam dunia fantasi terbuka. Ditampilkan sebagai contoh produk marketplace.",
            "witcher witcher 3 geralt cd projekt red cdpr rpg"));
    }

    private void filter() {
        shownGames.clear();
        for (Game g : allGames) {
            if (g.matches(currentQuery, selectedCategory, isFavoriteOnly)) {
                shownGames.add(g);
            }
        }

        applySorting();

        if (txtSectionTitle != null) {
            if (isFavoriteOnly) {
                txtSectionTitle.setText("Favorit saya (" + shownGames.size() + ")");
            } else if (!currentQuery.isEmpty()) {
                txtSectionTitle.setText("Hasil pencarian (" + shownGames.size() + ")");
            } else if (!selectedCategory.equalsIgnoreCase("Semua")) {
                txtSectionTitle.setText("Kategori: " + selectedCategory + " (" + shownGames.size() + ")");
            } else {
                txtSectionTitle.setText("Game populer (" + shownGames.size() + ")");
            }
        }

        adapter.notifyDataSetChanged();
        updateListHeight();
    }

    private void showDetail(Game g) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_game, null);
        ImageView cover = v.findViewById(R.id.detailCover);
        TextView txtTitle = v.findViewById(R.id.detailTitle);
        TextView txtMeta = v.findViewById(R.id.detailMeta);
        TextView txtDesc = v.findViewById(R.id.detailDesc);
        TextView txtPrice = v.findViewById(R.id.detailPrice);
        TextView btnFav = v.findViewById(R.id.detailFavBtn);
        TextView btnShare = v.findViewById(R.id.detailShareBtn);
        Button btnBuy = v.findViewById(R.id.detailBuyBtn);

        txtTitle.setText(g.title);
        txtMeta.setText(g.genre + "  •  " + g.publisher + "  •  ★ " + g.rating);
        txtDesc.setText(g.description);
        txtPrice.setText(g.price + "   " + g.discount);
        loader.load(g.coverUrl, cover);

        if (btnFav != null) {
            btnFav.setText(g.isFavorite ? "❤️ Favorit" : "♡ Favorit");
            btnFav.setOnClickListener(view -> {
                g.isFavorite = !g.isFavorite;
                btnFav.setText(g.isFavorite ? "❤️ Favorit" : "♡ Favorit");
                adapter.notifyDataSetChanged();
                Toast.makeText(this, g.isFavorite ? "Ditambahkan ke Favorit" : "Dihapus dari Favorit", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(view -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, g.title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Beli game " + g.title + " seharga " + g.price + " di PC Gamers Market App!");
                startActivity(Intent.createChooser(shareIntent, "Bagikan via"));
            });
        }

        AlertDialog dialog = new AlertDialog.Builder(this).setView(v).setPositiveButton("Tutup", null).create();

        if (btnBuy != null) {
            btnBuy.setOnClickListener(view -> {
                dialog.dismiss();
                new AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Pembelian")
                    .setMessage("Apakah Anda yakin ingin membeli " + g.title + " seharga " + g.price + "?")
                    .setPositiveButton("Beli Sekarang", (d, which) -> 
                        Toast.makeText(this, "Pembelian " + g.title + " berhasil! Kode seri game dikirim ke akun Anda.", Toast.LENGTH_LONG).show()
                    )
                    .setNegativeButton("Batal", null)
                    .show();
            });
        }

        dialog.show();
    }
}

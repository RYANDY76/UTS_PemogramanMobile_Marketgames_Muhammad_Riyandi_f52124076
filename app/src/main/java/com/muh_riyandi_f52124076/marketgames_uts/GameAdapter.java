package com.muh_riyandi_f52124076.marketgames_uts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class GameAdapter extends BaseAdapter {
    public interface OnGameActionListener {
        void onFavoriteChanged(Game game);
        void onAddToCart(Game game);
    }

    private Context context;
    private ArrayList<Game> data;
    private ImageLoader loader = new ImageLoader();
    private OnGameActionListener listener;

    public GameAdapter(Context context, ArrayList<Game> data, OnGameActionListener listener) {
        this.context = context;
        this.data = data;
        this.listener = listener;
    }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public Object getItem(int p) { return data.get(p); }

    @Override
    public long getItemId(int p) { return p; }

    static class ViewHolder {
        TextView title, meta, rating, price, discount, btnFav, btnCart;
        ImageView cover;
    }

    @Override
    public View getView(int p, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.title);
            holder.meta = convertView.findViewById(R.id.meta);
            holder.rating = convertView.findViewById(R.id.rating);
            holder.price = convertView.findViewById(R.id.price);
            holder.discount = convertView.findViewById(R.id.discount);
            holder.btnCart = convertView.findViewById(R.id.btnCartItem);
            holder.btnFav = convertView.findViewById(R.id.btnFavItem);
            holder.cover = convertView.findViewById(R.id.cover);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Game g = data.get(p);
        holder.title.setText(g.title);
        holder.meta.setText(g.genre + " • " + g.publisher);
        holder.rating.setText("★ " + g.rating + "   •   PC");
        holder.price.setText(g.price);
        holder.discount.setText(g.discount);
        holder.btnFav.setText(g.isFavorite ? "❤️" : "♡");

        holder.btnFav.setOnClickListener(v -> {
            g.isFavorite = !g.isFavorite;
            holder.btnFav.setText(g.isFavorite ? "❤️" : "♡");
            if (listener != null) {
                listener.onFavoriteChanged(g);
            }
        });

        if (holder.btnCart != null) {
            holder.btnCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCart(g);
                }
            });
        }

        loader.load(g.coverUrl, holder.cover);

        return convertView;
    }
}

//package com.example.fitgym.ui.adapter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.ScaleAnimation;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.preference.PreferenceManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.fitgym.R;
//import com.example.fitgym.data.model.Coach;
//import com.example.fitgym.ui.CoachDetailActivity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CoachAdapter extends RecyclerView.Adapter<CoachAdapter.CoachViewHolder> {
//
//    private Context context;
//    private List<Coach> coachList;
//    private SharedPreferences prefs;
//
//    public CoachAdapter(Context context, List<Coach> coachList) {
//        this.context = context;
//        this.coachList = coachList != null ? coachList : new ArrayList<>();
//        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
//    }
//
//    @NonNull
//    @Override
//    public CoachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_coach, parent, false);
//        return new CoachViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull CoachViewHolder holder, int position) {
//        Coach coach = coachList.get(position);
//
//        holder.nom.setText(coach.getNom() != null ? coach.getNom() : "");
//        holder.rating.setText("⭐ " + coach.getRating());
//        holder.reviewCount.setText("(" + coach.getReviewCount() + " avis)");
//        holder.description.setText(coach.getDescription() != null ? coach.getDescription() : "");
//        holder.sessionCount.setText(coach.getSessionCount() + " séances disponibles");
//
//        String photoUrl = coach.getPhotoUrl();
//        if (photoUrl == null || photoUrl.isEmpty()) {
//            Glide.with(context).load("file:///mnt/data/b87f9ec3-e0ff-4d73-99d0-7fb79e10f8ee.png")
//                    .placeholder(R.drawable.coach_placeholder)
//                    .into(holder.photo);
//        } else {
//            Glide.with(context).load(photoUrl).placeholder(R.drawable.coach_placeholder).into(holder.photo);
//        }
//
//        // tags dynamiques
//        holder.tagsContainer.removeAllViews();
//        if (coach.getSpecialites() != null) {
//            for (String tag : coach.getSpecialites()) {
//                TextView tv = new TextView(context);
//                tv.setText(tag);
//                tv.setPadding(24, 8, 24, 8);
//                tv.setTextSize(12);
//                tv.setTextColor(Color.BLACK);
//                tv.setBackground(context.getDrawable(R.drawable.tag_background));
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                params.setMarginEnd(12);
//                tv.setLayoutParams(params);
//                holder.tagsContainer.addView(tv);
//            }
//        }
//
//        // Favoris state (SharedPreferences simple: key = "fav_<coachId>" or name if id==null)
//        String key = "fav_" + (coach.getId() != null ? coach.getId() : coach.getNom());
//        boolean fav = prefs.getBoolean(key, false);
//        holder.btnFavorite.setImageResource(fav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
//
//        holder.btnFavorite.setOnClickListener(v -> {
//            boolean current = prefs.getBoolean(key, false);
//            prefs.edit().putBoolean(key, !current).apply();
//            holder.btnFavorite.setImageResource(!current ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
//
//            // petite animation
//            ScaleAnimation anim = new ScaleAnimation(0.7f,1f,0.7f,1f, ScaleAnimation.RELATIVE_TO_SELF,0.5f,ScaleAnimation.RELATIVE_TO_SELF,0.5f);
//            anim.setDuration(220);
//            holder.btnFavorite.startAnimation(anim);
//        });
//
//        // click on card -> open details with shared element-ish transition (simple)
//        holder.itemView.setOnClickListener(v -> {
//            Intent i = new Intent(context, CoachDetailActivity.class);
//            i.putExtra("coachId", coach.getId());
//            i.putExtra("coachNom", coach.getNom());
//            i.putExtra("coachPhoto", coach.getPhotoUrl());
//            i.putExtra("coachDesc", coach.getDescription());
//            i.putExtra("coachRating", coach.getRating());
//            i.putStringArrayListExtra("coachTags", coach.getSpecialites() != null ? new ArrayList<>(coach.getSpecialites()) : new ArrayList<>());
//            context.startActivity(i);
//            // animation
//            holder.itemView.animate().scaleX(0.98f).scaleY(0.98f).setDuration(80).withEndAction(() ->
//                    holder.itemView.animate().scaleX(1f).scaleY(1f).setDuration(80));
//        });
//    }
//
//    @Override
//    public int getItemCount() { return coachList.size(); }
//
//    public void updateList(List<Coach> newList) {
//        this.coachList = newList != null ? newList : new ArrayList<>();
//        notifyDataSetChanged();
//    }
//
//    static class CoachViewHolder extends RecyclerView.ViewHolder {
//        ImageView photo, btnFavorite;
//        TextView nom, rating, reviewCount, description, sessionCount;
//        LinearLayout tagsContainer;
//        View cardRoot;
//
//        CoachViewHolder(@NonNull View itemView) {
//            super(itemView);
//            photo = itemView.findViewById(R.id.imgCoach);
//            btnFavorite = itemView.findViewById(R.id.btnFavorite);
//            nom = itemView.findViewById(R.id.coachName);
//            rating = itemView.findViewById(R.id.ratingValue);
//            reviewCount = itemView.findViewById(R.id.ratingReviews);
//            description = itemView.findViewById(R.id.coachDescription);
//            sessionCount = itemView.findViewById(R.id.coachSessions);
//            tagsContainer = itemView.findViewById(R.id.tagContainer);
//            cardRoot = itemView.findViewById(R.id.cardRoot);
//        }
//    }
//}

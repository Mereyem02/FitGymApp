//package com.example.fitgym.ui;
//
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.preference.PreferenceManager;
//
//import com.bumptech.glide.Glide;
//import com.example.fitgym.R;
//
//import java.util.ArrayList;
//
//public class CoachDetailActivity extends AppCompatActivity {
//
//    ImageView detailImg, detailFav;
//    TextView detailName, detailRating, detailDescription;
//    LinearLayout detailTags;
//    Button btnContact;
//    SharedPreferences prefs;
//    String coachIdKey;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_coach_detail);
//
//        detailImg = findViewById(R.id.detailImg);
//        detailFav = findViewById(R.id.detailFav);
//        detailName = findViewById(R.id.detailName);
//        detailRating = findViewById(R.id.detailRating);
//        detailDescription = findViewById(R.id.detailDescription);
//        detailTags = findViewById(R.id.detailTags);
//        btnContact = findViewById(R.id.btnContact);
//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//        String id = getIntent().getStringExtra("coachId");
//        coachIdKey = "fav_" + (id != null ? id : getIntent().getStringExtra("coachNom"));
//        String nom = getIntent().getStringExtra("coachNom");
//        String photo = getIntent().getStringExtra("coachPhoto");
//        String desc = getIntent().getStringExtra("coachDesc");
//        double rating = getIntent().getDoubleExtra("coachRating", 0.0);
//        ArrayList<String> tags = getIntent().getStringArrayListExtra("coachTags");
//
//        detailName.setText(nom);
//        detailRating.setText("⭐ " + rating);
//        detailDescription.setText(desc != null ? desc : "");
//        if (photo == null || photo.isEmpty()) {
//            Glide.with(this).load("file:///mnt/data/b87f9ec3-e0ff-4d73-99d0-7fb79e10f8ee.png").into(detailImg);
//        } else {
//            Glide.with(this).load(photo).into(detailImg);
//        }
//
//        // tags
//        detailTags.removeAllViews();
//        if (tags != null) {
//            for (String t : tags) {
//                TextView tv = new TextView(this);
//                tv.setText(t);
//                tv.setPadding(20,8,20,8);
//                tv.setBackground(getDrawable(R.drawable.tag_background));
//                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                p.setMarginEnd(12);
//                tv.setLayoutParams(p);
//                detailTags.addView(tv);
//            }
//        }
//
//        boolean fav = prefs.getBoolean(coachIdKey, false);
//        detailFav.setImageResource(fav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
//
//        detailFav.setOnClickListener(v -> {
//            boolean current = prefs.getBoolean(coachIdKey, false);
//            prefs.edit().putBoolean(coachIdKey, !current).apply();
//            detailFav.setImageResource(!current ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
//        });
//
//        btnContact.setOnClickListener(v -> {
//            // Implémente contact (appel / message). Placeholder :
//            // Toast.makeText(this, "Contact: " + nom, Toast.LENGTH_SHORT).show();
//        });
//    }
//}

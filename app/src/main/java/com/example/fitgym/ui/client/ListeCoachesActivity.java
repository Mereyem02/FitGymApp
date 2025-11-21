//package com.example.fitgym.ui.client;
//
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.fitgym.MainActivity;
//import com.example.fitgym.R;
//import com.example.fitgym.data.dao.DAOCoach;
//import com.example.fitgym.data.model.Coach;
//import com.example.fitgym.data.repository.CoachRepository;
//import com.example.fitgym.ui.adapter.CoachAdapter;
////import com.example.fitgym.ui.adapter.CoachAdapter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ListeCoachesActivity {
//    RecyclerView recyclerCoachs;
//    EditText inputSearch;
//    CoachAdapter adapter;
//
//    List<Coach> coachList = new ArrayList<>();
//    List<Coach> filteredList = new ArrayList<>();
//
//    CoachRepository coachRepository = new CoachRepository();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        recyclerCoachs = findViewById(R.id.recyclerCoachs);
//        inputSearch = findViewById(R.id.inputSearch);
//
//        adapter = new CoachAdapter(this, filteredList);
//        recyclerCoachs.setLayoutManager(new LinearLayoutManager(this));
//        recyclerCoachs.setAdapter(adapter);
//
//        loadFirestoreCoachs(); // 🔹 Option B
//
//        // 🔍 Filtre en temps réel
//        inputSearch.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void afterTextChanged(Editable s) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filter(s.toString());
//            }
//        });
//    }
//
//    private void setContentView(int activityMain) {
//    }
//
//
//    private void loadFirestoreCoachs() {
//        DAOCoach.Lister (new CoachRepository.CoachCallback() {
//            @Override
//            public void onSuccess(List<Coach> list) {
//                coachList.addAll(list);
//                filteredList.addAll(list);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onError(String error) {
//                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//    // 🔍 Filtrage
//    private void filter(String text) {
//        filteredList.clear();
//        for (Coach c : coachList) {
//            if (c.getNom().toLowerCase().contains(text.toLowerCase())) {
//                filteredList.add(c);
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }
//}

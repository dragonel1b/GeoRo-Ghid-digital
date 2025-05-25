package com.example.myapplication.viewmodel;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.widget.AppCompatTextView;

public class SectionPreviewActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE = "section_title";
    public static final String EXTRA_CONTENT = "section_content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_preview);

        // Get data from intent
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }

        // Set content
        AppCompatTextView contentView = findViewById(R.id.sectionContent);
        contentView.setText(content);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Apply exit animation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}

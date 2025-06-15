package com.example.myapplication.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.views.fragments.AdminHomeFragment;
import com.example.myapplication.views.fragments.CategoryFragment;
import com.example.myapplication.views.fragments.ProductFragment;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        
        tokenManager = TokenManager.getInstance(this);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Setup drawer
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        // Update header info
        TextView navHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_title);
        navHeaderTitle.setText("Admin - " + tokenManager.getUsername());
        
        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminHomeFragment())
                .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        
        if (id == R.id.nav_home) {
            fragment = new AdminHomeFragment();
        } else if (id == R.id.nav_categories) {
            fragment = CategoryFragment.newInstance();
        } else if (id == R.id.nav_products) {
            fragment = ProductFragment.newInstance();
        } else if (id == R.id.nav_orders) {
            // Navigate to orders management screen
            Intent intent = new Intent(this, AdminOrderActivity.class);
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_logout) {
            // Clear all stored data
            tokenManager.clearAll();
            
            // Navigate to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        }
        
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
} 
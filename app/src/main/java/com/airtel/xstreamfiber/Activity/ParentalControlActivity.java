package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.airtel.xstreamfiber.Fragment.ParentalControl;
import com.airtel.xstreamfiber.Fragment.ParentalControlBlockedDevice;
import com.airtel.xstreamfiber.Fragment.ParentalControlUnblockedInactiveDevices;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.FragmentUtils;
import com.google.android.material.tabs.TabLayout;

/*
    In ParentalControlActivity, there are 3 tabs named Connected Devices, Disconnected Devices, and Blocked Devices, on selecting any tab
    the specific fragment is replaced with another fragment.
*/
public class ParentalControlActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtToolbarTitle;
    private ImageView imgBack;
    private Dialog mDialog;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parental_control);

        tabLayout = findViewById(R.id.tabs);

        FragmentUtils.openFragment(this, R.id.parentalcontainer, new ParentalControl(this));

        tabLayout.addTab(tabLayout.newTab().setText("Connected Devices"));
        tabLayout.addTab(tabLayout.newTab().setText("Disconnected Devices"));
        tabLayout.addTab(tabLayout.newTab().setText("Blocked Devices"));

        //Replacing fragment on tab change
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tabLayout.getSelectedTabPosition() == 0){

                    FragmentUtils.replaceFragment(ParentalControlActivity.this,R.id.parentalcontainer, new ParentalControl(ParentalControlActivity.this));
                }
                else if(tabLayout.getSelectedTabPosition() == 1){

                    FragmentUtils.replaceFragment(ParentalControlActivity.this,R.id.parentalcontainer, new ParentalControlUnblockedInactiveDevices(ParentalControlActivity.this));
                }
                else if(tabLayout.getSelectedTabPosition() == 2){

                    FragmentUtils.replaceFragment(ParentalControlActivity.this,R.id.parentalcontainer, new ParentalControlBlockedDevice(ParentalControlActivity.this));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Parental Control");
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();

        Intent i = new Intent(ParentalControlActivity.this, MainMenuActivity.class);
        startActivity(i);
        finish();
        ParentalControlActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }
        }
    }
}

package com.airtel.xstreamfiber.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airtel.xstreamfiber.R;

import org.jetbrains.annotations.NotNull;

public class NetworkMap extends Fragment {

    private LinearLayout llLeft, llRight, llParentEthernet;
    private Context context;
    private View view;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network_map, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        renderDynamicViews();
    }

    private void initViews(View view) {
        llLeft = view.findViewById(R.id.ll_left);
        llRight = view.findViewById(R.id.ll_right);
        llParentEthernet = view.findViewById(R.id.parent_ethernet);
    }


    public static NetworkMap newInstance() {
        return new NetworkMap();
    }

    private void renderDynamicViews() {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view;

        //Parse and show 2.5G data here. Modify loop based upon API response
        for (int i = 0; i < 5; i++) {

            view = layoutInflater.inflate(R.layout.layout_wireless_left, null);
            llLeft.addView(view);

            //Below line is added to hide extra line at the bottom left of Wireless data.
            if (i == 4) {
                View viewToHide = view.findViewById(R.id.view3);
                viewToHide.setVisibility(View.GONE);
            }
        }

        //Parse and show 5G data here.  Modify loop based upon API response
        for (int i = 0; i < 5; i++) {
            view = layoutInflater.inflate(R.layout.layout_wireless_right, null);
            llRight.addView(view);

            //Below line is added to hide extra line at the bottom right of Wireless data.
            if (i == 4) {
                View viewToHide = view.findViewById(R.id.view3);
                viewToHide.setVisibility(View.GONE);
            }
        }

        //Parse and show ehternet data here.  Modify loop based upon API response
        for (int i = 0; i < 3; i++) {
            view = layoutInflater.inflate(R.layout.layout_ehternet_data, null);
            llParentEthernet.addView(view);

            //Below line is added to hide extra line at the bottom right of Ethernet data.
            if (i == 2) {
                View viewToHide = view.findViewById(R.id.view3);
                viewToHide.setVisibility(View.GONE);
            }

        }
    }


}


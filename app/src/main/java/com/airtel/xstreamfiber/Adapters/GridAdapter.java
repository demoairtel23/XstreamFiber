package com.airtel.xstreamfiber.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtel.xstreamfiber.Activity.MainMenuActivity;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;

import static android.content.Context.MODE_PRIVATE;

//Adapter for grid view in mainMenu screen
public class GridAdapter extends BaseAdapter {

    Context context;
    String[] name;
    int[] images;
    String upgrade, userSerial;

    public GridAdapter(Context context, String[] name, int[] images) {
        this.context = context;
        this.name = name;
        this.images = images;
    }

    @Override
    public int getCount() {
        return name.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        v = inflater.inflate(R.layout.grid_item, null);
        v.setMinimumHeight((((MainMenuActivity) context).gridHeight / 4) - 1);  // prev -> gridHeight/5
        TextView gridTv = v.findViewById(R.id.gridTv);
        ImageView gridImg = v.findViewById(R.id.gridImg);

        TextView gridBadge = v.findViewById(R.id.badge_new);

        gridTv.setText(name[position]);
        gridImg.setImageResource(images[position]);

        SharedPreferences dduSharedPref = context.getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(context, userSerialEncrypted);

        SharedPreferences sharedPref = context.getSharedPreferences(userSerial, MODE_PRIVATE);
        upgrade = sharedPref.getString("upgrade", "");

        if (position == 6)
        {
            if (upgrade.length() != 0)
                gridBadge.setVisibility(View.GONE);
            else
                gridBadge.setVisibility(View.GONE);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainMenuActivity) context).clickMenu(position);
            }
        });

        return v;
    }
}

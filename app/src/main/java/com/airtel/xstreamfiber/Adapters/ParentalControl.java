package com.airtel.xstreamfiber.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airtel.xstreamfiber.Fragment.ParentalControlUnblockedInactiveDevices;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.model.UnblockedDevice;

import java.lang.reflect.Field;
import java.util.ArrayList;

//For showing / inflating views in Parental Control Connected devices and disconnected devices
public class ParentalControl extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AlertDialog alert;
    private Context context;
    ArrayList<UnblockedDevice> arrayListUnblock;
    com.airtel.xstreamfiber.Fragment.ParentalControl fragment;
    ParentalControlUnblockedInactiveDevices frag;
    private String blockedStatus = "0";

    //When called from Connected devices fragment
    public ParentalControl(Context context, ArrayList<UnblockedDevice> arrayListUnblock, com.airtel.xstreamfiber.Fragment.ParentalControl fragment) {
        this.context = context;
        this.arrayListUnblock = arrayListUnblock;
        this.fragment = fragment;
    }

    //When called from Disconnected devices fragment
    public ParentalControl(Context context, ArrayList<UnblockedDevice> arrayListUnblock, com.airtel.xstreamfiber.Fragment.ParentalControlUnblockedInactiveDevices fragment) {
        this.context = context;
        this.arrayListUnblock = arrayListUnblock;
        this.frag = fragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_parental_control, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;

        setData(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return arrayListUnblock.size();
    }


    private void setData(final ViewHolder holder, final int pos) {

        holder.tvName.setText(arrayListUnblock.get(pos).getHostName());

        holder.switchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    callAlertDialog(holder, pos); //Open dialog for selecting permanent or temporary block
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Switch switchToggle;
        private TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            switchToggle = itemView.findViewById(R.id.switch_toggle);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    public void callAlertDialog(ViewHolder holder, int pos)
    {
        alert = new AlertDialog.Builder(context).create();
        alert.setCancelable(false);

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.popup_layout, null);

        alert.setView(mView);

        RadioGroup radio_grp_disable = mView.findViewById(R.id.radio_grp_disable);

        RadioButton radio_permanent = mView.findViewById(R.id.radio_permanent);
        RadioButton radio_temp = mView.findViewById(R.id.radio_temp);

        Button btn_cancel = mView.findViewById(R.id.btn_cancel);
        Button btn_ok = mView.findViewById(R.id.btn_ok);

        alert.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                holder.switchToggle.setChecked(false);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (radio_grp_disable.getCheckedRadioButtonId() == R.id.radio_permanent)
                {
                    //If permanently blocked then show a confirmation dialog
                    alert.dismiss();
                    callConfirmDialog(holder, pos);
                }

                else if (radio_grp_disable.getCheckedRadioButtonId() == R.id.radio_temp)
                {
                    //If temporary blocked then show a dialog to select time period
                    alert.dismiss();
                    callTempAlertDialog(holder, pos);
                }
                else
                {
                    Toast.makeText(context, "Please choose one!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void callConfirmDialog(ViewHolder holder, int pos)
    {
        AlertDialog.Builder alert1 = new AlertDialog.Builder(context);
        alert1.setCancelable(false);

        alert1.setTitle("");
        alert1.setMessage("Are you sure you want to Permanently Block?");

        alert1.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        blockedStatus = "1";

                        if (NetworkUtils.isNetworkConnected(context)) {

                            if (fragment != null) //If called from Connected devices fragment
                            {
                                holder.switchToggle.setChecked(true);
                                fragment.blockUnblock(pos, blockedStatus, "", 0, 0, "yes");
                                alert.dismiss();
                                holder.switchToggle.setChecked(true);
                            }
                            else if (frag != null)  //If called from Disconnected devices fragment
                            {
                                holder.switchToggle.setChecked(true);
                                frag.blockUnblock(pos, blockedStatus, "", 0, 0, "yes");
                                alert.dismiss();

                            }
                        } else  {

                            Toast.makeText(context, context.getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        alert1.setNegativeButton(
                "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alert.dismiss();
                        holder.switchToggle.setChecked(false);
                    }
                }
        );

        alert = alert1.create();
        alert.show();
    }

    public void callTempAlertDialog(ViewHolder holder, int pos)
    {
        alert = new AlertDialog.Builder(context).create();
        alert.setCancelable(false);

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.popup_layout_temporary, null);

        alert.setView(mView);

        NumberPicker start_minutePicker, end_minutePicker;

        TimePicker start_time = mView.findViewById(R.id.start_time);
        TimePicker end_time = mView.findViewById(R.id.end_time);
        RadioButton radio_once = mView.findViewById(R.id.radio_once);
        RadioButton radio_daily = mView.findViewById(R.id.radio_daily);
        RelativeLayout rlTimer = mView.findViewById(R.id.rlTimer);
        Button btn_cancel = mView.findViewById(R.id.btn_cancel);
        Button btn_ok = mView.findViewById(R.id.btn_ok);

        start_time.setIs24HourView(true);
        end_time.setIs24HourView(true);

//        start_time.setCurrentHour(0);
//        end_time.setCurrentHour(0);

      //  Toast.makeText(context, "Please select start time and end time", Toast.LENGTH_SHORT).show();

        try
        {
            String[] min = {"00"};

            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field fieldMinute = classForid.getField("minute");

            start_time.setCurrentMinute(0);
            start_minutePicker = (NumberPicker) start_time.findViewById(fieldMinute.getInt(null));
            //start_minutePicker.setDisplayedValues(min);
            start_minutePicker.setMaxValue(0);
            start_minutePicker.setMinValue(0);
            start_minutePicker.setClickable(false);
            start_minutePicker.setEnabled(false);

            end_time.setCurrentMinute(0);
            end_minutePicker = (NumberPicker) end_time.findViewById(fieldMinute.getInt(null));
           // end_minutePicker.setDisplayedValues(min);
            end_minutePicker.setMaxValue(0);
            end_minutePicker.setMinValue(0);
            end_minutePicker.setClickable(false);
            end_minutePicker.setEnabled(false);
        }
        catch (Exception ignored)
        {
        }

        alert.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                holder.switchToggle.setChecked(false);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String time_limit="";

                int start_hour, end_hour;
                if (Build.VERSION.SDK_INT >= 23 ){
                    start_hour = start_time.getHour();
                    // start_minute = start_time.getMinute();
                    end_hour = end_time.getHour();
                    // end_time.setMinute(0);
                }
                else{
                    start_hour = start_time.getCurrentHour();
                    // start_minute = start_time.getCurrentMinute();
                    end_hour = start_hour + 12;
                    //  end_minute = 0;
                }

                if (radio_once.isChecked()) {
                    time_limit = "1";
                }
                else if (radio_daily.isChecked()) {
                    time_limit = "2";
                }

                if ((radio_once.isChecked() || radio_daily.isChecked() ) && end_hour != start_hour ) {
                    alert.dismiss();
                    //Call confirmation dialog in case of temporary block
                    if (start_hour == 0)
                        start_hour = 24;
                    if (end_hour == 0)
                        end_hour = 24;

                    callConfirmTempDialog(holder, pos, time_limit, start_hour, end_hour);
                }
                else if (end_hour == start_hour)
                {
                    Toast.makeText(context, "End Hour can not be equal to start hour !", Toast.LENGTH_SHORT).show();
                }
                else if (!(radio_once.isChecked() || radio_daily.isChecked() ) )
                {
                    Toast.makeText(context, "Please select a time limit !", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void callConfirmTempDialog(ViewHolder holder, int pos, String time_limit, int start_hour, int end_hour)
    {
        AlertDialog.Builder alert1 = new AlertDialog.Builder(context);
        alert1.setCancelable(false);

        alert1.setTitle("");
        alert1.setMessage("Are you sure you want to Block?");

        alert1.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        blockedStatus = "1";


                            if (NetworkUtils.isNetworkConnected(context)) {

                                if (fragment != null) //If called from Connected devices fragment
                                {
                                    fragment.blockUnblock(pos, blockedStatus, time_limit, start_hour, end_hour, "no");
                                }
                                else if (frag != null) //If called from Disconnected devices fragment
                                {
                                    frag.blockUnblock(pos, blockedStatus, time_limit, start_hour, end_hour, "no");
                                }

                                alert.dismiss();

                            } else  {

                                Toast.makeText(context, context.getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                            }
                    }
                }
        );

        alert1.setNegativeButton(
                "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alert.dismiss();
                        holder.switchToggle.setChecked(false);
                    }
                }
        );

        alert = alert1.create();
        alert.show();
    }
}

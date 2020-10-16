package com.airtel.xstreamfiber.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airtel.xstreamfiber.Fragment.ParentalControlBlockedDevice;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.model.BlockedDevice;

import java.util.ArrayList;

//For showing / inflating views in Parental Control Blocked devices
public class ParentalControlBlockedDevices extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    ArrayList<BlockedDevice> arrayListBlock;
    ParentalControlBlockedDevice fragment;
    private String blockedStatus = "0";

    public ParentalControlBlockedDevices(Context context, ArrayList<BlockedDevice> arrayListBlock, ParentalControlBlockedDevice fragment) {
        this.context = context;
        this.arrayListBlock = arrayListBlock;
        this.fragment = fragment;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_parental_control_blocked_devices, parent, false);
        return new ParentalControlBlockedDevices.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ParentalControlBlockedDevices.ViewHolder viewHolder = (ParentalControlBlockedDevices.ViewHolder) holder;
        setData(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return arrayListBlock.size();
    }

    private void setData(ParentalControlBlockedDevices.ViewHolder holder, int pos) {

        if (arrayListBlock.get(pos).getPermanent_disabled().equals("yes")) {

            holder.tv_name.setText(arrayListBlock.get(pos).getDevice_name());
            holder.tv_time_limit.setText("Permanently Disabled");
            holder.tvValTimeLimit.setVisibility(View.GONE);
           // holder.tvValTimeLimit.setText(arrayListBlock.get(pos).getPermanent_disabled());
            holder.iv_devices.setImageResource(R.drawable.ic_block);
            holder.rlTime.setVisibility(View.GONE);

        }
        else if (arrayListBlock.get(pos).getPermanent_disabled().equals("no")) {

            holder.tv_name.setText(arrayListBlock.get(pos).getDevice_name());

            if (arrayListBlock.get(pos).getTime_limit().equals("0")) {

                holder.tv_time_limit.setText("Time Limit : ");
                holder.tvValTimeLimit.setText(arrayListBlock.get(pos).getTime_limit());
            }
            else if (arrayListBlock.get(pos).getTime_limit().equals("1")) {

                holder.tv_time_limit.setText("Time Limit : ");
                holder.tvValTimeLimit.setText(arrayListBlock.get(pos).getTime_limit());
            }
            else {
                holder.tv_time_limit.setVisibility(View.GONE);
                holder.tvValTimeLimit.setVisibility(View.GONE);
            }
            holder.rlTime.setVisibility(View.VISIBLE);
            holder.tv_start_time.setText(arrayListBlock.get(pos).getStart_time() + ":00");
            holder.tv_end_time.setText(arrayListBlock.get(pos).getEnd_time() + ":00");
            holder.iv_devices.setImageResource(R.drawable.ic_block);
        }

        holder.switchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blockedStatus = "0";

                } else {
                    blockedStatus = "1";
                }

                if (NetworkUtils.isNetworkConnected(context)) {

                    fragment.blockUnblock(pos, blockedStatus);
                } else  {
                    Toast.makeText(context, context.getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Switch switchToggle;
        private TextView tv_name, tv_disable_status, tv_start_time, tv_end_time, tvValTimeLimit, tv_time_limit;
        private ImageView iv_devices;
        private RelativeLayout rlPermanentDisabled, rlTime;
        private LinearLayout llTemproryDisabled;

        ViewHolder(View itemView) {
            super(itemView);
            switchToggle = itemView.findViewById(R.id.switch_toggle);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_devices = itemView.findViewById(R.id.iv_devices);
            rlPermanentDisabled = itemView.findViewById(R.id.rlPermanentDisabled);
            llTemproryDisabled = itemView.findViewById(R.id.llTemproryDisabled);
            tv_disable_status = itemView.findViewById(R.id.tv_disable_status);
            tv_start_time = itemView.findViewById(R.id.tv_start_time);
            tv_end_time = itemView.findViewById(R.id.tv_end_time);
            tvValTimeLimit = itemView.findViewById(R.id.tvValtimeLimit);
            tv_time_limit = itemView.findViewById(R.id.tv_time_limit);
            rlTime = itemView.findViewById(R.id.rlTime);

        }
    }
}

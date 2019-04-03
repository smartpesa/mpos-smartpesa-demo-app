package com.smartpesa.smartpesa.adapters;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.util.DateUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import smartpesa.sdk.models.device.Device;

public class DevicesAdapter extends ArrayAdapter<Device> {

    public DevicesAdapter(Context context, int resource, List<Device> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_settings, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Device deviceInfo = getItem(position);

        holder.deviceIDTV.setText(getContext().getString(R.string.device_name, deviceInfo.getSerialNumber()));
        holder.hardwareVersionTV.setText(getContext().getString(R.string.device_hardware, deviceInfo.getHardwareVersion()));
        holder.firmwareVersionTV.setText(getContext().getString(R.string.device_firmware, deviceInfo.getFirmwareVersion()));

        if (deviceInfo.getLastSeen() != null) {
            //convert date to date and time
            String dateString = DateUtils.format(deviceInfo.getLastSeen(), DateUtils.DATE_FORMAT_DD_MM_YYYY);
            holder.lastUsedDateTV.setText(getContext().getString(R.string.device_last_used, dateString));
        } else {
            holder.lastUsedDateTV.setText("");
        }

        return convertView;
    }

    public static class ViewHolder {
        @BindView(R.id.deviceIDTV) TextView deviceIDTV;
        @BindView(R.id.hardwareVersionTV) TextView hardwareVersionTV;
        @BindView(R.id.firmwareVersionTV) TextView firmwareVersionTV;
        @BindView(R.id.lastUsedDateTV) TextView lastUsedDateTV;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

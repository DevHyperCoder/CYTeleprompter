package com.codeyard.teleprompter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;

class CustomAdapterTwoTextViews extends ArrayAdapter<DataModel> {

    private final Context mContext;
    private int lastPosition = -1;

    CustomAdapterTwoTextViews(List<DataModel> data, Context context) {
        super(context, R.layout.row_item, data);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        DataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtDate = convertView.findViewById(R.id._date);
            viewHolder.txtName = convertView.findViewById(R.id._name);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        if (dataModel != null) {
            viewHolder.txtDate.setText(dataModel.getDate());
        }
        if (dataModel != null) {
            viewHolder.txtName.setText(dataModel.getName());
        }
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        //Add the items
        TextView txtDate;
        TextView txtName;
    }
}
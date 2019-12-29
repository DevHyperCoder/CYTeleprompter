package com.codeyard.teleprompter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

class CustomAdapterTextViewAndCheckbox extends ArrayAdapter<UploadCheckboxModel> {
    private final Context mContext;
    private ArrayList<String> selectedStrings = new ArrayList<>();
    private int lastPosition = -1;

    CustomAdapterTextViewAndCheckbox(List<UploadCheckboxModel> data, Context context) {
        super(context, R.layout.upload_checkbox_row_item, data);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        UploadCheckboxModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.upload_checkbox_row_item, parent, false);
            viewHolder.checkBox = convertView.findViewById(R.id.checkbox_row_item);

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
            viewHolder.checkBox.setText(dataModel.getText());
        }

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedStrings.add(viewHolder.checkBox.getText().toString());
                } else {
                    selectedStrings.remove(viewHolder.checkBox.getText().toString());
                }

            }
        });


        // Return the completed view to render on screen
        return convertView;
    }

    ArrayList<String> getSelectedString() {
        return selectedStrings;
    }

    // View lookup cache
    private static class ViewHolder {
        //Add the items
        MaterialCheckBox checkBox;
    }
}
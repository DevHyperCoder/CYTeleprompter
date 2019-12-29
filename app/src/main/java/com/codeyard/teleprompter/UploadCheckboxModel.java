package com.codeyard.teleprompter;

class UploadCheckboxModel {
    private boolean isChecked;
    private String text;

    UploadCheckboxModel(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;

    }

    String getText() {
        return text;
    }

    void setText(String text) {
        text = text;
    }

    boolean getChecked() {
        return isChecked;
    }

    void setChecked(boolean checked) {
        isChecked = checked;
    }
}
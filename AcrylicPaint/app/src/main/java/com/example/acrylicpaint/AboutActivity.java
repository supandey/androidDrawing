package com.example.acrylicpaint;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Spanned htmlText = Html.fromHtml(getResources().getString(R.string.about_description));
        TextView aboutTextView = (TextView) findViewById(R.id.aboutTextView);
        aboutTextView.setText(htmlText);
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

package ru.repetitor_anglijskogo.whatdoesitmeaninrussian;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    int i_W, i_H;

    LinearLayout linear_Main;

    LinearLayout linear_ResetRating;
    CheckBox checkbox_ResetRating;

    LinearLayout linear_Buttons;
    Button button_Ok, button_Cancel;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        i_W = getIntent().getExtras().getInt(getResources().getString(R.string.intent_parameter_screen_available_w));
        i_H = getIntent().getExtras().getInt(getResources().getString(R.string.intent_parameter_screen_available_h));

        linear_Main = (LinearLayout) findViewById(R.id.linearMain);

        int iResetRatingH = i_H / 10;
        linear_ResetRating = new LinearLayout(this);
        LinearLayout.LayoutParams lparamsResetRating = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lparamsResetRating.setMargins(4, 4, 4, 4);
        linear_ResetRating.setOrientation(LinearLayout.HORIZONTAL);
        linear_ResetRating.setLayoutParams(lparamsResetRating);
        linear_Main.addView(linear_ResetRating);

        checkbox_ResetRating = new CheckBox(this);
        checkbox_ResetRating.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        checkbox_ResetRating.setText(R.string.settings_reset_rating);
        checkbox_ResetRating.setTextSize(TypedValue.COMPLEX_UNIT_PX, (2.0f*iResetRatingH) / 5.0f);
        linear_ResetRating.addView(checkbox_ResetRating);

        int iButtonsH = i_H / 10;
        linear_Buttons = new LinearLayout(this);
        LinearLayout.LayoutParams lparamsButtons = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, iButtonsH);
        lparamsButtons.setMargins(4, 4, 4, 4);
        linear_Buttons.setOrientation(LinearLayout.HORIZONTAL);
        linear_Buttons.setLayoutParams(lparamsButtons);
        linear_Main.addView(linear_Buttons);

        int iButtonW = i_W / 3;
        float fButtonText = (1.0f*iButtonsH)/2.0f;

        LinearLayout.LayoutParams lparamsButton = new LinearLayout.LayoutParams( iButtonW, LinearLayout.LayoutParams.MATCH_PARENT );
        lparamsButton.setMargins( 4, 4, 4, 4 );

        button_Ok = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Ok.setLayoutParams(lparamsButton);
        button_Ok.setText(getResources().getString(R.string.action_ok));
        button_Ok.setTextSize(TypedValue.COMPLEX_UNIT_PX, fButtonText);
        button_Ok.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnOk();
            }
        });
        linear_Buttons.addView(button_Ok);

        button_Cancel = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Cancel.setLayoutParams(lparamsButton);
        button_Cancel.setText(getResources().getString(R.string.action_cancel));
        button_Cancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fButtonText);
        button_Cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnCancel();
            }
        });
        linear_Buttons.addView(button_Cancel);
    }

    public void fnOk(){
        if( checkbox_ResetRating.isChecked() ) {
            SharedPreferences oPreferences = getSharedPreferences(getResources().getString(R.string.preferences_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editorPrefs = oPreferences.edit();
            editorPrefs.putInt( getResources().getString(R.string.preferences_rating), getResources().getInteger(R.integer.default_user_rating) );
            editorPrefs.putInt( getResources().getString(R.string.preferences_ncorrect), 0 );
            editorPrefs.putInt( getResources().getString(R.string.preferences_ntotal), 0 );
            editorPrefs.apply();
        }
        finish();
    }

    public void fnCancel() {
        finish();
    }

}

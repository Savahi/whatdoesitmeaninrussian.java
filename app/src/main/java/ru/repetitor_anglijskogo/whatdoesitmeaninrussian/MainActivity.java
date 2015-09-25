package ru.repetitor_anglijskogo.whatdoesitmeaninrussian;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class MainActivity extends Activity {

    Button button_Game, button_Help, button_Settings, button_Exit;

    LinearLayout linear_Main;

    int i_ScreenH, i_ScreenW;
    int i_ScreenAvailableW, i_ScreenAvailableH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linear_Main = (LinearLayout) findViewById(R.id.linearMain);
        linear_Main.post(new Runnable() {
            public void run() {

                Rect oRect = new Rect();
                Window oWindow = getWindow();  // Get the Window
                int iViewTop = oWindow.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                DisplayMetrics oMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(oMetrics);
                i_ScreenH = oMetrics.heightPixels;
                i_ScreenW = oMetrics.widthPixels;

                i_ScreenAvailableH = i_ScreenH - iViewTop - 2*getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
                i_ScreenAvailableW = i_ScreenW - 2*getResources().getDimensionPixelSize( R.dimen.activity_horizontal_margin);

                fnInitControls();

                fnDisplayImage();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            fnSettings(null);
            return true;
        }

        if (id == R.id.action_exit) {
            fnExit(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fnInitControls() {
        int iButtonW = i_ScreenW/2;
        int iButtonH = i_ScreenAvailableH/7;
        float fButtonText = (2.0f*(float)iButtonH)/6.0f;
        LinearLayout.LayoutParams layoutparamsButton = new LinearLayout.LayoutParams( iButtonW, iButtonH );
        layoutparamsButton.setMargins(2,2,2,2);

        //button_Game = new Button( getApplicationContext(), null, R.style.ButtonCustomStyle );
        button_Game = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Game.setLayoutParams(layoutparamsButton);
        button_Game.setText(getResources().getString(R.string.action_game));
        button_Game.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) fButtonText);
        button_Game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnGame(view);
            }
        });
        linear_Main.addView( button_Game );

        button_Help = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Help.setLayoutParams(layoutparamsButton);
        button_Help.setText(getResources().getString(R.string.action_help));
        button_Help.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) fButtonText);
        button_Help.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnHelp( view );
            }
        });
        linear_Main.addView( button_Help );

        button_Settings = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Settings.setLayoutParams(layoutparamsButton);
        button_Settings.setText(getResources().getString(R.string.action_settings));
        button_Settings.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) fButtonText);
        button_Settings.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnSettings( view );
            }
        });
        linear_Main.addView( button_Settings );
/*
        button_Exit = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Exit.setLayoutParams(layoutparamsButton);
        button_Exit.setText(getResources().getString(R.string.action_exit));
        button_Exit.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) fButtonText);
        button_Exit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnExit( view );
            }
        });
        linear_Main.addView( button_Exit );
*/
    }

    public void fnDisplayImage() {
        int iImageH = (4*i_ScreenAvailableH)/10;
        int iImageW = (iImageH*320)/240;
        FrameLayout.LayoutParams lparamsFramePromo = new FrameLayout.LayoutParams( iImageW, iImageH );
        lparamsFramePromo.setMargins( 0,i_ScreenAvailableH/20,0,0 );
        FrameLayout framePromo = new FrameLayout( this );
        framePromo.setLayoutParams( lparamsFramePromo );
        framePromo.setPadding(0,0,0,0);
        linear_Main.addView( framePromo );

        ImageView imageviewPromo = new ImageView(this);
        imageviewPromo.setImageResource( R.drawable.promo_320x240 );
        imageviewPromo.setScaleType(ImageView.ScaleType.FIT_XY);
        framePromo.addView( imageviewPromo );
    }

    public void fnGame( View view ) {
        Intent intentGame = new Intent( MainActivity.this, GameActivity.class );
        intentGame.putExtra( getResources().getString(R.string.intent_parameter_screen_available_w), i_ScreenAvailableW );
        intentGame.putExtra( getResources().getString(R.string.intent_parameter_screen_available_h), i_ScreenAvailableH );
        startActivity(intentGame);
    }

    public void fnHelp ( View view ) {
        Intent intentHelp = new Intent( MainActivity.this, HelpActivity.class );
        startActivity(intentHelp);
    }

    public void fnSettings( View view ) {
        Intent intentSettings = new Intent( MainActivity.this, SettingsActivity.class );
        intentSettings.putExtra( getResources().getString(R.string.intent_parameter_screen_available_w), i_ScreenAvailableW );
        intentSettings.putExtra( getResources().getString(R.string.intent_parameter_screen_available_h), i_ScreenAvailableH );
        startActivity(intentSettings);
    }

    public void fnExit( View view ) {
        android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(0);
    }
}

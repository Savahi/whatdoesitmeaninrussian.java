package ru.repetitor_anglijskogo.whatdoesitmeaninrussian;

import android.app.Activity;
import android.os.Bundle;

public class HelpActivity extends Activity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }
}

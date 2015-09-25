package ru.repetitor_anglijskogo.whatdoesitmeaninrussian;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class GameActivity extends Activity {

    int n_Matches=5;
    int n_WrongMatches = n_Matches - 1;

    public static final String s_MatcherURL = "http://www.repetitor-anglijskogo.ru/cgi-bin/matcher/matcher_receivequestion.cgi";

    public static final String s_MatcherCategory = "matcher_enru";

    public static final String s_ReportAnswerURL = "http://www.repetitor-anglijskogo.ru/cgi-bin/matcher/matcher_updatequestionrating.cgi";

    public static final String s_EntryTag="<entry>", s_MatchTag="<match>", s_MatchIdTag="<matchid>", s_MatchRatingTag="<matchrating>",
                               s_WrongMatchesTag="<wrongmatches>", s_MatchDetailsTag="<matchdetails>";

    int i_InitialRating;

    int i_W, i_H;

    LinearLayout linear_Main;
    TextView textview_UserInfo;

    FrameLayout frame_Controls;
    Button button_Next;
    TextView textview_Question;

    FrameLayout frame_Answer;
    LinearLayout linear_Answer;
    TextView textview_Loading;

    SharedPreferences o_Preferences;

    int i_UserRating;
    int i_UserNCorrect;
    int i_UserNTotal;

    int i_MatchCorrect;
    int i_MatchRating;
    int i_MatchId;
    int i_MatchClicked;
    StringBuffer s_MatchDetails = new StringBuffer("");

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        i_InitialRating = getResources().getInteger(R.integer.default_user_rating);

        o_Preferences = getSharedPreferences(getResources().getString(R.string.preferences_file), Context.MODE_PRIVATE);

        i_W = getIntent().getExtras().getInt(getResources().getString(R.string.intent_parameter_screen_available_w));
        i_H = getIntent().getExtras().getInt(getResources().getString(R.string.intent_parameter_screen_available_h));

        linear_Main = (LinearLayout)findViewById( R.id.linearMain );

        int iUserInfoH = i_H / 20;
        textview_UserInfo = new TextView( getApplicationContext() );
        textview_UserInfo.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, iUserInfoH) );
        textview_UserInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, (2.0f * (float) iUserInfoH) / 3.0f);
        textview_UserInfo.setTextColor(getResources().getColor(R.color.text_user_info));
        linear_Main.addView(textview_UserInfo);
        fnUserInfoRead();
        fnUserInfoDisplay();

        int iFrameControlsPadding = 2;
        int iFrameControlsH = (int)((i_H * 1.25) / 10);
        frame_Controls = new FrameLayout( this );
        frame_Controls.setLayoutParams( new LinearLayout.LayoutParams( i_W, iFrameControlsH) );
        frame_Controls.setPadding( iFrameControlsPadding,iFrameControlsPadding,iFrameControlsPadding,iFrameControlsPadding );
        linear_Main.addView( frame_Controls );

        button_Next = (Button)getLayoutInflater().inflate(R.layout.button_customstyle, null);
        button_Next.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT) );
        button_Next.setText(getResources().getString(R.string.action_next));
        button_Next.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (1.0 * (float) (iFrameControlsH - iFrameControlsPadding * 2)) / 2.0f);
        button_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fnLoadNextQuestion();
            }
        });
        frame_Controls.addView(button_Next);

        int iQuestionH = i_H / 10;
        int iQuestionPadding = 2;
        textview_Question = new TextView(this);
        textview_Question.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, iQuestionH ) );
        textview_Question.setTextSize(TypedValue.COMPLEX_UNIT_PX, (1.5f * (float) (iQuestionH - iQuestionPadding * 2)) / 2.0f);
        textview_Question.setTextColor( getResources().getColor( R.color.text_question) );
        textview_Question.setPadding(iQuestionPadding, iQuestionPadding, iQuestionPadding, iQuestionPadding);
        textview_Question.setText("");
        linear_Main.addView( textview_Question );

        int iFrameAnswerHMargin = getResources().getDimensionPixelSize(R.dimen.frameanswer_hmargin);
        int iFrameAnswerVMargin = getResources().getDimensionPixelSize(R.dimen.frameanswer_vmargin);
        int iFrameAnswerH = i_H * 5 / 10;
        FrameLayout.LayoutParams lparamsFrameAnswer = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, iFrameAnswerH);
        lparamsFrameAnswer.setMargins( iFrameAnswerHMargin, iFrameAnswerVMargin, iFrameAnswerHMargin, iFrameAnswerVMargin );
        frame_Answer = new FrameLayout( this );
        frame_Answer.setLayoutParams( lparamsFrameAnswer );
        frame_Answer.setPadding( iFrameControlsPadding, iFrameControlsPadding, iFrameControlsPadding, iFrameControlsPadding );
        linear_Main.addView( frame_Answer );

        linear_Answer = new LinearLayout( this );
        linear_Answer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linear_Answer.setGravity( Gravity.CENTER_VERTICAL );
        linear_Answer.setOrientation( LinearLayout.VERTICAL );
        frame_Answer.addView(linear_Answer);

        int iMatchHMargin = getResources().getDimensionPixelSize(R.dimen.match_hmargin);
        int iMatchVMargin = getResources().getDimensionPixelSize(R.dimen.match_vmargin);
        int iMatchH = (iFrameAnswerH -iMatchVMargin*2*n_Matches) / n_Matches;

        LinearLayout.LayoutParams lparamsMatch = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, iMatchH );
        lparamsMatch.setMargins( iMatchHMargin, iMatchVMargin, iMatchHMargin, iMatchVMargin );

        int iMatchBoxMargin = getResources().getDimensionPixelSize(R.dimen.match_boxmargin);

        FrameLayout.LayoutParams lparamsMatchBox = new FrameLayout.LayoutParams(iMatchH,iMatchH);
        lparamsMatchBox.setMargins( 0, 0, 0, 0 );
        
        HorizontalScrollView.LayoutParams lparamsMatchHorizontal = new HorizontalScrollView.LayoutParams( HorizontalScrollView.LayoutParams.MATCH_PARENT, iMatchH );
        lparamsMatchHorizontal.setMargins( 0, 0, 0, 0 );

        for( int i = 0 ; i < n_Matches ; i++ ) {
            LinearLayout linearMatch = new LinearLayout(this);
            linearMatch.setLayoutParams( lparamsMatch );
            linearMatch.setOrientation( LinearLayout.HORIZONTAL );
            linear_Answer.addView(linearMatch);

            FrameLayout frameMatchBox = new FrameLayout(this);
            frameMatchBox.setLayoutParams( lparamsMatchBox );
            frameMatchBox.setBackgroundResource(R.drawable.match);
            frameMatchBox.setClickable(true);
            frameMatchBox.setTag(new MyMatchTag(i));
            frameMatchBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fnDoMatchClick(view);
                }
            });
            linearMatch.addView(frameMatchBox);

            HorizontalScrollView horizontalMatch = new HorizontalScrollView(this);
            horizontalMatch.setLayoutParams( lparamsMatchHorizontal );
            horizontalMatch.setPadding( iMatchBoxMargin, 0, 0, 0 );
            linearMatch.addView(horizontalMatch);

            TextView textviewMatch = new TextView( this );
            textviewMatch.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, iMatchH ) );
            textviewMatch.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            textviewMatch.setSingleLine();
            textviewMatch.setClickable(true);
            textviewMatch.setPadding( 0,0,0,0 );
            //textviewMatch.setHorizontallyScrolling(true);
            //textviewMatch.setFocusable(true);
            //textviewMatch.setFocusableInTouchMode(true);
            textviewMatch.setTextSize(TypedValue.COMPLEX_UNIT_PX, (1.0f * (float) (iMatchH)) / 2.0f);
            textviewMatch.setTextColor(getResources().getColor(R.color.text_match));
            //textviewMatch.setBackgroundResource(R.drawable.match);
            //textviewMatch.setTag( new MyMatchTag( i ) );
            //textviewMatch.setOnClickListener(new View.OnClickListener() {
            //    @Override
            //    public void onClick(View view) {
            //        fnDoMatchClick(view);
            //    }
            //});
            //linear_Answer.addView( textviewMatch );
            horizontalMatch.addView(textviewMatch);
        }
        linear_Answer.setVisibility( View.INVISIBLE );

        textview_Loading = new TextView( getApplicationContext() );
        textview_Loading.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT) );
        textview_Loading.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textview_Loading.setTextSize( TypedValue.COMPLEX_UNIT_PX, ((float)iFrameAnswerH)/10.0f );
        textview_Loading.setText("");
        textview_Loading.setVisibility( View.VISIBLE );
        frame_Answer.addView( textview_Loading );

        fnLoadNextQuestion();
    }

    public void fnLoadNextQuestion() {
        fnButtonNextDisable();
        linear_Answer.setVisibility( View.INVISIBLE );
        for( int i = 0 ; i < linear_Answer.getChildCount() ; i++ ) {
            LinearLayout linearMatch = (LinearLayout)linear_Answer.getChildAt(i);

            FrameLayout frameMatchBox = (FrameLayout)linearMatch.getChildAt(0);
            frameMatchBox.setBackgroundResource( R.drawable.match );

            HorizontalScrollView horizontalMatch = (HorizontalScrollView)linearMatch.getChildAt(1);
            TextView textviewMatch = (TextView)horizontalMatch.getChildAt(0);
            textviewMatch.setText("");
        }
        textview_Loading.setText( R.string.status_waiting_next );
        textview_Loading.setVisibility( View.VISIBLE );
        textview_Question.setText("");

        MyNextQuestionAsynkTask oNextQuestionAsynkTask = new MyNextQuestionAsynkTask();
        oNextQuestionAsynkTask.execute();
    }

    public void fnUserInfoRead() {
        i_UserRating = o_Preferences.getInt( getResources().getString(R.string.preferences_rating), i_InitialRating );
        i_UserNCorrect = o_Preferences.getInt( getResources().getString(R.string.preferences_ncorrect), 0 );
        i_UserNTotal = o_Preferences.getInt( getResources().getString(R.string.preferences_ntotal), 0 );
    }

    public void fnUserInfoDisplay() {
        String sFormat = getResources().getString( R.string.user_info );
        String sInfo = String.format( sFormat, i_UserRating, i_UserNCorrect, i_UserNTotal );
        textview_UserInfo.setText( sInfo );
    }

    public void fnButtonNextDisable() {
        button_Next.setEnabled( false );
        button_Next.setTextColor( getResources().getColor(R.color.button_disabled_text_color) );
        button_Next.setShadowLayer( 0, 0, 0, 0xFF000000 );
    }

    public void fnButtonNextEnable() {
        button_Next.setEnabled( true );
        button_Next.setTextColor( getResources().getColor(R.color.button_text_color) );
        button_Next.setShadowLayer( getResources().getInteger(R.integer.button_shadow_dx), getResources().getInteger(R.integer.button_shadow_dy),
                                    getResources().getInteger(R.integer.button_shadow_radius), getResources().getColor(R.color.button_shadow_color) );
    }

    public void fnDoMatchClick( View viewClicked ) {

        FrameLayout frameClicked = (FrameLayout) viewClicked;
        MyMatchTag oTag = (MyMatchTag)frameClicked.getTag();
        int iClicked = oTag.iOrderNumber;

        if( i_MatchClicked != -1 ) {
            return;
        }
        i_MatchClicked = iClicked;

        // Highlighting correct choice
        FrameLayout frameCorrect = (FrameLayout)((LinearLayout)linear_Answer.getChildAt(i_MatchCorrect)).getChildAt(0);
        frameCorrect.setBackgroundResource(R.drawable.match_correct);

        if( iClicked != i_MatchCorrect ) {
            FrameLayout FrameIncorrect = (FrameLayout)((LinearLayout)linear_Answer.getChildAt(iClicked)).getChildAt(0);
            FrameIncorrect.setBackgroundResource(R.drawable.match_incorrect);
        }

        int iScore;
        if( iClicked == i_MatchCorrect ) {
            iScore = 10;
            i_UserNCorrect += 1;
        } else {
            iScore = 0;
        }
        i_UserNTotal++;

        if( i_MatchId != -1 ) {
            MyReportAnswerAsynkTask oReportAnswerAsynkTask = new MyReportAnswerAsynkTask();
            oReportAnswerAsynkTask.setScore( iScore );
            oReportAnswerAsynkTask.execute();
        }

        int iRatingChange = fnCalcRatingChange( i_UserRating, i_MatchRating, iScore );
        int iNewRating = i_UserRating + iRatingChange;
        if( iNewRating < 0 ) {
            iNewRating = 0;
        }
        i_UserRating = iNewRating;

        SharedPreferences.Editor editorPrefs = o_Preferences.edit();
        editorPrefs.putInt( getResources().getString(R.string.preferences_rating), i_UserRating );
        editorPrefs.putInt( getResources().getString(R.string.preferences_ncorrect), i_UserNCorrect );
        editorPrefs.putInt( getResources().getString(R.string.preferences_ntotal), i_UserNTotal );
        editorPrefs.apply();

        StringBuffer sToast = new StringBuffer();
        if( iRatingChange > 0 ) {
            sToast.append( getResources().getString(R.string.user_succeeded) );
            sToast.append( "! :) +" );
            sToast.append( iRatingChange );
        } else {
            sToast.append( getResources().getString(R.string.user_failed) );
            sToast.append( "... :( " );
            sToast.append( iRatingChange );
        }
        Toast.makeText(getApplicationContext(), sToast, Toast.LENGTH_SHORT).show();

        fnUserInfoDisplay();

        fnButtonNextEnable();
    }

    public int fnCalcRatingChange( int iUserRating, int iMatchRating, int iUserScore ) {
        double fUserScore, fUserScoreExp;
        double fUserRatingInc;

        if( !(iUserRating >= 0) ) {
            iUserRating = i_InitialRating;
        }
        if( !(iMatchRating >= 0) ) {
            iMatchRating = i_InitialRating;
        }

        if (iUserRating - iMatchRating > 500.0) {
            fUserScoreExp = 1.0;
        } else {
            if (iMatchRating - iUserRating > 500.0) {
                fUserScoreExp = 0.0;
            } else {
                fUserScoreExp = 1.0 / (1.0 + Math.pow( 10.0, ((iMatchRating - iUserRating) / 500.0)));
            }
        }

        fUserScore = (double)iUserScore / 10.0;
        fUserRatingInc = 25.0 * (fUserScore - fUserScoreExp);
        return (int)(fUserRatingInc + 0.5);
    }


    public class MyMatchTag {
        public Boolean bSelected;
        public int iOrderNumber;

        public MyMatchTag( int iSetOrderNumber ) {
            bSelected = false;
            iOrderNumber = iSetOrderNumber;
        }
    }

    class MyNextQuestionAsynkTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpClient oHttpClient = new DefaultHttpClient();
            HttpGet oHttpGet = new HttpGet( s_MatcherURL + "?userrating=" + i_UserRating + "&category=" + s_MatcherCategory );
            try {

                HttpResponse oHttpResponse = oHttpClient.execute( oHttpGet );

                InputStream oInputStream = oHttpResponse.getEntity().getContent();
                InputStreamReader oInputStreamReader = new InputStreamReader( oInputStream );
                BufferedReader oBufferedReader = new BufferedReader( oInputStreamReader );
                StringBuilder oStringBuilder = new StringBuilder();
                String sChunk;

                while( (sChunk = oBufferedReader.readLine()) != null ) {
                    oStringBuilder.append(sChunk);
                    oStringBuilder.append("\n");
                }
                return oStringBuilder.toString();

            }
            catch( ClientProtocolException e ) {
                e.printStackTrace();
            }
            catch( IOException e ) {
                e.printStackTrace(); }

            return null;
        }

        @Override
        protected void onPostExecute(String sReceived ) {
            super.onPostExecute( sReceived );

            if( sReceived == null ) {
                fnLoadingFailed();
                return;
            }

            int iEntryStart = sReceived.indexOf( s_EntryTag );
            int iEntryEnd = sReceived.indexOf("<", iEntryStart + s_EntryTag.length() );
            if( iEntryStart == -1 || !(iEntryEnd >= iEntryStart) ) {
                fnLoadingFailed();
                return;
            }
            iEntryStart += s_EntryTag.length();
            int iMatchIdStart = sReceived.indexOf( s_MatchIdTag );
            int iMatchIdEnd = sReceived.indexOf("<", iMatchIdStart + s_MatchIdTag.length() );
            if( iMatchIdStart == -1 || !(iMatchIdEnd >= iMatchIdStart) ) {
                fnLoadingFailed();
                return;
            }
            iMatchIdStart += s_MatchIdTag.length();
            int iMatchStart = sReceived.indexOf( s_MatchTag );
            int iMatchEnd = sReceived.indexOf("<", iMatchStart + s_MatchTag.length() );
            if( iMatchStart == -1 || !(iMatchEnd >= iMatchStart) ) {
                fnLoadingFailed();
                return;
            }
            iMatchStart += s_MatchTag.length();
            int iMatchRatingStart = sReceived.indexOf( s_MatchRatingTag );
            int iMatchRatingEnd = sReceived.indexOf("<", iMatchRatingStart + s_MatchRatingTag.length() );
            if( iMatchRatingStart == -1 || !(iMatchRatingEnd >= iMatchRatingStart) ) {
                fnLoadingFailed();
                return;
            }
            iMatchRatingStart += s_MatchRatingTag.length();
            int iWrongMatchesStart = sReceived.indexOf( s_WrongMatchesTag );
            int iWrongMatchesEnd = sReceived.indexOf("<", iWrongMatchesStart + s_WrongMatchesTag.length() );
            if( iWrongMatchesStart == -1 || !(iWrongMatchesEnd >= iWrongMatchesStart) ) {
                fnLoadingFailed();
                return;
            }
            iWrongMatchesStart += s_WrongMatchesTag.length();
            int iMatchDetailsStart = sReceived.indexOf( s_MatchDetailsTag );
            int iMatchDetailsEnd = sReceived.indexOf("<", iMatchDetailsStart + s_MatchDetailsTag.length() );
            if( iMatchDetailsStart == -1 || !(iMatchDetailsEnd >= iMatchDetailsStart) ) {
                fnLoadingFailed();
                return;
            }
            iMatchDetailsStart += s_MatchDetailsTag.length();

            String sEntry = sReceived.substring( iEntryStart, iEntryEnd );
            String sMatch = sReceived.substring( iMatchStart, iMatchEnd );
            String sMatchId = sReceived.substring( iMatchIdStart, iMatchIdEnd );
            String sMatchRating = sReceived.substring( iMatchRatingStart, iMatchRatingEnd );
            String sWrongMatches = sReceived.substring( iWrongMatchesStart, iWrongMatchesEnd );
            s_MatchDetails.setLength(0);
            s_MatchDetails.append( sReceived.substring( iMatchDetailsStart, iMatchDetailsEnd ) );

            try {
                i_MatchRating = Integer.valueOf(sMatchRating);
            } catch( NumberFormatException e ) {
                i_MatchRating = i_InitialRating;
            }

            try {
                i_MatchId = Integer.valueOf(sMatchId);
            } catch( NumberFormatException e ) {
                i_MatchId = -1;
            }

            String asWrongMatches[] = sWrongMatches.split("\\|\\|");
            int nWrongMatches = asWrongMatches.length;
            if( nWrongMatches > n_WrongMatches ) {
                nWrongMatches = n_WrongMatches;
            } else {
                if (nWrongMatches < n_WrongMatches) {
                    fnLoadingFailed();
                    return;
                }
            }

            Random oRandom = new Random();
            i_MatchCorrect = oRandom.nextInt(nWrongMatches + 1);

            HorizontalScrollView horizontalCorrect = (HorizontalScrollView)((LinearLayout)linear_Answer.getChildAt(i_MatchCorrect)).getChildAt(1);
            TextView textviewCorrect = (TextView)horizontalCorrect.getChildAt(0);
            textviewCorrect.setText(sMatch);

            for( int i = 0, iWrongMatchIndex = 0 ; i < linear_Answer.getChildCount() ; i++ ) {
                if( i == i_MatchCorrect ) {
                    continue;
                }
                HorizontalScrollView horizontalIncorrect = (HorizontalScrollView)((LinearLayout)linear_Answer.getChildAt(i)).getChildAt(1);
                TextView textviewIncorrect = (TextView)horizontalIncorrect.getChildAt(0);
                textviewIncorrect.setText( asWrongMatches[iWrongMatchIndex]);
                iWrongMatchIndex++;
            }

            i_MatchClicked = -1;

            textview_Loading.setText("");
            textview_Loading.setVisibility(View.INVISIBLE);

            textview_Question.setText( sEntry );

            linear_Answer.setVisibility(View.VISIBLE);

            fnButtonNextDisable();
        }
    }

    public void fnLoadingFailed() {
        textview_Loading.setText(R.string.status_loading_failed);
        fnButtonNextEnable();
    }

    class MyReportAnswerAsynkTask extends AsyncTask<Void, Void, String> {

        int iScore;

        public void setScore( int iSetScore ) {
            iScore = iSetScore;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //textview_Loading.setText( R.string.stringStatusReportingAnswer );
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpClient oHttpClient = new DefaultHttpClient();

            String sRequestURL = s_ReportAnswerURL + "?userrating=" + i_UserRating + "&userscore=" + iScore + "&matchid=" + i_MatchId + "&matchrating=" + i_MatchRating + "&category=" + s_MatcherCategory;
            HttpGet oHttpGet = new HttpGet(sRequestURL);
            try {
                oHttpClient.execute(oHttpGet);
                return null;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String sReceived) {
            super.onPostExecute(sReceived);
            if (sReceived == null) {
                textview_Loading.setText("");
            }
        }
    }

    /*
    public void fnExit( View view ) {
        android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(0);
    }
    */
}

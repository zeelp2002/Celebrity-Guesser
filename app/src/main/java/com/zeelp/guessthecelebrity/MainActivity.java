package com.zeelp.guessthecelebrity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.widget.TextView;


public class MainActivity extends Activity {

    //global variables
    ImageView celebrityImageView;
    Button answerButton0;
    Button answerButton1;
    Button answerButton2;
    Button answerButton3;
    TextView scoreTextView;
    Button startButton;
    TextView timerTextView;
    TextView gameOverTextView;


    ArrayList<String> celebURLs = new ArrayList<String>(); // array list containing the URLS to get celebrity image
    ArrayList<String> celebNames = new ArrayList<String>(); // array lst containing celebrity names

    int chosenCeleb = 0;  // which celeb is chosen
    int locationOfCorrectAnswer = 0; // the location of the correct celebrity
    int rightAnswer; // the total number of right answers
    int totalAnswer; // the total number of questions completed
    String[] answers = new String[4];

    public void startFunction(View view){
        startButton.setVisibility(View.GONE);
        startButton.setClickable(false);

        answerButton0.setVisibility(View.VISIBLE);
        answerButton1.setVisibility(View.VISIBLE);
        answerButton2.setVisibility(View.VISIBLE);
        answerButton3.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.VISIBLE);
        celebrityImageView.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.VISIBLE);
        gameOverTextView.setVisibility(View.INVISIBLE);

        rightAnswer = 0;
        totalAnswer = 0;


        createNewQuestion();
        timerFunction();
    }


    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }

            return null;
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {

                e.printStackTrace();

            }

            return null;
        }
    }

    public void timerFunction(){
        new CountDownTimer(30100,1000){

            public void onTick(long milliUntilDone){
                String seconds = String.valueOf(milliUntilDone/1000);

                timerTextView.setText(seconds);
                timerTextView.setVisibility(View.VISIBLE);


            }

            public void onFinish(){
                gameOverFunction();

            }
        }.start();
    }



    public void gameOverFunction(){
        gameOverTextView.setVisibility(View.VISIBLE);

        gameOverTextView.setText("You scored " + Integer.toString(rightAnswer) + "/" + Integer.toString(totalAnswer));

        answerButton0.setVisibility(View.INVISIBLE);
        answerButton1.setVisibility(View.INVISIBLE);
        answerButton2.setVisibility(View.INVISIBLE);
        answerButton3.setVisibility(View.INVISIBLE);
        scoreTextView.setVisibility(View.INVISIBLE);
        celebrityImageView.setVisibility(View.INVISIBLE);
        timerTextView.setVisibility(View.INVISIBLE);

        startButton.setVisibility(View.VISIBLE);
        startButton.setClickable(true);


    }

    public void createNewQuestion() {

        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());

        ImageDownloader imageTask = new ImageDownloader();

        Bitmap celebImage;

        try {

            celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            celebrityImageView.setImageBitmap(celebImage);
            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {

                if (i == locationOfCorrectAnswer) {

                    answers[i] = celebNames.get(chosenCeleb);

                } else {

                    incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    while (incorrectAnswerLocation == chosenCeleb) {

                        incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            answerButton0.setText(answers[0]);
            answerButton1.setText(answers[1]);
            answerButton2.setText(answers[2]);
            answerButton3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checker(View view){

        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            rightAnswer++;
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        totalAnswer++;
        scoreTextView.setText(Integer.toString(rightAnswer) + "/" + Integer.toString(totalAnswer));

        createNewQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;

        try {

            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebNames.add(m.group(1));
            }


        } catch (InterruptedException e) {

            e.printStackTrace();

        } catch (ExecutionException e) {

            e.printStackTrace();

        }

        createNewQuestion();

        celebrityImageView = (ImageView) findViewById(R.id.celebrityImageView);
        answerButton0 = (Button) findViewById(R.id.answerButton0);
        answerButton1 = (Button) findViewById(R.id.answerButton1);
        answerButton2 = (Button) findViewById(R.id.answerButton2);
        answerButton3 = (Button) findViewById(R.id.answerButton3);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        startButton = (Button) findViewById(R.id.startButton);
        timerTextView = (TextView) findViewById(R.id.timerTextView);
        gameOverTextView = (TextView) findViewById(R.id.gameOverTextView);


    }
}
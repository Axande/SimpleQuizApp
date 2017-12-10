package com.example.andrei.quizapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

//CHANGE strings- move to @strings

public class MainActivity extends AppCompatActivity{

    private String name;                    //the name of the player
    boolean help;                           //boolean for help or not
    int level;                              //the level value
    int hp;                                 //hp during the game(3 at start, 0 = lose)
    int noQuestions;                        //the number of questions in the correct cathegory
    boolean[] use = new boolean[40];        //array of bool variables to keep track of used questions
    Questions[] Q = new Questions[40];      //save all questions
    int currentQuestion = 0;
    int answeredCorrect = 0;
    Random rand;                            //random variable to be used for random numbers
    int helpCount = 0;                      //according to the level you might be entitled for help

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main); //load the activity_main layout

        setContentView(R.layout.intro);


        new CountDownTimer(2500,500){
            @Override
            public void onTick(long millisUntilFinished){}

            @Override
            public void onFinish(){
                //set the new Content of your activity
                MainActivity.this.setContentView(R.layout.activity_main);
            }
        }.start();

        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // TODO: Your application init goes here.
                Intent mInHome = new Intent(MainActivity.this, MainActivity.class);
                MainActivity.this.startActivity(mInHome);
                MainActivity.this.finish();
            }
        }, 3000);*/

    }





    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        /* NOT USED - for further development in case it is needed*/
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        /* NOT USED - for further development in case it is needed*/
        super.onRestoreInstanceState(savedInstanceState);

    }


    public void send(View view){    //when the start button is pressed in activity_main.xml
        boolean fail = false;
        level = 0;

        //save the name
        EditText input = findViewById(R.id.name);
        name = input.getText().toString();
        if(name.length() == 0) fail = true;

        //save the level(radio buttons)
        if(((RadioButton)findViewById(R.id.beginner)).isChecked()) level = 1;
        else if (((RadioButton)findViewById(R.id.advanced)).isChecked()) level = 2;
        else if (((RadioButton)findViewById(R.id.master)).isChecked()) level = 3;
        if(level == 0) fail = true;

        //save the help preferences
        CheckBox cb = findViewById(R.id.help);
        help = cb.isChecked();
        if(help && level == 3)
            fail = true;

        //failing conditions
        if(fail){
            TextView err;
            if(name.length() == 0) { //name of length 0
                input.setHint(R.string.text_infoName);
                input.setText("");

                err = findViewById(R.id.error_name);
                err.setText(R.string.err1);             //display the error err1
                err.setVisibility(View.VISIBLE);
            } else{                                     //else, no error to display
                err = findViewById(R.id.error_name);
                err.setVisibility(View.GONE);
            }

            if(level == 0){                             //no level selected
                err = findViewById(R.id.error_level);
                err.setText(R.string.err2);             //display the error err2
                err.setVisibility(View.VISIBLE);
            } else{                                     //else, no error to display
                err = findViewById(R.id.error_level);
                err.setVisibility(View.GONE);
            }

            if(level == 3 && help){                     // for level 3 you cannot get help
                err = findViewById(R.id.error_help);
                err.setText(R.string.err3);             //display err3
                err.setVisibility(View.VISIBLE);
            } else{                                     //no error to display
                err = findViewById(R.id.error_help);
                err.setVisibility(View.GONE);
            }
            //after a fail, the game waits for start button to be pressed again.
        }
        else {
            startGame(); //no error, the game can start
        }
    }

    void readQuestions() throws IOException{
        //get the file stream
        Scanner br;

        if(level == 1) br = new Scanner(getResources().openRawResource(R.raw.beginner));
        else if(level == 2) br = new Scanner(getResources().openRawResource(R.raw.advanced));
        else if(level == 3) br = new Scanner(getResources().openRawResource(R.raw.master));
        else{
            System.out.println(R.string.err_read);
            return;
        }


        noQuestions = Integer.parseInt(br.nextLine());

        for(int i = 0; i < noQuestions && br.hasNext(); i++){
            Q[i] = new Questions();
            Q[i].question = br.nextLine();
            Q[i].correct = Integer.parseInt(br.nextLine());

            Q[i].answ1 = br.nextLine();
            Q[i].answ2 = br.nextLine();
            Q[i].answ3 = br.nextLine();
            Q[i].answ4 = br.nextLine();

            use[i] = false;
        }
    }

    public void resetContent(){
        Button btn_help = findViewById(R.id.help);
        if(!help){
            btn_help.setEnabled(false);
        } else{
            btn_help.setEnabled(true);
        }
        helpCount = 0;
        progressBar(0);
        answeredCorrect = 0;
        hp = 3;
        for(int i = 0; i < 40; i++)
            use[i] = false;
    }

    public void startGame(){
        setContentView(R.layout.quiz); //change the layout

        resetContent();                //reset the data needed for the game

        try { //has to use try-catch because of the IOException.
            readQuestions();
        } catch(IOException e){ //if the file is not found, this error shows up
            System.out.println(R.string.err_read);
            infoScreen("The data is corrupted!");
        }

        //reference all the needed data
        TextView inputName = findViewById(R.id.name);
        TextView lvlDisplay = findViewById(R.id.lvl);

        //update the data
        inputName.setText("Hello " + name);
        lvlDisplay.setText("Level: " + level);

        getNextQuestion();
    }

    public void progressBar(int k){ // k is the correct number of answers out of the total
        ProgressBar pb = findViewById(R.id.progressBar);
        if(k == 0) pb.setProgress(0);
        else pb.setProgress(100*k/noQuestions);
    }

    public void getNextQuestion(){  //function that prints a random question on the screen
        if(answeredCorrect == noQuestions){ //no more questions left
            //end of game
            infoScreen("You finished level " + Integer.toString(level) +
                    " by using "+ (3-hp) + " lives and "+ helpCount + " helps!");
            return;
        }
        Button btnHelp = findViewById(R.id.help);
        btnHelp.setEnabled(help);
        enableAllButtons();

        TextView tw = findViewById(R.id.progress);          //update the progress text field
        tw.setText("" + (answeredCorrect+1) + "/" + noQuestions);

        rand = new Random();
        currentQuestion = rand.nextInt(noQuestions);//get a random question from the array
        while(use[currentQuestion] == true) currentQuestion = rand.nextInt(noQuestions);

        TextView question = findViewById(R.id.question);    //get btns ids
        TextView answ1 = findViewById(R.id.answer1);
        TextView answ2 = findViewById(R.id.answer2);
        TextView answ3 = findViewById(R.id.answer3);
        TextView answ4 = findViewById(R.id.answer4);

        question.setText(Q[currentQuestion].question);      //update buttons with answers
        answ1.setText(Q[currentQuestion].answ1);
        answ2.setText(Q[currentQuestion].answ2);
        answ3.setText(Q[currentQuestion].answ3);
        answ4.setText(Q[currentQuestion].answ4);
        use[currentQuestion] = true;                        //set the current question as used
    }

    public void btnReset(View v){                           //when back button is pressed
        setContentView(R.layout.activity_main); //change the layout
        EditText et = findViewById(R.id.name);
        et.setText(name);                                   //keep the same name
    }

    public void btnHelp(View v){                            //when help button is pressed
        int safe = Q[currentQuestion].correct;
        int turnOff = 0;
        helpCount++;

        if(level == 1){                                     //at beginner level cut 2 wrong answers
            //2 hints
            turnOff = rand.nextInt(4)+1;
            while (turnOff == safe) turnOff = rand.nextInt(4)+1;
            triggerBtn(turnOff, false);
            int turnOff2 = rand.nextInt(4)+1;
            while (turnOff2 == safe || turnOff == turnOff2) turnOff2 = rand.nextInt(4)+1;
            triggerBtn(turnOff2, false);

        } else if (level == 2) {                            //at advanced level cut 1 wrong answer
            //just one hint
            turnOff = rand.nextInt(4)+1;
            while (turnOff == safe) turnOff = rand.nextInt(4)+1;
            triggerBtn(turnOff, false);
        }
        else{                                               //at master level do nothing
            //no help
            return;
        }
        Button btnHelp = findViewById(R.id.help);           //disable the button for that question
        btnHelp.setEnabled(false);
    }

    public void triggerBtn(int x, boolean enabled){         //when a button is pressed
        Button btn;

        if(x == 1) btn = findViewById(R.id.answer1);
        else if(x == 2) btn = findViewById(R.id.answer2);
        else if(x == 3) btn = findViewById(R.id.answer3);
        else if(x == 4) btn = findViewById(R.id.answer4);
        else return;

        if(enabled == true){
            btn.setTextColor(getResources().getColor(R.color.white));
        }
        else{
            btn.setTextColor(getResources().getColor(R.color.grey));
        }
        btn.setEnabled(enabled);
    }

    public void enableAllButtons(){
        triggerBtn(1, true);
        triggerBtn(2, true);
        triggerBtn(3, true);
        triggerBtn(4, true);
    }

    public void btnAnswer1(View v){
        int answer = Q[currentQuestion].correct;
        if(answer == 1){
            answeredCorrect++;
            progressBar(answeredCorrect);
            getNextQuestion();
        }
        else{
            triggerBtn(1, false);
            lose1Hp();
        }
    }

    public void btnAnswer2(View v){
        int answer = Q[currentQuestion].correct;
        if(answer == 2){
            answeredCorrect++;
            progressBar(answeredCorrect);
            getNextQuestion();
        }
        else{
            triggerBtn(2, false);
            lose1Hp();
        }
    }

    public void btnAnswer3(View v){
        int answer = Q[currentQuestion].correct;
        if(answer == 3){
            answeredCorrect++;
            progressBar(answeredCorrect);
            getNextQuestion();
        }
        else{
            triggerBtn(3, false);
            lose1Hp();
        }
    }

    public void btnAnswer4(View v){
        int answer = Q[currentQuestion].correct;
        if(answer == 4){
            answeredCorrect++;
            progressBar(answeredCorrect);
            getNextQuestion();
        }
        else{
            triggerBtn(4, false);
            lose1Hp();
        }
    }

    public void lose1Hp(){                              //lose 1 hp and update the hearts
        //make a Toast.makeText

        hp--;
        if(hp == 2){
            ImageView img = findViewById(R.id.hp3);
            img.setVisibility(View.INVISIBLE);
        }
        else if(hp == 1){
            ImageView img = findViewById(R.id.hp2);
            img.setVisibility(View.INVISIBLE);
        } else if(hp == 0){
            infoScreen("You lost!");
        }
    }

    public void infoScreen(String s){                  //display a message on the end game screen
        setContentView(R.layout.info_page); //change the layout
        TextView info = findViewById(R.id.infoPageText);
        info.setText(s);
    }

    public void nextPage(View v){
        btnReset(v);
    }

}

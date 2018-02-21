package com.example.andrei.quizapp;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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

        for(int i = 0; i < noQuestions; i++){
            Q[i] = new Questions();
            Q[i].question = br.nextLine();
            Q[i].cath =  Integer.parseInt(br.nextLine());

            if(Q[i].cath == 1){             //if the question is of cathegory 1
                Q[i].correct[1] = Integer.parseInt(br.nextLine());

                for(int j = 1; j <= 4; j++)
                    Q[i].answ[j] = br.nextLine();

            } else if(Q[i].cath == 2){      //if the question is of cathegory 2
                Q[i].correct[0] = Integer.parseInt(br.nextLine());//the number of correct questions
                for(int j = 1; j <= Q[i].correct[0]; j++) Q[i].correct[j] = 0; //clear the array
                for(int j = 1; j <= Q[i].correct[0]; j++){                  //add the new answers
                    Q[i].correct[Integer.parseInt(br.nextLine())] = 1;          //mark that question x is correct
                }
                for(int j = 1; j <= 4; j++)
                    Q[i].answ[j] = br.nextLine();

            } else if(Q[i].cath == 3){      //if the question is of cathegory 3
                Q[i].answ[0] = br.nextLine();
            }
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

        progressBar(answeredCorrect);

        //reset the answers part
        LinearLayout L1 = findViewById(R.id.cathegory1);
        LinearLayout L2 = findViewById(R.id.cathegory2);
        LinearLayout L3 = findViewById(R.id.cathegory3);
        L1.setVisibility(View.GONE);
        L2.setVisibility(View.GONE);
        L3.setVisibility(View.GONE);

        //display score
        TextView tw = findViewById(R.id.progress);          //update the progress text field
        //tw.setText("" + (answeredCorrect+1) + "/" + noQuestions);
        tw.setText("Score: " + 10 * answeredCorrect);

        rand = new Random();
        currentQuestion = rand.nextInt(noQuestions);//get a random question from the array
        while(use[currentQuestion] == true) currentQuestion = rand.nextInt(noQuestions);

        //enable button help which might be used previous
        Button btnHelp = findViewById(R.id.help);
        if(Q[currentQuestion].cath == 1){
            btnHelp.setEnabled(help);
            L1.setVisibility(View.VISIBLE);
        }
        else if(Q[currentQuestion].cath == 2){
            btnHelp.setEnabled(false);
            L2.setVisibility(View.VISIBLE);
        } else if(Q[currentQuestion].cath == 3){
            btnHelp.setEnabled(false);
            L3.setVisibility(View.VISIBLE);
        }

        if(Q[currentQuestion].cath == 1){
            enableAllButtons();
            TextView question = findViewById(R.id.question);    //get btns ids
            TextView answ1 = findViewById(R.id.answer1);
            TextView answ2 = findViewById(R.id.answer2);
            TextView answ3 = findViewById(R.id.answer3);
            TextView answ4 = findViewById(R.id.answer4);

            question.setText(Q[currentQuestion].question);      //update buttons with answers
            answ1.setText(Q[currentQuestion].answ[1]);
            answ2.setText(Q[currentQuestion].answ[2]);
            answ3.setText(Q[currentQuestion].answ[3]);
            answ4.setText(Q[currentQuestion].answ[4]);

        } else if(Q[currentQuestion].cath == 2){
            TextView question = findViewById(R.id.question);    //get btns ids
            TextView answ1 = findViewById(R.id.cb_answer1);
            TextView answ2 = findViewById(R.id.cb_answer2);
            TextView answ3 = findViewById(R.id.cb_answer3);
            TextView answ4 = findViewById(R.id.cb_answer4);

            question.setText(Q[currentQuestion].question);      //update buttons with answers
            answ1.setText(Q[currentQuestion].answ[1]);
            answ2.setText(Q[currentQuestion].answ[2]);
            answ3.setText(Q[currentQuestion].answ[3]);
            answ4.setText(Q[currentQuestion].answ[4]);

        } else if(Q[currentQuestion].cath == 3){
            TextView question = findViewById(R.id.question);    //get btns ids
            question.setText(Q[currentQuestion].question);
            EditText et = findViewById(R.id.edit_text_field_answer);
            et.setText("");
        }
        use[currentQuestion] = true;                        //set the current question as used
    }

    public void btnReset(View v){                           //when back button is pressed
        setContentView(R.layout.activity_main); //change the layout
        EditText et = findViewById(R.id.name);
        et.setText(name);                                   //keep the same name
    }

    public void btnHelp(View v){                            //Works only for cath1 questions
        int safe = Q[currentQuestion].correct[1];
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
        RadioButton btn;

        if(x == 1) btn = findViewById(R.id.answer1);
        else if(x == 2) btn = findViewById(R.id.answer2);
        else if(x == 3) btn = findViewById(R.id.answer3);
        else if(x == 4) btn = findViewById(R.id.answer4);
        else return;

        if(enabled == true){
            btn.setTextColor(getResources().getColor(R.color.white));
        }
        else{
            btn.setChecked(false);
            btn.setTextColor(getResources().getColor(R.color.grey));
        }
        btn.setEnabled(enabled);
    }

    public void submitCheckBox(View view){
        int[] answ = new int[5];
        CheckBox answ1 = findViewById(R.id.cb_answer1);
        CheckBox answ2 = findViewById(R.id.cb_answer2);
        CheckBox answ3 = findViewById(R.id.cb_answer3);
        CheckBox answ4 = findViewById(R.id.cb_answer4);

        if(answ1.isChecked()) answ[1] = 1; else answ[1] = 0;
        if(answ2.isChecked()) answ[2] = 1; else answ[2] = 0;
        if(answ3.isChecked()) answ[3] = 1; else answ[3] = 0;
        if(answ4.isChecked()) answ[4] = 1; else answ[4] = 0;

        boolean fail = false;
        for(int i = 1; i <= 4; i++){
            if(answ[i] == 0 && Q[currentQuestion].correct[i] == 0);
            else if(answ[i] != 0 && Q[currentQuestion].correct[i] != 0);
            else{
                fail = true;
            }
        }
        if(fail){
            lose1Hp();
        }
        else{

            correctAnswer();
        }
    }

    public void submitOpen(View view){ //Works only for cath3
        EditText et = findViewById(R.id.edit_text_field_answer);
        if(et.getText().toString().trim().toLowerCase().compareTo(Q[currentQuestion].answ[0].toLowerCase()) == 0) {
            correctAnswer();
        }
        else{
            lose1Hp();
        }
    }

    public void btn_active(View view){
        RadioButton b1 = findViewById(R.id.answer1);
        RadioButton b2 = findViewById(R.id.answer2);
        RadioButton b3 = findViewById(R.id.answer3);
        RadioButton b4 = findViewById(R.id.answer4);

        if(b1.isEnabled()) {
            b1.setTextColor(getResources().getColor(R.color.white));
            b1.setChecked(false);
        }
        if(b2.isEnabled()) {
            b2.setTextColor(getResources().getColor(R.color.white));
            b2.setChecked(false);
        }
        if(b3.isEnabled()) {
            b3.setTextColor(getResources().getColor(R.color.white));
            b3.setChecked(false);
        }
        if(b4.isEnabled()) {
            b4.setTextColor(getResources().getColor(R.color.white));
            b4.setChecked(false);
        }

        switch (view.getId()){
            case R.id.answer1:
                b1.setChecked(true);
                b1.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.answer2:
                b2.setChecked(true);
                b2.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.answer3:
                b3.setChecked(true);
                b3.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.answer4:
                b4.setChecked(true);
                b4.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            default:
                break;
        }
    }

    public void enableAllButtons(){
        triggerBtn(1, true);
        triggerBtn(2, true);
        triggerBtn(3, true);
        triggerBtn(4, true);
    }

    public void correctAnswer(){
        answeredCorrect++;

        Context context = getApplicationContext();
        CharSequence text = "Well done! " + answeredCorrect + " questions answered correct so far!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        getNextQuestion();
    }

    public void submitRadio(View view){
        RadioButton answ1 = findViewById(R.id.answer1);
        RadioButton answ2 = findViewById(R.id.answer2);
        RadioButton answ3 = findViewById(R.id.answer3);
        RadioButton answ4 = findViewById(R.id.answer4);
        int answer = Q[currentQuestion].correct[1];
        int clicked = 0;

        if(answ1.isChecked()) clicked = 1;
        else if(answ2.isChecked()) clicked = 2;
        else if(answ3.isChecked()) clicked = 3;
        else if(answ4.isChecked()) clicked = 4;

        if(answer == clicked) {
            correctAnswer();
        }
        else {
            //if no other return reached, a wrong answer is given
            triggerBtn(clicked, false);
            lose1Hp();
        }
    }

    public void lose1Hp(){                              //lose 1 hp and update the hearts
        //make a Toast.makeText

        Context context = getApplicationContext();
        CharSequence text = "Wrong answer!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

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

        Toast toast = Toast.makeText(getApplicationContext(), "You solved " + answeredCorrect +" qustions", Toast.LENGTH_SHORT);
        toast.show();

        setContentView(R.layout.info_page); //change the layout
        TextView info = findViewById(R.id.infoPageText);
        info.setText(s);

        int totalScore = 10*answeredCorrect;
        TextView info2 = findViewById(R.id.infoPageText2);
        info2.setText("Total score: "+ totalScore + " out of " + 10 * noQuestions);
    }

    public void nextPage(View v){
        btnReset(v);
    }

}

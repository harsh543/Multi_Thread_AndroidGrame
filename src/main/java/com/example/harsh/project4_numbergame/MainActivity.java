package com.example.harsh.project4_numbergame;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements Handler.Callback {

    //The Message will have the Thread1 or Thread 2 by using ObtainMessage
    public static final int THREAD1 = 1;
    public static final int THREAD2 = 2;
    public static final int UIThread = 3;
    // will be set as arg2 for UI thread message by workers
    public static final int STATUS_start = 0;
    public static final int STATUS_over=2;
    public static final int STATUS_inprogress=1;
    //arg1 will have counter and obj will have the string message

    public Handler  mMainHandler = new Handler( this );
    StringBuilder sbThread1 ;    //stores the messages printed on TextView for Thread1
    StringBuilder sbThread2 ;    //stores the messages printed on TextView for Thread2


    Button button1;
    TextView textViewThread1,tvOneStatus;
    TextView textViewThread2,tvTwoStatus;
    Thread1 mThread1;
    Thread2 mThread2;


        public boolean handleMessage(Message messg) {
            int what = messg.what ;
            Message mThread1;
            switch (what) {
                case THREAD1:
                    switch(messg.arg2){
                        case STATUS_start:

                            tvOneStatus.setText((String)messg.obj);

                            //send messg to WOrker Thread 1 to start game

                            MainActivity.this.mThread1.mThread1Handler.post(new Runnable() {
                                @Override
                                public void run() { // for running code when UI thread sends messg to start game
                                    int wildGuess= MainActivity.this.mThread1.makeGuess( new GuessResponse());
                                    MainActivity.this.mThread1.countOfGuess++;
                                    //get messg which was sent to UI thread

                                    mMainHandler.obtainMessage(THREAD1,MainActivity.this.mThread1.countOfGuess,STATUS_inprogress,"Guess"+ MainActivity.this.mThread1.countOfGuess+" :"+wildGuess+"\r\n").sendToTarget();
                                    //send message to worker thread 2
                                   Message messg = mThread2.mThread2Handler.obtainMessage(THREAD1);
                                    messg.arg1= MainActivity.this.mThread1.countOfGuess;
                                    GuessResponse gr = new GuessResponse();
                                    gr.myGuess=wildGuess;
                                    messg.obj = gr;
                                    mThread2.mThread2Handler.sendMessage(messg);
                                }
                            });


                            break;

                        case STATUS_inprogress://20 attempts check has to be put
                            int count = messg.arg1;
                            System.out.println("COUNT THREAD1:"+count);
                            if(count<=20){


                                tvOneStatus.setText((String)messg.obj);
                            }else{//i.e. 21st guess stop the thread

                                tvOneStatus.setText("Over 20 guesses...Thread1 stops\r\n");
                                mThread1= MainActivity.this.mThread1.mThread1Handler.obtainMessage(UIThread);
                                mThread1.arg2=STATUS_over;
                                MainActivity.this.mThread1.mThread1Handler.sendMessage(mThread1);
                            }

                            //textViewThread1.setText((String)messg.obj);

                            break;
                        case STATUS_over:
                            /*Once any thread wins we declare the result and print the output*/

                            //Thread 1 wins

                            textViewThread1.setText((String)messg.obj);
                            MainActivity.this.mThread1.mThread1Handler.post(new Runnable() {
                                @Override
                                public void run() { // end the thread 2 when over 20 guesses
                                    Looper.myLooper().quitSafely();
                                }
                            });
                            // thread 2 wins if game over msg came from thr2
                            mThread2.mThread2Handler.post(new Runnable() {
                                @Override
                                public void run() { // end the thread 2 when over 20 guesses
                                    Looper.myLooper().quitSafely();
                                }
                            });
                            // thread 1 wins
                            break;
                    }
//                    textViewThread1.setText((String)messg.obj);
                    break;
                case THREAD2:
                    switch(messg.arg2){
                        case STATUS_start:

                            tvTwoStatus.setText((String)messg.obj);

                            break;
                        case STATUS_inprogress://20 attempst check has to be put
                            int count = messg.arg1;
                            System.out.println("COUNT THREAD2:"+count);
                            if(count<=20){


                                tvTwoStatus.setText((String)messg.obj);
                            }else{//it can't go more than 21 so stop here
                               //ignore the response
                                tvTwoStatus.setText("Over 20 guesses...Thread2 stops\r\n");

                                mThread2.mThread2Handler.post(new Runnable() {
                                    @Override
                                    public void run() { // end the thread 2 when over 20 guesses
                                        Looper.myLooper().quitSafely();
                                    }
                                });
                       }

                            break;
                        case STATUS_over:// thread 2 wins if game over msg came from thr2

                            tvTwoStatus.setText((String)messg.obj);
                            //Close thread 1
                            MainActivity.this.mThread1.mThread1Handler.post(new Runnable() {
                                @Override
                                public void run() { // end the thread 2 when over 20 guesses
                                    Looper.myLooper().quitSafely();
                                }
                            });
                            // thread 2 wins if game over msg came from thr2
                            mThread2.mThread2Handler.post(new Runnable() {
                                @Override
                                public void run() { // end the thread 2 when over 20 guesses
                                    Looper.myLooper().quitSafely();
                                }
                            });

                            break;
                    }
//                    textViewThread2.setText((String)messg.obj);
                    break;

            }

return  false;
        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button);
        textViewThread1 = (TextView)findViewById(R.id.textView10);

        textViewThread2 = (TextView)findViewById(R.id.textView20);
        tvOneStatus =(TextView) findViewById(R.id.textView11);
        tvTwoStatus = (TextView) findViewById(R.id.textView21);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sbThread1 = new StringBuilder();
                sbThread2 = new StringBuilder();
                // start the thread
                mThread1 = new Thread1(MainActivity.this);
                mThread2 = new Thread2(MainActivity.this);
                mThread1.start();
                mThread2.start();

            }
        });


    }

/*Creating worker thread 1*/
    class Thread1 extends Thread implements Runnable {

        int countOfGuess=0;
        Handler mThread1Handler;
        int secretNum_1 = getLegalSecretRandomNumber();
        int guess;
        Activity mainActivity;
        Handler mainHandler;
        private ArrayList<Integer> numbs;
        public Thread1(MainActivity anActivity) {
            numbs = new ArrayList<Integer>();
            this.mainActivity = anActivity;
            this.mainHandler = anActivity.mMainHandler;
            for (int i = 1000; i <= 9999; i++) {
                numbs.add(i);
            }
        }

        public void run() {

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {//Running the main Handler
                    System.out.println("::::::"+secretNum_1);
                    //display random number to user by running it on UI thread when thread runs
                    sbThread1.append(""+secretNum_1+"\r\n");
                    textViewThread1.setText(sbThread1);
                    //send messg to T1 to start game
                    Message mThread1= MainActivity.this.mThread1.mThread1Handler.obtainMessage(UIThread);
                    mThread1.arg2=STATUS_start;
                    MainActivity.this.mThread1.mThread1Handler.sendMessage(mThread1);
                }
            });

            Looper.prepare();
            mThread1Handler = new Handler() {
                public void handleMessage(Message msg) {
                    int what = msg.what ;
                    try{
                        /*sleep so that the UI threads can interact print messages*/
                        mThread1.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    switch (what) {
                        case UIThread://response came from UI Thread
                            switch (msg.arg2){
                                case STATUS_start:
                                    // take a guess to start the game
                                    int wildGuess= makeGuess( new GuessResponse());
                                    countOfGuess++;
                                    //send messg to UI thread


                                    mainHandler.obtainMessage(THREAD1,countOfGuess,STATUS_inprogress,"Guess"+countOfGuess+" :"+wildGuess+"\r\n").sendToTarget();
                                    //send message to thread 2
                                 Message   messg = mThread2.mThread2Handler.obtainMessage(THREAD1);
                                    messg.arg1=countOfGuess;
                                    GuessResponse gr = new GuessResponse();
                                    gr.myGuess=wildGuess;
                                    messg.obj = gr;
                                    mThread2.mThread2Handler.sendMessage(messg);
                                    break;
                                case STATUS_over:
                                    //quiting the looper makes thread as free
                                    Looper.myLooper().quitSafely();
                                    //Thread 2 or thread 1 has won end this thread now
                                    break;

                            }
                            break;
                        case THREAD2://response from mThread2
                            switch(msg.arg2){// arg2 for variable win status
                                //if arg2 i.e, win =1(i.e, t1 wins) then send msg to Ui thread and t1 dies
                                //else if arg2 =0 (i.e, t1 guessing continues)
                                case 1:
                                    /*Writing back to main UI thread*/
                                    mMainHandler.obtainMessage(THREAD1,countOfGuess,STATUS_over,"WORKER THREAD 1 Wins\r\n").sendToTarget();
                                    /*or  Message messg = mMainHandler.obtainMessage(THREAD1);
                                    messg.arg1=countOfGuess;
                                    messg.arg2= STATUS_over;
                                    messg.obj = "WORKER THREAD 1 Wins\r\n";
                mainHandler.obtainMessage( messg.arg1,messg.arg2,messg.obj ).sendToTarget();
               */
                                    //Just send the response to UI thread no need to end this thread.
                                    // UI will handle
                                    //END THE THREAD AND EXIT THE LOOPER looper.quit
                                    break;
                                case 0:

                                    //else game will continue by guessing another number and
                                    // building a response to Worker Thread 2 guess
                                    //send response and new guess to UI Thread
                                    //send response to worker thread 2
                                    int wildGuess = makeGuess((GuessResponse) msg.obj);
                                    countOfGuess++;
                                    GuessResponse guessRes1 = processTheGuess(new GuessResponse(),secretNum_1,((GuessResponse) msg.obj).myGuess );

                                    //Send the response to UI thread or we can use runOnUiThread like as here I passing several variable so will pass in object itself than make them final
                                    /*  mainActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                               }
                        });*/
                                             mMainHandler.obtainMessage(THREAD1,countOfGuess,STATUS_inprogress,"Response For Thread2 Guess:\nCorrectly positioned->"+guessRes1.correctlyPos+"\r\n"+
                                            "Correct Numbers->"+guessRes1.wrongPosMap+"\r\n"+"Thread1 Guess"+countOfGuess+" :"+wildGuess+"\r\n").sendToTarget();

                                    //Send guess to Thread2 which we get from makeGuess
                                    Message messg = mThread2.mThread2Handler.obtainMessage(THREAD1);
                                    messg.arg1=wildGuess;
                                    messg.arg2=guessRes1.win_flag ; //set The Win Flag
                                    //setting my wildGuess to guess flag in response
                                    guessRes1.myGuess=wildGuess;
                                    messg.obj=guessRes1;
                                    mThread2.mThread2Handler.sendMessage(messg);
                                    break;
                            }
                            break;
                    }
                }
            };
            Looper.loop();



        }

        public int getLegalSecretRandomNumber() {
            boolean legal=false;
            int number=0;
            while(!legal){
                Random rand = new Random();
                number = rand.nextInt(9000)+1000;
                String num = String.valueOf(number);
                if(num.charAt(0)==num.charAt(1)||num.charAt(0)==num.charAt(2)||
                        num.charAt(0)==num.charAt(3)||num.charAt(1)==num.charAt(2)||
                        num.charAt(1)==num.charAt(3)||num.charAt(2)==num.charAt(3)){
                    legal=false;
                }else{
                    legal =true;
                }
            }

            return number;
        }
        public void updateMyGuess(GuessResponse response) {
            // update the guess based on the number of matching digits claimed by the user

            int nmatches=Integer.valueOf(response.correctlyPos.trim());
            int matches;
            for (int j = 0; j < numbs.size(); j++) {
                matches = 0;
                for (int i = 3; i >= 0; i--) {
                    if (String.valueOf(guess).charAt(i) == String.valueOf(numbs.get(j)).charAt(i)) {
                        matches++;
                    }
                }

                if(nmatches==0 && matches>1){
                    numbs.remove(j);
                    j--;
                }
                else if (matches != nmatches) {
                    numbs.remove(j);
                    j--;
                }
                else {
                    System.out.println(numbs.get(j));
                }

            }
        }
        /*Strategy of guessing for worker thread 1*/
        public int makeGuess(GuessResponse response){
            if(response.correctlyPos.length()>0)
            updateMyGuess(response);
            Random rand = new Random();
            int NumbsSize = numbs.size();
            boolean xsize;
            if (NumbsSize > 1) {
                xsize = true;
            } else {
                xsize = false;
            }
            if (xsize == true) {


                guess = numbs.get(rand.nextInt(numbs.size() - 1));

                return guess;
            } else if(NumbsSize==1) {
                return numbs.get(0);
            }
            else {
                int number = rand.nextInt(9000)+1000;
                return number;
            }
        }
      /*Check the number of correctpos and number of correct numbers in the guess*/
        public GuessResponse processTheGuess(GuessResponse gres,  int num, int guess ){

            gres.guessed_number=guess;
            String guessStr = String.valueOf(guess);
            String secretNum= String.valueOf(num);


            int noOfCorrectPositioned=0;
            int noOfWrongPositioned=0;
            String correctPosMap="";
            String correctIndx="";
            String wrongPosMap="";
            String correctlyPos="";
            for (int i=0;i<4;i++){
                if(guessStr.charAt(i)==secretNum.charAt(i)){
                    noOfCorrectPositioned++;
                    correctPosMap=correctPosMap+guessStr.charAt(i);
                    correctlyPos=correctlyPos+guessStr.charAt(i)+" ";
                    //Check numbers that are correct positioned at match
                }
            }

            if(noOfCorrectPositioned<4){

               /*CHeck numbers that are correct but at wrong position*/
                for(int i=0;i<guessStr.length();i++){
                    for(int j=0;j<secretNum.length();j++){
                        if(guessStr.charAt(i)== secretNum.charAt(j)){

                            noOfWrongPositioned++;
                        }
                    }
                }
            }

            gres.correctlyPos=String.valueOf(noOfCorrectPositioned);
            gres.wrongPosMap=String.valueOf(noOfWrongPositioned);
            if(noOfCorrectPositioned==4){
                gres.win_flag=1;//Means that Thread 2 wins
            }
            return gres;
        }



    }

/*Creating worker thread 2*/
    class Thread2 extends Thread implements Runnable{
        Handler mThread2Handler;
        int countOfGuess=0;
        Activity mainActivity;
        Handler mainHandler;
        int secretNum_2 = getLegalSecretRandomNumber();
        int guess;
        public ArrayList<Integer> numbers = new ArrayList<Integer>();
    /*Initalise the MainActivity to get UIHandler*/
        public Thread2(MainActivity anActivity) {
            this.mainActivity = anActivity;
            this.mainHandler = anActivity.mMainHandler;
            for (int i = 1000; i < 10000; i++) {
                numbers.add(i);
            }

        }
        public void updateMyGuess(GuessResponse response) {
            int nmatches=Integer.valueOf(response.correctlyPos.trim());
            // update the guess based on the number of matching digits claimed by the user
            ArrayList<Integer> temp = new ArrayList<Integer>();
            ArrayList<Integer> temp2 = new ArrayList<Integer>();
            int first = guess/1000;
            int second = (guess/100)%10;
            int third = (guess/10)%10;
            int fourth = guess%10;
            //variables for each digit of guess
            if (nmatches == 1) {
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i)/1000 == first) temp.add(numbers.get(i));
                    if ((numbers.get(i)/100)%10 == second) temp.add(numbers.get(i));
                    if ((numbers.get(i)/10)%10 == third) temp.add(numbers.get(i));
                    if (numbers.get(i)%10 == fourth) temp.add(numbers.get(i));
                }
                numbers.clear();
                numbers.addAll(temp);
            }else if (nmatches == 2) {
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i)/1000 == first &&
                            (numbers.get(i)/100)%10 == second) temp.add(numbers.get(i));
                    if (numbers.get(i)/1000 == first &&
                            (numbers.get(i)/10)%10 == third) temp.add(numbers.get(i));
                    if (numbers.get(i)/1000 == first &&
                            numbers.get(i)%10 == fourth) temp.add(numbers.get(i));
                    if ((numbers.get(i)/100)%10 == second &&
                            (numbers.get(i)/10)%10 == third) temp.add(numbers.get(i));
                    if ((numbers.get(i)/100)%10 == second &&
                            numbers.get(i)%10 == fourth) temp.add(numbers.get(i));
                    if ((numbers.get(i)/10)%10 == third &&
                            numbers.get(i)%10 == fourth) temp.add(numbers.get(i));
                }
                numbers.clear();
                numbers.addAll(temp);
            }else if (nmatches == 3) {
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i)/1000 == first && (numbers.get(i)/100)%10 == second
                            && (numbers.get(i)/10)%10 == third) temp.add(numbers.get(i));
                    if (numbers.get(i)/1000 == first && (numbers.get(i)/10)%10 == third
                            && numbers.get(i)%10 == fourth) temp.add(numbers.get(i));
                    if ((numbers.get(i)/100)%10 == second && (numbers.get(i)/10)%10 == third
                            && numbers.get(i)%10 == fourth) temp.add(numbers.get(i));
                }
                numbers.clear();
                numbers.addAll(temp);
            }else {
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i)/1000 == first) temp2.add(numbers.get(i));
                    if ((numbers.get(i)/100)%10 == second) temp2.add(numbers.get(i));
                    if ((numbers.get(i)/10)%10 == third) temp2.add(numbers.get(i));
                    if (numbers.get(i)%10 == fourth) temp2.add(numbers.get(i));
                }
                numbers.removeAll(temp2);
            }
            //creates new smaller ArrayList with better guesses
            //sets numbers as the smaller list
            //update the guess based on the number of matching digits claimed by the user

        }
      /*Executed when thread start called from Main Activity*/
        public void run() {

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {

                    //display random number to user by running it on UI thread when thread runs
                    sbThread2.append(""+secretNum_2+"\r\n");
                    textViewThread2.setText(sbThread2);
                }
            });



            Looper.prepare();
            /*Create a threadhandler which comes with job queue*/
            mThread2Handler = new Handler() {
                public void handleMessage(Message msg) {

                    try{
                        mThread2.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int what = msg.what ;
                    switch (what) {
                        case UIThread://response came from UI Thread
                            switch (msg.arg2){
                                case STATUS_start:
                                    // New guess to begin this game
                                    int wildGuess= makeGuess( new GuessResponse());
                                    countOfGuess++;
                                    //send messg to UI thread
                                    Message messg = mMainHandler.obtainMessage(THREAD2);
                                    messg.arg1 = countOfGuess;
                                    messg.arg2=STATUS_inprogress;
                                    messg.obj = "Guess"+countOfGuess+" :"+wildGuess+"\r\n";
                                    mMainHandler.obtainMessage(THREAD2,countOfGuess,STATUS_inprogress,"Guess"+countOfGuess+" :"+wildGuess+"\r\n").sendToTarget();
                                    //send message to thread 1
                                    messg = mThread1.mThread1Handler.obtainMessage(THREAD2);
                                    messg.arg1=wildGuess;
                                    mThread1.mThread1Handler.sendMessage(messg);
                                    break;
                                case STATUS_over:
                                    Looper.myLooper().quitSafely();
                                    //Thread 2 or thread 1 has won end this thread now
                                    break;

                            }
                            break;
                        case THREAD1://response from mThread1
                            switch(msg.arg2){// arg2 for variable win status
                                //if arg2 i.e, win =1(i.e, t1 wins) then send msg to Ui thread and t1 dies
                                //else if arg2 =0 (i.e, t1 guessing continues)
                                case 1:

                                    mMainHandler.obtainMessage(THREAD2,countOfGuess,STATUS_over,"WORKER THREAD 2 Wins\r\n").sendToTarget();
                                    //Just send the response to Main thread no need to end this thread.
                                    // UI will handle
                                    //END THE THREAD AND EXIT THE LOOPER....looper.quitsafely
                                    break;
                                case 0:

                                    //game will continue by guessing another number and
                                    // building a response to Worker Thread 1 guess
                                    //send response and new guess to UI Thread
                                    //send response to WOrker Thread 1
                                    int wildGuess = makeGuess((GuessResponse) msg.obj);
                                    countOfGuess++;
                                    GuessResponse guessRes1 = processTheGuess(new GuessResponse(),secretNum_2,((GuessResponse) msg.obj).myGuess );

                                    //Send the response to UI thread

                                           mMainHandler.obtainMessage(THREAD2,countOfGuess,STATUS_inprogress,"Response For Thread1 Guess:\nCorrectly positioned->"+guessRes1.correctlyPos+"\r\n"+
                                            "Correct Numbers->"+guessRes1.wrongPosMap+"\r\n"+"Thread2 Guess"+countOfGuess+" :"+wildGuess+"\r\n").sendToTarget();

                                    //Send guess to Thread1
                                   Message messg = mThread1.mThread1Handler.obtainMessage(THREAD2);
                                    messg.arg1=wildGuess;
                                    messg.arg2=guessRes1.win_flag ; //set The Win Flag
                                    //setting my wildGuess to guess flag in response
                                    guessRes1.myGuess=wildGuess;
                                    messg.obj=guessRes1;
                                    mThread1.mThread1Handler.sendMessage(messg);
                                    break;
                            }
                            break;
                    }



                }
            };
            Looper.loop();


        }


            public int getLegalSecretRandomNumber() {
                boolean legal=false;
                int number=0;
                while(!legal){
                    Random rand = new Random();
                    number = rand.nextInt(9000)+1000;
                    String num = String.valueOf(number);
                    if(num.charAt(0)==num.charAt(1)||num.charAt(0)==num.charAt(2)||
                            num.charAt(0)==num.charAt(3)||num.charAt(1)==num.charAt(2)||
                            num.charAt(1)==num.charAt(3)||num.charAt(2)==num.charAt(3)){
                        legal=false;
                    }else{
                        legal =true;
                    }
                }

                return number;
            }

        public int makeGuess(GuessResponse response){
            Random rand = new Random();
            int myguess=rand.nextInt(9000)+1000;
            if(response.correctlyPos.length()>0)
                updateMyGuess(response);
            if(numbers.size()>1) {
                //increases number of guesses
                int tempIndex = (int) (Math.random() * numbers.size());
                 myguess = numbers.get(tempIndex);
                //creates a new guess
                guess = myguess;
                //sets global variable
            }
            else
            {
             guess=myguess;
            }
            return myguess;


        }

        public GuessResponse processTheGuess(GuessResponse gres,  int num, int guess ){

            gres.guessed_number=guess;
            String guessStr = String.valueOf(guess);
            String secretNum= String.valueOf(num);


            int noOfCorrectPositioned=0;
            int noOfWrongPositioned=0;
            String correctPosMap="";
            String correctIndx="";
            String wrongPosMap="";
            String correctlyPos="";
            for (int i=0;i<4;i++){
                if(guessStr.charAt(i)==secretNum.charAt(i)){
                    noOfCorrectPositioned++;
                    correctPosMap=correctPosMap+guessStr.charAt(i);
                    correctlyPos=correctlyPos+guessStr.charAt(i)+" ";
                    //Check numbers that are correct positioned at match
                }
            }

            if(noOfCorrectPositioned<4){

               /*CHeck numbers that are correct but at wrong position*/
                for(int i=0;i<guessStr.length();i++){
                    for(int j=0;j<secretNum.length();j++){
                        if(guessStr.charAt(i)== secretNum.charAt(j)){

                            noOfWrongPositioned++;
                        }
                    }
                }
            }

            gres.correctlyPos=String.valueOf(noOfCorrectPositioned);
            gres.wrongPosMap=String.valueOf(noOfWrongPositioned);
            if(noOfCorrectPositioned==4){
                gres.win_flag=1;//Means that Thread 1 has won
            }
            return gres;
        }


    }
}
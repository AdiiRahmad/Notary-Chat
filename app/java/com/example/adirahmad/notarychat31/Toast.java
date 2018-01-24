package com.example.adirahmad.notarychat31;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Toast extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);

        CountDownTimer countdowntimer = null;
        Button button;
        Toast toastMessage;
        TextView textView;
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            button = (Button)findViewById(R.id.button1);

            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    //Creating toast message.
                    toastMessage = new Toast(MainActivity.this);

                    //Creating TextView.
                    textView = new TextView(MainActivity.this);

                    //Setting up Text Color.
                    textView.setTextColor(Color.parseColor("#fafafa"));

                    //Setting up Text Size.
                    textView.setTextSize(17);

                    //Setting up Toast Message Text.
                    textView.setText("Toast message to a specific time");

                    //Add padding to Toast message.
                    textView.setPadding(20, 20, 20, 23);

                    //Adding TextView into Toast.
                    toastMessage.setView(textView);

                    //Access toast message as View.
                    View toastView = toastMessage.getView();

                    //Set Custom Background on Toast.
                    toastView.setBackgroundResource(R.layout.toast_message_style);


                    countdowntimer = new CountDownTimer(40000, 1000)
                    {
                        public void onTick(long millisUntilFinished)
                        {
                            toastMessage.show();
                        }
                        public void onFinish()
                        {
                            toastMessage.cancel();
                        }

                    }.start();

                }

            });
        }
    }
}

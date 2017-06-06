package jtime.jtime;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import jtime.jtime.network.JTimeUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JTimeUtil.instance.initTime(this);

        findViewById(R.id.networkOne).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.networkOne_tv)).setText(
                        "本机时间：" + System.currentTimeMillis() +"\n"
                                +"网络时间："+ JTimeUtil.instance.getCurrentTimeMillis(MainActivity.this)
                );
            }
        });

        findViewById(R.id.networkTwo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.networkTwo_tv)).setText(
                        "本机时间：" + System.currentTimeMillis() +"\n"
                                +"网络时间："+ JTimeUtil.instance.getSyncCurrentTimeMillis(MainActivity.this)
                );
            }
        });

        findViewById(R.id.networkThree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JTimeUtil.instance.getAsynCurrentTimeMillis(MainActivity.this,new JTimeUtil.TimeCallBack(){

                    @Override
                    public void onTimeCallBack(long time) {
                        ((TextView)findViewById(R.id.networkThree_tv)).setText(
                                "本机时间：" + System.currentTimeMillis() +"\n"
                                        +"网络时间："+ time
                        );
                    }
                });


            }
        });


    }

}

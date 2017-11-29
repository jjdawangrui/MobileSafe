package com.itheima.mobilesafe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dao.LocationDao;

public class Activity_commontools_1_querylocation extends AppCompatActivity {

    @InjectView(R.id.button_query)
    Button buttonQuery;
    @InjectView(R.id.tv_location)
    TextView tvLocation;
    @InjectView(R.id.edittext_number)
    EditText edittextNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commontools_1_querylocation);
        ButterKnife.inject(this);

        //设置监听效果，EditText里面只要做了修改，就执行一次
        edittextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String number = edittextNumber.getText().toString();
                if (!TextUtils.isEmpty(number)){
                    String location = LocationDao.getLocation(number,Activity_commontools_1_querylocation.this);
                    tvLocation.setText("归属地："+location);
                }
            }
        });
    }

    @OnClick(R.id.button_query)
    public void onViewClicked() {
        String number = edittextNumber.getText().toString();
        if (!TextUtils.isEmpty(number)){
            String location = LocationDao.getLocation(number,this);
            tvLocation.setText("归属地："+location);
        }else{
            Toast.makeText(this,"请输入电话号码",Toast.LENGTH_SHORT).show();
            Animation shake = AnimationUtils.loadAnimation(this,R.anim.shake);
            edittextNumber.startAnimation(shake);
        }
    }
}

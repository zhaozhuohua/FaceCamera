package com.aaron.facecamera;

import android.os.Bundle;

import com.cvr.fswitcher.fragment.CameraFaceFm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        FragmentManager mFragmentManager = getSupportFragmentManager();

        //事务是不能共享的，每次用到都要重新开启一个事务，之后提交
        FragmentTransaction fragmentTransactiontwo = mFragmentManager.beginTransaction();
        //参数：1.父容器   2.要替换的fragment。
        fragmentTransactiontwo.replace(R.id.fragment_layout, CameraFaceFm.Companion.newInstance(1));
        //提交事务
        fragmentTransactiontwo.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}

package com.aaron.facecamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.cvr.fswitcher.fragment.CameraFaceFm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkWriteStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            //如果没有被授予
            if(checkWriteStoragePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
                Toast.makeText(this, "没有相机权限!", Toast.LENGTH_SHORT).show();
                return;
            }else{
                addCameraFm();
            }
        } else {
            addCameraFm();
        }
    }

    private void addCameraFm() {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        //事务是不能共享的，每次用到都要重新开启一个事务，之后提交
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        //参数：1.父容器   2.要替换的fragment。
        fragmentTransaction.replace(R.id.fragment_layout, CameraFaceFm.Companion.newInstance(1));
        //提交事务
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    addCameraFm();
                }else{
                    Toast.makeText(this, "获取权限失败!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

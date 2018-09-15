package tthcc.rubiksolver.solver.faceletlabel;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import tthcc.rubiksolver.solver.faceletlabel.traindata.TrainDataUtil;

public class TrainDataFaceActivity extends AppCompatActivity {

    private TextView faceletName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_data_face);
        this.createTextViewOverlay();
        Intent intent = this.getIntent();
        String dataItem = intent.getStringExtra("dataItem");
        String[] value = dataItem.split("_");
        String originalFilename = TrainDataUtil.PATH + "/train/" + value[0] + "_original.jpg";
        Uri uri = Uri.fromFile(new File(originalFilename));
        ((ImageView)this.findViewById(R.id.original_data)).setImageURI(uri);
        this.faceletName.setText(value[1] + "_" + value[2]);
    }

    private void createTextViewOverlay() {
        this.faceletName = new TextView(this.getApplicationContext());
        this.faceletName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.faceletName.setTextColor(Color.WHITE);
        this.faceletName.setTextSize(30);
        FrameLayout.LayoutParams stopwatchTvLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stopwatchTvLayoutParams.gravity = Gravity.TOP;
        stopwatchTvLayoutParams.topMargin = 100;
        this.addContentView(this.faceletName, stopwatchTvLayoutParams);
    }
}

package tthcc.rubiksolver.solver.faceletlabel;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import tthcc.rubiksolver.solver.faceletlabel.traindata.TrainDataUtil;

public class TrainDataLabelActivity extends AppCompatActivity {

    private String dataItem;
    private int itemNum = 0;
    private int labeledNum = 0;
    private TextView labelNumView;
    private TrainDataUtil trainDataUtil = new TrainDataUtil();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_data);

        this.findViewById(R.id.face_U).setOnClickListener(this.faceClickListener);
        this.findViewById(R.id.face_F).setOnClickListener(this.faceClickListener);
        this.findViewById(R.id.face_D).setOnClickListener(this.faceClickListener);
        this.findViewById(R.id.face_B).setOnClickListener(this.faceClickListener);
        this.findViewById(R.id.face_L).setOnClickListener(this.faceClickListener);
        this.findViewById(R.id.face_R).setOnClickListener(this.faceClickListener);
        this.findViewById(R.id.face_X).setOnClickListener(this.faceClickListener);

        this.findViewById(R.id.button_next).setOnClickListener(this.nextButtonClickListener);
        this.findViewById(R.id.face_data).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), TrainDataFaceActivity.class);
                intent.putExtra("dataItem", dataItem);
                startActivity(intent);
            }
        });

        this.createTextViewOverlay();

        this.trainDataUtil.initLabel();
        int[] size = this.trainDataUtil.getLabelSize();
        this.labeledNum = size[0];
        this.itemNum = size[1];

        this.labelNumView.setText(String.format(" %s / %s", labeledNum, itemNum));

        this.dataItem = this.trainDataUtil.getNextItem();

        this.showDateItem();
    }

    /**
     *
     */
    private void showDateItem() {
        ((ImageView) this.findViewById(R.id.face_U)).getDrawable().mutate().setAlpha(0);
        ((ImageView) this.findViewById(R.id.face_F)).getDrawable().mutate().setAlpha(0);
        ((ImageView) this.findViewById(R.id.face_D)).getDrawable().mutate().setAlpha(0);
        ((ImageView) this.findViewById(R.id.face_B)).getDrawable().mutate().setAlpha(0);
        ((ImageView) this.findViewById(R.id.face_L)).getDrawable().mutate().setAlpha(0);
        ((ImageView) this.findViewById(R.id.face_R)).getDrawable().mutate().setAlpha(0);
        ((ImageView) this.findViewById(R.id.face_X)).getDrawable().mutate().setAlpha(255);

        Uri uri = Uri.fromFile(new File(TrainDataUtil.PATH + "/train/" + this.dataItem + ".jpg"));
        ((ImageView)this.findViewById(R.id.face_data)).setImageURI(uri);
    }

    /**
     *
     */
    private View.OnClickListener faceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((ImageView)findViewById(R.id.face_U)).getDrawable().mutate().setAlpha(0);
            ((ImageView)findViewById(R.id.face_F)).getDrawable().mutate().setAlpha(0);
            ((ImageView)findViewById(R.id.face_D)).getDrawable().mutate().setAlpha(0);
            ((ImageView)findViewById(R.id.face_B)).getDrawable().mutate().setAlpha(0);
            ((ImageView)findViewById(R.id.face_L)).getDrawable().mutate().setAlpha(0);
            ((ImageView)findViewById(R.id.face_R)).getDrawable().mutate().setAlpha(0);
            ((ImageView)findViewById(R.id.face_X)).getDrawable().mutate().setAlpha(255);
            ((ImageView)view).getDrawable().mutate().setAlpha(50);
        }
    };

    /**
     *
     */
    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String label = null;
            if(((ImageView)findViewById(R.id.face_U)).getDrawable().getAlpha() == 50) {
                label = "2";
            } else if(((ImageView)findViewById(R.id.face_F)).getDrawable().getAlpha() == 50) {
                label = "0";
            } if(((ImageView)findViewById(R.id.face_D)).getDrawable().getAlpha() == 50) {
                label = "5";
            } if(((ImageView)findViewById(R.id.face_B)).getDrawable().getAlpha() == 50) {
                label = "1";
            } if(((ImageView)findViewById(R.id.face_L)).getDrawable().getAlpha() == 50) {
                label = "4";
            } if(((ImageView)findViewById(R.id.face_R)).getDrawable().getAlpha() == 50) {
                label = "3";
            }if(((ImageView)findViewById(R.id.face_X)).getDrawable().getAlpha() == 50) {
                label = "X";
            }
            if(label != null) {
                dataItem = trainDataUtil.markAsLabelAndGetNext(dataItem, label);
                labeledNum++;
                showDateItem();
                labelNumView.setText(String.format(" %s / %s", labeledNum, itemNum));
            }
        }
    };

    /**
     *
     */
    private void createTextViewOverlay() {
        this.labelNumView = new TextView(this.getApplicationContext());
        this.labelNumView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.labelNumView.setTextColor(Color.WHITE);
        this.labelNumView.setTextSize(30);
        FrameLayout.LayoutParams stopwatchTvLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stopwatchTvLayoutParams.gravity = Gravity.LEFT;
        stopwatchTvLayoutParams.topMargin = 10;
        this.addContentView(this.labelNumView, stopwatchTvLayoutParams);
    }
}

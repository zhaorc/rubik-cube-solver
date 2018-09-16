package tthcc.rubiksolver.solver.faceletlabel;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import tthcc.rubiksolver.solver.faceletlabel.traindata.TrainDataUtil;

public class TrainDataLabelActivity extends AppCompatActivity {
    private static final String TAG = TrainDataLabelActivity.class.getSimpleName();
    private String dataItem;
    private int itemNum = 0;
    private int labeledNum = 0;
    private TextView labelNumView;
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
        this.findViewById(R.id.button_previous).setOnClickListener(this.previousButtonClickListener);
        this.findViewById(R.id.face_data).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), TrainDataFaceActivity.class);
                intent.putExtra("dataItem", dataItem);
                startActivity(intent);
            }
        });

        this.createTextViewOverlay();

        TrainDataUtil.getInstance().initLabel();
        int[] size = TrainDataUtil.getInstance().getLabelSize();
        this.labeledNum = size[0];
        if(this.labeledNum == 0) {
            this.findViewById(R.id.button_previous).setEnabled(false);
            this.findViewById(R.id.button_previous).setBackgroundColor(Color.rgb(80,80,80));
        }
        this.itemNum = size[1];

        this.labelNumView.setText(String.format(" %s / %s", labeledNum+1, itemNum));
        String[] value =  TrainDataUtil.getInstance().getNextItem();
        this.dataItem = value[0];
        //XXX
        Log.i(TAG, "this.dataItem=" + this.dataItem);
        this.showDataItem(value);
    }

    @Override
    protected void onDestroy() {
        TrainDataUtil.getInstance().destroy();
        super.onDestroy();
    }

    /**
     *
     */
    private void showDataItem(String[] value) {
        String face = value[1];
        if(face.equals("2")) {
            ((ImageView) this.findViewById(R.id.face_U)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_U)).getDrawable().mutate().setAlpha(0);
        }

        if(face.equals("0")) {
            ((ImageView) this.findViewById(R.id.face_F)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_F)).getDrawable().mutate().setAlpha(0);
        }
        if(face.equals("5")) {
            ((ImageView) this.findViewById(R.id.face_D)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_D)).getDrawable().mutate().setAlpha(0);
        }
        if(face.equals("1")) {
            ((ImageView) this.findViewById(R.id.face_B)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_B)).getDrawable().mutate().setAlpha(0);
        }
        if(face.equals("4")) {
            ((ImageView) this.findViewById(R.id.face_L)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_L)).getDrawable().mutate().setAlpha(0);
        }
        if(face.equals("3")) {
            ((ImageView) this.findViewById(R.id.face_R)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_R)).getDrawable().mutate().setAlpha(0);
        }
        if(face.equals("X")) {
            ((ImageView) this.findViewById(R.id.face_X)).getDrawable().mutate().setAlpha(50);
        }
        else {
            ((ImageView) this.findViewById(R.id.face_X)).getDrawable().mutate().setAlpha(100);
        }
        Uri uri = Uri.fromFile(new File(TrainDataUtil.PATH + "/train/" + value[0] + ".jpg"));
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
                String[] value = TrainDataUtil.getInstance().markAsLabelAndGetNext(dataItem, label);
                dataItem = value[0];
                labeledNum++;
                labelNumView.setText(String.format(" %s / %s", labeledNum+1, itemNum));
                showDataItem(value);
            }
            if(!findViewById(R.id.button_previous).isEnabled()) {
                findViewById(R.id.button_previous).setEnabled(true);
                findViewById(R.id.button_previous).setBackgroundColor(Color.rgb(0x4d,0xb6,0x5f));
            }
        }
    };

    /**
     *
     */
    private View.OnClickListener previousButtonClickListener = new View.OnClickListener() {
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
                //dataItem =
                String[] value = TrainDataUtil.getInstance().markAsLabelAndGetPrevious(dataItem, label);
                dataItem = value[0];
                labeledNum--;
                labelNumView.setText(String.format(" %s / %s", labeledNum+1, itemNum));
                showDataItem(value);
            }
            if(labeledNum == 0) {
                findViewById(R.id.button_previous).setEnabled(false);
                findViewById(R.id.button_previous).setBackgroundColor(Color.rgb(80,80,80));
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

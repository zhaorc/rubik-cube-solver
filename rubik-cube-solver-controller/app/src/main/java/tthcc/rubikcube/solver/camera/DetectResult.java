package tthcc.rubikcube.solver.camera;

import android.graphics.Bitmap;
import android.os.Handler;

import com.catalinjurjiu.rubikdetector.model.RubikFacelet;

public class DetectResult {

    private RubikFacelet[][] facelets;
    private Bitmap faceBitmap;


    public RubikFacelet[][] getFacelets() {
        return facelets;
    }

    public void setFacelets(RubikFacelet[][] facelets) {
        this.facelets = facelets;
    }

    public Bitmap getFaceBitmap() {
        return faceBitmap;
    }

    public void setFaceBitmap(Bitmap faceBitmap) {
        this.faceBitmap = faceBitmap;
    }

}

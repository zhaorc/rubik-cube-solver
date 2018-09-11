package tthcc.rubiksolver.solver.faceletlabel.traindata;

import android.graphics.Bitmap;
import android.util.Log;

import com.catalinjurjiu.rubikdetector.model.Point2d;
import com.catalinjurjiu.rubikdetector.model.RubikFacelet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * 样本读写工具<br>
 * 样本文件存储的根目录为：/sdcard/rubiks-cube <br>
 *     <pre>
 *         目录结构：
 *         |---/train
 *         |     |---{timestamp}_original.jpg   --  原始图片
 *         |     |---{timestamp}_face.jpg       --  魔方区域切割图片
 *         |     |---{timestamp}_facelet_{n}.jpg  --  第n魔方方块切割图片
 *         |     |---{timestamp}_info.txt         -- 魔方在原始图片中的位置信息
 *         |
 *         |---data.txt
 *         |---label.txt
 *
 *     </pre>
 *
 */
public class TrainDataUtil {
    private static final String TAG = TrainDataUtil.class.getSimpleName();
    private static final String PATH = "/storage/emulated/0/rubiks-cube";
    private BufferedReader dataReader = null;
    private FileWriter dataWriter = null;

    /**
     *
     */
    public TrainDataUtil() {
        try{
            // data file
            File dataFile = new File(TrainDataUtil.PATH + "/data.txt");
            if(!dataFile.exists()) {
                dataFile.createNewFile();
            }
            this.dataReader = new BufferedReader(new FileReader(dataFile));
            this.dataWriter = new FileWriter(dataFile, true);
            //TODO label file
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
    }

    /**
     *
     * @return
     */
    public int getDataSize() {
        int size = 0;
        try{
            String line;
            while((line = dataReader.readLine()) != null) {
                if(line.length() > 0) {
                    size++;
                }
            }
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        return size;
    }

    /**
     * 保存样本
     * @param bitmap
     * @param rubikFacelets
     */
    public void saveFace(Bitmap bitmap, RubikFacelet[][] rubikFacelets) {
        String prefix = String.valueOf(System.currentTimeMillis());
        //原始文件
        this.saveOriginalImage(bitmap, prefix);
        //face
        this.saveSubImage(bitmap, rubikFacelets[0][0].corners()[0],
                                  rubikFacelets[0][2].corners()[1],
                                  rubikFacelets[2][0].corners()[0],
                                  rubikFacelets[2][2].corners()[1],
                                  prefix, "face");
        //facelet
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                this.saveSubImage(bitmap, rubikFacelets[i][j].corners()[0],
                        rubikFacelets[i][j].corners()[1],
                        rubikFacelets[i][j].corners()[2],
                        rubikFacelets[i][j].corners()[3],
                        prefix, "facelet" +"_" + (i*3+j));
            }
        }
        // TODO detect info
        // TODO datafile
    }

    /**
     *
     * @param bitmap
     * @param filenamePrefix
     */
    private void saveOriginalImage(Bitmap bitmap, String filenamePrefix) {
        FileOutputStream originalOuts = null;
        try {
            originalOuts = new FileOutputStream(String.format("%s/train/%s_original.jpg", TrainDataUtil.PATH, filenamePrefix));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, originalOuts);
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        finally {
            try{
                if(originalOuts != null) {
                    originalOuts.close();
                }
            }
            catch(Exception exp) {
            }
        }
    }

    /**
     *
     * @param bitmap
     * @param topleft
     * @param topright
     * @param bottomleft
     * @param bottomright
     * @param filenamePrefix
     * @param filenameSufix
     */
    private void saveSubImage(Bitmap bitmap,
                              Point2d topleft, Point2d topright, Point2d bottomleft, Point2d bottomright,
                              String filenamePrefix, String filenameSufix) {
        int x1 = topleft.x < bottomleft.x ? (int) topleft.x : (int) bottomleft.x;
        int y1 = topleft.y < topright.y ? (int) topleft.y : (int) topright.y;
        int x2 = topright.x > bottomright.x ? (int) topright.x : (int) bottomright.x;
        int y2 = bottomleft.y > bottomright.y ? (int)bottomleft.x : (int)bottomright.y;
        int w = x2 - x1;
        int h = y2 - y1;
        Bitmap sub = Bitmap.createBitmap(bitmap, x1, y1, w, h);
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(String.format("%s/train/%s_%s.jpg", TrainDataUtil.PATH, filenamePrefix, filenameSufix));
            // TODO 统一图片尺寸
            sub.compress(Bitmap.CompressFormat.JPEG, 100, fout);
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        finally {
            try{
                if(fout != null) {
                    fout.close();
                }
            }
            catch (Exception exp) {
                Log.e(TAG, exp.getMessage(), exp);
            }
        }
    }
}

package tthcc.rubiksolver.solver.faceletlabel.traindata;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.catalinjurjiu.rubikdetector.model.Point2d;
import com.catalinjurjiu.rubikdetector.model.RubikFacelet;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.List;

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
 *         |---data_facelet.txt     -- 魔方facelet数据集
 *         |---label_facelet.txt    -- 魔方facelet标签
 *         |---data_face.txt        -- 魔方face数据集
 *
 *
 *     </pre>
 *
 */
public class TrainDataUtil {
    private static final String TAG = TrainDataUtil.class.getSimpleName();
    public static final String PATH = "/storage/emulated/0/rubiks-cube";
    private static final int FaceletWidth = 120;
    private static final int FaceWidth = 360;
    private FileWriter dataFaceletWriter = null;
    private FileWriter dataFaceWriter = null;
    private FileWriter dataOriginalWriter = null;

    private RandomAccessFile dataLabelFile = null;
    private RandomAccessFile dataFaceletFile = null;
    private RandomAccessFile bookmarkFile = null;
    private String currentItem = null;
    private String bookmark = null;
    private int labeledNum = 0;

    private static TrainDataUtil INSTANCE = new TrainDataUtil();

    private TrainDataUtil(){};

    /**
     *
     * @return
     */
    public static TrainDataUtil getInstance() {
        return INSTANCE;
    }

    /**
     *
     */
    public void init() {
        try{
            File dir = new File(TrainDataUtil.PATH + "/train");
            if(!dir.exists()) {
                dir.mkdirs();
            }
            // data_facelet file
            File dataFaceletFile = new File(TrainDataUtil.PATH + "/data_facelet.txt");
            if(!dataFaceletFile.exists()) {
                dataFaceletFile.createNewFile();
            }
            this.dataFaceletWriter = new FileWriter(dataFaceletFile, true);
            // data_face file
            File dataFaceFile = new File(TrainDataUtil.PATH + "/data_face.txt");
            if(!dataFaceFile.exists()) {
                dataFaceFile.createNewFile();
            }
            this.dataFaceWriter = new FileWriter(dataFaceFile, true);
            // data_original file
            File dataOriginalFile = new File(TrainDataUtil.PATH + "/data_original.txt");
            if(!dataOriginalFile.exists()) {
                dataOriginalFile.createNewFile();
            }
            this.dataOriginalWriter = new FileWriter(dataOriginalFile, true);
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
    }

    /**
     *
     */
    public void initLabel() {
        try{
            // bookmark
            File bkfile = new File(TrainDataUtil.PATH + "/label_bookmark.txt");
            if(!bkfile.exists()) {
                bkfile.createNewFile();
            }
            this.bookmarkFile = new RandomAccessFile(bkfile, "rw");
            // label file
            File labelFile = new File(TrainDataUtil.PATH + "/label_facelet.txt");
            if(!labelFile.exists()) {
                labelFile.createNewFile();
            }
            this.dataFaceletFile = new RandomAccessFile(TrainDataUtil.PATH + "/data_facelet.txt", "r");
            this.dataLabelFile = new RandomAccessFile(labelFile,"rw");
            // 读取bookmark
            this.bookmark = this.bookmarkFile.readLine();
            // 数据集和标签文件定位到bookmark位置
            this.labeledNum = 0;
            if(this.bookmark != null) {
                String line;
                while ((line = this.dataFaceletFile.readLine()) != null) {
                    this.labeledNum++;
                    if (line.equals(this.bookmark)) {
                        this.labeledNum--;
                        this.dataFaceletFile.seek(this.dataFaceletFile.getFilePointer() - line.length() - 2);
                        break;
                    }
                }
                this.dataLabelFile.seek((line.length() + 4) * labeledNum);
            }
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
    }

    /**
     *
     */
    public void destroy() {
       try{
           if(this.dataFaceletWriter != null) {
               this.dataFaceletWriter.close();
           }
           if(this.dataFaceWriter != null) {
               this.dataFaceWriter.close();
           }
           if(this.dataOriginalWriter != null) {
               this.dataOriginalWriter.close();
           }
           if(this.dataFaceletFile != null) {
               this.dataFaceletFile.close();
           }
           if(this.dataLabelFile != null) {
               this.dataLabelFile.close();
           }
           //XXX
           Log.i(TAG, "this.bookmark=" + this.bookmark);
           if(this.bookmarkFile != null) {
               if(this.bookmark == null) {
                   this.bookmarkFile.close();
               }
               else {
                   this.bookmarkFile.seek(0);
                   this.bookmarkFile.writeBytes(this.bookmark);
                   this.bookmarkFile.close();
               }
           }
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
        BufferedReader dataReader = null;
        try{
            String line;
            dataReader = new BufferedReader(new FileReader(TrainDataUtil.PATH + "/data_original.txt"));
            while((line = dataReader.readLine()) != null) {
                if(line.length() > 0) {
                    size++;
                }
            }
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        finally {
            try{
                if(dataReader != null) {
                    dataReader.close();
                }
            }
            catch(Exception exp) {
                Log.e(TAG, exp.getMessage(), exp);
            }
        }
        return size;
    }

    /**
     *
     * @return 0 - 已标记的数量, 1 - 总数据梁
     */
    public int[] getLabelSize() {
        int[] size = new int[2];
        BufferedReader dataReader = null;
        BufferedReader labelReader = null;
        try{
            int dataSize = 0;
            String line;
            dataReader = new BufferedReader(new FileReader(TrainDataUtil.PATH + "/data_facelet.txt"));
            while((line = dataReader.readLine()) != null) {
                if(line.length() > 0) {
                    dataSize++;
                }
            }
            size[0] = this.labeledNum;
            size[1] = dataSize;
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        finally {
            try {
                if(dataReader != null) {
                    dataReader.close();
                }
                if(labelReader != null) {
                    labelReader.close();
                }
            }
            catch(Exception exp) {
                Log.e(TAG, exp.getMessage(), exp);
            }
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
        //XXX
        Log.i(TAG, "angle=" + rubikFacelets[0][0].angle);
        //face
        this.saveSubImage(bitmap, rubikFacelets[0][0], rubikFacelets[0][2], rubikFacelets[2][0], rubikFacelets[2][2],
                          prefix, "face");
        //facelet
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                this.saveSubImage(bitmap, rubikFacelets[i][j],
                        prefix, "facelet" +"_" + (i*3+j));
            }
        }
        // detect info
        this.saveInfo(rubikFacelets, prefix);
        // data index file
        this.saveIndex(prefix);
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
     * @param facelet0
     * @param facelet1
     * @param facelet2
     * @param facelet3
     * @param filenamePrefix
     * @param filenameSufix
     */
    private void saveSubImage(Bitmap bitmap, RubikFacelet facelet0,
                                             RubikFacelet facelet1,
                                             RubikFacelet facelet2,
                                             RubikFacelet facelet3,
                              String filenamePrefix, String filenameSufix) {
        int x1 = this.min(this.min(facelet0.corners()[0].x, facelet0.corners()[1].x, facelet0.corners()[2].x, facelet0.corners()[3].x),
                          this.min(facelet1.corners()[0].x, facelet1.corners()[1].x, facelet1.corners()[2].x, facelet1.corners()[3].x),
                          this.min(facelet2.corners()[0].x, facelet2.corners()[1].x, facelet2.corners()[2].x, facelet2.corners()[3].x),
                          this.min(facelet3.corners()[0].x, facelet3.corners()[1].x, facelet3.corners()[2].x, facelet3.corners()[3].x));
        int y1 = this.min(this.min(facelet0.corners()[0].y, facelet0.corners()[1].y, facelet0.corners()[2].y, facelet0.corners()[3].y),
                          this.min(facelet1.corners()[0].y, facelet1.corners()[1].y, facelet1.corners()[2].y, facelet1.corners()[3].y),
                          this.min(facelet2.corners()[0].y, facelet2.corners()[1].y, facelet2.corners()[2].y, facelet2.corners()[3].y),
                          this.min(facelet3.corners()[0].y, facelet3.corners()[1].y, facelet3.corners()[2].y, facelet3.corners()[3].y));

        int x2 = this.max(this.max(facelet0.corners()[0].x, facelet0.corners()[1].x, facelet0.corners()[2].x, facelet0.corners()[3].x),
                          this.max(facelet1.corners()[0].x, facelet1.corners()[1].x, facelet1.corners()[2].x, facelet1.corners()[3].x),
                          this.max(facelet2.corners()[0].x, facelet2.corners()[1].x, facelet2.corners()[2].x, facelet2.corners()[3].x),
                          this.max(facelet3.corners()[0].x, facelet3.corners()[1].x, facelet3.corners()[2].x, facelet3.corners()[3].x));
        int y2 = this.max(this.max(facelet0.corners()[0].y, facelet0.corners()[1].y, facelet0.corners()[2].y, facelet0.corners()[3].y),
                          this.max(facelet1.corners()[0].y, facelet1.corners()[1].y, facelet1.corners()[2].y, facelet1.corners()[3].y),
                          this.max(facelet2.corners()[0].y, facelet2.corners()[1].y, facelet2.corners()[2].y, facelet2.corners()[3].y),
                          this.max(facelet3.corners()[0].y, facelet3.corners()[1].y, facelet3.corners()[2].y, facelet3.corners()[3].y));

        int w = x2 - x1;
        int h = y2 - y1;
//        //XXX
//        Log.i(TAG, "x1=" + x1 + ", y1=" + y1);
//        Log.i(TAG, "x2=" + x2 + ", y2=" + y2);
//        Log.i(TAG, " w=" + w + ", h=" + h);
        int length = w > h ? w : h;
        int size = filenameSufix.startsWith("facelet") ? TrainDataUtil.FaceletWidth : TrainDataUtil.FaceWidth;
        float ratio = (1.0f*size) / (1.0f*length);
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap sub = Bitmap.createBitmap(bitmap, x1, y1, length, length, matrix, false);

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

    /**
     *
     * @param bitmap
     * @param rubikFacelet
     * @param filenamePrefix
     * @param filenameSufix
     */
    private void saveSubImage(Bitmap bitmap, RubikFacelet rubikFacelet,
                              String filenamePrefix, String filenameSufix) {
        int x1 = this.min(rubikFacelet.corners()[0].x, rubikFacelet.corners()[1].x, rubikFacelet.corners()[2].x, rubikFacelet.corners()[3].x);
        int y1 = this.min(rubikFacelet.corners()[0].y, rubikFacelet.corners()[1].y, rubikFacelet.corners()[2].y, rubikFacelet.corners()[3].y);
        int x2 = this.max(rubikFacelet.corners()[0].x, rubikFacelet.corners()[1].x, rubikFacelet.corners()[2].x, rubikFacelet.corners()[3].x);
        int y2 = this.max(rubikFacelet.corners()[0].y, rubikFacelet.corners()[1].y, rubikFacelet.corners()[2].y, rubikFacelet.corners()[3].y);
        int w = x2 - x1;
        int h = y2 - y1;
//        //XXX
//        Log.i(TAG, "x1=" + x1 + ", y1=" + y1);
//        Log.i(TAG, "x2=" + x2 + ", y2=" + y2);
//        Log.i(TAG, " w=" + w + ", h=" + h);
        int length = w > h ? w : h;
        int size = filenameSufix.startsWith("facelet") ? TrainDataUtil.FaceletWidth : TrainDataUtil.FaceWidth;
        float ratio = (1.0f*size) / (1.0f*length);
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap sub = Bitmap.createBitmap(bitmap, x1, y1, length, length, matrix, false);

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


    /**
     *
     * @param rubikFacelets
     * @param filenamePrefix
     */
    private void saveInfo(RubikFacelet[][] rubikFacelets, String filenamePrefix) {
        FileWriter writer = null;
        try{
            writer = new FileWriter(String.format("%s/train/%s_info.txt", TrainDataUtil.PATH, filenamePrefix));
            for(int i=0; i<3; i++) {
                for(int j=0; j<3; j++) {
                    writer.write(JSONObject.toJSONString(rubikFacelets[i][j]));
                    writer.write("\r\n");
                }
            }
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }
            catch(Exception exp) {
                Log.e(TAG, exp.getMessage(), exp);
            }
        }
    }

    /**
     *
     * @param filenamePrefix
     */
    private void saveIndex(String filenamePrefix) {
        try {
            this.dataFaceWriter.write(filenamePrefix+"_face");
            this.dataFaceWriter.write("\r\n");
            this.dataFaceWriter.flush();
            for(int i=0; i<9; i++) {
                this.dataFaceletWriter.write(filenamePrefix+"_facelet_" + i);
                this.dataFaceletWriter.write("\r\n");
            }
            this.dataFaceletWriter.flush();
            this.dataOriginalWriter.write(filenamePrefix + "_original");
            this.dataOriginalWriter.write("\r\n");
            this.dataOriginalWriter.flush();
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
    }

    /**
     * 将数据项item打上label标签
     * @param item
     * @param label
     */
    public String[] markAsLabelAndGetNext(String item, String label) {
        try {
            this.dataLabelFile.writeBytes(String.format("%s,%s%s", item, label, "\r\n"));
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        return this.getNextItem();
    }

    /**
     *
     * @return
     */
    public String[] getNextItem() {
        String[] value = new String[2];
       try{
           this.currentItem = this.dataFaceletFile.readLine();
           // 已标记的标签
           String line = this.dataLabelFile.readLine();
           if(line != null) {
               value[1] = line.split(",")[1];
               this.dataLabelFile.seek(this.dataLabelFile.getFilePointer() - line.length() - 2);
           }
           else {
               value[1] = "";
           }
       }
       catch(Exception exp) {
           Log.e(TAG, exp.getMessage(), exp);
       }
       this.bookmark = this.currentItem;
       value[0] = this.currentItem;
       return value;
    }

    /**
     *
     * @param item
     * @param label
     * @return
     */
    public String[] markAsLabelAndGetPrevious(String item, String label) {
        try {
            this.dataLabelFile.writeBytes(String.format("%s,%s%s", item, label, "\r\n"));
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }

        return this.getPreviousItem();
    }

    /**
     *
     * @return
     */
    public String[] getPreviousItem() {
        String[] value = new String[2];
        int size = this.currentItem.length();
        try{
            // 数据集文件指针
            long pos = this.dataFaceletFile.getFilePointer();
            this.dataFaceletFile.seek(pos - (size + 2) * 2);
            this.currentItem = this.dataFaceletFile.readLine();
            // 标签文件指针
            pos = this.dataLabelFile.getFilePointer();
            this.dataLabelFile.seek(pos - (size  + 4) * 2);
            // 已标记的标签
            String line = this.dataLabelFile.readLine();
            value[1] = line.split(",")[1];
            this.dataLabelFile.seek(this.dataLabelFile.getFilePointer() - line.length() - 2);
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
        this.bookmark = this.currentItem;
        value[0] = this.currentItem;
        return value;
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    private int min(float a, float b, float c, float d) {
        float x = a;
        x = x < b ? x : b;
        x = x < c ? x : c;
        x = x < d ? x : d;

        return (int)x;
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    private int max(float a, float b, float c, float d) {
        float x = a;
        x = x > a ? x : a;
        x = x > b ? x : b;
        x = x > c ? x : c;
        x = x > d ? x : d;

        return (int)x;
    }
}

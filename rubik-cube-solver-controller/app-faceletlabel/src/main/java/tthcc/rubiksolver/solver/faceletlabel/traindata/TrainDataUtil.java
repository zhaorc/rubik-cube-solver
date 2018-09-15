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

    private FileWriter dataLabelWriter = null;
    private BufferedReader dataFaceletReader = null;
    private String currentItem = null;

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
            // label file
            File labelFile = new File(TrainDataUtil.PATH + "/label_facelet.txt");
            if(!labelFile.exists()) {
                labelFile.createNewFile();
            }
            this.dataLabelWriter = new FileWriter(labelFile, true);
            //读取第一个待标记的数据项
            //已标记的数据项
            List<String> labeledItemList = Lists.newArrayList();
            BufferedReader labeledItemReader = new BufferedReader(new FileReader(labelFile));
            //全部数据集
            this.dataFaceletReader = new BufferedReader(new FileReader(TrainDataUtil.PATH + "/data_facelet.txt"));
            String line;
            try {
                while((line = labeledItemReader.readLine()) != null) {
                    if(line.length() > 0) {
                        String[] value = line.split(",");
                        labeledItemList.add(value[0]);
                    }
                }
                while((this.currentItem = this.dataFaceletReader.readLine()) != null) {
                    if(this.currentItem.length() > 0 && !labeledItemList.contains(this.currentItem)) {
                        break;
                    }
                }
            }
            finally {
                if(labeledItemReader != null) {
                    labeledItemReader.close();
                }
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
           if(this.dataLabelWriter != null) {
               this.dataLabelWriter.close();
           }
           if(this.dataFaceletReader != null) {
               this.dataFaceletReader.close();
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
            int labeledSize = 0;
            String line = null;
            dataReader = new BufferedReader(new FileReader(TrainDataUtil.PATH + "/data_facelet.txt"));
            while((line = dataReader.readLine()) != null) {
                if(line.length() > 0) {
                    dataSize++;
                }
            }
            labelReader = new BufferedReader(new FileReader(TrainDataUtil.PATH + "/label_facelet.txt"));
            while((line = labelReader.readLine()) != null) {
                if(line.length() > 0) {
                    labeledSize++;
                }
            }
            size[0] = labeledSize;
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
        //face
        this.saveSubImage(bitmap, rubikFacelets[0][0].corners()[1],
                                  rubikFacelets[0][2].corners()[2],
                                  rubikFacelets[2][0].corners()[0],
                                  rubikFacelets[2][2].corners()[3],
                                  prefix, "face");
        //facelet
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                this.saveSubImage(bitmap, rubikFacelets[i][j].corners()[1],
                        rubikFacelets[i][j].corners()[2],
                        rubikFacelets[i][j].corners()[0],
                        rubikFacelets[i][j].corners()[3],
                        prefix, "facelet" +"_" + (i*3+j));
            }
        }
        // Tdetect info
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
        int y2 = bottomleft.y > bottomright.y ? (int)bottomleft.y : (int)bottomright.y;
        int w = x2 - x1;
        int h = y2 - y1;
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
     * 将数据项item搭上label标签
     * @param item
     * @param label
     */
    public String markAsLabelAndGetNext(String item, String label) {
        try {
            dataLabelWriter.write(String.format("%s,%s%s", item, label, "\r\n"));
            dataLabelWriter.flush();
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
    public String getNextItem() {
       String item = this.currentItem;
       try{
           this.currentItem = this.dataFaceletReader.readLine();
       }
       catch(Exception exp) {
           Log.e(TAG, exp.getMessage(), exp);
       }
       return item;
    }

}

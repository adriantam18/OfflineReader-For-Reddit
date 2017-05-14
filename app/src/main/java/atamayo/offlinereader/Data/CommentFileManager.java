package atamayo.offlinereader.Data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * A class responsible for file access using Android's file system.
 */
public class CommentFileManager {
    Context mContext;

    public CommentFileManager(Context context){
        mContext = context;
    }

    /**
     * Writes a string into a file and saves it
     * @param filename identifier for this file
     * @param data  string to write
     * @return true if write is successful, false otherwise
     */
    public boolean writeToFile(String filename, String data){
        try {
            FileOutputStream fos = mContext.openFileOutput(filename, MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    /**
     * Loads a string saved in a file
     * @param filename name of file to load string from
     * @return string content of file
     */
    public String loadFile(String filename){
        try {
            FileInputStream fileInputStream = mContext.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            StringBuilder builder = new StringBuilder();
            String contents;

            while((contents = bufferedReader.readLine()) != null){
                builder.append(contents);
            }
            bufferedReader.close();

            return builder.toString();
        }catch (FileNotFoundException e){
            Log.e(TAG, e.toString());
            return "";
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return "";
        }
    }

    /**
     * Delets a file
     * @param filename name of file to delete
     * @return true if file is deleted, false otherwise
     */
    public boolean deleteFile(String filename){
        try {
            return mContext.deleteFile(filename);
        }catch (NullPointerException e){
            return false;
        }
    }
}

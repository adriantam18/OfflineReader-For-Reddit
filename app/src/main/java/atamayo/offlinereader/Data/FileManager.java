package atamayo.offlinereader.Data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;

/**
 * A class responsible for file access using Android's file system.
 */
public class FileManager {
    private static final String TAG = "File manager";
    private Context mContext;

    public FileManager(Context context){
        mContext = context;
    }

    /**
     * Writes a string into a file and saves it.
     * @param filename identifier for this file
     * @param data  string to write
     * @return the path to the saved file, or empty if writing failed
     */
    public String writeToFile(String filename, byte[] data){
        FileOutputStream outputStream = null;
        try {
            outputStream = mContext.openFileOutput(filename, MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
            return mContext.getFilesDir().getAbsolutePath() + "/" + filename;
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, e.toString());
            return "";
        } finally {
            if(outputStream != null){
                try{
                    outputStream.close();
                }catch (IOException e){
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    /**
     * Loads a string saved in a file.
     * @param filename name of file to load string from
     * @return string content of file
     */
    public String loadFile(String filename){
        FileInputStream inputStream = null;
        try {
            inputStream = mContext.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();
            String contents;

            while((contents = bufferedReader.readLine()) != null){
                builder.append(contents);
            }
            bufferedReader.close();

            return builder.toString();
        } catch (IOException e){
            Log.e(TAG, e.toString());
            return "";
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e){
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    /**
     * Deletes a file.
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

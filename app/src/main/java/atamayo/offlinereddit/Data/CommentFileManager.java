package atamayo.offlinereddit.Data;

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

public class CommentFileManager {
    Context mContext;

    public CommentFileManager(Context context){
        mContext = context;
    }

    public boolean writeToFile(String filename, String jsonData){
        try {
            FileOutputStream fos = mContext.openFileOutput(filename, MODE_PRIVATE);
            fos.write(jsonData.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

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

    public boolean deleteFile(String filename){
        try {
            return mContext.deleteFile(filename);
        }catch (NullPointerException e){
            return false;
        }
    }
}

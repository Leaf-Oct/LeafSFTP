package cn.leaf.leafsftp.sharedpreferences;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import cn.leaf.leafsftp.LeafApplication;

public class Config {
    private static Config c;
    public String SD_card_path="";
//    public int port=2222;
    public boolean keep_alive=false;
    public boolean is_running=false;
    public boolean has_SD_card=false;
    private File json;
    private JSONObject json_obj;
    public static Config getInstance(){
        if(c==null){
            c=new Config();
        }
        return c;
    }
    private Config(){
        var initial_result=loadConfig();
        if(!initial_result){
            Toast.makeText(LeafApplication.getContext(), "初始化配置信息失败", Toast.LENGTH_LONG).show();
//            generateJks();
            detectMedia();
            saveConfig();
        }
    }

    public boolean saveConfig(){
        try {
            if (json_obj==null){
                json_obj=new JSONObject();
            }
            json_obj.put("SD_card_path", SD_card_path);
//            json_obj.put("port", port);
            json_obj.put("keep_alive", keep_alive);
//            json_obj.put("ssl_enable", ssl_enable);
            json_obj.put("has_SD_card", has_SD_card);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Log.w("json", json_obj.toString());
            json.delete();
            var fw=new FileWriter(json);
            IOUtils.write(json_obj.toString(), fw);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("config json file", "save failed");
            return false;
        }
        return true;
    }
    private boolean loadConfig(){
        json=new File(LeafApplication.getContext().getExternalFilesDir(null).getAbsolutePath()+File.separator+"config.json");
        if (!json.exists()){
            try {
                json.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("config json file", "create failed");
            }
            return false;
        }
        try {
            json_obj=new JSONObject(IOUtils.toString(new FileReader(json)));
            SD_card_path=json_obj.getString("SD_card_path");
//            port=json_obj.getInt("port");
            keep_alive=json_obj.getBoolean("keep_alive");
//            ssl_enable=json_obj.getBoolean("ssl_enable");
            has_SD_card=json_obj.getBoolean("has_SD_card");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("read config", "read json file failed");
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("json format", "resolve json file format error");
            return false;
        }
        return true;
    }
//    检测挂载的存储媒体设备
    public boolean detectMedia(){
        try {
            File[] medias = LeafApplication.getContext().getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            if (medias.length == 0) {
                return true;
            }
            if (medias.length > 1) {
                has_SD_card = true;
                var path = medias[1].getAbsolutePath();
                SD_card_path = path.substring(0, path.toLowerCase(Locale.ROOT).indexOf("/android/data"));
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}

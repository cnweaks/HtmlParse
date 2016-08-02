package com.cnweak.rebash;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cnweak.rebash.htmlpase.HtmlUtils;
import com.cnweak.rebash.htmlpase.PieProgress;
import com.cnweak.rebash.htmlpase.TranslateUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/1/31.
 */
public class GetCodeforgeCode   extends Activity {
    private Button buttonread , buttonsave,buttonprint,buttonplist;
    private TextView textjava;
    private ArrayAdapter<String> mAdapter;

    private String dangqian = null , dangname = null;
    private ListView javalist;

    private Boolean taskstat = true,javates = true;
    // 0      1   2           3             4         5
    private String[] APIURL = new String[] {"http",":","/","www.codeforge.cn","article","259130"};

    private ArrayList<String> javadata = new ArrayList<String>();
    private ArrayList<String>  javaname = new ArrayList<String>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getcodeforgecode);
        javalist = (ListView) findViewById(R.id.view_listjava);
        textjava = (TextView)findViewById(R.id.view_java);
        buttonread = (Button)findViewById(R.id.get_java);
        buttonread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buttonsave = (Button)findViewById(R.id.get_save);
        buttonsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeFileData(dangname,APIURL[5],dangqian);
            }
        });
        buttonprint = (Button)findViewById(R.id.get_print);
        buttonprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buttonplist = (Button)findViewById(R.id.get_list);
        buttonplist.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.CUPCAKE)
            @Override
            public void onClick(View v) {

                if (taskstat){
                    new AsyncHttpTask().execute(APIURL);
                }else {
                    Toast.makeText(GetCodeforgeCode.this, "存在运行中的线程", Toast.LENGTH_SHORT);
                }


            }
        });
    }
    public void writeFileData(String fileName , String pathstr , String message){
        String pathName= "/storage/emulated/0/htmlparse/"+pathstr+"/";
        try {
            File path = new File(pathName);
            File file = new File(path + "/"+fileName);
            if( !path.exists()) { path.mkdir();}
            if( !file.exists()) {path.createNewFile();}
            FileOutputStream stream = new FileOutputStream(file);
            byte[] buf = message.getBytes();
            stream.write(buf);
            stream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public class AsyncHttpTask extends AsyncTask<String ,Void ,Integer> {

        protected Integer doInBackground(String...strings) {
            taskstat = false;
            int result = 0;//，1表示成功，-1表示失败，0为初始状态
            try {
                String httpurl = strings[0]+strings[1]+strings[2]+strings[2]+strings[3]+strings[2]+strings[4]+strings[2]+strings[5];
                Element bodyelement = Jsoup.connect(httpurl).get().body();//从url获得解析后的HTML文档
                Elements itemcontainer = bodyelement.select("tbody>tr");
                for (int c = 0; c < itemcontainer.size(); c++) {
                    String ssss =  itemcontainer.get(c).child(0).text();
                    System.out.println("当前文件序号："+ssss.indexOf(".java"));
                    if(ssss.indexOf(".java")>0){
                        javadata.add("");
                        javaname.add(ssss);
                        System.out.println("java文件："+ssss);
                    }
                    if(ssss.indexOf(".jar")>0){
                        System.out.println("jar文件："+ssss);
                    }
                    if(ssss.indexOf(".png")>0){
                        System.out.println("图片文件："+ssss);
                    }
                    if(ssss.indexOf(".txt")>0) {
                        System.out.println("文本文件：" + ssss);
                    }
                }

                result = 1 ;
            } catch (Exception ex) {
                ex.printStackTrace();
                result = -1;
            }
            taskstat = true;
            return result;
        }
        protected void onPostExecute(Integer result) {
            if (result == -1){
                Toast.makeText(GetCodeforgeCode.this, "加载出错，请重试", Toast.LENGTH_SHORT);
            }
            if (result == 0){
                Toast.makeText(GetCodeforgeCode.this, "加载出错，请重试", Toast.LENGTH_SHORT);
            }
            if (result == 1){
                mAdapter = new ArrayAdapter<String>(GetCodeforgeCode.this, R.layout.html_item_view, javaname);
                if(javaname != null){
                    javalist.setAdapter(mAdapter);
                    javalist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                            String itemname = adapterView.getAdapter().getItem(i).toString();
                            if(getJavaStr(itemname) != null && javates){
                                dangqian =  getJavaStr(itemname);
                                dangname =  itemname;
                                textjava.setText(dangqian);
                                textjava.setMovementMethod(ScrollingMovementMethod.getInstance());
                            }

                            //此处的i虽为指针的位置，其实际也与资源数据位置对应，故此不受影响
                            //Toast.makeText(GetCodeforgeCode.this,"加载出错，请重试",Toast.LENGTH_SHORT);
                        } });
                }
            }
        }

    }


    private String getJavaStr(String javafilename){
        javates = false;
        String javatext = "http://www.codeforge.cn/read/"+APIURL[5]+APIURL[2]+javafilename+"__html";
        String javadata = null;
        try {
            javadata = Jsoup.connect(javatext).get().body().select("div>pre").text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        javates = true;
        return javadata;
    }


}

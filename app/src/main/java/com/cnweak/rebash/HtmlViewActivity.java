package com.cnweak.rebash;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import android.os.*;
import android.widget.Toast;
import com.cnweak.rebash.htmlpase.PieProgress;
import com.cnweak.rebash.htmlpase.HtmlUtils;
import com.cnweak.rebash.htmlpase.*;

/**
 * Created by Administrator on 2016/1/20.
 */
public class HtmlViewActivity  extends Activity {
    private TextView apimethord , apiexample,apitranslate;
    ArrayAdapter<String> mAdapter;
    private ListView apilist;
    private LinearLayout  listlayout , tranlayout;
    private String[] APIURL = new String[]{"http://f.cnweak.com/Bashell/android_smail.html"};
    ArrayList<HtmlUtils>  apigetdata = new ArrayList<HtmlUtils>();
    ArrayList<String>  apigetname = new ArrayList<String>();
    private PieProgress mPieProgress;
    boolean pieRunning;
    int  pieProgress = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_view_html);
        apilist = (ListView) findViewById(R.id.API_list_view);
        apimethord = (TextView)findViewById(R.id.API_item_methord);
		apitranslate = (TextView)findViewById(R.id.API_item_translate);
        apiexample = (TextView)findViewById(R.id.API_item_example);
        mPieProgress = (PieProgress)findViewById(R.id.pie_progress);
        tranlayout = (LinearLayout)findViewById(R.id.translate_layout);
    }
    //处理Item内容
	private void InitView(int i){
		String mthord = apigetdata.get(i).getApi_item_methord();
		apiexample.setText(apigetdata.get(i).getApi_item_example());
		apimethord.setText(mthord);
		apitranslate.setText(getTranslateText(mthord));
	}
//加载时的logo
    final Runnable indicatorRunnable = new Runnable() {
        public void run() {
            pieRunning = true;
            while (pieProgress < 361) {
                mPieProgress.setProgress(pieProgress);
                pieProgress += 2;
                try {Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pieRunning = false;
        }
    };
//取得翻译结果
private String getTranslateText(String text){
	TranslateUtils strbing = new TranslateUtils();
	String tranText = "";
	try
	{
	tranText = strbing.BingTranslate(text);
	}
	catch (Exception e)
	{ e.printStackTrace();}
	return tranText;
}
//菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.html_menu, menu);
        return true;
    }
//Item选择事件
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public boolean onMenuItemSelected(int i,MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_translate:
				InitView(i);
                break;
            case R.id.menu_item_point:
           mPieProgress.setVisibility(View.VISIBLE);
                tranlayout.setWeightSum(1);
           if (!pieRunning) {
                    pieProgress = 0;
                    new Thread(indicatorRunnable).start();
            }
                (new AsyncHttpTask()).execute(APIURL);
             break;
            case  R.id.menu_item_java:
                startActivity(new Intent(this,GetCodeforgeCode.class));
                break;
            default:
             break;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public class AsyncHttpTask extends AsyncTask<String ,Void ,Integer>{

        protected Integer doInBackground(String...strings) {
            int result = 0;//，1表示成功，-1表示失败，0为初始状态
            try {
                Elements bodyelement = Jsoup.connect(strings[0]).get().body().getElementsByTag("tr");//从url获得解析后的HTML文档
                for (int c = 0; c < bodyelement.size(); c++) {
                    Element sindex = bodyelement.get(c).children().first();//序号，以0-F的16进制计数
                    Element siname = sindex.nextElementSibling();//获得序号后的第一个元素名称
                    Element sitext = siname.nextElementSibling();//获得名称后的第一个元素简介
                    Element exampe = sitext.nextElementSibling();//获得简介后的第一个元素为举例
                    HtmlUtils sielement = new HtmlUtils();
                    sielement.setApi_item_id(sindex.text());//存放序号
                    sielement.setApi_item_name(siname.text());//存放名称
                    apigetname.add(siname.text());//列表存放名称
                    if (sitext.hasText()){
                        sielement.setApi_item_methord(sitext.text());//有内容时存放用法
                    }else {
                        sielement.setApi_item_methord("此用法暂未找到用法或不适用");//无内容时存放
                    }
                    if (sitext.hasText()){
                        sielement.setApi_item_example(exampe.text());//有内容时存放举例
                    }else {
                        sielement.setApi_item_example("此举例暂未找到或不适用");//无内容时存放
                    }
                    System.out.println("结果值："+sitext.hasText());
                /*
                System.out.println(""
                + "，\n序号" + sindex.text()
                + "，\n名称" + siname.text()
                + "，\n简介" + sitext.text()
                + "，\n举例" + exampe.text());
                */
                    apigetdata.add(sielement);//添加到组，供后续操作
                }
                result = 1 ;
            } catch (Exception ex) {
                ex.printStackTrace();
                result = -1;
            }
            return result;
        }
        protected void onPostExecute(Integer result) {
            if (result == -1){
                Toast.makeText(HtmlViewActivity.this,"加载出错，请重试",Toast.LENGTH_SHORT);
            }
            if (result == 0){
                Toast.makeText(HtmlViewActivity.this, "加载出错，请重试", Toast.LENGTH_SHORT);
            }
            if (result == 1){
               mAdapter = new ArrayAdapter<String>(HtmlViewActivity.this, R.layout.html_item_view, apigetname);
               apilist.setAdapter(mAdapter);
                apilist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                        // String itemname = adapterView.getAdapter().getItem(i).toString();
                        //此处的i虽为指针的位置，其实际也与资源数据位置对应，故此不受影响
                        InitView(i);
                        mPieProgress.setVisibility(View.INVISIBLE);
                        tranlayout.setMinimumHeight(0);
                        Toast.makeText(HtmlViewActivity.this,"加载出错，请重试",Toast.LENGTH_SHORT);
                    } }); }
        }
    }


    /***
     * parents()获取父级的同类
     * accumulateParents获取根
     * child获取元素的子元素，并从 0 开始索引编号。
     * children获取此元素的子元素。
     * textNodes 获取此子元素的可见文本。列表不可修改，可修改文本
     * dataNodes获取元素的子数据节点。列表不可修改，可修改数据。
     * select选择器，使用dom查询匹配所指定匹配的元素
     * appendChild向此元素添加子子元素。
     * prependChild将元素添加到子元素的首位。
     * insertChildren将当前节点转移到指定元素索引处。
     * appendElement创建一个新元素的标记名称，并将其添加为最后一个子级。
     * prependElement创建一个新元素的标记名称，并将其添加为第一个孩子。
     * appendText创建并将一个新的文本节点追加到此元素。
     * prependText创建并添加一个新的文本节点到此元素。
     * append将内部 HTML 添加到此元素。提供的 HTML 将被解析，和每个节点追加到子元素到结束点。
     * prepend将内部 HTML 添加到此元素。提供的 HTML 将被解析，和每个节点其前面添加的元素的子元素开始。
     * before 将指定的 HTML (作为前一个同级) 插入到 DOM 在此元素之前。
     * after插入指定的 HTML DOM 后此元素 (如下面的兄弟姐妹)。
     * empty 删除所有元素的子节点
     * wrap换行此元素周围的 HTML。
     * siblingElements获取一个同级元素。如果元素具有没有同级元素，将返回一个空列表。
     * nextElementSibling获取此元素的下一个同级元素
     * previousElementSibling获取此元素的上元一个同级。
     * firstElementSibling获取此元素的第一个元素兄弟。
     * elementSiblingIndex其元素同级列表中获取此元素的列表索引。例如，如果这是的第一个元素
     * lastElementSibling获取此元素的最后一个元素同级
     * getElementsByTag指定的标记名称，包括和递归下级元素。
     * getElementById查找按 ID，包括或在此元素下的元素。
     * getElementsByClass发现有此类，包括或在此元素下的元素。大小写不敏感。
     * getElementsByAttribute查找已命名的属性集的元素。大小写不敏感。
     * getElementsByAttributeStarting与所提供的前缀开始一个属性名称的元素
     * getElementsByAttributeValue查找具有特定值的属性的元素。大小写不敏感。
     * getElementsByAttributeValueNot查找元素不具有此属性，或有一个不同的值。大小写不敏感。
     * getElementsByAttributeValueStarting查找具有以值前缀开头的属性元素。大小写不敏感。
     * getElementsByAttributeValueEnding查找具有以价值后缀结尾的属性元素。大小写不敏感。
     * getElementsByAttributeValueContaining查找具有的属性值包含匹配字符串的元素。大小写不敏感。
     * getElementsByAttributeValueMatching查找具有的属性的值与提供的正则表达式相匹配的元素。
     * getElementsByIndexLessThan查找的元素的同级索引小于所提供的索引。
     * getElementsByIndexGreaterThan查找的元素的同级索引大于所提供的索引。
     * getElementsByIndexEquals查找此同级元素的给定同级元素
     * getElementsContainingText查找包含指定的字符串的元素。搜索是大小写不敏感的。可直接显示的文本
     * getElementsContainingOwnText查找直接包含指定的字符串的元素。搜索是大小写不敏感的。必须直接显示的文本
     * getElementsMatchingText查找的元素的文本匹配提供的正则表达式。
     * getElementsMatchingText查找的元素的文本匹配提供的正则表达式。
     * getElementsMatchingOwnText查找其自己的文本匹配提供的正则表达式的元素。
     * getElementsMatchingOwnText查找元素的文本匹配正则表达式。
     * getAllElements找到此元素 (包括自我，和子元素) 下的所有元素。
     * text获取此元素及其所有子级的合并案文。
     * ownText获取由只; 此元素的文本不会所有子元素的合并案文。
     * hasText如果此元素具有任何文本内容 (即不只是空白) 进行测试。
     * data获取此元素的组合的数据
     * className获取此元素的"class"属性，其中可能包括多个类名称，空间的文本值
     * hasClass如果此元素具有一个类的测试。大小写不敏感。
     * addClass将一个类名称添加到此元素
     * removeClass删除此元素
     * toggleClass切换此元素
     * val获取窗体元素 (输入、 文本等) 的值。
     * html检索元素的内部 HTML
     *
     *
     *
     *
     *
     *
     * */

}

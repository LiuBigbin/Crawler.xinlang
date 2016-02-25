package xinlangcrawler;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;



public class Spider {   
    private  URL url = null;
    public Spider(){
    }
    public String getHtmlStringByUrl(String url_str) throws Exception{          
        try {
            url = new URL(url_str);
        } catch (MalformedURLException e) {
        	System.out.println("new URL 失败");
            e.printStackTrace();
            throw e;
        }
        
        String charset = "utf-8";
        int sec_cont = 1000;
        String htm_str = null;
        try {
            URLConnection url_con = url.openConnection();
            url_con.setDoOutput(false);
            url_con.setRequestProperty("Cookie", "SINAGLOBAL=5269825584003.646.1449204385110; ULV=1452825118411:7:6:5:4106686441496.2544.1452825118395:1452773362579; SUBP=0033WrSXqPxfM72wWs9jqgMF55529P9D9WFvOun8Ta7Z7NS.HZBc2Zp75JpV2hMRS05ceo57e2WpMC4odcXt; SUHB=0pHhPxE7AXzfEu; UOR=,,login.sina.com.cn; myuid=5828995527; un=787062000@qq.com; YF-Page-G0=061259b1b44eca44c2f66c85297e2f50; SUB=_2AkMhxNbEdcNhrAZZmvAcxW7nbopXzQDzudDzME7cZ2JCMnoQgT5nqiRotBF_DN7Ym0e6s1AEmrIgRKPODtmB7yWthvH0dT32gr4.; YF-Ugrow-G0=57484c7c1ded49566c905773d5d00f82; _s_tentry=login.sina.com.cn; Apache=4106686441496.2544.1452825118395; YF-V5-G0=694581d81c495bd4b6d62b3ba4f9f1c8; login_sid_t=5b0c6296a51c8610e9244cfa23b959c9");
            url_con.setReadTimeout(10 * sec_cont);
            url_con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
            InputStream htm_in = url_con.getInputStream();
            
            htm_str = InputStream2String(htm_in,charset);
             
        }catch(Exception e){
			System.out.println("尼玛，又被封端口了，请重新运行程序！");
			e.printStackTrace();
			throw e;
			} 
        	
        return htm_str;
    }
    
    private String InputStream2String(InputStream in_st,String charset) throws IOException{
        BufferedReader buff = new BufferedReader(new InputStreamReader(in_st, charset));
        StringBuffer res = new StringBuffer();
        String line = "";
        while((line = buff.readLine()) != null){
            res.append(line);
        }
        return res.toString();
    }

    
   
   
    
}
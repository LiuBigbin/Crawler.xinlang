package xinlangcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import net.sf.json.JSONObject;

/*
 * 给指定的目录，会遍历该目录下所有的微博的目录，根据微博目录名获得对应的微博id
 * 然后自动爬取该微博的转发信息，并将该信息存储在对应的微博目录中
 * 如果微博目录里面有转发信息的断点文件，那么直接的读取断点文件爬取
 * 没有断点文件的话，如果文件夹里面已经有转发文件了，那么直接跳过
 * 没有转发文件的话，那么说明该微博还没有爬取过转发信息，直接进行爬取
 */
public class Crawler4weiboRepost {

	private String fileDir = null;
	private OutputStreamWriter rePostOut = null;
	private BufferedReader urlReader = null;
	

	public Crawler4weiboRepost(String fileDir) {
		this.fileDir = fileDir;
	}

	public void work() {
		File Dir = new File(fileDir);
		if(!Dir.exists() || !Dir.isDirectory()){
			System.err.println(fileDir+"不是目录，或者不存在");
			return ;
		}
		String[] FileList = Dir.list();
		int success = 0;
		int filenum = 0;
		int fail = 0;
		for(String dirName: FileList){
			dirName = fileDir+"\\"+dirName;
			File dir = new File(dirName);
			if(dir.isFile()){
				System.out.println(dirName + "为文件，不是目录，忽略！");
				filenum++;
				continue;
			}
				
			String id = dirName.substring(dirName.lastIndexOf('_')+1, dirName.length());
			String repostBreakPointName = dirName+"\\"+"repostBreakPoint.txt";
			File repostrepostBreakPointNameFile = new File(repostBreakPointName);
			//断点文件存在
			if(repostrepostBreakPointNameFile.exists()){
				System.out.println("断点文件存在："+repostBreakPointName);
				try {
					urlReader = new BufferedReader(new FileReader(repostrepostBreakPointNameFile));
					String url = urlReader.readLine();
					url = url.trim();
					if(url == null || "".equals(url)){
						System.out.println("断点文件为空："+repostBreakPointName);
						continue;
					}
					if(work(dirName, url)){
						success++;
						System.out.println("处理文件夹：" + dirName + "成功！！！！！");
					}
					else{
						System.err.println("处理文件夹：" + dirName + "失败！！！！！");
						fail++;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				String repostName = dirName+"\\"+"repost.txt";
				File repostFile = new File(repostName);
				if(repostFile.exists() && repostFile.length() != 0){
					System.out.println("转发文件存在："+repostName+"直接跳过！!!!");
					continue;
				}
				else{
					String url = "http://weibo.com/aj/v6/mblog/info/big?ajwvr=6&id="+id;
					if(work(dirName, url)){
						success++;
						System.out.println("处理文件夹：" + dirName + "成功！！！！！");
					}
					else{
						System.err.println("处理文件夹：" + dirName + "失败！！！！！");
						fail++;
					}
				}
			}
		}
		System.out.println("一共成功处理文件夹个数为："+success);
		System.out.println("一共处理失败的文件夹个数为："+fail);
		System.out.println("忽略的文件个数为："+filenum);
	}
	

	private boolean work(String dirName, String url){
		//开始根据链接获取转发信息
		String repostName = dirName + "\\" + "repost.txt";
		
		try {
			File repostFile = new File(repostName);
			if(!repostFile.exists())
				repostFile.createNewFile();
			rePostOut = new OutputStreamWriter(new FileOutputStream(repostName, true), "utf-8");
			Spider spider = new Spider();
			while(url!=null && !"".equals(url)){
				String htmlStr = spider.getHtmlStringByUrl(url);
				
				JSONObject data = new JSONObject(htmlStr);
				JSONObject dataJson = data.optJSONObject("data");
				htmlStr = dataJson.optString("html");
				
				HashMap<String, Object> map = getRepostersFromhtmlStr(htmlStr);
				ArrayList<Reposter> reposterList = (ArrayList<Reposter>)map.get("reposters");
				if(reposterList != null && reposterList.size() != 0){
					for(Reposter rp: reposterList){
						String line = "";
						line += rp.getUid()+"    ";
						line += rp.getName()+"    ";
						line += rp.getTime()+"    ";
						line += rp.getWeiboUrl()+"    ";
						line += rp.getMid()+"\n";
						rePostOut.write(line);
					}
					System.out.println("成功获得链接数据："+url);
					String nextUrl = (String)map.get("next");
					if(nextUrl == null || "".equals(nextUrl))
						url = "";
					else{
						url = url.substring(0, url.indexOf("id=")) + nextUrl;
					}
					
				}
				else{
					String repostBreakPointName = dirName+"\\"+"repostBreakPoint.txt";
					System.out.println("获取链接数据："+url + "失败,url："+url+"存入断点文件："+repostBreakPointName+"！！！！！！！！");
					OutputStreamWriter repostBreakPoint = new OutputStreamWriter(new FileOutputStream(repostBreakPointName, false), "utf-8");
					repostBreakPoint.write(url+"\n");
					repostBreakPoint.close();
					rePostOut.close();
					return false;
				}
				
			}
			rePostOut.close();
			//删除断点文件
			String repostBreakPointName = dirName+"\\"+"repostBreakPoint.txt";
			File repostBreakPointFile = new File(repostBreakPointName);
			if(url == null || "".equals(url)){
				if(repostBreakPointFile.exists())
					repostBreakPointFile.delete();
			}
		
		} catch (Exception e){
			e.printStackTrace();
			try{
				String repostBreakPointName = dirName+"\\"+"repostBreakPoint.txt";
				System.out.println("获取链接数据："+url + "失败,url："+url+"存入断点文件："+repostBreakPointName+"！！！！！！！！");
				OutputStreamWriter repostBreakPoint = new OutputStreamWriter(new FileOutputStream(repostBreakPointName, false), "utf-8");
				repostBreakPoint.write(url+"\n");
				repostBreakPoint.close();
				rePostOut.close();
			} catch (Exception ex){
				ex.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	private HashMap<String, Object> getRepostersFromhtmlStr(String htmlStr){
		HashMap<String, Object> map = new HashMap<String, Object>();
		if(htmlStr == null || "".equals(htmlStr))
			return map;
		try {
			String htmlStrnext = htmlStr;
			String htmlStrTime = htmlStr;
			Parser parser4Repost = new Parser(htmlStr);
			Parser parser4Time = new Parser(htmlStrTime);
			Parser parser4Next = new Parser(htmlStrnext);
			
			HasAttributeFilter repostFilter = new HasAttributeFilter("action-type", "feed_list_forward");
			HasAttributeFilter timeFilter = new HasAttributeFilter("node-type", "feed_list_item_date");
			HasAttributeFilter nextFilter = new HasAttributeFilter("class", "page next S_txt1 S_line1");
			
			NodeList repostList = parser4Repost.extractAllNodesThatMatch(repostFilter);
			NodeList timeList = parser4Time.extractAllNodesThatMatch(timeFilter);
			NodeList nextList = parser4Next.extractAllNodesThatMatch(nextFilter);
			
			if(repostList.size() == 0 || timeList.size() == 0 || repostList.size() != timeList.size())
				return map;
			ArrayList<Reposter> rpl = new ArrayList<Reposter>();
			for(int i=0; i<timeList.size(); i++){
				Reposter reposter = new Reposter();
				LinkTag reNode = (LinkTag)repostList.elementAt(i);
				LinkTag timeNode = (LinkTag)timeList.elementAt(i);
				String actionData = reNode.getAttribute("action-data");
				String url = actionData.substring(actionData.lastIndexOf("url"));
				String weiboUrl = url.substring(url.indexOf('=')+1, url.indexOf('&'));
				String mid = url.substring(url.indexOf("mid=")+4, url.indexOf("&name"));
				String name = url.substring(url.indexOf("name=")+5, url.indexOf("&uid"));
				String uid = url.substring(url.indexOf("uid=")+4, url.indexOf("&domain"));
				String time = timeNode.getAttribute("title");
				reposter.setUid(uid);
				reposter.setMid(mid);
				reposter.setName(name);
				reposter.setTime(time);
				reposter.setWeiboUrl(weiboUrl);
				rpl.add(reposter);
			}
			
			String nextUrl = "";
			if(nextList.size() == 1){
				LinkTag linkTag = (LinkTag)nextList.elementAt(0);
				Span span = (Span)linkTag.getChild(1);
				nextUrl = span.getAttribute("action-data");
			}
			map.put("reposters", rpl);
			map.put("next", nextUrl);
			
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		return map;
	}
	
	public static void main(String args[]) {
		Crawler4weiboRepost crawler4weiboRepost = new Crawler4weiboRepost("新建文件夹的路径");
		crawler4weiboRepost.work();
	}

}

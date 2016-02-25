package xinlangcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class Crawler4weiboComment {

	private String fileDir = null;
	private OutputStreamWriter commentOut = null;
	private BufferedReader urlReader = null;
	
	public Crawler4weiboComment(String fileDir) {
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
			String commentBreakPointName = dirName+"\\"+"commentBreakPoint.txt";
			File commentBreakPointNameFile = new File(commentBreakPointName);
			//断点文件存在
			if(commentBreakPointNameFile.exists()){
				System.out.println("断点文件存在："+commentBreakPointName);
				try {
					urlReader = new BufferedReader(new FileReader(commentBreakPointNameFile));
					String url = urlReader.readLine();
					url = url.trim();
					if(url == null || "".equals(url)){
						System.out.println("断点文件为空："+commentBreakPointName);
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
				String commentName = dirName+"\\"+"comment.txt";
				File commentFile = new File(commentName);
				if(commentFile.exists() && commentFile.length() != 0){
					System.out.println("评论文件存在："+commentName+"直接跳过！!!!");
					continue;
				}
				else{
					String url = "http://weibo.com/aj/v6/comment/big?ajwvr=6&id="+id;
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
		//开始根据链接获取评论信息
		String commentName = dirName + "\\" + "comment.txt";
		
		try {
			File commentFile = new File(commentName);
			if(!commentFile.exists())
				commentFile.createNewFile();
			commentOut = new OutputStreamWriter(new FileOutputStream(commentName, true), "utf-8");
			Spider spider = new Spider();
			while(url!=null && !"".equals(url)){
				String htmlStr = spider.getHtmlStringByUrl(url);
				
				JSONObject data = new JSONObject(htmlStr);
				JSONObject dataJson = data.optJSONObject("data");
				htmlStr = dataJson.optString("html");
				
				HashMap<String, Object> map = getCommentFromhtmlStr(htmlStr);
				ArrayList<Commenter> commenterList = (ArrayList<Commenter>)map.get("comenters");
				if(commenterList != null && commenterList.size() != 0){
					for(Commenter cm: commenterList){
						String line = "";
						line += cm.getUid()+"    ";
						line += cm.getName()+"    ";
						line += cm.getTime()+"    ";
						line += "no comment url"+"    ";
						line += cm.getCid()+"\n";
						commentOut.write(line);
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
					String commentBreakPointName = dirName+"\\"+"commentBreakPoint.txt";
					System.out.println("获取链接数据："+url + "失败,url："+url+"存入断点文件："+commentBreakPointName+"！！！！！！！！");
					OutputStreamWriter commentBreakPoint = new OutputStreamWriter(new FileOutputStream(commentBreakPointName, false), "utf-8");
					commentBreakPoint.write(url+"\n");
					commentBreakPoint.close();
					commentOut.close();
					return false;
				}
				
			}
			commentOut.close();
			//删除断点文件
			String commentBreakPointName = dirName+"\\"+"commentBreakPoint.txt";
			File commentBreakPointFile = new File(commentBreakPointName);
			if(url == null || "".equals(url))
				if(commentBreakPointFile.exists())
					commentBreakPointFile.delete();
			

		} catch (Exception e){
			e.printStackTrace();
			try{
				String commentBreakPointName = dirName+"\\"+"commentBreakPoint.txt";
				System.out.println("获取链接数据："+url + "失败,url："+url+"存入断点文件："+commentBreakPointName+"！！！！！！！！");
				OutputStreamWriter commentBreakPoint = new OutputStreamWriter(new FileOutputStream(commentBreakPointName, false), "utf-8");
				commentBreakPoint.write(url+"\n");
				commentBreakPoint.close();
				commentOut.close();
			}catch (Exception ex){
				ex.printStackTrace();
				
			}
			
			return false;
		}
		return true;
	}
	
	private HashMap<String, Object> getCommentFromhtmlStr(String htmlStr){
		HashMap<String, Object> map = new HashMap<String, Object>();
		if(htmlStr == null || "".equals(htmlStr))
			return map;
		try {
			String htmlStrnext = htmlStr;
			String htmlStrTime = htmlStr;
			Parser parser4Comment = new Parser(htmlStr);
			Parser parser4Time = new Parser(htmlStrTime);
			Parser parser4Next = new Parser(htmlStrnext);
			
			HasAttributeFilter commentFilter = new HasAttributeFilter("action-type", "reply");
			HasAttributeFilter timeFilter = new HasAttributeFilter("class", "WB_from S_txt2");
			HasAttributeFilter nextFilter = new HasAttributeFilter("class", "page next S_txt1 S_line1");
			
			NodeList commentList = parser4Comment.extractAllNodesThatMatch(commentFilter);
			NodeList timeList = parser4Time.extractAllNodesThatMatch(timeFilter);
			NodeList nextList = parser4Next.extractAllNodesThatMatch(nextFilter);
			
			if(commentList.size() == 0 || timeList.size() == 0 || commentList.size() != timeList.size())
				return map;
			ArrayList<Commenter> rpl = new ArrayList<Commenter>();
			for(int i=0; i<timeList.size(); i++){
				Commenter commenter = new Commenter();
				LinkTag cmNode = (LinkTag)commentList.elementAt(i);
				Div timeNode = (Div)timeList.elementAt(i);
				String actionData = cmNode.getAttribute("action-data");
				String uid = "";
				String cid = "";
				String name = "";
				Pattern pattern = Pattern.compile("(?<=ouid=)[0-9]+");
				Matcher matcher = pattern.matcher(actionData);
				if(matcher.find())
					uid = matcher.group();
				pattern = Pattern.compile("(?<=cid=)[0-9]+");
				matcher = pattern.matcher(actionData);
				if(matcher.find())
					cid = matcher.group();
				pattern = Pattern.compile("(?<=nick=)[^&]*(?=&)");
				matcher = pattern.matcher(actionData);
				if(matcher.find())
					name = matcher.group();

				String time = timeNode.getStringText();
				commenter.setUid(uid);
				commenter.setCid(cid);
				commenter.setName(name);
				commenter.setTime(time);

				rpl.add(commenter);
			}
			
			String nextUrl = "";
			if(nextList.size() == 1){
				LinkTag linkTag = (LinkTag)nextList.elementAt(0);
				Span span = (Span)linkTag.getChild(1);
				nextUrl = span.getAttribute("action-data");
			}
			map.put("comenters", rpl);
			map.put("next", nextUrl);
			
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		return map;
	}
	
	public static void main(String args[]) {
		Crawler4weiboComment crawler4weiboComment = new Crawler4weiboComment("新建文件夹的路径");
		crawler4weiboComment.work();
	}
}

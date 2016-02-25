package xinlangcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler4weiboid {
	
	private String fileDir = null;
	private String fileName = null;
	private OutputStreamWriter seedOuts = null;
	private BufferedReader urlReader = null;
	private ArrayList<String> urls = new ArrayList<String>();
    private ArrayList<String> urlsBreakPoint = new ArrayList<String>();
	public Crawler4weiboid(String fileDir, String fileName){
		this.fileDir = fileDir;
		this.fileName = fileName;
	}
	
	public void work() {
		String file = fileName.substring(0, fileName.indexOf('.'));
		String extendName = fileName.substring(fileName.indexOf('.') + 1,
				fileName.length());
		String breakpointFileName = fileDir + "\\" + file + "breakPoint" + "."
				+ extendName;
		File breakPointFile = new File(breakpointFileName);
		try {
			// 断点文件存在，直接读取断点文件
			if (breakPointFile.exists()) {
				urlReader = new BufferedReader(new FileReader(breakPointFile));
				String line = null;
				while ((line = urlReader.readLine()) != null) {
					line = line.trim();
					if (line == null || "".equals(line))
						continue;
					urls.add(line);
				}
			} else {
				File urlFile = new File(fileDir + "\\" + fileName);
				urlReader = new BufferedReader(new FileReader(urlFile));
				String line = null;
				while ((line = urlReader.readLine()) != null) {
					line = line.trim();
					if (line == null || "".equals(line))
						continue;
					urls.add(line);
				}
			}
			urlReader.close();
			for (String weibourl : urls)
				urlsBreakPoint.add(weibourl);
		} catch (Exception e) {
			System.err.println("读取微博url文件或是断点文件失败");
			e.printStackTrace();
			return;
		}
		Spider spider = new Spider();
		int count = 0;
		ArrayList<String> dumpList = new ArrayList<String>();
		ArrayList<String> errorList = new ArrayList<String>();
		for (String weibourl : urls) {
			if (weibourl == null || "".equals(weibourl))
				continue;

			try {
				String htmlStr = spider.getHtmlStringByUrl(weibourl);
				String id = getWeiboIdFromhtmlStr(htmlStr);
				if (id == null || "".equals(id)) {
					System.err.println("微博链接：" + weibourl + " 获取微博id失败！");
					continue;
				}
				String weibourl2FileName = weibourl.replace('/', ' ');
				weibourl2FileName = weibourl2FileName.replace(':', '@');
				weibourl2FileName = weibourl2FileName.replace('?', '!');
				String weiboDirName = fileDir + "\\" + weibourl2FileName + "__"
						+ id;
				File weiboDir = new File(weiboDirName);
				if (!weiboDir.exists() || !weiboDir.isDirectory()) {
					weiboDir.mkdir();
				} else {
					System.err.println("微博链接：" + weibourl + "已存在！！！！");
					dumpList.add("重复的链接："+ weibourl);
					urlsBreakPoint.remove(weibourl);
					continue;
				}
				if(!weiboDir.exists()){
					System.err.println("微博链接：" + weibourl + "文件名不合法！！！！");
					errorList.add("不合法的链接："+ weibourl);
				}
				else{
					urlsBreakPoint.remove(weibourl);
					System.out.println("微博链接：" + weibourl + "获取微博id成功！！！！");
					count++;
				}
				

			} catch (Exception e) {
				System.err.println("获取微博链接:"+weibourl+"失败!!!！");
				e.printStackTrace();
				/*
				try {
					File breakpointFile = new File(breakpointFileName);
					if (!breakpointFile.exists())
						breakpointFile.createNewFile();
					seedOuts = new OutputStreamWriter(new FileOutputStream(
							breakpointFileName, true), "utf-8");
					for (String restweibourl : urlsBreakPoint) {
						seedOuts.write(restweibourl + "\n");
					}
					seedOuts.close();
				} catch (UnsupportedEncodingException e1) {
					System.err.println("保存断点文件失败！");
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.err.println("保存断点文件失败！");
					e1.printStackTrace();
				}
				*/
			}

		}
		System.err.println("成功获取" + count + "条微博的id！！！！！！！！！！！！！！！");
		System.err.println("重复的微博id为条数为：" + dumpList.size() + "分别为：");
		for (String weibourl : dumpList)
			System.out.println(weibourl);
		System.err.println("不合法的文件名条数为：" + errorList.size() + "分别为：");
		for (String weibourl : errorList)
			System.out.println(weibourl);
		if (urlsBreakPoint.size() != 0) {
			try {
				File breakpointFile = new File(breakpointFileName);
				if (!breakpointFile.exists())
					breakpointFile.createNewFile();
				seedOuts = new OutputStreamWriter(new FileOutputStream(
						breakpointFileName, false), "utf-8");
				for (String restweibourl : urlsBreakPoint) {
					seedOuts.write(restweibourl + "\n");
				}
				seedOuts.close();
			} catch (IOException e) {
				System.err.println("保存断点文件失败！");
				e.printStackTrace();
			}
		}
		else{
			File breakpointFile = new File(breakpointFileName);
			if (breakpointFile.exists())
				breakpointFile.delete();
		}

		
	}

	private String getWeiboIdFromhtmlStr(String htmlStr){
		String id = "";
		 Pattern pattern = Pattern.compile("mid=[0-9]+");
	     Matcher matcher = pattern.matcher(htmlStr);
	     if(matcher.find()){
	        id = matcher.group();
	        id = id.substring(id.indexOf("=")+1, id.length());

	     }
	     return id;
	}
	
	public static void main(String args[]){
		Crawler4weiboid crawler4weiboid = new Crawler4weiboid("新建文件夹的路径", "weibourls.txt");
		crawler4weiboid.work();
	}
	
}

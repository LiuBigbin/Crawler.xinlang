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



public class Crawler4weiboUser {

	private String fileDir = null;
	private OutputStreamWriter userOut = null;
	private BufferedReader urlReader = null;
	private boolean is4Reposter;
	private String seedFileNamePostfix = null;
	private String breakPointFileNamePostfix = null;
	private String seedFileName = null;
	private String breakPointFileName = null;
	private String userInfoFileNamePostfix = null;
	private String userInfoFileName = null;
	private String userUrlPrefix = "http://weibo.com/u/";
    private String userUrlPostfix = "?is_all=1";
	public Crawler4weiboUser(String fileDir, boolean is4Reposter) {
		this.fileDir = fileDir;
		this.is4Reposter = is4Reposter;
		if (is4Reposter) {
			seedFileNamePostfix = "repost.txt";
			breakPointFileNamePostfix = "repostBreakPoint4UserInfo.txt";
			userInfoFileNamePostfix = "repostUserInfo.txt";
		} else {
			seedFileNamePostfix = "comment.txt";
			breakPointFileNamePostfix = "commentBreakPoint4UserInfo.txt";
			userInfoFileNamePostfix = "commentUserInfo.txt";
		}
	}

	public void work() {
		File Dir = new File(fileDir);
		if (!Dir.exists() || !Dir.isDirectory()) {
			System.err.println(fileDir + "不是目录，或者不存在");
			return;
		}
		String[] FileList = Dir.list();
		int success = 0;
		int filenum = 0;
		int fail = 0;
		for (String dirName : FileList) {
			ArrayList<String> useridList = new ArrayList<String>();
			ArrayList<String> useridBreakPointList = new ArrayList<String>();
			dirName = fileDir + "\\" + dirName;
			userInfoFileName = dirName + "\\" + userInfoFileNamePostfix;
			seedFileName = dirName + "\\" + seedFileNamePostfix;
			File dir = new File(dirName);
			if (dir.isFile()) {
				System.out.println(dirName + "为文件，不是目录，忽略！");
				filenum++;
				continue;
			}

			breakPointFileName = dirName + "\\" + breakPointFileNamePostfix;
			File BreakPointFile = new File(breakPointFileName);
			// 断点文件存在
			if (BreakPointFile.exists()) {
				System.out.println("断点文件存在：" + breakPointFileName);
				try {
					urlReader = new BufferedReader(new FileReader(
							BreakPointFile));
					String line = null;
					while ((line = urlReader.readLine()) != null) {
						line = line.trim();
						if (line == null || "".equals(line))
							continue;
						
						useridList.add(line);
					}
					if (useridList.size() == 0) {
						System.out.println("断点文件为空：" + breakPointFileName);
						continue;
					}
					for (String id : useridList)
						useridBreakPointList.add(id);
					if (workHandler(useridList, useridBreakPointList)) {
						success++;
						System.out.println("处理文件夹：" + dirName + "成功！！！！！");
					} else {
						System.err.println("处理文件夹：" + dirName + "失败！！！！！");
						fail++;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				try {
					File userInfoFile = new File(userInfoFileName);
					if (userInfoFile.exists() && userInfoFile.length() != 0) {
						System.out.println("用户信息文件存在：" + userInfoFileName
								+ "直接跳过！!!!");
						continue;
					} else {
						File seedFile = new File(seedFileName);
						if(!seedFile.exists()){
							System.out.println("用户种子文件不存在：" + seedFileName);
							continue;
						}
						urlReader = new BufferedReader(new FileReader(
								seedFileName));
						String line = null;
						while ((line = urlReader.readLine()) != null) {
							line = line.trim();
							if (line == null || "".equals(line))
								continue;
							String id = line.substring(0, line.indexOf(' '));
							if (id == null || "".equals(id))
								continue;
							useridList.add(id);
						}
						if (useridList.size() == 0) {
							System.out.println("用户种子文件为空：" + seedFileName);
							continue;
						}
						for (String id : useridList)
							useridBreakPointList.add(id);
						if (workHandler(useridList, useridBreakPointList)) {
							success++;
							System.out.println("处理文件夹：" + dirName + "成功！！！！！");
						} else {
							System.err.println("处理文件夹：" + dirName + "失败！！！！！");
							fail++;
						}

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("一共成功处理文件夹个数为：" + success);
		System.out.println("一共处理失败的文件夹个数为：" + fail);
		System.out.println("忽略的文件个数为：" + filenum);
	}

	private boolean workHandler(ArrayList<String> useridList,
			ArrayList<String> useridBreakPointList) {
		try {
			File repostFile = new File(userInfoFileName);
			if (!repostFile.exists())
				repostFile.createNewFile();
			userOut = new OutputStreamWriter(new FileOutputStream(
					userInfoFileName, true), "utf-8");
			Spider spider = new Spider();
			for (String id : useridList) {
				String url = userUrlPrefix + id + userUrlPostfix;
				String htmlStr = spider.getHtmlStringByUrl(url);
				WeiboUser user = getUserInfoFromhtmlStr(htmlStr, id);
				if (user != null) {
					String line = "";
					line += user.getUid() + "    ";
					line += user.getName() + "    ";
					line += user.getFollowNumber() + "    ";
					line += user.getFansNumber() + "    ";
					line += user.getWeiboNumber() + "    ";
					if (user.isVerify())
						line += "认证用户" + "    ";
					else
						line += "非认证用户" + "    ";
					line += user.getLocation() + "    ";
					line += user.getLevel() + "\n";
					userOut.write(line);
					System.out.println("成功获得链接数据：" + url);
					useridBreakPointList.remove(id);

				} else {
					System.out.println("获取链接数据：" + url + "失败!!!");
					OutputStreamWriter BreakPointFile = new OutputStreamWriter(
							new FileOutputStream(breakPointFileName, false),
							"utf-8");
					for (String restId : useridBreakPointList)
						BreakPointFile.write(restId + '\n');
					BreakPointFile.close();
					userOut.close();
					return false;
				}

			}
			userOut.close();
			
			//删除断点文件
			File BreakPointFile = new File(breakPointFileName);
			if(BreakPointFile.exists() && useridBreakPointList.size() == 0)
				BreakPointFile.delete();


		} catch (Exception e) {
			e.printStackTrace();
			try{
				OutputStreamWriter BreakPointFile = new OutputStreamWriter(
						new FileOutputStream(breakPointFileName, false),
						"utf-8");
				for (String restId : useridBreakPointList)
					BreakPointFile.write(restId + '\n');
				BreakPointFile.close();
				userOut.close();
				return false;
			} catch (Exception ex){
				ex.printStackTrace();
				return false;
			}
			
		}
		return true;
	}

	private WeiboUser getUserInfoFromhtmlStr (String htmlStr, String uid) throws Exception{
		if (htmlStr == null || "".equals(htmlStr))
			return null;
		WeiboUser user = new WeiboUser();
		//System.out.println(htmlStr);
		htmlStr = htmlStr.substring(htmlStr.indexOf("['onick']='") + 11);
		String name = htmlStr.substring(0, htmlStr.indexOf("';")).trim();
		if (name == null || "".equals(name))
			name = "null";
		boolean isVerify = true;
		int index = htmlStr.indexOf("verify clearfix");
		if (index == -1)
			isVerify = false;
		int levelIndex = 0;
		if (isVerify)
			levelIndex = htmlStr.indexOf("icon_group S_line1 W_fl");
		else
			levelIndex = htmlStr.indexOf("item_text W_fl");
		
		if(levelIndex == -1){
			System.out.println("在解析htmlStr获得用户的微博级别和位置时出错！！！");
			//System.out.println(htmlStr);
			throw new Exception();
		}
		

		int Pl_Core_T8CustomTriColumn__3IndexFirst = htmlStr.indexOf("Pl_Core_T8CustomTriColumn__3");
		if(Pl_Core_T8CustomTriColumn__3IndexFirst == -1){
			System.out.println("在解析htmlStr获得用户的关注、粉丝和微博数量时出错！！！");
			throw new Exception();
		}
		String htmlStrSub = htmlStr.substring(Pl_Core_T8CustomTriColumn__3IndexFirst+28);
		int Pl_Core_T8CustomTriColumn__3IndexSecond = 0;
		Pl_Core_T8CustomTriColumn__3IndexSecond = htmlStrSub.indexOf("Pl_Core_T8CustomTriColumn__3");
		if(Pl_Core_T8CustomTriColumn__3IndexSecond == -1){
			System.out.println("在解析htmlStr获得用户的关注、粉丝和微博数量时出错！！！");
			throw new Exception();
		}
		Pl_Core_T8CustomTriColumn__3IndexSecond += Pl_Core_T8CustomTriColumn__3IndexFirst + 28;
		int followNumber = 0;
		int fansNumber = 0;
		int weiboNumber = 0;
		String location = null;
		String locationBackup = "";
		String level = null;
		if(levelIndex < Pl_Core_T8CustomTriColumn__3IndexSecond){
			//System.out.println(htmlStr);
			htmlStr = htmlStr.substring(levelIndex);
			Pl_Core_T8CustomTriColumn__3IndexSecond -= levelIndex;
			//System.out.println(htmlStr);
			int inde = htmlStr.indexOf("title=");
			htmlStr = htmlStr.substring(inde + 8);
			Pl_Core_T8CustomTriColumn__3IndexSecond -= (inde + 8);
			level = htmlStr.substring(0, htmlStr.indexOf(" ")).trim();
			level = level.substring(0, level.length()-2);
			int infoIndex = htmlStr.indexOf("info");
			int item_text_W_flIndex = htmlStr.indexOf("item_text W_fl");

			if(infoIndex == -1 || infoIndex > item_text_W_flIndex){
				htmlStr = htmlStr.substring(item_text_W_flIndex + 17);
				Pl_Core_T8CustomTriColumn__3IndexSecond -= (item_text_W_flIndex + 17);
				location = htmlStr.substring(0, htmlStr.indexOf("<")).trim();
			}
			else{
				htmlStr = htmlStr.substring(infoIndex+13);
				Pl_Core_T8CustomTriColumn__3IndexSecond -= (infoIndex+13);
				locationBackup = htmlStr.substring(0, htmlStr.indexOf('<')).trim();
				htmlStr = htmlStr.substring(item_text_W_flIndex+17);
				Pl_Core_T8CustomTriColumn__3IndexSecond -= (item_text_W_flIndex+17);
				location = htmlStr.substring(0, htmlStr.indexOf("<")).trim();
				
			}
			//System.out.println(htmlStr);
			htmlStr = htmlStr.substring(Pl_Core_T8CustomTriColumn__3IndexSecond + 28);
			Pattern pattern = Pattern.compile("(?<=>)[0-9]+");
			Matcher matcher = pattern.matcher(htmlStr);
			//System.out.println(htmlStr);
			if (matcher.find()) {
				followNumber = Integer.valueOf(matcher.group());
			}
			if (matcher.find()) {
				fansNumber = Integer.valueOf(matcher.group());
			}
			if (matcher.find()) {
				weiboNumber = Integer.valueOf(matcher.group());
			}
		}
		else{
			htmlStr = htmlStr.substring(Pl_Core_T8CustomTriColumn__3IndexSecond + 28);
			//System.out.println(htmlStr);
			Pattern pattern = Pattern.compile("(?<=>)[0-9]+");
			Matcher matcher = pattern.matcher(htmlStr);
			
			if (matcher.find()) {
				followNumber = Integer.valueOf(matcher.group());
			}
			if (matcher.find()) {
				fansNumber = Integer.valueOf(matcher.group());
			}
			if (matcher.find()) {
				weiboNumber = Integer.valueOf(matcher.group());
			}
			htmlStr = htmlStr.substring(levelIndex-Pl_Core_T8CustomTriColumn__3IndexSecond);
			int inde = htmlStr.indexOf("title=");
			htmlStr = htmlStr.substring(inde + 8);
			level = htmlStr.substring(0, htmlStr.indexOf(" ")).trim();
			level = level.substring(0, level.length()-2);
			//System.out.println(htmlStr);
			int infoIndex = htmlStr.indexOf("info");
			int item_text_W_flIndex = htmlStr.indexOf("item_text W_fl");
			if(infoIndex == -1 || infoIndex > item_text_W_flIndex){
				htmlStr = htmlStr.substring(item_text_W_flIndex + 17);
				location = htmlStr.substring(0, htmlStr.indexOf("<")).trim();
			}
			else{
				htmlStr = htmlStr.substring(infoIndex+13);
				item_text_W_flIndex -= (infoIndex+13);
				locationBackup = htmlStr.substring(0, htmlStr.indexOf('<')).trim();
				htmlStr = htmlStr.substring(item_text_W_flIndex+17);
				location = htmlStr.substring(0, htmlStr.indexOf("<")).trim();
				
			}

	
		}
		while(locationBackup.startsWith("\\r") || locationBackup.startsWith("\\t") || locationBackup.startsWith("\\n"))
			locationBackup = locationBackup.substring(2).trim();
		while(locationBackup.endsWith("\\r") || locationBackup.endsWith("\\t") || locationBackup.endsWith("\\n"))
			locationBackup = locationBackup.substring(0, locationBackup.length()-2).trim();
		locationBackup = locationBackup.trim();
		
		while(location.startsWith("\\r") || location.startsWith("\\t") || location.startsWith("\\n"))
			location = location.substring(2).trim();
		while(location.endsWith("\\r") || location.endsWith("\\t") || location.endsWith("\\n"))
			location = location.substring(0, location.length()-2).trim();
		location = location.trim();
		if(location == null || "".equals(location))
			location = locationBackup;
		
		user.setFansNumber(fansNumber);
		user.setFollowNumber(followNumber);
		user.setLevel(level);
		user.setLocation(location);
		user.setName(name);
		user.setUid(uid);
		user.setVerify(isVerify);
		user.setWeiboNumber(weiboNumber);
		return user;
	}

	public static void main(String args[]) {
		//true表示的是爬取转发用户的数据，false表示的是爬取评论用户的数据
		Crawler4weiboUser crawler4weiboUser = new Crawler4weiboUser(
				"新建文件夹的路径", true);
		crawler4weiboUser.work();
	}

}

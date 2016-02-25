package xinlangcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class FileCombine {

	private String dirPath = null;
	
	public FileCombine(String dirPath){
		this.dirPath = dirPath;
		
	}
	
	public void combine(){
		if(dirPath == null || "".equals(dirPath)){
			System.err.println("待处理的文件夹目录为空,请输入文件夹目录");
			return ;
		}
		File dir = new File(dirPath);
		if(!dir.exists()){
			System.err.println("文件夹：" + dirPath + "不存在");
			return ;
		}
		if(dir.isFile()){
			System.err.println("文件夹：" + dirPath + "为文件，并不是目录");
			return ;
		}
		
		/*
		 * 注意，下面的1和2只能选一个
		 */
		
		/*
		 * 1、如果是想合并用户信息，那么去掉下面两行代码前面的//，否则的话请在下面的两行代码前加//
		 */
		//combineHelper("repostUserInfo");
		//combineHelper("commentUserInfo");
		
		/*
		 * 2、如果想要合并转发和评论文件，那么去掉下面两行代码前面的//，否则的话请在下面的两行代码前加//
		 */
		combineHelper("repost");
		combineHelper("comment");
		
		
	}
	
	//根据fileName来处理评论或是转发文件,合并后的文件命名为total+fileName
	private void combineHelper(String fileName){
		File dir = new File(dirPath);
		String temp = fileName;
		String combineFileName = dir + "\\" + "total" + fileName + ".txt";
		//判断文件是否已经存在
		File combineFile = new File(combineFileName);
		if(combineFile.exists() && combineFile.length() > 0){
			System.err.println("合并文件：" + combineFileName + "已经存在，如果想要重新合并的话，请删除该文件再执行程序！！！");
			return ;
		}
		/*
		//判断weiboTime文件是否存在
		String weiboTimeFileName = dir + "\\" + "weiboTime.txt";
		File weiboTimeFile = new File(weiboTimeFileName);
		if(!weiboTimeFile.exists()){
			System.err.println("微博时间文件：" + weiboTimeFileName + "不存在，请生成该文件！！！");
			return ;
		}
		if(weiboTimeFile.length() <= 0){
			System.err.println("微博时间文件：" + weiboTimeFileName + "为空，请填写该文件的数据！！！");
			return ;
		}

		//读取weiboTime文件的数据
		Map<String, String> timeMap = new HashMap<String, String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(weiboTimeFile));
			String line = null;
			while((line = br.readLine()) != null){
				String time = null;
				String url = null;
				line = line.trim();
				if(line == null || "".equals(line))
					continue;
				//得到时间
				int index = line.indexOf(' ');
				time = line.substring(0, index+1);
				line = line.substring(index+1);
				index = line.indexOf('\t');
				time += line.substring(0, index);
				
				//得到url
				index = line.lastIndexOf('\t');
				url = line.substring(index+1);
				
				timeMap.put(url, time);
				
			}
			br.close();
			
		} catch (Exception e){
			e.printStackTrace();
			return ;
		}
		*/
		//开始处理每一个url所对应的文件夹里面的数据
		if(!combineFile.exists()){
			try {
				combineFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return ;
			}
		}
		try {
			OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(combineFileName, true), "utf-8");
			File[] fileList = dir.listFiles();
			for(File weibo : fileList){
				if(weibo.isFile()){
					System.out.println(weibo.getAbsolutePath() + "为文件，忽略！！");
					continue;
				}
				System.out.println("开始处理文件夹：" + weibo.getAbsolutePath() + "**************************");
				String url = weibo.getName();
				String weiboid = url.substring(url.lastIndexOf('_')+1);
				url = url.substring(0, url.indexOf("__"));
				url = url.replace('@', ':');
				url = url.replaceAll(" ", "/");
				url = url.replace('!', '?');
				//String time = timeMap.get(url);
				//if(time == null){
				//	System.err.println("处理文件夹 ：" + weibo.getName() + "失败， 因为获取不到对应的微博时间！！！");
				//	continue;
				//}
				fileName = weibo.getAbsolutePath() + "\\" + temp + ".txt";
				File file = new File(fileName);
				if(!file.exists()){
					System.err.println("文件：" + fileName + "不存在，忽略该微博");
					continue ;
				}
				if(file.length() == 0){
					System.err.println("文件：" + fileName + "为空，忽略该微博");
					continue ;
				}
				//开始读取该微博对应的需要合并的文件
				BufferedReader bbr = new BufferedReader(new FileReader(file));
				String line = null;
				//用于文件的去重，根据评论或是转发的id来去重
				Map<String,  Boolean> map = new HashMap<String, Boolean>();
				while((line = bbr.readLine()) != null){
					line = line.trim();
					if(line == null || "".equals(line))
						continue;
					//转发或是评论每一行的最后为其对应的转发id或是评论id，但是对于用户信息来说，每一行的最开始部分为用户的id
					//所以当合并用户信息的时候，需要修改下面获得id的代码
					//获得转发或是评论的id
					String id = line.substring(line.lastIndexOf(' ')+1);
					//获得用户的id
					//String id = line.substring(0, line.indexOf(' '));

					if(map.containsKey(id))
						continue;
					map.put(id, true);
					//line = url + "    " + time + "    " + weiboid + "    " + line;
					line = url + "    " + weiboid + "    " + line;
					op.write(line + '\n');
					
				}
				System.out.println("处理文件夹：" + weibo.getAbsolutePath() + "完成！！！");
				bbr.close(); 
			}
			op.close();
			System.out.println("合并" + temp + "完成！！！");
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}
		
	}


	public static void main(String args[]){
		//运行后，该目录下面将生成totalrepost.txt和totacomment.txt文件，就是合并后的转发文件和评论文件
		FileCombine fc = new FileCombine("新建文件夹的路径");
		fc.combine();
	}
}


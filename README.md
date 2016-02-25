&#160; &#160; &#160; &#160; 该程序用来爬取新浪微博的评论和转发信息以及评论、转发的用户信息。

&#160; &#160; &#160; &#160; 运行程序时，需要新建一个空文件夹，在该文件夹里面创建weibourls.txt文件，将需要爬取转发或是评论信息的微博链接存放在该文件里面，每行一条链接。接下来按照下面的爬取步骤爬取信息：

&#160; &#160; &#160; &#160; 第一步：运行Crawler4weiboid.java程序，运行后会在上面新建的文件夹里面生成相应的子文件夹，每一个子文件夹代表一条微博，微博后面的转发、评论或是用户信息都会存在该子文件里面。运行程序前，需要将Crawler4weiboid.java文件里最后的代码：
```java
Crawler4weiboid crawler4weiboid = new Crawler4weiboid("新建文件夹的路径", "weibourls.txt");
```
中的"新建文件夹路径", 改为你自己新建的文件夹路径。

&#160; &#160; &#160; &#160; 第二步：可以爬取评论或是转发信息了，运行Crawler4weiboRepost.java爬取转发信息，运行Crawler4weiboComment.java 爬取评论信息，同样，运行前都需要在对应的java文件里面填入自己创建的空文件夹路径，Crawler4weiboRepost.java文件为最后面的代码：
```java
Crawler4weiboRepost crawler4weiboRepost = new Crawler4weiboRepost("新建文件夹的路径");
```
Crawler4weiboComment.java文件为最后面的代码：
```java
Crawler4weiboComment crawler4weiboComment = new Crawler4weiboComment("新建文件夹的路径");
```
&#160; &#160; &#160; &#160; 第三步：爬取完转发信息和评论信息后就可以来爬去转发和评论者信息了，注意，爬取转发者信息必须在转发信息爬取完成后才可以爬，评论者信息也是一样的，因为两者都是基于上一步所爬取得到的信息来进行的。运行Crawler4weiboUser.java就可以爬取的到用户信息，文件最后的代码：
```java
Crawler4weiboUser crawler4weiboUser = new Crawler4weiboUser(
				"新建文件夹的路径", true);
```
仍然需要将自己新建的文件夹的路径填到函数参数里面，true代表的是爬取转发者信息，false代表的是爬取评论者信息，可以根据需要进行修改。

&#160; &#160; &#160; &#160; 上面每一步所爬取的数据都会存在每一个微博链接所对应的文件夹里面，如果需要将所有的文件合并，比如：需要合并所有的转发信息文件、评论信息文件或是用户信息文件，那么可以运行FileCombine.java文件，运行该程序需要修改两个地方：
&#160; &#160; &#160; &#160; 第一：将文件最后面的代码
```java
FileCombine fc = new FileCombine("新建文件夹的路径");
```
填入你新建的文件夹路径。

&#160; &#160; &#160; &#160; 第二：合并用户信息或是转发评论信息需要根据下面的代码提示修改：
```java
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
  combineHelper("repostUserInfo");
  combineHelper("commentUserInfo");

  /*
   * 2、如果想要合并转发和评论文件，那么去掉下面两行代码前面的//，否则的话请在下面的两行代码前加//
   */
   //combineHelper("repost");
 		//combineHelper("comment");


}
```

&#160; &#160; &#160; &#160; 接下来按照代码里面的注解修改combineHelper函数里面的代码：
```java
while((line = bbr.readLine()) != null){
			    line = line.trim();
					if(line == null || "".equals(line))
						continue;
					//转发或是评论每一行的最后为其对应的转发id或是评论id，但是对于用户信息来说，每一行的最开始部分为用户的id

					//获得转发或是评论的id,合并转发或是评论信息时，去掉下面代码前面的//
					//String id = line.substring(line.lastIndexOf(' ')+1);
					//获得用户的id, 合并用户信息时，去掉下面代码前面的//
					String id = line.substring(0, line.indexOf(' '));

					if(map.containsKey(id))
						continue;
					map.put(id, true);
					line = url + "    " + time + "    " + weiboid + "    " + line;
					op.write(line + '\n');

				}
```
上面两部分代码的修改需要同步匹配。运行程序后，合并用户信息的话将生成totalrepostUserInfo.txt和totalcommentUserInfo.txt文件，合并转发或是评论信息的话将生成totalrepost.txt和totacomment.txt文件，这四个文件存放在你新建的文件夹里面。

&#160; &#160; &#160; &#160; 如果上面程序在运行过程中有出现中断的，可以直接重新运行程序，程序会在上一次出错的地方开始运行，支持断点续爬！如果因为网络原因而爬取或是解析不到数据，重新爬取就可以了，如果是得到了数据但是在解析的时候出错，那么有可能就是数据本身有问题，此时提示的错误可以忽略！

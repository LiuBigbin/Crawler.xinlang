package xinlangcrawler;

public class WeiboUser {

	private String uid;
	private String name;
	private int followNumber;
	private int fansNumber;
	private int weiboNumber;
	private boolean isVerify;
	private String location;
	private String level;
	
	public WeiboUser(){
		
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFollowNumber() {
		return followNumber;
	}

	public void setFollowNumber(int followNumber) {
		this.followNumber = followNumber;
	}

	public int getFansNumber() {
		return fansNumber;
	}

	public void setFansNumber(int fansNumber) {
		this.fansNumber = fansNumber;
	}

	public int getWeiboNumber() {
		return weiboNumber;
	}

	public void setWeiboNumber(int weiboNumber) {
		this.weiboNumber = weiboNumber;
	}

	
	
	public boolean isVerify() {
		return isVerify;
	}

	public void setVerify(boolean isVerify) {
		this.isVerify = isVerify;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	
	
	
}

package com.maywidehb.qrpush.entity;


@SuppressWarnings("serial")
public class QrCodeTv2  implements java.io.Serializable{

	private long qrid;//二维码id
	private String qrurl;//二维码链接
	private int qrwp; //二维码距离背景图左边距
	private int qrhp; //二维码距离背景图上边距
	private int qrsize; //二维码尺寸

	private String backurl;//背景图链接
	private int backwp; //背景图左边距
	private int backhp; //背景图上边距
	private int backsize; //背景图尺寸

	private int bl; //背景图长
	private int bw; //背景图宽

	private boolean countdown ; //是否需要倒计时
	private Long aftertime;//多长时间后显示|倒计时时间 (毫秒  -1二维码不再显示)
	private Long workhours;//显示多长时间(毫秒) (-1一直显示)


	private String deliverid; //排期id
	private String aid;	  //广告位ID
	private String qrscene;//场景值

	private String starttime;//开始时间
	private String timestamp;//时间戳
	private String cardid;          //卡号
	private String serviceid; 		//频道号
	private String channelname;     //频道名称

	public String getCardid() {
		return cardid;
	}

	public void setCardid(String cardid) {
		this.cardid = cardid;
	}

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getChannelname() {
		return channelname;
	}

	public void setChannelname(String channelname) {
		this.channelname = channelname;
	}

	public long getQrid() {
		return qrid;
	}

	public void setQrid(long qrid) {
		this.qrid = qrid;
	}

	public String getQrurl() {
		return qrurl;
	}

	public void setQrurl(String qrurl) {
		this.qrurl = qrurl;
	}

	public int getQrwp() {
		return qrwp;
	}

	public void setQrwp(int qrwp) {
		this.qrwp = qrwp;
	}

	public int getQrhp() {
		return qrhp;
	}

	public void setQrhp(int qrhp) {
		this.qrhp = qrhp;
	}

	public int getQrsize() {
		return qrsize;
	}

	public void setQrsize(int qrsize) {
		this.qrsize = qrsize;
	}

	public String getBackurl() {
		return backurl;
	}

	public void setBackurl(String backurl) {
		this.backurl = backurl;
	}

	public int getBackwp() {
		return backwp;
	}

	public void setBackwp(int backwp) {
		this.backwp = backwp;
	}

	public int getBackhp() {
		return backhp;
	}

	public void setBackhp(int backhp) {
		this.backhp = backhp;
	}

	public int getBacksize() {
		return backsize;
	}

	public void setBacksize(int backsize) {
		this.backsize = backsize;
	}

	public int getBl() {
		return bl;
	}

	public void setBl(int bl) {
		this.bl = bl;
	}

	public int getBw() {
		return bw;
	}

	public void setBw(int bw) {
		this.bw = bw;
	}

	public boolean isCountdown() {
		return countdown;
	}

	public void setCountdown(boolean countdown) {
		this.countdown = countdown;
	}

	public Long getAftertime() {
		return aftertime;
	}

	public void setAftertime(Long aftertime) {
		this.aftertime = aftertime;
	}

	public Long getWorkhours() {
		return workhours;
	}

	public void setWorkhours(Long workhours) {
		this.workhours = workhours;
	}

	public String getDeliverid() {
		return deliverid;
	}

	public void setDeliverid(String deliverid) {
		this.deliverid = deliverid;
	}

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}

	public String getQrscene() {
		return qrscene;
	}

	public void setQrscene(String qrscene) {
		this.qrscene = qrscene;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "QrCodeTv2 [qrid=" + qrid + "&qrurl=" + qrurl + "&qrwp=" + qrwp + "&qrhp=" + qrhp + "&qrsize="
				+ qrsize + "&backurl=" + backurl + "&backwp=" + backwp + "&backhp=" + backhp + "&backsize="
				+ backsize + "&bl=" + bl + "&bw=" + bw + "&countdown=" + countdown + "&aftertime=" + aftertime
				+ "&workhours=" + workhours + "&deliverid=" + deliverid + "&aid=" + aid + "&qrscene=" + qrscene
				+ "&starttime=" + starttime + "&timestamp=" + timestamp + "&cardid=" + cardid + "&serviceid="
				+ serviceid + "&channelname=" + channelname + "]";
	}




}

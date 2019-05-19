package top.starrysea.object.view.out;

import top.starrysea.object.dto.User;

public class UserInfo {

	private String userId;
	private String userEmail;
	private String username;
	private short osuPerson;
	private short osuTeam;
	private short osuGrade;
	private short isDD;
	private String avatar;

	public UserInfo(User user) {
		this.userId = user.getUserId();
		this.userEmail = user.getUserEmail();
		this.username = user.getUsername();
		this.osuPerson = user.getOsuPerson();
		this.osuTeam = user.getOsuTeam();
		this.osuGrade = user.getOsuGrade();
		this.isDD = user.getIsDD();
		this.avatar = user.getAvatar();
	}

	public String getUserId() {
		return userId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getUsername() {
		return username;
	}

	public short getOsuPerson() {
		return osuPerson;
	}

	public short getOsuTeam() {
		return osuTeam;
	}

	public short getOsuGrade() {
		return osuGrade;
	}

	public short getIsDD() {
		return isDD;
	}

	public String getAvatar() {
		return avatar;
	}
}

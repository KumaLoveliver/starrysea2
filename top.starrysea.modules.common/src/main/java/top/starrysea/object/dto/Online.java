package top.starrysea.object.dto;

import top.starrysea.kql.entity.Entity;
import top.starrysea.kql.entity.IBuilder;

public class Online implements Entity {

	private String onlineId;
	private String onlineEmail;
	private String onlinePhone;
	private short onlineStatus;

	private Online(Builder builder) {
		this.onlineId = builder.onlineId;
		this.onlineEmail = builder.onlineEmail;
		this.onlinePhone = builder.onlinePhone;
		this.onlineStatus = builder.onlineStatus;
	}

	public Online() {

	}

	public static class Builder implements IBuilder<Online> {

		private String onlineId;
		private String onlineEmail;
		private String onlinePhone;
		private short onlineStatus;

		public Builder onlineId(String onlineId) {
			this.onlineId = onlineId;
			return this;
		}

		public Builder onlineEmail(String onlineEmail) {
			this.onlineEmail = onlineEmail;
			return this;
		}

		public Builder onlinePhone(String onlinePhone) {
			this.onlinePhone = onlinePhone;
			return this;
		}

		public Builder onlineStatus(short onlineStatus) {
			this.onlineStatus = onlineStatus;
			return this;
		}

		@Override
		public Online build() {
			return new Online(this);
		}

	}

	public String getOnlineId() {
		return onlineId;
	}

	public void setOnlineId(String onlineId) {
		this.onlineId = onlineId;
	}

	public String getOnlineEmail() {
		return onlineEmail;
	}

	public void setOnlineEmail(String onlineEmail) {
		this.onlineEmail = onlineEmail;
	}

	public String getOnlinePhone() {
		return onlinePhone;
	}

	public void setOnlinePhone(String onlinePhone) {
		this.onlinePhone = onlinePhone;
	}

	public short getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(short onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

}

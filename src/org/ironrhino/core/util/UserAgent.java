package org.ironrhino.core.util;

import java.io.Serializable;
import java.util.Locale;

import org.ironrhino.core.util.AppInfo.Stage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserAgent implements Serializable {

	private static final long serialVersionUID = 7964528679540704029L;

	private String userAgent;

	private String name = "unknown";

	private String platform = "unknown";

	private String version = "unknown";

	private int majorVersion;

	private int minorVersion;

	private String device;

	private boolean mobile;

	private String appId;

	private String appName;

	public UserAgent(String userAgent) {
		setUserAgent(userAgent);
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		if (userAgent != null) {
			try {
				if (userAgent.contains("(BlackBerry;")) {
					mobile = true;
					name = "webkit";
					platform = "blackberry os";
					String[] arr = userAgent.split(";");
					device = arr[2].trim();
					return;
				}

				if (userAgent.contains("; Android ")) {
					mobile = true;
					name = "webkit";
					platform = "android";
					String[] arr = userAgent.split(";");
					if (arr.length > 2 && arr[2].indexOf("Build") > 0) {
						device = arr[2].substring(0, arr[2].indexOf("Build") - 1).trim();
					} else if (arr.length > 4 && arr[4].indexOf("Build") > 0) {
						device = arr[4].substring(0, arr[4].indexOf("Build") - 1).trim();
					}
					return;
				}

				if (userAgent.contains("; Windows Phone OS ")) {
					mobile = true;
					name = "msie";
					platform = "windows phone os";
					return;
				}

				String lower = userAgent.toLowerCase(Locale.ROOT);
				mobile = lower.contains("mobile") || lower.contains("mobi");
				if (lower.contains("windows")) {
					platform = "windows";
				} else if (lower.contains("linux")) {
					platform = "linux";
				} else if (lower.contains("os x")) {
					platform = "osx";
				} else if (lower.contains("android")) {
					platform = "android";
				} else if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ios")) {
					platform = "ios";
				}

				if (lower.contains("webkit")) {
					name = "webkit";
				} else if (userAgent.contains("Opera")) {
					name = "opera";
				} else if (userAgent.contains("MSIE")) {
					name = "msie";
					String str = "MSIE";
					int index = userAgent.indexOf(str) + str.length() + 1;
					String s = userAgent.substring(index);
					index = s.indexOf(';');
					if (index > 0)
						version = s.substring(0, index);
				} else if (userAgent.contains("Trident")) {
					name = "msie";
					String str = "rv:";
					int index = userAgent.indexOf(str) + str.length();
					String s = userAgent.substring(index);
					index = s.indexOf(')');
					if (index > 0)
						version = s.substring(0, index);
				} else if (userAgent.contains("Mozilla")) {
					name = "mozilla";
				}

				if (lower.contains("iphone")) {
					device = "iPhone";
				} else if (lower.contains("ipad")) {
					device = "iPad";
				} else if (lower.contains("macintosh")) {
					device = "Macintosh";
				}

				setVersion(version);
			} catch (Exception e) {
				if (AppInfo.getStage() == Stage.DEVELOPMENT)
					e.printStackTrace();
			}
		}
	}

	public void setVersion(String version) {
		this.version = version;
		if (version != null && !version.equals("unknown")) {
			String[] arr = version.split("\\.");
			try {
				majorVersion = Integer.parseInt(arr[0]);
				if (arr.length > 1)
					minorVersion = Integer.parseInt(arr[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

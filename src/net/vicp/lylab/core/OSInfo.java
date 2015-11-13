package net.vicp.lylab.core;

/**
 * 操作系统类： 获取System.getProperty("os.name")对应的操作系统
 * 
 * Modified by Young Lee at 2015.11.13
 * 
 * @author isea533
 */
public class OSInfo {
	private String OS = System.getProperty("os.name").toLowerCase();

	private OSPlatform platform;

	protected OSInfo() {
	}

	public boolean isLinux() {
		return OS.indexOf("linux") >= 0;
	}

	public boolean isMacOS() {
		return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0;
	}

	public boolean isMacOSX() {
		return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
	}

	public boolean isWindows() {
		return OS.indexOf("windows") >= 0;
	}

	public boolean isOS2() {
		return OS.indexOf("os/2") >= 0;
	}

	public boolean isSolaris() {
		return OS.indexOf("solaris") >= 0;
	}

	public boolean isSunOS() {
		return OS.indexOf("sunos") >= 0;
	}

	public boolean isMPEiX() {
		return OS.indexOf("mpe/ix") >= 0;
	}

	public boolean isHPUX() {
		return OS.indexOf("hp-ux") >= 0;
	}

	public boolean isAix() {
		return OS.indexOf("aix") >= 0;
	}

	public boolean isOS390() {
		return OS.indexOf("os/390") >= 0;
	}

	public boolean isFreeBSD() {
		return OS.indexOf("freebsd") >= 0;
	}

	public boolean isIrix() {
		return OS.indexOf("irix") >= 0;
	}

	public boolean isDigitalUnix() {
		return OS.indexOf("digital") >= 0 && OS.indexOf("unix") > 0;
	}

	public boolean isNetWare() {
		return OS.indexOf("netware") >= 0;
	}

	public boolean isOSF1() {
		return OS.indexOf("osf1") >= 0;
	}

	public boolean isOpenVMS() {
		return OS.indexOf("openvms") >= 0;
	}

	/**
	 * 获取操作系统名字
	 * 
	 * @return 操作系统名
	 */
	public OSPlatform getOSname() {
		if (isAix()) {
			platform = OSPlatform.AIX;
		} else if (isDigitalUnix()) {
			platform = OSPlatform.Digital_Unix;
		} else if (isFreeBSD()) {
			platform = OSPlatform.FreeBSD;
		} else if (isHPUX()) {
			platform = OSPlatform.HP_UX;
		} else if (isIrix()) {
			platform = OSPlatform.Irix;
		} else if (isLinux()) {
			platform = OSPlatform.Linux;
		} else if (isMacOS()) {
			platform = OSPlatform.Mac_OS;
		} else if (isMacOSX()) {
			platform = OSPlatform.Mac_OS_X;
		} else if (isMPEiX()) {
			platform = OSPlatform.MPEiX;
		} else if (isNetWare()) {
			platform = OSPlatform.NetWare_411;
		} else if (isOpenVMS()) {
			platform = OSPlatform.OpenVMS;
		} else if (isOS2()) {
			platform = OSPlatform.OS2;
		} else if (isOS390()) {
			platform = OSPlatform.OS390;
		} else if (isOSF1()) {
			platform = OSPlatform.OSF1;
		} else if (isSolaris()) {
			platform = OSPlatform.Solaris;
		} else if (isSunOS()) {
			platform = OSPlatform.SunOS;
		} else if (isWindows()) {
			platform = OSPlatform.Windows;
		} else {
			platform = OSPlatform.Others;
		}
		return platform;
	}

}

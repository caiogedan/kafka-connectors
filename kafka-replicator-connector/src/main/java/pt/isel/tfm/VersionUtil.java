package pt.isel.tfm;

/**
 * Created by Caio Silva on 20/12/2020.
 */
public class VersionUtil {
	public static String getVersion() {
		try {
			return VersionUtil.class.getPackage().getImplementationVersion();
		} catch (Exception ex) {
			return "0.0.0.0";
		}
	}
}

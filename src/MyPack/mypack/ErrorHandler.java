package mypack;

public interface ErrorHandler {

	public static final int CANT_CONNECT = 0;
	public static final int LOGIN_FAILED = 1;
	public static final int OUTDATED_LAUNCHER = 2;
	public static final int USER_NOT_PREMIUM = 3;
	public static final int OTHER = 4;
	
	public void error(int err, String errStr);

}

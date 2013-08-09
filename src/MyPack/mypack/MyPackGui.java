package mypack;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

public class MyPackGui extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3657485589334081184L;
	
	private Applet launcher;
	
	public MyPackGui(Frame frame, String username, String password, ErrorHandler handler) {
		MyPackLauncher.updateSystemProperties();
		String[] playerData = MyPackLauncher.getPlayerData(username, password, handler);
		if (playerData.length != 0) {
			this.launcher = MyPackLauncher.createLauncher(playerData);
		} else {
			return;
		}
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent paramAnonymousWindowEvent) {
				new Thread() {
					
					@Override
					public void run() {
						try {
							Thread.sleep(30000L);
						} catch (InterruptedException localInterruptedException) {
							localInterruptedException.printStackTrace();
						}
						System.out.println("FORCING EXIT!");
						System.exit(0);
					}
				}
				.start();
				
				if (MyPackGui.this.launcher != null) {
					MyPackLauncher.stop(MyPackGui.this.launcher);
				}
				System.exit(0);
			}
			
		});
		setLayout(new BorderLayout());
		add(this.launcher, "Center");
		validate();
		MyPackLauncher.launch(launcher);
	}

}

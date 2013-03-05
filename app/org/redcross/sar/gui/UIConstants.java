package org.redcross.sar.gui;

import java.awt.Font;

public class UIConstants {

	public enum ButtonPlacement {
		LEFT,
		TOP,
		RIGHT,
		BOTTOM
	}

	public enum ButtonSize {
		TINY,
		SMALL,
		NORMAL,
		LONG
	}

	public final static String DEFAULT_FONT_NAME = "Tahoma";
	public final static String DIALOG_FONT = "Dialog";
	public final static int FRONT_SIZE_MEDIUM = 14;
	public final static int FONT_SIZE_LARGE = 16;
	public final static Font DEFAULT_BOLD_SMALL_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 10);
	public final static Font DEFAULT_PLAIN_MEDIUM_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, FRONT_SIZE_MEDIUM);
	public final static Font DEFAULT_PLAIN_LARGE_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, FONT_SIZE_LARGE);
	public final static Font DIALOG_PLAIN_MEDIUM_FONT = new Font(DIALOG_FONT, Font.PLAIN, FRONT_SIZE_MEDIUM);

}

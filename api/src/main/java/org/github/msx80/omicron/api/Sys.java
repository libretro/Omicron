package org.github.msx80.omicron.api;

public interface Sys 
{
	/**
	 * Draws a portion of an image into the screen. The current color is applied to the image.
	 * @param sheetNum the sheet from which to copy the image. References the file "sheet<num>.png" from Resources folder.
	 * @param x
	 * @param y
	 * @param srcx
	 * @param srcy
	 * @param w
	 * @param h
	 * @param rotate
	 * @param flip
	 */
    void draw(int sheetNum, int x, int y, int srcx, int srcy, int w, int h, int rotate, int flip);
    /**
     * Create a new surface with the specified dimension. Result can be used as sheetNum in all context.
     * @param w
     * @param h
     * @return
     */
    int newSurface(int w, int h);
    /**
     * Return the color of a pixel in a specific sheet.
     * @param sheetNum 0 for screen
     * @param x
     * @param y
     * @return
     */
    int getPix(int sheetNum, int x, int y);
    
    /**
     * Fill an area of a surface with an uniform color.
     * @param sheetNum 0 for screen, or any sheet number
     * @param x
     * @param y
     * @param w
     * @param h
     * @param color
     */
    void fill(int sheetNum, int x, int y, int w, int h, int color);
    
    /**
     * move the origin (0,0) by the specified offset.
     * Note: offset is reset before every render();
     * @param x
     * @param y
     */
	void offset(int x, int y);
	
	/**
	 * Clear the screen with a specific color
	 * Note: alpha is ignored, it's always full alpha
	 * @param color
	 */
	void clear(int color);
	
	/**
	 * Set a color for subsequent draw() calls. The color will be multiplied to the pixels being written.
	 * Set to white full alpha for direct copy.
	 * @param color
	 */
	void color(int color);
	
	/**
	 * Return current frame per second.
	 * @return
	 */
	int fps();
	
	/**
	 * Get a value from persistent memory.
	 * @param key
	 * @return
	 */
	String mem(String key);
	
	/**
	 * Write a value to persistent memory. It will be available on the next run of the game.
	 * Note: all data are associated to the "code" passed on SysConfig
	 * @param key
	 * @param value
	 */
	void mem(String key, String value);
	
	/**
	 * Return the state of the mouse
	 * @return
	 */
	Mouse mouse();
	
	/**
	 * Return the state of all available controllers.
	 * Firse one is the keyboard, then all gamepads/joysticks.
	 * @return
	 */
	Controller[] controllers();
	
	/**
	 * Play a sound
	 * @param soundNum
	 * @param volume
	 * @param pitch
	 */
	void sound(int soundNum, float volume, float pitch);
	
	/**
	 * Play a music
	 * @param musicNum
	 * @param volume
	 * @param loop
	 */
	void music(int musicNum, float volume, boolean loop);
	
	/**
	 * Stop currently playing music 
	 */
	void stopMusic();
	String hardware(String module, String command, String param);
}

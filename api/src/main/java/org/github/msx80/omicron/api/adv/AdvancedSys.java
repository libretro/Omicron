package org.github.msx80.omicron.api.adv;

import java.util.function.Consumer;

import org.github.msx80.omicron.api.Game;
import org.github.msx80.omicron.api.Sys;


/**
 * This is an advanced version of Sys that offer some methods for chaining Games, so
 * that a Game can run a second Game (and so on). Each parent can get a result from the child Game when it
 * terminates.
 * Resources (sheet, sounds, musics, etc) are separated and read from Game class resources, so that the
 * appropriate Classloader will be used
 *
 */
public interface AdvancedSys extends Sys {

	/**
	 * Starts and pass control to another Game.
	 * This method returns immediately and should be called as the last instruction of the update() method.
	 * No more update() or render() will be called for the current Game until the child one terminates, 
	 * after that, the onResult callback is called with the result from child game, then the execution of parent
	 * resumes (with a render() first)
	 * @param game
	 * @param onResult
	 */
	void execute(Game game, Consumer<String> onResult);
	
	/**
	 * Terminate a running game, returning a value to parent game, if any.
	 * If this is the root game, the application is terminated.
	 * This method returns immediately and should be called as the last instruction of the update() method.
	 * @param result
	 */
	void quit(String result);
}
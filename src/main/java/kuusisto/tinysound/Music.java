/*
 * Copyright (c) 2012, Finn Kuusisto
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package kuusisto.tinysound;

/**
 * The Music interface is an abstraction for music.  Music objects should only
 * be loaded via the TinySound {@code loadMusic()} functions.  Music can be
 * played, paused, resumed, stopped and looped from specified positions.
 * 
 * @author Finn Kuusisto
 */
public interface Music {

	/**
	 * Play this Music and loop if specified.
	 * @param loop if this Music should loop
	 */
	void play(boolean loop);
	
	/**
	 * Stop playing this Music and set its position to the beginning.
	 */
	void stop();
	
	/**
	 * Determine if this Music is playing.
	 * @return true if this Music is playing
	 */
	boolean playing();

	/**
	 * Rewinds the music to the start.
	 * <br>Added by Cristichi. I can't believe this wasn't here before.
	 */
	void rewind();

	/**
	 * Adds this Runnable to be executed once, only the next time the music finishes.
	 * <br>Added by Cristichi.
	 * @param runnable Runnable to run when sound is finished.
	 */
	void addOnEndListener(Runnable runnable);

	/**
	 * Removes all end listeners from the music. Also called after executing them once.
	 * <br>Added by Cristichi.
	 */
	void clearEndListeners();
}

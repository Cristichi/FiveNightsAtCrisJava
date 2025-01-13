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
package kuusisto.tinysound.internal;

/**
 * The MusicReference interface is the Mixer's interface to the audio data of a 
 * Music object.  MusicReference is an internal interface of the TinySound
 * system and should be of no real concern to the average user of TinySound.
 * 
 * @author Finn Kuusisto
 */
public interface MusicReference {

	/**
	 * Get the playing setting of this MusicReference.
	 * @return true if this MusicReference is set to play
	 */
	boolean getPlaying();
	
	/**
	 * Get the volume of this MusicReference.
	 * @return volume of this MusicReference
	 */
	double getVolume();
	
	/**
	 * Get the pan of this MusicReference.
	 * @return pan of this MusicReference
	 */
	double getPan();
	
	/**
	 * Set whether this MusicReference is playing.
	 * @param playing whether this MusicReference is playing
	 */
	void setPlaying(boolean playing);
	
	/**
	 * Set whether this MusicReference will loop.
	 * @param loop whether this MusicReference will loop
	 */
	void setLoop(boolean loop);
	
	/**
	 * Set the byte index of this MusicReference.
	 * @param position the byte index to set
	 */
	void setPosition(long position);
	
	/**
	 * Get the number of bytes remaining for each channel until the end of this
	 * Music.
	 * @return number of bytes remaining for each channel
	 */
	long bytesAvailable();
	
	/**
	 * Skip a specified number of bytes of the audio data.
	 * @param num number of bytes to skip
	 */
	void skipBytes(long num);
	
	/**
	 * Get the next two bytes from the music data in the specified endianness.
	 * @param data length-2 array to write in next two bytes from each channel
	 * @param bigEndian true if the bytes should be read big-endian
	 */
	void nextTwoBytes(int[] data, boolean bigEndian);
	
}

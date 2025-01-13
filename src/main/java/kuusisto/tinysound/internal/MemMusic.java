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

import kuusisto.tinysound.Music;

/**
 * The MemMusic class is an implementation of the Music interface that stores
 * all audio data in memory for low latency.
 * 
 * @author Finn Kuusisto
 */
public class MemMusic implements Music {
	
	private final byte[] left;
	private final byte[] right;
	private final Mixer mixer;
	private final MusicReference reference;
	
	/**
	 * Construct a new MemMusic with the given music data and the Mixer with
	 * which to register this MemMusic.
	 * @param left left channel of music data
	 * @param right right channel of music data
	 * @param mixer Mixer with which this Music is registered
	 */
	public MemMusic(byte[] left, byte[] right, Mixer mixer) {
		this.left = left;
		this.right = right;
		this.mixer = mixer;
		this.reference = new MemMusicReference(this.left, this.right, false,
				false, 0, 0, 1.0, 0.0);
		this.mixer.registerMusicReference(this.reference);
	}
	
	/**
	 * Play this MemMusic and loop if specified.
	 * @param loop if this MemMusic should loop
	 */
	@Override
	public void play(boolean loop) {
		this.reference.setLoop(loop);
		this.reference.setPlaying(true);
	}
	
	/**
	 * Stop playing this MemMusic and set its position to the beginning.
	 */
	@Override
	public void stop() {
		this.reference.setPlaying(false);
		this.rewind();
	}
	
	/**
	 * Set this MemMusic's position to the beginning.
	 */
	private void rewind() {
		this.reference.setPosition(0);
	}
	
	/**
	 * Determine if this MemMusic is playing.
	 * @return true if this MemMusic is playing
	 */
	@Override
	public boolean playing() {
		return this.reference.getPlaying();
	}
	
	/////////////
	//Reference//
	/////////////
	
	/**
	 * The MemMusicReference is an implementation of the MusicReference
	 * interface.
	 * 
	 * @author Finn Kuusisto
	 */
	private static class MemMusicReference implements MusicReference {

		private final byte[] left;
		private final byte[] right;
		private boolean playing;
		private boolean loop;
		private final int loopPosition;
		private int position;
		private final double volume;
		private final double pan;
		
		/**
		 * Construct a new MemMusicReference with the given audio data and
		 * settings.
		 * @param left left channel of music data
		 * @param right right channel of music data
		 * @param playing true if the music should be playing
		 * @param loop true if the music should loop
		 * @param loopPosition byte index of the loop position in music data
		 * @param position byte index position in music data
		 * @param volume volume to play the music
		 * @param pan pan to play the music
		 */
		public MemMusicReference(byte[] left, byte[] right, boolean playing,
				boolean loop, int loopPosition, int position, double volume,
				double pan) {
			this.left = left;
			this.right = right;
			this.playing = playing;
			this.loop = loop;
			this.loopPosition = loopPosition;
			this.position = position;
			this.volume = volume;
			this.pan = pan;
		}
		
		/**
		 * Get the playing setting of this MemMusicReference.
		 * @return true if this MemMusicReference is set to play
		 */
		@Override
		public synchronized boolean getPlaying() {
			return this.playing;
		}
		
		/**
		 * Get the volume of this MemMusicReference.
		 * @return volume of this MemMusicReference
		 */
		@Override
		public synchronized double getVolume() {
			return this.volume;
		}

		/**
		 * Get the pan of this MemMusicReference.
		 * @return pan of this MemMusicReference
		 */
		@Override
		public synchronized double getPan() {
			return this.pan;
		}
		
		/**
		 * Set whether this MemMusicReference is playing.
		 * @param playing whether this MemMusicReference is playing
		 */
		@Override
		public synchronized void setPlaying(boolean playing) {
			this.playing = playing;
		}
		
		/**
		 * Set whether this MemMusicReference will loop.
		 * @param loop whether this MemMusicReference will loop
		 */
		@Override
		public synchronized void setLoop(boolean loop) {
			this.loop = loop;
		}
		
		/**
		 * Set the byte index of this MemMusicReference.
		 * @param position the byte index to set
		 */
		@Override
		public synchronized void setPosition(long position) {
			if (position >= 0 && position < this.left.length) {
				this.position = (int)position;
			}
		}
		
		/**
		 * Get the number of bytes remaining for each channel until the end of
		 * this MemMusicReference.
		 * @return number of bytes remaining for each channel
		 */
		@Override
		public synchronized long bytesAvailable() {
			return this.left.length - this.position;
		}
		
		/**
		 * Skip a specified number of bytes of the audio data.
		 * @param num number of bytes to skip
		 */
		@Override
		public synchronized void skipBytes(long num) {
			for (int i = 0; i < num; i++) {
				this.position++;
				//wrap if looping, stop otherwise
				if (this.position >= this.left.length) {
					if (this.loop) {
						this.position = this.loopPosition;
					}
					else {
						this.playing = false;
					}
				}
			}
		}
		
		/**
		 * Get the next two bytes from the music data in the specified
		 * endianness.
		 * @param data length-2 array to write in next two bytes from each
		 * channel
		 * @param bigEndian true if the bytes should be read big-endian
		 */
		@Override
		public synchronized void nextTwoBytes(int[] data, boolean bigEndian) {
			if (bigEndian) {
				//left
				data[0] = ((this.left[this.position] << 8) |
						(this.left[this.position + 1] & 0xFF));
				//right
				data[1] = ((this.right[this.position] << 8) |
						(this.right[this.position + 1] & 0xFF));
			}
			else {
				//left
				data[0] = ((this.left[this.position + 1] << 8) |
						(this.left[this.position] & 0xFF));
				//right
				data[1] = ((this.right[this.position + 1] << 8) |
						(this.right[this.position] & 0xFF));
			}
			this.position += 2;
			//wrap if looping, stop otherwise
			if (this.position >= this.left.length) {
				if (this.loop) {
					this.position = this.loopPosition;
				}
				else {
					this.playing = false;
				}
			}
		}
		
	}

}

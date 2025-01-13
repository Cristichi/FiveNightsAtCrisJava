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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The StreamMusic class is an implementation of the Music interface that
 * streams audio data from a temporary file to reduce memory overhead.
 * 
 * @author Finn Kuusisto
 */
public class StreamMusic implements Music {
	
	private final URL dataURL;
	private final Mixer mixer;
	private final MusicReference reference;
	
	/**
	 * Construct a new StreamMusic with the given data and the Mixer with which
	 * to register this StreamMusic.
	 * @param dataURL URL of the temporary file containing audio data
	 * @param numBytesPerChannel the total number of bytes for each channel in
	 * the file
	 * @param mixer Mixer that will handle this StreamSound
	 * @throws IOException if a stream cannot be opened from the URL
	 */
	public StreamMusic(URL dataURL, long numBytesPerChannel, Mixer mixer)
			throws IOException {
		this.dataURL = dataURL;
		this.mixer = mixer;
		this.reference = new StreamMusicReference(this.dataURL, false, false, 0,
				0, numBytesPerChannel, 1.0, 0.0);
		this.mixer.registerMusicReference(this.reference);
	}

	/**
	 * Play this StreamMusic and loop if specified.
	 * @param loop if this StreamMusic should loop
	 */
	@Override
	public void play(boolean loop) {
		this.reference.setLoop(loop);
		this.reference.setPlaying(true);
	}
    
    /**
	 * Stop playing this StreamMusic and set its position to the beginning.
	 */
	@Override
	public void stop() {
		this.reference.setPlaying(false);
		this.rewind();
	}
    
    /**
	 * Set this StreamMusic's position to the beginning.
	 */
	private void rewind() {
		this.reference.setPosition(0);
	}
    
    /**
	 * Determine if this StreamMusic is playing.
	 * @return true if this StreamMusic is playing
	 */
	@Override
	public boolean playing() {
		return this.reference.getPlaying();
	}
	
	/////////////
	//Reference//
	/////////////

	/**
	 * The StreamMusicReference is an implementation of the MusicReference
	 * interface.
	 */
	private static class StreamMusicReference implements MusicReference {
		
		private final URL url;
		private InputStream data;
		private final long numBytesPerChannel; //not per frame, but the whole sound
		private final byte[] buf;
		private final byte[] skipBuf;
		private boolean playing;
		private boolean loop;
		private final long loopPosition;
		private long position;
		private final double volume;
		private final double pan;
		
		/**
		 * Constructs a new StreamMusicReference with the given audio data and
		 * settings.
		 * @param dataURL URL of the temporary file containing audio data
		 * @param playing true if the music should be playing
		 * @param loop true if the music should loop
		 * @param loopPosition byte index of the loop position in music data
		 * @param position byte index position in music data
		 * @param numBytesPerChannel the total number of bytes for each channel
		 * in the file
		 * @param volume volume to play the music
		 * @param pan pan to play the music
		 * @throws IOException if a stream cannot be opened from the URL
		 */
		public StreamMusicReference(URL dataURL, boolean playing, boolean loop,
				long loopPosition, long position, long numBytesPerChannel,
				double volume, double pan) throws IOException {
			this.url = dataURL;
			this.playing = playing;
			this.loop = loop;
			this.loopPosition = loopPosition;
			this.position = position;
			this.numBytesPerChannel = numBytesPerChannel;
			this.volume = volume;
			this.pan = pan;
			this.buf = new byte[4];
			this.skipBuf = new byte[50];
			//now get the data stream
			this.data = this.url.openStream();
		}

		/**
		 * Get the playing setting of this StreamMusicReference.
		 * @return true if this StreamMusicReference is set to play
		 */
		@Override
		public synchronized boolean getPlaying() {
			return this.playing;
		}
		
		/**
		 * Get the volume of this StreamMusicReference.
		 * @return volume of this StreamMusicReference
		 */
		@Override
		public synchronized double getVolume() {
			return this.volume;
		}

		/**
		 * Get the pan of this StreamMusicReference.
		 * @return pan of this StreamMusicReference
		 */
		@Override
		public synchronized double getPan() {
			return this.pan;
		}

		/**
		 * Set whether this StreamMusicReference is playing.
		 * @param playing whether this StreamMusicReference is playing
		 */
		@Override
		public synchronized void setPlaying(boolean playing) {
			this.playing = playing;
		}

		/**
		 * Set whether this StreamMusicReference will loop.
		 * @param loop whether this StreamMusicReference will loop
		 */
		@Override
		public synchronized void setLoop(boolean loop) {
			this.loop = loop;
		}

		/**
		 * Set the byte index of this StreamMusicReference.
		 * @param position the byte index to set
		 */
		@Override
		public synchronized void setPosition(long position) {
			if (position >= 0 && position < this.numBytesPerChannel) {
				//if it's later, skip
				if (position >= this.position) {
					this.skipBytes(position - this.position);
				}
				else { //otherwise skip from the beginning
					//first close our current stream
					try {
						this.data.close();
					} catch (IOException e) {
						//whatever...
					}
					//open a new stream
					try {
						this.data = this.url.openStream();
						this.position = 0;
						this.skipBytes(position);
					} catch (IOException e) {
						System.err.println("Failed to open stream for StreamMusic");
						this.playing = false;
					}
				}
			}
		}
		
		/**
		 * Get the number of bytes remaining for each channel until the end of
		 * this StreamMusicReference.
		 * @return number of bytes remaining for each channel
		 */
		@Override
		public synchronized long bytesAvailable() {
			return this.numBytesPerChannel - this.position;
		}
		
		/**
		 * Skip a specified number of bytes of the audio data.
		 * @param num number of bytes to skip
		 */
		@Override
		public synchronized void skipBytes(long num) {
			//couple of shortcuts if we are going to complete the stream
			if ((this.position + num) >= this.numBytesPerChannel) {
				//if we're not looping, nothing special needs to happen
				if (!this.loop) {
					this.position += num;
					//now stop since we're out
					this.playing = false;
                }
				else {
					//compute the next position
					long loopLength = this.numBytesPerChannel -
						this.loopPosition;
					long bytesOver = (this.position + num) -
						this.numBytesPerChannel;
					long nextPosition = this.loopPosition +
						(bytesOver % loopLength);
					//and set us there
					this.setPosition(nextPosition);
                }
                return;
            }
			//this is the number of bytes to skip per channel, so double it
			long numSkip = num * 2;
			//spin read since skip is not always supported apparently and won't
			//guarantee a correct skip amount
			int tmpRead = 0;
			int numRead = 0;
			try {
				while (numRead < numSkip && tmpRead != -1) {
					//determine safe length to read
					long remaining = numSkip - numRead;
					int len = remaining > this.skipBuf.length ?
							this.skipBuf.length : (int)remaining;
					//and read
					tmpRead = this.data.read(this.skipBuf, 0, len);
					numRead += tmpRead;
				}
			} catch (IOException e) {
				//hmm... I guess invalidate this reference
				this.position = this.numBytesPerChannel;
				this.playing = false;
			}
			//increment the position appropriately
			if (tmpRead == -1) { //reached end of file in the middle of reading
				this.position = this.numBytesPerChannel;
				this.playing = false;
			}
			else {
				this.position += num;
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
			//try to read audio data
			int tmpRead = 0;
			int numRead = 0;
			try {
				while (numRead < this.buf.length && tmpRead != -1) {
					tmpRead = this.data.read(this.buf, numRead,
							this.buf.length - numRead);
					numRead += tmpRead;
				}
			} catch (IOException e) {
				//this shouldn't happen if the bytes were written correctly to
				//the temp file, but this sound should now be invalid at least
				this.position = this.numBytesPerChannel;
				System.err.println("Failed reading bytes for stream sound");
			}
			//copy the values into the caller buffer
			if (bigEndian) {
				//left
				data[0] = ((this.buf[0] << 8) |
						(this.buf[1] & 0xFF));
				//right
				data[1] = ((this.buf[2] << 8) |
						(this.buf[3] & 0xFF));
			}
			else {
				//left
				data[0] = ((this.buf[1] << 8) |
						(this.buf[0] & 0xFF));
				//right
				data[1] = ((this.buf[3] << 8) |
						(this.buf[2] & 0xFF));
			}
			//increment the position appropriately
			if (tmpRead == -1) { //reached end of file in the middle of reading
				//this should never happen
				this.position = this.numBytesPerChannel;
			}
			else {
				this.position += 2;
			}
			//wrap if looping, stop otherwise
			if (this.position >= this.numBytesPerChannel) {
				if (this.loop) {
					this.setPosition(this.loopPosition);
				}
				else {
					this.playing = false;
				}
			}
		}
		
	}
}
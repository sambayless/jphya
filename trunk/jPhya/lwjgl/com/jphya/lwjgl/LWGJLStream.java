/*
Copyright (c) 2009, Sam Bayless
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation and/or 
   other materials provided with the distribution.
 * Neither the name of 'JPhya' nor the names of its contributors may be used 
   to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jphya.lwjgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.OpenALException;

import com.jphya.audio.AudioOutputStream;

public class LWGJLStream implements AudioOutputStream {
	private int format = AL10.AL_FORMAT_MONO16;

	/**
	 * This byte buffer is being used as a pointer to a location in audio memory
	 */
	// private IntBuffer ptrBuffer;
	private int samplerate = 44100;

	private IntBuffer ptrBuffer;
	private final int numBuffers = 16;
	private int nextBuffer = 0;

	private ArrayList<Integer> freeBuffers = new ArrayList<Integer>();
	private ArrayList<Integer> allBuffers = new ArrayList<Integer>();
	//private Timer starvationTimer ;
	private int lastBuffer ;
	private final int source;
	private boolean closed = false;
	private Lock streamLock = new ReentrantLock();
//	private TimerTask starvationTask;
	public LWGJLStream(int source) {
		super();
		
		/*
		 * ptrBuffers = new IntBuffer[4]; for(int i = 0;i<numBuffers;i++) {
		 * ptrBuffers[i] =
		 * ByteBuffer.allocateDirect(Integer.SIZE/Byte.SIZE).asIntBuffer(); }
		 */
		this.source = source;
		ptrBuffer = ByteBuffer.allocateDirect(Integer.SIZE / Byte.SIZE).order(ByteOrder.nativeOrder()).asIntBuffer();

		// will have to use al calls to create buffers for this array
		for (int i = 0; i < numBuffers; i++) {
			ptrBuffer.clear();
			AL10.alGenBuffers(ptrBuffer);
			allBuffers.add(ptrBuffer.get(0));
			freeBuffers.add(ptrBuffer.get(0));
		}
		ptrBuffer.clear();
/*		AL10.alGenBuffers(ptrBuffer);
		allBuffers.add(ptrBuffer.get(0));
		lastBuffer = (ptrBuffer.get(0));*/
		 
		ptrBuffer.clear();
/*		starvationTimer = new Timer();
		starvationTask = new TimerTask(){

			@Override
			public void run() {
				checkStarvation();
				
			}
			
		};
		starvationTimer.scheduleAtFixedRate(starvationTask, 1000,50);*/
	}

	

	


	public void writeSamples(ByteBuffer start, int frames) {
		streamLock.lock();
		try{
			start.rewind();
			if(closed)
				return;
			// convert byte buffer to float version of the same...
			ShortBuffer s = start.asShortBuffer();
			FloatBuffer f = start.asFloatBuffer();
	
			long max = Short.MAX_VALUE;
			long min = Short.MIN_VALUE;
			for (int i = 0; i < frames; i++) {
				long sample = (long) f.get();
				if (sample > max)
					sample = max;
				if (sample < min)
					sample = min;
				// sample/=
				// ((float)(max-min)/((float)(Short.MAX_VALUE-Short.MIN_VALUE)));
				s.put(((short) sample));// this works because shorts are smaller
										// than floats
			}
			start.limit(frames * Short.SIZE / Byte.SIZE);
			
			if(!AL.isCreated())
				return;
			if(closed)
				return;
			//AL10.alSourcei(getSource(), AL10.AL_LOOPING,AL10.AL_FALSE);//make sure we are not looping at this point
			
			// ok, now we have prepared the data, but before we can queue it onto
			// the buffer stream, we must wait for one of the queues to be ready
			
			waitForFreeBuffer();
		
			if(!AL.isCreated())
				return;
			int bufferPointer = freeBuffers.remove(freeBuffers.size() - 1);
			
			ptrBuffer.clear();
			AL10.alBufferData(bufferPointer, format, start, samplerate);
			ptrBuffer.put(0, bufferPointer);
			
			//AL10.alBufferData(lastBuffer, format, start, samplerate);
			
			// queue this buffer
			AL10.alSourceQueueBuffers(getSource(), ptrBuffer);
			ptrBuffer.clear();
	
			if(!AL.isCreated())
				return;
			
			// check if the source is currently playing, if it is not, start it
			// playing
			int state = AL10.alGetSourcei(getSource(), AL10.AL_SOURCE_STATE);
			if (state != AL10.AL_PLAYING) {
				{
					AL10.alSourcePlay(getSource());
					//AL10.alSourcei(getSource(), AL10.AL_LOOPING,AL10.AL_TRUE);
					
				}
			}
		}catch(OpenALException e)
		{
			e.printStackTrace();
		}finally{
			streamLock.unlock();
		}
	}

	private void waitForFreeBuffer() {
		//Note: This section of code inspired by JME's (BSD licensed) OpenALStreamedAudioPlayer
		do {
		
			if(!AL.isCreated())
				return;
			if(closed)
				return;
			//unqueue buffers even if there are some free already				
			int processedBuffers = 0;
			processedBuffers = AL10.alGetSourcei(getSource(), AL10.AL_BUFFERS_PROCESSED);
	
				//we need to block until a buffer becomes available
	
				while (processedBuffers-- > 0) {
					AL10.alSourceUnqueueBuffers(getSource(), ptrBuffer);
					int buf = ptrBuffer.get(0);
					if(buf!=lastBuffer)
						freeBuffers.add(ptrBuffer.get(0));
					ptrBuffer.rewind();
				}
			
				// blocking procedure to wait until a buffer is free
				if(freeBuffers.isEmpty())
				{//block here if there are still no buffers
					streamLock.unlock();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {					
					}finally{
						streamLock.lock();
					}
					if(closed)
						return;
				}

		}while(freeBuffers.isEmpty());
	}



	private final int getSource() {
		return source;
	}

	public void setSource(IntBuffer source) {
		this.ptrBuffer.put(0, source.get(0));

	}

	public int getFormat() {
		return format;
	}

	public void setBufferPointer(IntBuffer b) {
		this.ptrBuffer = b.duplicate();

	}

	public void setFormat(int format) {
		this.format = format;
	}

	public int getSampleRate() {
		return samplerate;
	}

	public void setSamplerate(int samplerate) {
		this.samplerate = samplerate;
	}

	public IntBuffer getPtrBuffer() {
		return ptrBuffer;
	}
	
	public void empty()
	{
		streamLock.lock();
		try{
			
				
				if(!AL.isCreated())
					return;
				AL10.alSourceStop(getSource());
				//unqueue buffers even if there are some free already	
				
				int allBuffers =  AL10.alGetSourcei(getSource(), AL10.AL_BUFFERS_QUEUED);
			
						
					while (allBuffers-- > 0) {
						AL10.alSourceUnqueueBuffers(getSource(), ptrBuffer);
						int buf = ptrBuffer.get(0);
						if(buf!=lastBuffer)
							freeBuffers.add(ptrBuffer.get(0));
						ptrBuffer.rewind();
					}
				


		}finally{
			streamLock.unlock();
		}
	}

	public void close()
	{
		closed = true;
		empty();
		
		streamLock.lock();
		try{
	
		/*if(starvationTask!=null)
			starvationTask.cancel();*/
			clearBuffers();
		}finally{
			streamLock.unlock();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		clearBuffers();
		super.finalize();
	}

	private void clearBuffers() {
		for(int buffer:allBuffers)
		{
			if(AL10.alIsBuffer(buffer))
			{
				ptrBuffer.clear();
				ptrBuffer.put(buffer);
				//you can only delete a buffer when it is in state UNUSED
				AL10.alDeleteBuffers(ptrBuffer);
			}
		}
		allBuffers.clear();
	}

}

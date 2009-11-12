/*
Copyright (C) 2001, Dylan Menzies
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

package com.jphya.surface;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.jphya.contact.FunctionContactGenerator;
import com.jphya.utility.WavIO;

public class WavFunction extends Function{
	
	private	ByteBuffer m_start;
	private	int m_nFrames;
	private	int m_interp;

	
	public	WavFunction() 
	{ m_start = null;
	m_interp = 1; }
	
	public	void setInterpOn() { m_interp = 1; };
	public	void setInterpOff() { m_interp = 0; };
	
	/**
	 * Set the sound data for this wave function. 
	 * @param soundData ByteBuffer filled with floats
	 * @param nFrames the number of samples in the buffer (ie, the number of floats)
	 */
	public void setSoundData(ByteBuffer soundData, int nFrames)
	{
		this.m_nFrames = nFrames;
		this.m_start=soundData;
	}
	
	public static ByteBuffer readWave(URL wave) throws IOException
	{
		WavIO wavIO = new WavIO(wave);
		wavIO.read();
		if(wavIO.myBitsPerSample!=16)
			throw new IOException("Wrong format");
		
		
		
		ByteBuffer shortBuf = ByteBuffer.wrap(wavIO.myData);
		ShortBuffer buf = shortBuf.asShortBuffer();
		ByteBuffer floatBuf = ByteBuffer.allocateDirect(shortBuf.capacity()*2).order(ByteOrder.nativeOrder());
		FloatBuffer floatBuffer = floatBuf.asFloatBuffer();
		for (int i = 0;i<buf.capacity();i++)
		{
			floatBuffer.put(buf.get());
		}
		
		return floatBuf;
	}
	
	/*
	public int readWav(String filename)//split the responsibility for reading the wave file out of this class. what t
	{
		CAIOwaveConfig config;
		
		if (m_start != null) 
			m_start=null;

		if (AIOloadWaveFile(filename, config) == -1) 
			return -1;

		// Make sure its in the required format.
		if (config.m_nBitsPerMonoSample != 16) return -1;
		if (config.m_nChannels != 1) return -1;
	 

		m_nFrames = config.m_nFrames;
		m_start = new short[m_nFrames+1]; //(short*)paMalloc( (m_nFrames+1) * sizeof(short) );	// Reserve +1 for unwrapped start[0].

		System.arraycopy( config.m_start, 0, m_start, 0, m_nFrames);
		//System.arraycopy(m_start, 0, config.m_start, m_nFrames, m_interp)
		//paMemcpy( m_start, config.m_start, sizeof(short)* m_nFrames );
		AIOfreeWaveFile();


		m_start[m_nFrames] = m_start[0];	// Unwrapped value added for efficient interpolation.
											// (more unwrapped values would be required for higher order interpolation.)
		return 0;
	}*/


	/*public void
	freeWav()
	{
		m_start = null;//paFree(m_start);
	}*/


	public void tick(FunctionContactGenerator gen){

		FloatBuffer out = gen.getOutput().getStart();
		float x = gen.getM_x();
		float r = gen.getM_rate();
		float y;
		float a;
		float n = (float)m_nFrames;
		int i;
		int wi;
		
		assert(m_start!=null);

		if (m_interp!=0)
		{	for(i = 0; i < gen.getScene().getNFrames(); i++) {
	
				wi = (int)x;
				//! This instruction can cost an unreasonable amount of time on i386 machines, may want to use fixed point.
	
				a = x - (float)wi;			// calculate the fractional part of the position.
				// Linear interpolation between two successive samples. This is slighty
				// rearranged for speed from: out = (1-a) * wav[wi] + a * wav[wi+1]
	
				y = (float)m_start.get(wi);
				out.put(i,a * ((float)m_start.get(wi+1) - y) + y);
	
				x += r;
				if (x >= n) x -= n;			// Wrap play position.
				else if (x < 0) x += n;
			}

		}else{
			for(i = 0; i < gen.getScene().getNFrames(); i++) {
				wi = (int)x;
				//! This instruction can cost an unreasonable amount of time on i386 machines, may want to use fixed point.
	
				out.put(i, (float)m_start.get(wi));	//	No interpolation.
	
				x += r;
				if (x >= n) x -= n;					// Wrap play position.
				else if (x < 0) x += n;
	
			}
		}
		gen.setM_x(x);			// Save state.
	}

}

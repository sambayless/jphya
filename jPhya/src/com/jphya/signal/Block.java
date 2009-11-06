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

package com.jphya.signal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jphya.scene.Scene;
import com.jphya.utility.Rnd;

public class Block {
	//
	// paBlock.hpp
	//
	// Container class for audio blocks used in signal processing.
	//
	// This provides a friendly extra layer of indirection to the sample data,
	// which is neccessary if we wish to establish signal connections between
	// objects once,
	// and later page-switch samples in the output paBlock of an object.
	// The price of the extra indirection is not important since it is only
	// incurred
	// once per block operation.
	private ByteBuffer buffer; // Location of this block's own samples.
	private FloatBuffer m_samples; // Location of this block's own samples.
	private FloatBuffer m_start; // Start of sample block, initially this block, but
	// can point to other blocks.
	// bool m_zeroState; // Experimental flag for reducing arithmetic
	// operations. Probably more hassle than its worth.

	//public static ObjectPool<Block> pool = new ObjectPool<Block>(Block.class);
	private final Scene scene;
	// public int m_poolHandle; // Used for freeing a block to the pool.

	// void setZeroState(void) { m_zeroState = true; } // Makes efficient buffer
	// filling easy: If m_zeroState is set 'addWithMultiply' interpreted as
	// 'copyWithMultiply' so 'zero' is avoided.
	// void clearZeroState(void) { m_zeroState = false; } // Makes efficient
	// buffer filling easy: If m_zeroState is set 'addWithMultiply' interpreted
	// as 'copyWithMultiply' so 'zero' is avoided.
	// bool isZero() { return m_zeroState; }
	public FloatBuffer getStart() {
		return m_start;
	}

	public void setStart(FloatBuffer s) {
		m_start = s;
	}

	public void resetStart() {
		m_start = m_samples.duplicate();//look into this...
	}

	public static final int INIT_NBLOCKFRAMES = 128;

	private static final float SHRT_MAX = Short.MAX_VALUE;
	private static final float SHRT_MIN = Short.MIN_VALUE;
//	public static int nMaxFrames = INIT_NBLOCKFRAMES;
	//public static int nFrames = INIT_NBLOCKFRAMES;

	public static Block newBlock(Scene scene) {
		//first check the allocation pool:
		Block b  = scene.getAllocationManager().acquireObject(Block.class);
		if(b==null)
		{
			b = new Block(scene);// pool.newActiveObject();
		}
		
		scene.addBlock(b);
		return b;
	}

	public static void deleteBlock(Block b) {
		b.scene.removeBlock(b);
		b.scene.getAllocationManager().returnObject(b);
	}

	/**
	 * Use the static constructor for allocation management.
	 * @param scene
	 */
	protected Block(Scene scene) {
		this.scene = scene;	
		buffer = ByteBuffer.allocateDirect(scene.getNMaxFrames()*Float.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder());
		
		m_samples = buffer.asFloatBuffer(); // (float)paCalloc(nMaxFrames,
		// float.SIZE);
		//CRITICALLY IMPRTANT: duplicate does not preserve byte order
		m_start = buffer.duplicate().order(ByteOrder.nativeOrder()).asFloatBuffer();
		
	//	scene.addBlock(this);
		// m_zeroState = false;
		// m_poolHandle = -1;
	}

	public ByteBuffer getStartBytes()
	{
		return buffer;
	}

	public int getNFrames()
	{
		return scene.getNFrames();
	}
	
	public void zero() {
		// m_zeroState = true;

		assert (getNFrames() <= scene.getNMaxFrames());
		int i;
		for (i = 0; i < getNFrames(); i++)
			m_start.put(i,0f);
		
	}

	public void copy(Block input) {
		// m_zeroState = false;
		m_start.rewind();
		m_start.put(input.m_start);
		m_start.rewind();
		//System.arraycopy(input.m_start, 0, m_start, 0, nFrames);
		// paMemcpy( m_start, input.m_start, float.SIZE * nFrames );
	}

	public void multiplyBy(float multfactor) {
		int i;
		m_start.rewind();
		for (i = 0; i < getNFrames(); i++)
		{		
			m_start.put(i, m_start.get(i)*multfactor);
		}
		// compilation.
	}

	public void add(Block inBlock) {
		int i;

		assert (inBlock != null);
		// if (m_zeroState) { copy(inBlock); m_zeroState = false; return; };
	
		for (i = 0; i < getNFrames(); i++)
		{
			m_start.put(i, m_start.get(i)+inBlock.m_start.get(i));
		}
	}

	public void addWithMultiply(Block inBlock, float multfactor) {
		int i;

		// if (m_zeroState) { copyWithMultiply(inBlock, multfactor); return; };

		if (multfactor != 0) {
			
			for (i = 0; i <  getNFrames(); i++)
			{
				m_start.put(i, m_start.get(i)+inBlock.m_start.get(i)*multfactor);
			}
		}else
			zero();
	}

	public void copyWithMultiply(Block inBlock, float multfactor) {
		int i;

		// if (multfactor == 0) {
		// m_zeroState = true;
		// return;
		// }
		// m_zeroState = false;
		if(multfactor!=0f)
		{	
		//float[] in = inBlock.m_start;
			for (i = 0; i < getNFrames(); i++)
			{
				m_start.put(i, inBlock.m_start.get(i)*multfactor);
				//m_start[i] = in[i] * multfactor;
			}
		}else{
			zero();
		}
	}

/*	public static int setnMaxFrames(int n) {
		if (pool.getnObjects() != 0)
			return -1; // Can't change now.

		nMaxFrames = n;
		scene.setNFrames(n);
		return 0;
	}*/

	public int setnFrames(int n) {
		if (n > scene.getNMaxFrames())
			return -1;

		scene.setNFrames(n);
		return 0;
	}

	public void square() {
		int i;
		for (i = 0; i <  getNFrames(); i++)
		{
			float v = m_start.get(i);
			m_start.put(i, v*v);
			//m_start[i] = m_start[i] * m_start[i];
		}
	}

	void fillWithNoise() {
		int i;
		m_start.rewind();
		float shift = Rnd.RAND_MAX * .5f; // NB this gives true zero average.
		for (i = 0; i <  getNFrames(); i++)
		{
			m_start.put((float) Rnd.random() - shift);
			//m_start[i] = (float) Rnd.random() - shift;
		}
		m_start.rewind();
	}

	void fillWithNoise(float amp) {
		int i;
		m_start.rewind();
		float scale = 2 * amp / Rnd.RAND_MAX;
		for (i = 0; i <  getNFrames(); i++)
		{
			m_start.put((float)-amp + scale * Rnd.random());
			//m_start[i] = -amp + scale * Rnd.random();
		}
		m_start.rewind();
	}

	float sum() {
		float sum = 0;

		// ! This isn't the most numerically-accurate method:

		int i;
		for (i = 0; i <  getNFrames(); i++)
			sum += m_start.get(i);

		return sum;
	}

	public void fadeout() {
		int i;
		float incr = (float) -1f / ((float)  getNFrames());
		float mult = (float) 1.0f;
		for (i = 0; i <  getNFrames(); i++) {
			m_start.put(i, m_start.get(i)*mult);
			//m_start[i] *= mult;
			mult += incr;
		}
	}

	void limit() {
		int i;
		/*float a = SHRT_MAX * 0.75f;
		float b = SHRT_MAX;
		float c = b - a;*/

		for (i = 0; i <  getNFrames(); i++) {
			if (m_start.get(i) > SHRT_MAX)
				m_start.put(i, SHRT_MAX);
			else if (m_start.get(i) < SHRT_MIN)
				m_start.put(i, SHRT_MIN);

			/*
			 * if (m_start[i] > a) { m_start[i] = b - cc/(m_start[i]-a+c); }
			 * else if (m_start[i] < -a) { m_start[i] = -b +
			 * cc/(-m_start[i]-a+c); }
			 */
		}
	}

	public Scene getScene() {
		return scene;
	}

}
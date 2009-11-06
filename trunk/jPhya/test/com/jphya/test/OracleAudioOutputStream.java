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

package com.jphya.test;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.jphya.audio.AudioOutputStream;

/**
 * This class compares the output of JPhya against a pre-recorded output ('oracle') from the original Phya library.
 * @author Sam
 *
 */
public class OracleAudioOutputStream implements AudioOutputStream{
	private ArrayList<Float> oracle ;
	public OracleAudioOutputStream() {
		super();
		Scanner scanner = new Scanner( getClass().getResourceAsStream("oracle"));
		oracle = new ArrayList<Float>();
		while(scanner.hasNext())
		{
			try{
			float f = scanner.nextFloat();
				oracle.add(f);
			}catch(InputMismatchException  e)
			{
				scanner.next();
			}
		}
		
		data = ByteBuffer.allocate(Float.SIZE*44100*20).order(ByteOrder.nativeOrder());
		fData = data.asFloatBuffer();
	}

	private ByteBuffer data;
	private FloatBuffer fData;
	

	private static int curPos =0;
	private static int curLine = 0;

	private boolean compare(float o, float f) {
		if(o == f)
			return true;
		
		float dif = Math.abs(o-f);
		float maxDif =  Math.abs(f+o)/20f;
		if(dif>maxDif)
			return false;
		return true;
	}


	public float[] getFloats() {
		float[] f = new float[fData.position()];
		fData.flip();
		fData.get(f);
		return f;
		
	}

	public void writeSamples(ByteBuffer start, int frames) {
		
		FloatBuffer fStart = start.asFloatBuffer();
		for(int i =0;i<frames;i++)
		{
			float o = oracle.get(curPos++);
			if(!compare(o,fStart.get(i)))
			{
				System.err.println("Mismatch at line" + curLine + ","  + i + " :\t" + fStart.get(i) + "!=" + o);
			}
		}
	}


	public int getSampleRate() {
		return 44100;
	}
	
	public void close() {

	}
  
}

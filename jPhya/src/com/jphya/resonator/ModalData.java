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

package com.jphya.resonator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ModalData {

	private boolean m_usingDefaultModes; // Used to ensure default gets wiped
	// when new modes loaded.
	private int m_nModes;

	public int getM_nModes() {
		return m_nModes;
	}

	public float[] m_freq;
	public float[] m_damp;
	public float[] m_amp;

	public ModalData() {

		m_nModes = 1; // Initially a single test mode.
		m_usingDefaultModes = true;

		// ! Allocation of memory for modes should be done after reading number
		// of modes. For now..
		m_freq = new float[Modal.nMaxModes]; //paFloatCalloc(paModal::nMaxModes)
		// ;
		m_damp = new float[Modal.nMaxModes];//paFloatCalloc(paModal::nMaxModes);
		m_amp = new float[Modal.nMaxModes];// paFloatCalloc(paModal::nMaxModes);

		m_freq[0] = 440; // Handy default.
		m_damp[0] = 1;
		m_amp[0] = 1;

	}

	/*
	 * ~paModalData() {
	 * 
	 * paFree(m_freq); paFree(m_damp); paFree(m_amp); }
	 */
	
	public int read(String fileName) throws IOException
	{
		return read(new BufferedInputStream( new FileInputStream(new File( fileName))));
	}

	public int  read(InputStream inputStream) throws IOException {

		//int err;
		float freqScale;
		float dampScale;
		float ampScale;

		float f,d,a;
		//TODO:implement this later..
		
		

		Scanner scanner = new Scanner(inputStream);
		
		freqScale = Float.valueOf(scanner.next());
		dampScale = Float.valueOf(scanner.next());
		ampScale = Float.valueOf(scanner.next());

		if (m_usingDefaultModes) {
			m_usingDefaultModes = false;	// Otherwise add new data to existing data.
			m_nModes = 0;
		}

		try{
		
		while( m_nModes < Modal.nMaxModes)  {//&& (err = fscanf(fh, "%f %f %f\n", &f, &d, &a)) == 3 )
			f = Float.valueOf(scanner.next());
			d = Float.valueOf(scanner.next());
			a = Float.valueOf(scanner.next());
			
			m_freq[m_nModes] = f * freqScale;
			m_damp[m_nModes] = d * dampScale;
			m_amp[m_nModes] = a * ampScale;
			m_nModes++;
		}
		}catch(NoSuchElementException e)
		{
			//ok
		}
	/*	if (err == 3 && m_nModes == Modal.nMaxModes) 
			System.err.printf( "Total mode limit of %d exceeded: Some modes lost.\n\n", Modal.nMaxModes);
*/

		scanner.close();
/*
		File fh = new File(fileName); // fopen(filename, "r");
		if (!fh.exists()) {
			fprintf(stderr, "\nModal datafile %s failed to load.\n\n", filename);
			return(-1);
		}


	////// Load data and prepare runtime data from it.


		if ( fscanf(fh, "%f	%f	%f\n", freqScale, dampScale, ampScale) != 3 )
		{
			fprintf(stderr, "\nBad scaling factors in %s.\n\n", filename);
			fclose(fh);
			return(-1);
		}

		if (m_usingDefaultModes) {
			m_usingDefaultModes = false;	// Otherwise add new data to existing data.
			m_nModes = 0;
		}

		while( m_nModes < Modal.nMaxModes && (err = fscanf(fh, "%f %f %f\n", &f, &d, &a)) == 3 ) {
			m_freq[m_nModes] = f * freqScale;
			m_damp[m_nModes] = d * dampScale;
			m_amp[m_nModes] = a * ampScale;
			m_nModes++;
		}

		if (err == 3 && m_nModes == Modal.nMaxModes) 
			fprintf(stderr, "Total mode limit of %d exceeded: Some modes lost.\n\n", Modal.nMaxModes);


		fclose(fh);*/

		return(0);
	}
}

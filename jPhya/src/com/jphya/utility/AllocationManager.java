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

package com.jphya.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class allows for efficient memory management, to avoid excessive object creation
 * @author Sam
 *
 */
public class AllocationManager {
	private Map<Class<?>,ArrayList<?>> allocationSets = new HashMap<Class<?>,ArrayList<?>>();
	
	/**
	 * Return an object from the previously created stores, IF it exists, otherwise return null.
	 * @param <E>
	 * @param objectClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> E acquireObject(Class<E> objectClass)
	{
		ArrayList<E> list = (ArrayList<E>) allocationSets.get(objectClass);
		if (list == null)
		{
			list =(ArrayList<E> ) new ArrayList<Object>();
			allocationSets.put(objectClass, list);
		}
		
		if (list.isEmpty())
		{
			return null;
		}
		
		return list.remove(list.size()-1);
	}
	
	/**
	 * Return an object to the pool for future use. It is the responsibility of the user to ensure
	 * that no outside references are retained to the ojbect; unspecified behaviour occurs otherwise.
	 * @param <E>
	 * @param object
	 */
	@SuppressWarnings("unchecked")
	public  <E> void returnObject(E object)
	{
		ArrayList<E> list = (ArrayList<E>) allocationSets.get(object.getClass());
		if (list == null)
		{
			list =(ArrayList<E> ) new ArrayList<Object>();
			allocationSets.put(object.getClass(), list);
		}
		list.add(object);
	}
	/**
	 * Release all allocated objects for garbage collection
	 */
	public void clear()
	{
		allocationSets.clear();
	}


}

/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * $Id$
 */

template<class TYPE>
class Array {
private:
	TYPE *m_data;
	unsigned int m_size;
	unsigned int m_capacity;
	
public:
	Array(unsigned int size = 256);
	~Array();
	
	void add(TYPE element);
	void reset();
	unsigned int getSize();
	TYPE get(unsigned int index) const;
	
private:
	void ensureCapacity(unsigned int capacity);
};

template<class TYPE>
Array<TYPE>::Array(unsigned int capacity) {
    m_capacity = capacity;
    m_size = 0;
    m_data = new TYPE[m_capacity];
}

template<class TYPE>
Array<TYPE>::~Array() {
    delete[] m_data;
}

template<class TYPE>
void Array<TYPE>::ensureCapacity(unsigned int capacity) {
    if (capacity > m_capacity) {
		unsigned int size = m_capacity * 2;
		if (size < capacity)
			size = capacity;
		TYPE *data = new TYPE[size];
		memcpy(data, m_data, m_capacity * sizeof(TYPE));
		delete[] m_data;
		m_data = data;
		m_capacity = size;
    }
}

template<class TYPE>
void Array<TYPE>::add(TYPE element) {
	int size = m_size + 1;
	ensureCapacity(size);
    m_data[m_size] = element;
	m_size = size;
}

template<class TYPE>
inline TYPE Array<TYPE>::get(unsigned int index) const {
	return m_data[index];
}

template<class TYPE>
inline unsigned int Array<TYPE>::getSize() {
	return m_size;
}

template<class TYPE>
void Array<TYPE>::reset() {
    m_size = 0;
}

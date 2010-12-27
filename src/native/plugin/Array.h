/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
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
	bool remove(unsigned int index);
	void reset();
	unsigned int size();
	const TYPE *data();
	TYPE get(unsigned int index) const;
	void set(unsigned int index, TYPE element);

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
bool Array<TYPE>::remove(unsigned int index) {
	if (index >= 0 && index < m_size) {
		for (int i = index + 1; i < m_size; i++)
			m_data[i - 1] = m_data[i];
		m_size--;
		return true;
	}
	return false;
}

template<class TYPE>
inline TYPE Array<TYPE>::get(unsigned int index) const {
	return m_data[index];
}

template<class TYPE>
inline void Array<TYPE>::set(unsigned int index, TYPE element) {
	m_data[index] = element;
}

template<class TYPE>
inline unsigned int Array<TYPE>::size() {
	return m_size;
}

template<class TYPE>
inline const TYPE *Array<TYPE>::data() {
	return m_data;
}

template<class TYPE>
void Array<TYPE>::reset() {
    m_size = 0;
}

/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 20.12.2004.
 *
 * $RCSfile: ArgumentReader.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:58 $
 */

package com.scriptographer.js;

import java.awt.geom.*;
import java.util.*;

import org.mozilla.javascript.*;

import com.scriptographer.ai.*;

public class ArgumentReader {
	private Object[] args;
	private int pos;
	private Object[] saveArgs;
	private int savePos;
	
	public ArgumentReader() {
	}
	
	public ArgumentReader(Object[] args, int pos) {
		init(args, pos);
	}
	
	public ArgumentReader(Object[] args) {
		init(args, 0);
	}
	
	private void init(Object[] args, int pos) {
		this.args = args;
		this.pos = pos;
	}
	
	private void save() {
		saveArgs = args;
		savePos = pos;
	}
	
	private void restore() {
		args = saveArgs;
		pos = savePos;
	}
	
	private void switchTo(Object[] args) {
		save();
		init(args, 0);
	}
	
	private double[] readNumbers(Scriptable obj, String[] names) {
		double[] values = new double[names.length];
		int i = 0;
		do {
			Object valueObj = ScriptableObject.getProperty(obj, names[i]);
			if (valueObj == null) break;
			Number value = readNumber(valueObj);
			if (value == null) break;
			values[i] = value.doubleValue();
		} while (++i < names.length);
		
		if (i < names.length)
			values = null;
		
		return values;
	}
	
	private double[] readNumbers(int count) {
		double[] values = new double[count];
		int i = 0;
		do {
			Number value = readNumber();
			if (value == null) break;
			values[i] = value.doubleValue();
		} while (++i < count);
		
		if (i < count)
			values = null;
		
		return values;
	}
	
	/*
	 * Object
	 */
	public Object readObject() {
		if (pos < args.length) {
			Object res = args[pos];
			if (res != null) {
				pos++;
				return res;
			}
		}
		return null;
	}
	
	/*
	 * String
	 */
	public String readString(Object arg) {
		if (arg instanceof String) {
			return (String)arg;
		}
		return null;
	}

	public String readString() {
		if (pos < args.length) {
			String res = readString(args[pos]);
			if (res != null) {
				pos++;
				return res;
			}
		}
		return null;
	}

	/*
	 * Function
	 */
	public Function readFunction(Object arg) {
		if (arg instanceof Function) {
			return (Function)arg;
		}
		return null;
	}

	public Function readFunction() {
		if (pos < args.length) {
			Function res = readFunction(args[pos]);
			if (res != null) {
				pos++;
				return res;
			}
		}
		return null;
	}

	/*
	 * Number
	 */
	public Number readNumber(Object arg) {
		if (arg instanceof Number) {
			return (Number)arg;
		} else if (arg instanceof String) {
			return Double.valueOf((String)arg);
		}
		return null;
	}

	public Number readNumber() {
		if (pos < args.length) {
			Number res = readNumber(args[pos]);
			if (res != null) {
				pos++;
				return res;
			}
		}
		return null;
	}
	
	/*
	 * Boolean
	 */
	public Boolean readBoolean(Object arg) {
		if (arg instanceof Boolean) {
			return (Boolean)arg;
		} else if (arg instanceof Number) {
			return new Boolean(((Number)arg).intValue() != 0);
		} else if (arg instanceof String) {
			return Boolean.valueOf((String)arg);
		}
		return null;
	}

	public Boolean readBoolean() {
		if (pos < args.length) {
			Boolean res = readBoolean(args[pos]);
			if (res != null) {
				pos++;
				return res;
			}
		}
		return null;
	}
	
	/*
	 * Point
	 */
	public Point readPoint(Object arg) {
		Point res = null;
		if (arg instanceof NativeJavaObject) {
			arg = ((NativeJavaObject)arg).unwrap();
			if (arg instanceof Point) {
				res = (Point)arg;
			} else if (arg instanceof Point2D) {
				res = new Point((Point2D)arg);
			}
		} else if (arg instanceof NativeArray) {
			// take the array's elements and use the reader on this:
			switchTo(Context.getCurrentContext().getElements((NativeArray)arg));
			try {
				res = readPoint();
			} finally {
				restore();
			}
		} else if (arg instanceof NativeObject) {
			NativeObject obj = (NativeObject)arg;
			Object xObj = ScriptableObject.getProperty(obj, "x");
			Object yObj = ScriptableObject.getProperty(obj, "y");
			if (xObj != null && yObj != null) {
				Number x = readNumber(xObj);
				Number y = readNumber(yObj);
				if (x != null || y != null) {
					res = new Point(x.floatValue(), y.floatValue());
				}
			}
		}
		return res;
	}
	
	public Point readPoint() {
		Point res = null;
		if (pos < args.length) {
			res = readPoint(args[pos]);
			if (res != null) {
				pos++;
			} else {
				Number x = readNumber();
				if (x != null) {
					Number y = readNumber();
					if (y != null) {
						res = new Point(x.floatValue(), y.floatValue());
					} else {
						float val = x.floatValue();
						res = new Point(val, val);
					}
				}
			}
		}
		return res;
	}
		
	/*
	 * Rectangle
	 */
	
	private static final String[] rectangleProperties1 = {
		"x", "y", "width", "height"
	};
	
	private static final String[] rectangleProperties2 = {
		"left", "top", "right", "bottom"
	};

	public Rectangle readRectangle(Object arg) {
		Rectangle res = null;
		if (arg instanceof NativeJavaObject) {
			arg = ((NativeJavaObject)arg).unwrap();
			if (arg instanceof Rectangle) {
				res = (Rectangle)arg;
			} else if (arg instanceof Rectangle2D) {
				res = new Rectangle((Rectangle2D)arg);
			}
		} else if (arg instanceof NativeArray) {
			// take the array's elements and use the reader on this:
			switchTo(Context.getCurrentContext().getElements((NativeArray)arg));
			try {
				res = readRectangle();
			} finally {
				restore();
			}
		} else if (arg instanceof NativeObject) {
			NativeObject obj = (NativeObject)arg;
			double[] values = readNumbers(obj, rectangleProperties1);
			if (values != null) {
				res = new Rectangle(values[0], values[1], values[2], values[3]);
			} else {
				values = readNumbers(obj, rectangleProperties2);
				if (values != null) {
					// TODO: fix bottom up, top down???
					res = new Rectangle(values[0], values[1], values[2] - values[0], values[3] - values[1]);
				}
			}
		}
		return res;
	}
	
	public Rectangle readRectangle() {
		Rectangle res = null;
		if (pos < args.length) {
			res = readRectangle(args[pos]);
			if (res != null) {
				pos++;
			} else {
				/*
				double[] segmentValues = readNumbers(4);
				if (segmentValues != null) {
					res = new Rectangle(segmentValues[0], segmentValues[1], segmentValues[2], segmentValues[3]);
				}
				*/
				Point pt = readPoint();
				if (pt != null) {
					Point size = readPoint();
					if (size != null) {
						res = new Rectangle(pt, size);
					}
				}
			}
		}
		return res;
	}
	
	/*
	 * Color
	 */
	
	private static final String[] colorRGBProperties = {
		"red", "green", "blue"
	};
	
	/*
	public Color readColor(Object arg) {
		Color res = null;
		if (arg instanceof NativeJavaObject) {
			arg = ((NativeJavaObject)arg).unwrap();
			if (arg instanceof Color) {
				res = (Color)arg;
			} else if (arg instanceof java.awt.Color) {
				res = new Color((java.awt.Color)arg);
			}
		} else if (arg instanceof NativeArray) {
			// take the array's elements and use the reader on this:
			switchTo(Context.getCurrentContext().getElements((NativeArray)arg));
			try {
				res = readColor();
			} finally {
				restore();
			}
		} else if (arg instanceof Number) {
			return new Color(((Number)arg).intValue());
		} else if (arg instanceof String) {
			return new Color(Integer.decode((String)arg).intValue());
		}else if (arg instanceof NativeJavaObject) {
			arg = ((NativeJavaObject)arg).unwrap();
			if (arg instanceof java.awt.Color) {
				res = new Color((java.awt.Color)arg);
			}
		} else if (arg instanceof NativeObject) {
			NativeObject obj = (NativeObject)arg;
			double[] values = readNumbers(obj, colorRGBProperties);
			if (values != null) {
				res = new Color((int)values[0], (int)values[1], (int)values[2]);
			} else {
				// TODO: cmyk support?
			}
		}
		return res;
	}

	public Color readColor() {
		if (pos < args.length) {
			Color res = readColor(args[pos]);
			if (res != null) {
				pos++;
				return res;
			} else {
				double[] values = readNumbers(3);
				if (values != null) {
					// see wether there's one more: cmyk, otherwise: rgb
					Number value = readNumber();
					if (value != null)
						res = new Color((int)values[0], (int)values[1], (int)values[2], value.intValue());
					else
						res = new Color((int)values[0], (int)values[1], (int)values[2]);
				}
			}
		}
		return null;
	}
	*/
	/*
	 * Matrix
	 */
	public Matrix readMatrix(Object arg) {
		Matrix res = null;
		if (arg instanceof NativeJavaObject) {
			arg = ((NativeJavaObject)arg).unwrap();
			if (arg instanceof Matrix) {
				res = (Matrix)arg;
			} else if (arg instanceof AffineTransform) {
				res = new Matrix((AffineTransform)arg);
			}
		} else if (arg instanceof NativeArray) {
			// take the array's elements and use the reader on this:
			switchTo(Context.getCurrentContext().getElements((NativeArray)arg));
			try {
				res = readMatrix();
			} finally {
				restore();
			}
		} else if (arg instanceof NativeObject) {
			double[] values = readNumbers((NativeObject)arg, matrixProperties);
			if (values != null) {
				res = new Matrix(values);
			}
		}
		return res;
	}	

	public Matrix readMatrix() {
		Matrix res = null;
		// first see wether there's just a matrix object:
		if (pos < args.length) {
			res = readMatrix(args[pos]);
			if (res != null) {
				pos++;
			} else {
				// scaleX, shearY, shearX, scaleY, translateX, translateY
				double[] values = readNumbers(6);
				if (values != null) {
					res = new Matrix(values);
				}
			}
		}
		return res;
	}

	/*
	 * Segment
	 */
	public Segment readSegment(Object arg) {
		return readSegment(arg, false);
	}

	public Segment readSegment(Object arg, boolean allwaysCreate) {
		Segment res = null;
		if (arg instanceof Segment) {
			if (allwaysCreate) res = new Segment((Segment)arg);
			else res = (Segment)arg;
		} else if (arg instanceof NativeArray) {
			// take the array's elements and use the reader on this:
			switchTo(Context.getCurrentContext().getElements((NativeArray)arg));
			try {
				res = readSegment(allwaysCreate);
			} finally {
				restore();
			}
		} else if (arg instanceof NativeObject) {
			NativeObject obj = (NativeObject)arg;
			Object ptObj = ScriptableObject.getProperty(obj, "point");
			if (ptObj != null) {
				Point2D pt = readPoint(ptObj);
				if (pt != null) {
					Object inObj = ScriptableObject.getProperty(obj, "handleIn");
					Object outObj = ScriptableObject.getProperty(obj, "handleOut");
					Object cornerObj = ScriptableObject.getProperty(obj, "corner");
					if (inObj != null || outObj != null || cornerObj != null) {
						Point2D in = inObj != null ? readPoint(inObj) : null;
						Point2D out = outObj != null ? readPoint(outObj) : null;
						Boolean corner = cornerObj != null ? readBoolean(cornerObj) : null;
						if (in != null || out != null) {
							res = new Segment(pt, in, out, corner != null ? corner.booleanValue() : false);
						}
					}
					if (res == null) {
						res = new Segment(pt);
					}
				}
			}
		}
		return res;
	}	
	
	public Segment readSegment() {
		return readSegment(false);
	}

	public Segment readSegment(boolean allwaysCreate) {
		Segment res = null;
		// first see wether there's just a listener object:
		if (pos < args.length) {
			res = readSegment(args[pos], allwaysCreate);
			if (res != null) {
				pos++;
			} else {
				Point2D pt = readPoint();
				if (pt != null) {
					int oldPos = pos;
					Point2D in = readPoint();
					Point2D out = in != null ? readPoint() : null;
					Boolean corner = out != null ? readBoolean() : null;
					if (in != null) {
						res = new Segment(pt, in, out, corner != null ? corner.booleanValue() : false);
					} else {
						pos = oldPos; // back up, if in was read but not out
						res = new Segment(pt);
					}
				}
			}
		}
		return res;
	}
		
	private static final String[] matrixProperties = {
		"scaleX", "shearY", "shearX", "scaleY", "translateX", "translateY"
	};
	
	/*
	 * SegmentList
	 */
	public Collection readSegmentList(Object arg) {
		Collection res = null;
		if (arg instanceof Collection) {
			res = (Collection)arg;
		} else if (arg instanceof NativeArray) {
			// take the array's elements and use the reader on this:
			Object[] objs = Context.getCurrentContext().getElements((NativeArray)arg);
			switchTo(objs);
			try {
				res = readSegmentList();
			} finally {
				restore();
			}
		}
		return res;
	}	

	public Collection readSegmentList() {
		Collection res = null;
		// first see wether there's just a listener object:
		if (pos < args.length) {
			res = readSegmentList(args[pos]);
			if (res != null) {
				pos++;
			} else {
				res = new ArrayList();
				while (true) {
					Segment segment = readSegment();
					if (segment == null) break;
					res.add(segment);
				}
			}
		}
		return res;
	}
}
